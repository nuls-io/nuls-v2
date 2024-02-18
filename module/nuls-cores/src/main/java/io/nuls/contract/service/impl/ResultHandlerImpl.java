/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.*;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTransferHandler;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.AnalyzerResult;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.service.ContractCaller;
import io.nuls.contract.service.ResultHanlder;
import io.nuls.contract.util.CompareTxOrderAsc;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.config.ContractContext.ASSET_ID;
import static io.nuls.contract.config.ContractContext.CHAIN_ID;
import static io.nuls.core.constant.TxType.CALL_CONTRACT;

/**
 * @author: PierreLuo
 * @date: 2018/11/23
 */
@Component
public class ResultHandlerImpl implements ResultHanlder {

    @Autowired
    private ContractCaller contractCaller;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTransferHandler contractTransferHandler;

    @Override
    public List<ContractResult> handleAnalyzerResult(int chainId, ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, String preStateRoot) {
        try {
            BlockHeader currentBlockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
            long blockTime = currentBlockHeader.getTime();
            // Obtain the result of re executing the contract
            List<ContractResult> reCallResultList = this.reCall(batchExecutor, analyzerResult, chainId, preStateRoot);
            // Handle contracts that have failed calls and return those that need to be returnedNULS Generate an internal transfer transaction for a contract and return it to the caller
            this.handleFailedContract(chainId, analyzerResult, blockTime);
            // Assemble all contract results
            List<ContractResult> finalResultList = new ArrayList<>();
            finalResultList.addAll(analyzerResult.getSuccessList());
            finalResultList.addAll(analyzerResult.getFailedSet());
            finalResultList.addAll(reCallResultList);
            // Sort in ascending order of received transactions
            return finalResultList.stream().sorted(CompareTxOrderAsc.getInstance()).collect(Collectors.toList());
        } catch (IOException e) {
            Log.error(e);
            return Collections.emptyList();
        } catch (NulsException e) {
            Log.error(e);
            return Collections.emptyList();
        }
    }

    private void handleFailedContract(int chainId, AnalyzerResult analyzerResult, long blockTime) throws IOException, NulsException {
        ContractTempBalanceManager tempBalanceManager = contractHelper.getBatchInfoTempBalanceManager(chainId);
        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetId();

        Set<ContractResult> failedSet = analyzerResult.getFailedSet();
        for (ContractResult contractResult : failedSet) {
            ContractWrapperTransaction orginTx = contractResult.getTx();
            if (orginTx.getType() != CALL_CONTRACT) {
                continue;
            }
            ContractData contractData = orginTx.getContractData();
            BigInteger value = contractData.getValue();
            if (value.compareTo(BigInteger.ZERO) > 0) {

                byte[] contractAddress = contractData.getContractAddress();
                ContractTransferData txData = new ContractTransferData(orginTx.getHash(), contractAddress);

                CoinData coinData = new CoinData();
                ContractBalance balance = tempBalanceManager.getBalance(contractAddress, CHAIN_ID, ASSET_ID).getData();
                byte[] nonceBytes = RPCUtil.decode(balance.getNonce());

                CoinFrom coinFrom = new CoinFrom(contractAddress, chainId, assetsId, value, nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                CoinTo coinTo = new CoinTo(contractData.getSender(), chainId, assetsId, value, 0L);
                coinData.getTo().add(coinTo);

                ContractTransferTransaction tx = new ContractTransferTransaction();
                tx.setCoinDataObj(coinData);
                tx.setTxDataObj(txData);
                // The offset of the time for internal transfer transactions within the contract, used for sorting
                //tx.setTime(blockTime + orginTx.getOrder());
                tx.setTime(blockTime);

                tx.serializeData();
                NulsHash hash = NulsHash.calcHash(tx.serializeForHash());
                byte[] hashBytes = hash.getBytes();
                byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
                balance.setNonce(RPCUtil.encode(currentNonceBytes));
                tx.setHash(hash);
                contractResult.getContractTransferList().add(tx);
                contractResult.setMergedTransferList(contractTransferHandler.contractTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
                contractResult.setMergerdMultyAssetTransferList(contractTransferHandler.contractMultyAssetTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
            }
        }
    }

    private List<ContractResult> reCall(ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, int chainId, String preStateRoot) throws NulsException {
        // Re execute the contract
        List<ContractResult> list = analyzerResult.getReCallTxList();
        List<ContractWrapperTransaction> collectTxs = list.stream().sorted(CompareTxOrderAsc.getInstance()).map(c -> c.getTx()).collect(Collectors.toList());
        List<ContractResult> resultList = contractCaller.reCallTx(batchExecutor, collectTxs, chainId, preStateRoot);
        return resultList;
    }
}
