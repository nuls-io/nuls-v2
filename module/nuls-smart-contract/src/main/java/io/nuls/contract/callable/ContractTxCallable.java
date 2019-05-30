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
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractNewTxHandler;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.CallableResult;
import io.nuls.contract.model.bo.ContractContainer;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static io.nuls.contract.util.ContractUtil.*;
import static io.nuls.core.constant.TxType.*;


/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class ContractTxCallable implements Callable<ContractResult> {

    private ContractExecutor contractExecutor;
    private ContractHelper contractHelper;
    private ContractNewTxHandler contractNewTxHandler;
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
        this.contractNewTxHandler = SpringLiteContext.getBean(ContractNewTxHandler.class);
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
        ChainManager.chainHandle(chainId);
        long start = 0L;
        if (Log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        CallableResult callableResult = container.getCallableResult();
        ContractData contractData;
        ContractResult contractResult = null;
        contractData = tx.getContractData();
        int type = tx.getType();
        do {
            // 创建合约无论成功与否，后续的其他的跳过执行，视作失败 -> 合约锁定中或者合约不存在
            if (container.isHasCreate()) {
                contractResult = contractHelper.makeFailedContractResult(chainId, tx, callableResult, "contract lock or not exist.");
                break;
            }
            // 删除合约成功后，后续的其他的跳过执行，视作失败 -> 合约已删除
            if (container.isDelete()) {
                contractResult = contractHelper.makeFailedContractResult(chainId, tx, callableResult, "contract has been terminated.");
                break;
            }

            if (type != DELETE_CONTRACT && !ContractUtil.checkPrice(contractData.getPrice())) {
                contractResult = contractHelper.makeFailedContractResult(chainId, tx, callableResult, "The minimum value of price is 25.");
                break;
            }

            //TODO sender public key storage

            switch (type) {
                case CREATE_CONTRACT:
                    container.setHasCreate(true);
                    contractResult = contractExecutor.create(executor, contractData, number, preStateRoot, extractPublicKey(tx));
                    checkCreateResult(tx, callableResult, contractResult);
                    break;
                case CALL_CONTRACT:
                    contractResult = contractExecutor.call(executor, contractData, number, preStateRoot, extractPublicKey(tx));
                    checkCallResult(tx, callableResult, contractResult);
                    break;
                case DELETE_CONTRACT:
                    contractResult = contractExecutor.delete(executor, contractData, number, preStateRoot);
                    boolean isDelete = checkDeleteResult(tx, callableResult, contractResult);
                    container.setDelete(isDelete);
                    break;
                default:
                    break;
            }
        } while (false);
        if (!contractResult.isSuccess()) {
            Log.error("Failed TxType [{}] Execute ContractResult is {}", tx.getType(), contractResult.toString());
        }
        if (Log.isDebugEnabled()) {
            Log.debug("[Per Contract Execution Cost Time] TxType is {}, TxHash is {}, Cost Time is {}", tx.getType(), tx.getHash().toString(), System.currentTimeMillis() - start);
        }
        return contractResult;
    }

    private void checkCreateResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        if (contractResult.isSuccess()) {
            Result checkResult = contractHelper.validateNrc20Contract(chainId, (ProgramExecutor) contractResult.getTxTrack(), tx, contractResult);
            if (checkResult.isFailed()) {
                Log.error("check validateNrc20Contract Result is {}", checkResult.toString());
            }
            if (checkResult.isSuccess()) {
                container.getCommitSet().add(contract);
                commitContract(contractResult);
            }
            callableResult.getResultList().add(contractResult);
        } else {
            // 执行失败，添加到执行失败的集合中
            callableResult.putFailed(chainId, contractResult);
        }
    }


    private void checkCallResult(ContractWrapperTransaction tx, CallableResult callableResult, ContractResult contractResult) throws IOException {
        makeContractResult(tx, contractResult);
        List<ContractResult> reCallList = callableResult.getReCallList();
        boolean isConflict = checker.checkConflict(chainId, tx, contractResult, container.getCommitSet());
        if (isConflict) {
            // 冲突后，添加到重新执行的集合中，但是gas消耗完的不再重复执行
            if (!isNotEnoughGasError(contractResult)) {
                Log.error("Conflict TxType [{}] Execute ContractResult is {}", tx.getType(), contractResult.toString());
                reCallList.add(contractResult);
            } else {
                // 执行失败，添加到执行失败的集合中
                callableResult.putFailed(chainId, contractResult);
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
            // 处理合约生成的其他交易、临时余额、合约内部转账
            contractNewTxHandler.handleContractNewTx(chainId, blockTime, tx, contractResult, tempBalanceManager);
        }
        // 处理合约内部转账成功后，提交合约
        if (contractResult.isSuccess()) {
            callableResult.getResultList().add(contractResult);
            commitContract(contractResult);
        } else {
            // 处理合约内部转账时可能失败，合约视为执行失败，执行失败，添加到执行失败的集合中
            callableResult.putFailed(chainId, contractResult);
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
            if (Log.isDebugEnabled()) {
                Log.debug("One of Batch contract[{}] commit", AddressTool.getStringAddressByBytes(contractResult.getContractAddress()));
            }
        }
    }

    private void checkConflictWithFailedMap(CallableResult callableResult, ContractResult contractResult) {
        Map<String, Set<ContractResult>> failedMap = callableResult.getFailedMap();
        Set<String> addressSet = collectAddress(chainId, contractResult);
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
            callableResult.putFailed(chainId, contractResult);
        }
        return result;
    }
}
