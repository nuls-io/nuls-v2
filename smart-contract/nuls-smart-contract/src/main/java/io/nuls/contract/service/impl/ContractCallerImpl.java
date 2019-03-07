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
package io.nuls.contract.service.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.contract.callable.ContractTxCallable;
import io.nuls.contract.executorbus.ContractExecutorBus;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTransferHandler;
import io.nuls.contract.manager.TempBalanceManager;
import io.nuls.contract.model.bo.CallableResult;
import io.nuls.contract.model.bo.CallerResult;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractCaller;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.util.*;
import java.util.concurrent.Future;

import static io.nuls.contract.constant.ContractConstant.TX_TYPE_CALL_CONTRACT;
import static io.nuls.contract.util.ContractUtil.makeContractResult;


/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Service
public class ContractCallerImpl implements ContractCaller {

    @Autowired
    private ContractExecutor contractExecutor;

    @Autowired
    private ContractHelper contractHelper;

    @Autowired
    private ContractTransferHandler contractTransferHandler;

    @Override
    public CallerResult caller(int chainId, ProgramExecutor batchExecutor, Map<String, List<ContractWrapperTransaction>> txMap, String preStateRoot) {

        List<CallableResult> resultList = new ArrayList<>();
        CallerResult callerResult = new CallerResult();
        try {
            BlockHeader currentBlockHeader = contractHelper.getCurrentBlockHeader(chainId);
            long blockTime = currentBlockHeader.getTime();
            long number = currentBlockHeader.getHeight();

            callerResult.setCallableResultList(resultList);

            ContractExecutorBus contractExecutorBus = ContractExecutorBus.newInstance();

            ContractConflictChecker checker = ContractConflictChecker.newInstance();
            Set<String>[] sets = new Set[txMap.size()];
            checker.setContractSetArray(sets);
            Set<Map.Entry<String, List<ContractWrapperTransaction>>> entries = txMap.entrySet();
            Set<String> commitSet;

            int i = 0;
            for (Map.Entry<String, List<ContractWrapperTransaction>> addressTxs : entries) {
                commitSet = new HashSet<>();
                sets[i++] = commitSet;
                String contract = addressTxs.getKey();
                List<ContractWrapperTransaction> txList = addressTxs.getValue();
                contractExecutorBus.add(new ContractTxCallable(chainId, blockTime, batchExecutor, contract, txList, number, preStateRoot, checker, commitSet));
            }

            List<Future<CallableResult>> executeList = contractExecutorBus.execute();
            if (executeList == null) {
                return callerResult;
            }
            for (Future<CallableResult> future : executeList) {
                CallableResult callableResult = future.get();
                resultList.add(callableResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return callerResult;
    }

    @Override
    public List<ContractResult> callerReCallTx(ProgramExecutor batchExecutor, List<ContractWrapperTransaction> reCallTxList, int chainId, String preStateRoot) {
        BlockHeader currentBlockHeader = contractHelper.getCurrentBlockHeader(chainId);
        long blockTime = currentBlockHeader.getTime();
        long number = currentBlockHeader.getHeight();
        TempBalanceManager tempBalanceManager = contractHelper.getTempBalanceManager(chainId);
        List<ContractResult> resultList = new ArrayList<>();
        ContractData contractData;
        ContractResult contractResult;
        for (ContractWrapperTransaction tx : reCallTxList) {
            contractData = tx.getContractData();
            switch (tx.getType()) {
                case TX_TYPE_CALL_CONTRACT:
                    contractResult = contractExecutor.call(batchExecutor, contractData, number, preStateRoot);
                    makeContractResult(tx, contractResult);
                    // 处理重新执行的合约的结果
                    contractTransferHandler.handleContractTransfer(chainId, blockTime, tx, contractResult, tempBalanceManager);
                    resultList.add(contractResult);
                    break;
                default:
                    break;
            }
        }
        return resultList;
    }

    @Override
    public Result<byte[]> commitBatchExecute(ProgramExecutor executor) {
        if (executor == null) {
            return ContractUtil.getSuccess();
        }
        executor.commit();
        byte[] stateRoot = executor.getRoot();
        return ContractUtil.getSuccess().setData(stateRoot);
    }

}
