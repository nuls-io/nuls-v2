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
package io.nuls.contract.helper;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.rpc.util.RPCUtil;
import org.apache.commons.lang3.StringUtils;
import org.ethereum.crypto.HashUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.nuls.contract.constant.ContractErrorCode.TOO_SMALL_AMOUNT;
import static io.nuls.contract.util.ContractUtil.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class ContractTransferHandler {

    @Autowired
    private ContractHelper contractHelper;

    public boolean handleContractTransfer(int chainId, long blockTime, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        this.refreshTempBalance(chainId, contractResult, tempBalanceManager);
        return this.handleContractTransferTxs(contractResult, tempBalanceManager, chainId, blockTime);
    }

    private void refreshTempBalance(int chainId, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        // 增加转入, 扣除转出
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        if (transfers != null && transfers.size() > 0) {
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[1];
            byte[] contractBytes;
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                contractBytes = asBytes(to.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.addTempBalance(contractBytes, to.getValue());
            }
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                contractBytes = asBytes(from.getKey());
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes).getData();
                if (StringUtils.isBlank(balance.getPreNonce())) {
                    balance.setPreNonce(balance.getNonce());
                }
                tempBalanceManager.minusTempBalance(contractBytes, from.getValue());
            }
        }
    }

    private void rollbackContractTempBalance(int chainId, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        // 增加转出, 扣除转入
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        if (transfers != null && transfers.size() > 0) {
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[1];
            byte[] contractBytes;
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                contractBytes = asBytes(from.getKey());
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes).getData();
                if (StringUtils.isNotBlank(balance.getPreNonce())) {
                    balance.setNonce(balance.getPreNonce());
                }
                tempBalanceManager.addTempBalance(contractBytes, from.getValue());
            }
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                contractBytes = asBytes(to.getKey());
                tempBalanceManager.minusTempBalance(contractBytes, to.getValue());
            }
        }
    }

    private LinkedHashMap<String, BigInteger>[] filterContractValue(int chainId, List<ProgramTransfer> transfers) {
        LinkedHashMap<String, BigInteger> contractFromValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger>[] contracts = new LinkedHashMap[2];
        contracts[0] = contractFromValue;
        contracts[1] = contractToValue;

        byte[] from, to;
        BigInteger transferValue;
        for (ProgramTransfer transfer : transfers) {
            from = transfer.getFrom();
            to = transfer.getTo();
            transferValue = transfer.getValue();
            if (ContractUtil.isLegalContractAddress(chainId, from)) {
                mapAddBigInteger(contractFromValue, from, transferValue);
            }
            if (ContractUtil.isLegalContractAddress(chainId, to)) {
                mapAddBigInteger(contractToValue, to, transferValue);
            }
        }
        return contracts;
    }

    private boolean handleContractTransferTxs(ContractResult contractResult, ContractTempBalanceManager tempBalanceManager, int chainId, long blockTime) {
        boolean isCorrectContractTransfer = true;
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        // 创建合约转账(从合约转出)交易
        if (transfers != null && transfers.size() > 0) {
            Result result;
            do {
                // 验证合约转账(从合约转出)交易的最小转移金额
                result = this.verifyTransfer(transfers);
                if (result.isFailed()) {
                    isCorrectContractTransfer = false;
                    break;
                }
                // 合并内部转账交易并创建合约内部转账交易
                try {
                    this.mergeContractTransfer(contractResult, chainId, blockTime, tempBalanceManager);
                } catch (Exception e) {
                    isCorrectContractTransfer = false;
                    Log.error(e);
                    break;
                }
            } while (false);

            // 如果合约转账(从合约转出)出现错误，整笔合约交易视作合约执行失败
            if (!isCorrectContractTransfer) {
                Log.warn("contract transfer execution failed, reason: {}", contractResult.getErrorMessage());
                contractResult.setError(true);
                contractResult.setErrorMessage(result.getErrorCode().getMsg());
                // 回滚临时余额
                this.rollbackContractTempBalance(chainId, contractResult, tempBalanceManager);
                // 清空内部转账列表
                transfers.clear();
            }
        }
        return isCorrectContractTransfer;
    }

    private Result verifyTransfer(List<ProgramTransfer> transfers) {
        if (transfers == null || transfers.size() == 0) {
            return getSuccess();
        }
        for (ProgramTransfer transfer : transfers) {
            if (transfer.getValue().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                return Result.getFailed(TOO_SMALL_AMOUNT);
            }
        }
        return getSuccess();
    }

    private void mergeContractTransfer(ContractResult contractResult, int chainId, long blockTime, ContractTempBalanceManager tempBalanceManager) throws Exception {
        ContractWrapperTransaction tx = contractResult.getTx();
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        byte[] contractAddress = contractResult.getContractAddress();

        List<ContractTransferTransaction> contractTransferList = new ArrayList<>();
        contractResult.setContractTransferList(contractTransferList);
        ContractTransferData txData = new ContractTransferData(tx.getHash(), contractAddress);

        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetsId();
        Map<String, CoinTo> mergeCoinToMap = MapUtil.createHashMap(transfers.size());

        CoinData coinData = null;
        CoinFrom coinFrom = null;
        CoinTo coinTo;
        ByteArrayWrapper compareFrom = null;
        byte[] nonceBytes;
        ContractTransferTransaction contractTransferTx = null;
        ContractBalance contractBalance = null;
        // 合约内部转账交易的时间的偏移量，用于排序
        long timeOffset;
        //int i = 0;
        for (ProgramTransfer transfer : transfers) {
            byte[] from = transfer.getFrom();
            byte[] to = transfer.getTo();
            BigInteger value = transfer.getValue();
            ByteArrayWrapper wrapperFrom = new ByteArrayWrapper(from);
            if (compareFrom == null || !compareFrom.equals(wrapperFrom)) {
                // 产生新交易
                if (compareFrom == null) {
                    // 第一次遍历，获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.getBalance(from).getData();
                    nonceBytes = RPCUtil.decode(contractBalance.getNonce());
                } else {
                    // 产生另一个合并交易，更新之前的合并交易的hash和账户的nonce
                    this.updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);
                    mergeCoinToMap.clear();
                    // 获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.getBalance(from).getData();
                    nonceBytes = RPCUtil.decode(contractBalance.getNonce());
                }
                Log.info("From is {}, nonce is {}", AddressTool.getStringAddressByBytes(from), contractBalance.getNonce());
                compareFrom = wrapperFrom;
                coinData = new CoinData();
                coinFrom = new CoinFrom(from, chainId, assetsId, value, nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                coinTo = new CoinTo(to, chainId, assetsId, value, 0L);
                coinData.getTo().add(coinTo);
                mergeCoinToMap.put(asString(to), coinTo);
                //timeOffset = tx.getOrder() + (i++);
                timeOffset = 0L;
                contractTransferTx = this.createContractTransferTx(coinData, txData, blockTime, timeOffset);
                contractTransferList.add(contractTransferTx);
            } else {
                // 增加coinFrom的转账金额
                coinFrom.setAmount(coinFrom.getAmount().add(value));
                // 合并coinTo
                this.mergeCoinTo(mergeCoinToMap, coinData, to, chainId, assetsId, value);
            }
        }
        // 最后产生的合并交易，遍历结束后更新它的hash和账户的nonce
        this.updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);

        List<ContractMergedTransfer> mergerdTransferList = this.contractTransfer2mergedTransfer(tx, contractTransferList);
        contractResult.setMergedTransferList(mergerdTransferList);
    }

    private void mergeCoinTo(Map<String, CoinTo> mergeCoinToMap, CoinData coinData, byte[] to, int chainId, int assetsId, BigInteger value) {
        CoinTo coinTo;
        String key = asString(to);
        if ((coinTo = mergeCoinToMap.get(key)) != null) {
            coinTo.setAmount(coinTo.getAmount().add(value));
        } else {
            coinTo = new CoinTo(to, chainId, assetsId, value, 0L);
            coinData.getTo().add(coinTo);
            mergeCoinToMap.put(key, coinTo);
        }
    }

    public List<ContractMergedTransfer> contractTransfer2mergedTransfer(Transaction tx, List<ContractTransferTransaction> transferList) throws NulsException {
        List<ContractMergedTransfer> resultList = new ArrayList<>();
        for (ContractTransferTransaction transfer : transferList) {
            resultList.add(this.transformMergedTransfer(tx.getHash(), transfer));
        }
        return resultList;
    }

    private ContractMergedTransfer transformMergedTransfer(byte[] orginHash, ContractTransferTransaction transfer) throws NulsException {
        ContractMergedTransfer result = new ContractMergedTransfer();
        CoinData coinData = transfer.getCoinDataObj();
        CoinFrom coinFrom = coinData.getFrom().get(0);
        result.setFrom(coinFrom.getAddress());
        result.setValue(coinFrom.getAmount());
        List<CoinTo> toList = coinData.getTo();
        List<Output> outputs = result.getOutputs();
        Output output;
        for (CoinTo to : toList) {
            output = new Output();
            output.setTo(to.getAddress());
            output.setValue(to.getAmount());
            outputs.add(output);
        }
        result.setHash(transfer.getHash());
        result.setOrginHash(orginHash);
        return result;
    }

    private void updatePreTxHashAndAccountNonce(ContractTransferTransaction tx, ContractBalance balance) throws IOException {
        tx.serializeData();
        byte[] hash = io.nuls.core.parse.HashUtil.calcHash(tx.serializeForHash());
        byte[] currentNonceBytes = Arrays.copyOfRange(hash, hash.length - 8, hash.length);
        balance.setNonce(RPCUtil.encode(currentNonceBytes));
        tx.setHash(hash);
        Log.info("TxType is {}, hash is {}, nextNonce is {}", tx.getType(), hash.toString(), RPCUtil.encode(currentNonceBytes));
    }

    private ContractTransferTransaction createContractTransferTx(CoinData coinData, ContractTransferData txData, long blockTime, long timeOffset) {
        ContractTransferTransaction contractTransferTx = new ContractTransferTransaction();
        contractTransferTx.setCoinDataObj(coinData);
        contractTransferTx.setTxDataObj(txData);
        contractTransferTx.setTime(blockTime + timeOffset);
        return contractTransferTx;
    }
}
