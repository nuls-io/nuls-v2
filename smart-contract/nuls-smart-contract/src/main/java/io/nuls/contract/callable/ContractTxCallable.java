/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.callable;

import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTransferHandler;
import io.nuls.contract.manager.TempBalanceManager;
import io.nuls.contract.model.bo.CallableResult;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.ioc.SpringLiteContext;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.util.ContractUtil.*;


/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Getter
@Setter
public class ContractTxCallable implements Callable<CallableResult> {

    private ContractExecutor contractVM;
    private ContractHelper contractHelper;
    private VMContext vmContext;
    private ContractTransferHandler contractTransferHandler;
    private TempBalanceManager tempBalanceManager;
    private ProgramExecutor executor;
    private String contract;
    private List<ContractWrapperTransaction> txList;
    private long number;
    private String preStateRoot;
    private ContractConflictChecker checker;
    private Set<String> commitSet;
    private int chainId;
    private long blockTime;


    public ContractTxCallable(int chainId, long blockTime, ProgramExecutor executor, String contract, List<ContractWrapperTransaction> txList, long number, String preStateRoot, ContractConflictChecker checker, Set<String> commitSet) {
        this.chainId = chainId;
        this.blockTime = blockTime;
        this.contractVM = SpringLiteContext.getBean(ContractExecutor.class);
        this.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        this.vmContext = SpringLiteContext.getBean(VMContext.class);
        this.contractTransferHandler = SpringLiteContext.getBean(ContractTransferHandler.class);
        this.tempBalanceManager = contractHelper.getTempBalanceManager(chainId);
        this.executor = executor;
        this.contract = contract;
        this.txList = txList;
        this.number = number;
        this.preStateRoot = preStateRoot;
        this.checker = checker;
        this.commitSet = commitSet;
    }

    @Override
    public CallableResult call() throws Exception {
        CallableResult callableResult = CallableResult.newInstance();
        List<ContractResult> resultList = callableResult.getResultList();
        callableResult.setContract(contract);

        ContractData contractData;
        // 创建合约无论成功与否，后续的其他的跳过执行，视作失败 -> 合约锁定中或者合约不存在
        // 删除合约成功后，后续的其他的跳过执行，视作失败 -> 合约已删除
        boolean hasCreate = false;
        boolean isDelete = false;
        ContractResult contractResult;
        for (ContractWrapperTransaction tx : txList) {
            contractData = tx.getContractData();
            if (hasCreate) {
                resultList.add(ContractResult.getFailed(contractData, "contract lock or not exist."));
                continue;
            }
            if (isDelete) {
                resultList.add(ContractResult.getFailed(contractData, "contract has been terminated."));
                continue;
            }
            if(!ContractUtil.checkPrice(contractData.getPrice())) {
                resultList.add(ContractResult.getFailed(contractData, "The minimum value of price is 25."));
                continue;
            }
            switch (tx.getType()) {
                case TX_TYPE_CREATE_CONTRACT:
                    hasCreate = true;
                    contractResult = contractVM.create(executor, contractData, number, preStateRoot);
                    checkCreateResult(tx, callableResult, contractResult);
                    break;
                case TX_TYPE_CALL_CONTRACT:
                    contractResult = contractVM.call(executor, contractData, number, preStateRoot);
                    checkCallResult(tx, callableResult, contractResult);
                    break;
                case TX_TYPE_DELETE_CONTRACT:
                    contractResult = contractVM.delete(executor, contractData, number, preStateRoot);
                    isDelete = checkDeleteResult(tx, callableResult, contractResult);
                    break;
                default:
                    break;
            }
        }
        return callableResult;
    }

    private void checkCreateResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        if (contractResult.isSuccess()) {
            Result checkResult = contractHelper.validateNrc20Contract(chainId, (ProgramExecutor) contractResult.getTxTrack(), tx, contractResult);
            if(checkResult.isSuccess()) {
                commitSet.add(contract);
                commitContract(contractResult);
            }
        }
        List<ContractResult> resultList = callableResult.getResultList();
        resultList.add(contractResult);
    }


    private void checkCallResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) throws IOException {
        makeContractResult(tx, contractResult);
        List<ContractResult> reCallList = callableResult.getReCallList();
        boolean isConflict = checker.checkConflict(tx, contractResult, commitSet);
        if (isConflict) {
            // 冲突后，添加到重新执行的集合中
            reCallList.add(contractResult);
        } else {
            // 没有冲突, 处理合约结果
            dealCallResult(tx, callableResult, contractResult, chainId, blockTime);
        }
    }

    private void dealCallResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult, int chainId, long blockTime) throws IOException {
        if (contractResult.isSuccess()) {
            // 执行成功，检查与执行失败的交易是否有冲突，把执行失败的交易添加到重新执行的集合中
            checkConflictWithFailedMap(callableResult, contractResult);
            // 本合约与成功执行的其他合约没有冲突，处理业务逻辑，提交本合约
            byte[] contractAddress = contractResult.getContractAddress();
            // 获取合约当前余额
            BigInteger balance = vmContext.getBalance(chainId, contractAddress);
            contractResult.setPreBalance(balance);
            // 处理临时余额和合约内部转账
            contractTransferHandler.handleContractTransfer(chainId, blockTime, tx, contractResult, tempBalanceManager);
        }
        // 处理合约内部转账时可能失败，合约视为执行失败
        if(contractResult.isSuccess()) {
            callableResult.getResultList().add(contractResult);
            commitContract(contractResult);
        } else {
            // 执行失败，添加到执行失败的集合中
            putAll(callableResult.getFailedMap(), contractResult);
        }
    }

    private void commitContract(ContractResult contractResult) {
        if(!contractResult.isSuccess()) {
            return;
        }
        Object txTrackObj = contractResult.getTxTrack();
        if (txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
            ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
            txTrack.commit();
        }
    }

    private void checkConflictWithFailedMap(CallableResult callableResult, ContractResult contractResult) {
        Map<String, Set<ContractResult>> failedMap = callableResult.getFailedMap();
        Set<String> addressSet = collectAddress(contractResult);
        List<ContractResult> reCallList = callableResult.getReCallList();
        for (String address : addressSet) {
            Set<ContractResult> removedSet = failedMap.remove(address);
            if (removedSet != null) {
                reCallList.addAll(removedSet);
            }
        }
    }

    private boolean checkDeleteResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        boolean result = false;
        if (contractResult.isSuccess()) {
            result = true;
            commitContract(contractResult);
        }
        List<ContractResult> resultList = callableResult.getResultList();
        resultList.add(contractResult);
        return result;
    }
}
