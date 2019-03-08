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

import io.nuls.base.data.*;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.TempBalanceManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.service.ContractCaller;
import io.nuls.contract.service.ResultHanlder;
import io.nuls.contract.util.CompareTx;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.constant.ContractConstant.TX_TYPE_CALL_CONTRACT;

/**
 * @author: PierreLuo
 * @date: 2018/11/23
 */
@Service
public class ResultHandlerImpl implements ResultHanlder {

    @Autowired
    private ContractCaller contractCaller;

    @Autowired
    private ContractHelper contractHelper;

    @Override
    public List<ContractResult> handleAnalyzerResult(int chainId, ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, String preStateRoot) {
        try {
            BlockHeader currentBlockHeader = contractHelper.getCurrentBlockHeader(chainId);
            long blockTime = currentBlockHeader.getTime();
            long number = currentBlockHeader.getHeight();
            // 得到重新执行的合约结果
            List<ContractResult> reCallResultList = this.reCall(batchExecutor, analyzerResult, chainId, blockTime, number, preStateRoot);
            // 处理调用失败的合约，把需要退还的NULS 生成一笔合约内部转账交易，退还给调用者
            this.handleFailedContract(chainId, analyzerResult, blockTime);
            // 组装所有的合约结果
            List<ContractResult> finalResultList = new ArrayList<>();
            finalResultList.addAll(analyzerResult.getSuccessList());
            finalResultList.addAll(analyzerResult.getFailedSet());
            finalResultList.addAll(reCallResultList);
            // 按时间排序
            return finalResultList.stream().sorted(CompareTx.getInstance()).collect(Collectors.toList());
        } catch (IOException e) {
            Log.error(e);
            return Collections.emptyList();
        }
    }

    private void handleFailedContract(int chainId, AnalyzerResult analyzerResult, long blockTime) throws IOException {
        TempBalanceManager tempBalanceManager = contractHelper.getTempBalanceManager(chainId);
        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetsId();

        Set<ContractResult> failedSet = analyzerResult.getFailedSet();
        for(ContractResult contractResult : failedSet) {
            long value = contractResult.getValue();
            if(value > 0) {
                ContractWrapperTransaction orginTx = contractResult.getTx();
                if(orginTx.getType() != TX_TYPE_CALL_CONTRACT) {
                    continue;
                }

                ContractTransferData txData = new ContractTransferData();
                txData.setOrginTxHash(orginTx.getHash());
                byte[] contractAddress = contractResult.getContractAddress();
                txData.setContractAddress(contractAddress);

                CoinData coinData = new CoinData();
                ContractBalance balance = tempBalanceManager.getBalance(contractAddress).getData();
                byte[] nonceBytes = Hex.decode(balance.getNonce());
                CoinFrom coinFrom = new CoinFrom(contractAddress, chainId, assetsId, BigInteger.valueOf(value), nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                CoinTo coinTo = new CoinTo(contractResult.getSender(), chainId, assetsId, BigInteger.valueOf(value), 0L);
                coinData.getTo().add(coinTo);

                ContractTransferTransaction tx = new ContractTransferTransaction();
                tx.setCoinDataObj(coinData);
                tx.setTxDataObj(txData);
                // 合约内部转账交易的时间的偏移量，用于排序
                tx.setTime(blockTime + orginTx.getOrder());

                tx.serializeData();
                NulsDigestData hash = NulsDigestData.calcDigestData(tx.serializeForHash());
                byte[] hashBytes = hash.serialize();
                byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
                balance.setNonce(Hex.toHexString(currentNonceBytes));
                tx.setHash(hash);
                contractResult.getContractTransferList().add(tx);
            }
        }
    }

    private List<ContractResult> reCall(ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, int chainId, long blockTime, long number, String preStateRoot) {
        // 重新执行合约
        List<ContractResult> list = analyzerResult.getReCallTxList();
        List<ContractWrapperTransaction> collect = list.stream().map(c -> c.getTx()).collect(Collectors.toList());
        List<ContractResult> resultList = contractCaller.callerReCallTx(batchExecutor, collect, chainId, preStateRoot);
        return resultList;
    }
}
