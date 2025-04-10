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
package io.nuls.contract.helper;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractConstant;
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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.config.ContractContext.LOCAL_MAIN_ASSET_ID;
import static io.nuls.contract.config.ContractContext.LOCAL_CHAIN_ID;
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

    //public boolean handleContractTransfer(int chainId, long blockTime, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
    //    this.refreshTempBalance(chainId, contractResult, tempBalanceManager);
    //    return this.handleContractTransferTxs(contractResult, tempBalanceManager, chainId, blockTime);
    //}

    public boolean refreshTempBalance(int chainId, List<ProgramTransfer> transfers, ContractTempBalanceManager tempBalanceManager) {
        try {
            // Increase transfer in, Deduction transfer out
            if (transfers != null && transfers.size() > 0) {
                LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
                LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
                LinkedHashMap<String, BigInteger> contractToValue = contracts[1];
                LinkedHashMap<String, BigInteger> contractToLockValue = contracts[2];
                byte[] contractBytes;
                int assetChainId, assetId;
                // Increase lock in transfer
                Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
                for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                    String key = lockTo.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    // Initialize temporary balance
                    tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                    tempBalanceManager.addLockedTempBalance(contractBytes, lockTo.getValue(), assetChainId, assetId);
                }
                // Increase transfer in
                Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
                for (Map.Entry<String, BigInteger> to : tos) {
                    String key = to.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    // Initialize temporary balance
                    tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                    tempBalanceManager.addTempBalance(contractBytes, to.getValue(), assetChainId, assetId);
                }
                // Deduction transfer out
                Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
                for (Map.Entry<String, BigInteger> from : froms) {
                    String key = from.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    ContractBalance balance = tempBalanceManager.getBalance(contractBytes, assetChainId, assetId).getData();
                    if (StringUtils.isBlank(balance.getPreNonce())) {
                        balance.setPreNonce(balance.getNonce());
                    }
                    tempBalanceManager.minusTempBalance(contractBytes, from.getValue(), assetChainId, assetId);
                }
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public boolean rollbackContractTempBalance(int chainId, List<ProgramTransfer> transfers, ContractTempBalanceManager tempBalanceManager) {
        try {
            // Increase transfer out, Deduction of transfer in
            if (transfers != null && transfers.size() > 0) {
                LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
                LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
                LinkedHashMap<String, BigInteger> contractToValue = contracts[1];
                LinkedHashMap<String, BigInteger> contractToLockValue = contracts[2];
                byte[] contractBytes;
                int assetChainId, assetId;
                // Increase transfer out
                Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
                for (Map.Entry<String, BigInteger> from : froms) {
                    String key = from.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    ContractBalance balance = tempBalanceManager.getBalance(contractBytes, assetChainId, assetId).getData();
                    if (StringUtils.isNotBlank(balance.getPreNonce())) {
                        balance.setNonce(balance.getPreNonce());
                    }
                    tempBalanceManager.addTempBalance(contractBytes, from.getValue(), assetChainId, assetId);
                }
                // Deduction of transfer in
                Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
                for (Map.Entry<String, BigInteger> to : tos) {
                    String key = to.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    tempBalanceManager.minusTempBalance(contractBytes, to.getValue(), assetChainId, assetId);
                }
                // Deduction lock in transfer
                Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
                for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                    String key = lockTo.getKey();
                    String[] keySplit = key.split(ContractConstant.LINE);
                    contractBytes = asBytes(keySplit[0]);
                    assetChainId = Integer.parseInt(keySplit[1]);
                    assetId = Integer.parseInt(keySplit[2]);
                    // Initialize temporary balance
                    tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                    tempBalanceManager.minusLockedTempBalance(contractBytes, lockTo.getValue(), assetChainId, assetId);
                }
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    private LinkedHashMap<String, BigInteger>[] filterContractValue(int chainId, List<ProgramTransfer> transfers) {
        LinkedHashMap<String, BigInteger> contractFromValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToLockValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger>[] contracts = new LinkedHashMap[3];
        contracts[0] = contractFromValue;
        contracts[1] = contractToValue;
        contracts[2] = contractToLockValue;

        byte[] from, to;
        int assetChainId, assetId;
        BigInteger transferValue;
        boolean lock;
        for (ProgramTransfer transfer : transfers) {
            from = transfer.getFrom();
            to = transfer.getTo();
            transferValue = transfer.getValue();
            assetChainId = transfer.getAssetChainId();
            assetId = transfer.getAssetId();
            lock = transfer.getLockedTime() > 0;
            if (ContractUtil.isLegalContractAddress(chainId, from)) {
                mapAddBigInteger(contractFromValue, from, assetChainId, assetId, transferValue);
            }
            if (ContractUtil.isLegalContractAddress(chainId, to)) {
                if (lock) {
                    mapAddBigInteger(contractToLockValue, to, assetChainId, assetId, transferValue);
                } else {
                    mapAddBigInteger(contractToValue, to, assetChainId, assetId, transferValue);
                }
            }
        }
        return contracts;
    }

    public boolean handleContractTransferTxs(int chainId, long blockTime, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        boolean isCorrectContractTransfer = true;
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        // Create contract transfer(Transfer out from contract)transaction
        if (transfers != null && transfers.size() > 0) {
            Result result;
            do {
                if (ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.PROTOCOL_22) {
                    // Verify contract transfer(Transfer out from contract)Minimum transfer amount for transactions
                    result = this.verifyTransferP22(transfers);
                } else {
                    // Verify contract transfer(Transfer out from contract)Minimum transfer amount for transactions
                    result = this.verifyTransfer(transfers);
                }
                if (result.isFailed()) {
                    isCorrectContractTransfer = false;
                    break;
                }
                // Merge internal transfer transactions and create contract internal transfer transactions
                try {
                    this.mergeContractTransfer(contractResult, chainId, blockTime, tempBalanceManager);
                } catch (Exception e) {
                    isCorrectContractTransfer = false;
                    Log.error(e);
                    break;
                }
            } while (false);

            // If the contract transfer is made(Transfer out from contract)If an error occurs, the entire contract transaction will be considered as a contract execution failure
            if (!isCorrectContractTransfer) {
                Log.warn("contract transfer execution failed, reason: {}", contractResult.getErrorMessage());
                contractResult.setError(true);
                contractResult.setErrorMessage(result.getErrorCode().getMsg());
                // Rollback temporary balance
                this.rollbackContractTempBalance(chainId, contractResult.getTransfers(), tempBalanceManager);
                // Clear internal transfer list
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
            if (transfer.getAssetChainId() == LOCAL_CHAIN_ID && transfer.getAssetId() == LOCAL_MAIN_ASSET_ID && transfer.getValue().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                return Result.getFailed(TOO_SMALL_AMOUNT);
            }
        }
        return getSuccess();
    }

    private Result verifyTransferP22(List<ProgramTransfer> transfers) {
        return getSuccess();
    }

    private void mergeContractTransfer(ContractResult contractResult, int chainId, long blockTime, ContractTempBalanceManager tempBalanceManager) throws Exception {
        ContractWrapperTransaction tx = contractResult.getTx();
        List<ProgramTransfer> transfers = contractResult.getTransfers();
        byte[] contractAddress = contractResult.getContractAddress();

        List<ContractTransferTransaction> contractTransferList = new ArrayList<>();
        contractResult.setContractTransferList(contractTransferList);
        ContractTransferData txData = new ContractTransferData(tx.getHash(), contractAddress);

        Map<String, CoinTo> mergeCoinToMap = MapUtil.createHashMap(transfers.size());

        CoinData coinData = null;
        CoinFrom coinFrom = null;
        CoinTo coinTo;
        String compareFrom = null;
        byte[] nonceBytes;
        ContractTransferTransaction contractTransferTx = null;
        ContractBalance contractBalance = null;
        Map<String, ContractTransferTransaction> preTx = new HashMap<>();
        Map<String, ContractBalance> preBalance = new HashMap<>();
        // The offset of the time for internal transfer transactions within the contract, used for sorting（Abandoned）
        long timeOffset;
        //int i = 0;
        for (ProgramTransfer transfer : transfers) {
            byte[] from = transfer.getFrom();
            byte[] to = transfer.getTo();
            BigInteger value = transfer.getValue();
            int assetChainId = transfer.getAssetChainId();
            int assetId = transfer.getAssetId();
            long lockedTime = transfer.getLockedTime();
            String wrapperFrom = addressKey(from, assetChainId, assetId);
            if (compareFrom == null || !compareFrom.equals(wrapperFrom)) {
                // Generate new transactions
                if (compareFrom == null) {
                    // First traversal to obtain new transactionscoinFromofnonce
                    contractBalance = tempBalanceManager.getBalance(from, assetChainId, assetId).getData();
                    nonceBytes = RPCUtil.decode(contractBalance.getNonce());
                } else {
                    // Generate another merger transaction and update the previous merger transactionhashAnd the account'snonce
                    this.updatePreTxHashAndAccountNonce(preTx.get(compareFrom), preBalance.get(compareFrom));
                    mergeCoinToMap.clear();
                    // Obtain new transactionscoinFromofnonce
                    contractBalance = tempBalanceManager.getBalance(from, assetChainId, assetId).getData();
                    nonceBytes = RPCUtil.decode(contractBalance.getNonce());
                }
                Log.info("From is {}, assetChainId is {}, assetId is {}, nonce is {}", AddressTool.getStringAddressByBytes(from), assetChainId, assetId, contractBalance.getNonce());
                compareFrom = wrapperFrom;
                coinData = new CoinData();
                coinFrom = new CoinFrom(from, assetChainId, assetId, value, nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                coinTo = new CoinTo(to, assetChainId, assetId, value, lockedTime == 0 ? lockedTime : (blockTime + lockedTime));
                coinData.getTo().add(coinTo);
                mergeCoinToMap.put(addressLockedKey(to, assetChainId, assetId, lockedTime), coinTo);
                //timeOffset = tx.getOrder() + (i++);
                timeOffset = 0L;
                contractTransferTx = this.createContractTransferTx(coinData, txData, blockTime, timeOffset);
                contractTransferList.add(contractTransferTx);
                preTx.put(wrapperFrom, contractTransferTx);
                preBalance.put(wrapperFrom, contractBalance);
            } else {
                // increasecoinFromTransfer amount
                coinFrom.setAmount(coinFrom.getAmount().add(value));
                // mergecoinTo
                this.mergeCoinTo(mergeCoinToMap, coinData, to, value, assetChainId, assetId, lockedTime, blockTime);
            }
        }
        // The final merger transaction is updated after the traversal is completedhashAnd the account'snonce
        this.updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);

        List<ContractMergedTransfer> mergerdTransferList = this.contractTransfer2mergedTransfer(tx, contractTransferList);
        List<ContractMultyAssetMergedTransfer> mergerdMultyAssetTransferList = this.contractMultyAssetTransfer2mergedTransfer(tx, contractTransferList);
        contractResult.setMergedTransferList(mergerdTransferList);
        contractResult.setMergerdMultyAssetTransferList(mergerdMultyAssetTransferList);
    }

    private void mergeCoinTo(Map<String, CoinTo> mergeCoinToMap, CoinData coinData, byte[] to, BigInteger value, int assetChainId, int assetId, long lockedTime, long blockTime) {
        CoinTo coinTo;
        String key = addressLockedKey(to, assetChainId, assetId, lockedTime);
        if ((coinTo = mergeCoinToMap.get(key)) != null) {
            coinTo.setAmount(coinTo.getAmount().add(value));
        } else {
            coinTo = new CoinTo(to, assetChainId, assetId, value, lockedTime == 0 ? lockedTime : (blockTime + lockedTime));
            coinData.getTo().add(coinTo);
            mergeCoinToMap.put(key, coinTo);
        }
    }

    public List<ContractMultyAssetMergedTransfer> contractMultyAssetTransfer2mergedTransfer(Transaction tx, List<ContractTransferTransaction> transferList) throws NulsException {
        List<ContractMultyAssetMergedTransfer> resultList = new ArrayList<>();
        for (ContractTransferTransaction transfer : transferList) {
            CoinData coinData = transfer.getCoinDataObj();
            CoinFrom coinFrom = coinData.getFrom().get(0);
            int assetChainId = coinFrom.getAssetsChainId();
            int assetId = coinFrom.getAssetsId();
            if (LOCAL_CHAIN_ID != assetChainId || LOCAL_MAIN_ASSET_ID != assetId) {
                resultList.add(this.transformMultyAssetMergedTransfer(tx.getHash(), transfer));
            }
        }
        return resultList;
    }

    public List<ContractMergedTransfer> contractTransfer2mergedTransfer(Transaction tx, List<ContractTransferTransaction> transferList) throws NulsException {
        List<ContractMergedTransfer> resultList = new ArrayList<>();
        for (ContractTransferTransaction transfer : transferList) {
            CoinData coinData = transfer.getCoinDataObj();
            CoinFrom coinFrom = coinData.getFrom().get(0);
            int assetChainId = coinFrom.getAssetsChainId();
            int assetId = coinFrom.getAssetsId();
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                resultList.add(this.transformMergedTransfer(tx.getHash(), transfer));
            }
        }
        return resultList;
    }

    private ContractMultyAssetMergedTransfer transformMultyAssetMergedTransfer(NulsHash orginHash, ContractTransferTransaction transfer) throws NulsException {
        ContractMultyAssetMergedTransfer result = new ContractMultyAssetMergedTransfer();
        CoinData coinData = transfer.getCoinDataObj();
        CoinFrom coinFrom = coinData.getFrom().get(0);
        int assetChainId = coinFrom.getAssetsChainId();
        int assetId = coinFrom.getAssetsId();
        result.setFrom(coinFrom.getAddress());
        result.setAssetChainId(assetChainId);
        result.setAssetId(assetId);
        result.setValue(coinFrom.getAmount());
        List<CoinTo> toList = coinData.getTo();
        List<MultyAssetOutput> outputs = result.getOutputs();
        MultyAssetOutput output;
        for (CoinTo to : toList) {
            output = new MultyAssetOutput();
            output.setTo(to.getAddress());
            output.setValue(to.getAmount());
            output.setAssetChainId(to.getAssetsChainId());
            output.setAssetId(to.getAssetsId());
            output.setLockTime(to.getLockTime());
            outputs.add(output);
        }
        result.setHash(transfer.getHash());
        result.setOrginHash(orginHash);
        return result;
    }

    private ContractMergedTransfer transformMergedTransfer(NulsHash orginHash, ContractTransferTransaction transfer) throws NulsException {
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
            output.setLockTime(to.getLockTime());
            outputs.add(output);
        }
        result.setHash(transfer.getHash());
        result.setOrginHash(orginHash);
        return result;
    }

    private void updatePreTxHashAndAccountNonce(ContractTransferTransaction tx, ContractBalance balance) throws IOException {
        tx.serializeData();
        NulsHash hash = NulsHash.calcHash(tx.serializeForHash());
        byte[] hashBytes = hash.getBytes();
        byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
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
