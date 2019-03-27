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

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTransferHandler;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.CallableResult;
import io.nuls.contract.model.bo.ContractContainer;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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
public class ContractTxCallable implements Callable<ContractResult> {

    private ContractExecutor contractExecutor;
    private ContractHelper contractHelper;
    private VMContext vmContext;
    private ContractTransferHandler contractTransferHandler;
    private ContractTempBalanceManager tempBalanceManager;
    private ProgramExecutor executor;
    private String contract;
    private ContractWrapperTransaction tx;
    private long number;
    private String preStateRoot;
    private ContractConflictChecker checker;
    private ContractContainer container;
    private int chainId;
    private long blockTime;


    public ContractTxCallable(int chainId, long blockTime, ProgramExecutor executor, String contract, ContractWrapperTransaction tx, long number, String preStateRoot, ContractConflictChecker checker, ContractContainer container) {
        this.chainId = chainId;
        this.blockTime = blockTime;
        this.contractExecutor = SpringLiteContext.getBean(ContractExecutor.class);
        this.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        this.vmContext = SpringLiteContext.getBean(VMContext.class);
        this.contractTransferHandler = SpringLiteContext.getBean(ContractTransferHandler.class);
        this.tempBalanceManager = contractHelper.getBatchInfoTempBalanceManager(chainId);
        this.executor = executor;
        this.contract = contract;
        this.tx = tx;
        this.number = number;
        this.preStateRoot = preStateRoot;
        this.checker = checker;
        this.container = container;
    }

    @Override
    public ContractResult call() throws Exception {
        CallableResult callableResult = container.getCallableResult();
        ContractData contractData;
        ContractResult contractResult = null;
        contractData = tx.getContractData();
        int type = tx.getType();
        do {
            // 创建合约无论成功与否，后续的其他的跳过执行，视作失败 -> 合约锁定中或者合约不存在
            if (container.isHasCreate()) {
                contractResult = ContractResult.genFailed(contractData, "contract lock or not exist.");
                break;
            }
            // 删除合约成功后，后续的其他的跳过执行，视作失败 -> 合约已删除
            if (container.isDelete()) {
                contractResult = ContractResult.genFailed(contractData, "contract has been terminated.");
                break;
            }

            if (type != TX_TYPE_DELETE_CONTRACT && !ContractUtil.checkPrice(contractData.getPrice())) {
                contractResult = ContractResult.genFailed(contractData, "The minimum value of price is 25.");
                break;
            }

            switch (type) {
                case TX_TYPE_CREATE_CONTRACT:
                    container.setHasCreate(true);
                    contractResult = contractExecutor.create(executor, contractData, number, preStateRoot);
                    checkCreateResult(tx, callableResult, contractResult);
                    break;
                case TX_TYPE_CALL_CONTRACT:
                    contractResult = contractExecutor.call(executor, contractData, number, preStateRoot);
                    checkCallResult(tx, callableResult, contractResult);
                    break;
                case TX_TYPE_DELETE_CONTRACT:
                    contractResult = contractExecutor.delete(executor, contractData, number, preStateRoot);
                    boolean isDelete = checkDeleteResult(tx, callableResult, contractResult);
                    container.setDelete(isDelete);
                    break;
                default:
                    break;
            }
        } while (false);
        if(!contractResult.isSuccess()) {
            Log.error("TxType [{}] Execute ContractResult is {}", tx.getType(), contractResult);
        }
        return contractResult;
    }

    private void checkCreateResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        if (contractResult.isSuccess()) {
            Result checkResult = contractHelper.validateNrc20Contract(chainId, (ProgramExecutor) contractResult.getTxTrack(), tx, contractResult);
            if(checkResult.isFailed()) {
                Log.error("check validateNrc20Contract Result is {}", checkResult);
            }
            if (checkResult.isSuccess()) {
                container.getCommitSet().add(contract);
                commitContract(contractResult);
            }
            callableResult.getResultList().add(contractResult);
        } else {
            // 执行失败，添加到执行失败的集合中
            putAll(callableResult.getFailedMap(), contractResult);
        }
    }


    private void checkCallResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) throws IOException {
        makeContractResult(tx, contractResult);
        List<ContractResult> reCallList = callableResult.getReCallList();
        boolean isConflict = checker.checkConflict(tx, contractResult, container.getCommitSet());
        if (isConflict) {
            // 冲突后，添加到重新执行的集合中，但是gas消耗完的不再重复执行
            if (!isNotEnoughGasError(contractResult)) {
                Log.error("Conflict TxType [{}] Execute ContractResult is {}", tx.getType(), contractResult);
                reCallList.add(contractResult);
            } else {
                // 执行失败，添加到执行失败的集合中
                putAll(callableResult.getFailedMap(), contractResult);
            }
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
            Log.info("[{}] current balance is {}", AddressTool.getStringAddressByBytes(contractAddress), balance.toString());
            contractResult.setPreBalance(balance);
            // 处理临时余额和合约内部转账
            contractTransferHandler.handleContractTransfer(chainId, blockTime, tx, contractResult, tempBalanceManager);
        }
        // 处理合约内部转账成功后，提交合约
        if (contractResult.isSuccess()) {
            callableResult.getResultList().add(contractResult);
            commitContract(contractResult);
        } else {
            // 处理合约内部转账时可能失败，合约视为执行失败，执行失败，添加到执行失败的集合中
            putAll(callableResult.getFailedMap(), contractResult);
        }
    }

    private void commitContract(ContractResult contractResult) {
        if (!contractResult.isSuccess()) {
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
        for (String address : addressSet) {
            Set<ContractResult> removedSet = failedMap.get(address);
            if (removedSet != null) {
                List<ContractResult> recallList = new ArrayList<>();
                // 失败的合约，gas消耗完的不再重复执行
                for (ContractResult _contractResult : removedSet) {
                    if (!isNotEnoughGasError(_contractResult)) {
                        callableResult.getReCallList().add(_contractResult);
                        recallList.add(_contractResult);
                    }
                }
                // 移除失败合约（已转移到重新执行的合约集合中）
                if (recallList.size() > 0) {
                    if (recallList.size() == removedSet.size()) {
                        failedMap.remove(address);
                    } else {
                        removedSet.removeAll(recallList);
                    }
                }
            }
        }
    }

    private boolean checkDeleteResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        boolean result = false;
        if (contractResult.isSuccess()) {
            result = true;
            commitContract(contractResult);
            callableResult.getResultList().add(contractResult);
        } else {
            // 执行失败，添加到执行失败的集合中
            putAll(callableResult.getFailedMap(), contractResult);
        }
        return result;
    }
}
