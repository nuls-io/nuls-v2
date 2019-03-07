package io.nuls.contract.callable;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractMergedTransfer;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.bo.Output;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.tools.data.ByteArrayWrapper;
import io.nuls.tools.exception.NulsException;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.asString;

public class ContractTxCallableTest {

    @Test
    public void mergeContractTransferTest() throws IOException, NulsException {
        List<ProgramTransfer> transfers = new ArrayList<>();
        transfers.add(new ProgramTransfer(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3}, new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,2,3,4}, BigInteger.ONE));
        transfers.add(new ProgramTransfer(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3}, new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,2,3,4}, BigInteger.TWO));
        transfers.add(new ProgramTransfer(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,3,4,5}, new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,4,5,6}, BigInteger.TEN));
        transfers.add(new ProgramTransfer(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3}, new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,4,5,6}, BigInteger.TWO));
        transfers.add(new ProgramTransfer(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3}, new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,5,3,4}, BigInteger.ONE));
        CoinData coinData = null;
        CoinFrom coinFrom = null;
        CoinTo coinTo = null;
        ByteArrayWrapper compareFrom = null;
        byte[] nonceBytes;
        ContractTransferTransaction contractTransferTx = null;
        ContractBalance contractBalance = null;
        int chainId = 1;
        int assetsId = 1;
        ContractTransferData txData = new ContractTransferData();
        txData.setContractAddress(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3});
        NulsDigestData orginHash = NulsDigestData.calcDigestData(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8});
        txData.setOrginTxHash(orginHash);

        List<ContractTransferTransaction> contractTransferList = new ArrayList<>();
        Map<String, CoinTo> mergeCoinToMap = MapUtil.createHashMap(transfers.size());
        Map<String, ContractBalance> tempBalanceManager = MapUtil.createHashMap(8);
        ContractBalance balance = ContractBalance.newInstance();
        balance.setNonce(Hex.toHexString(new byte[]{1,2,3,1,2,3,1,2}));
        tempBalanceManager.put(asString(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,1,2,3}), balance);
        balance = ContractBalance.newInstance();
        balance.setNonce(Hex.toHexString(new byte[]{8,2,3,8,2,3,8,2}));
        tempBalanceManager.put(asString(new byte[]{1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,8,8,3,4,5}), balance);

        for (ProgramTransfer transfer : transfers) {
            byte[] from = transfer.getFrom();
            byte[] to = transfer.getTo();
            BigInteger value = transfer.getValue();
            ByteArrayWrapper wrapperFrom = new ByteArrayWrapper(from);
            if(compareFrom == null || !compareFrom.equals(wrapperFrom)) {
                // 产生新交易
                if(compareFrom == null) {
                    // 第一次遍历，获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.get(asString(from));
                    nonceBytes = Hex.decode(contractBalance.getNonce());
                } else {
                    // 产生另一个合并交易，更新之前的合并交易的hash和账户的nonce
                    updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);
                    mergeCoinToMap.clear();
                    // 获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.get(asString(from));
                    nonceBytes = Hex.decode(contractBalance.getNonce());
                }
                compareFrom = wrapperFrom;
                coinData = new CoinData();
                coinFrom = new CoinFrom(from, chainId, assetsId, value, nonceBytes, (byte) 0);
                coinData.getFrom().add(coinFrom);
                coinTo = new CoinTo(to, chainId, assetsId, value, 0L);
                coinData.getTo().add(coinTo);
                mergeCoinToMap.put(asString(to), coinTo);
                contractTransferTx = createContractTransferTx(coinData, txData);
                contractTransferList.add(contractTransferTx);
            } else {
                // 增加coinFrom的转账金额
                coinFrom.setAmount(coinFrom.getAmount().add(value));
                // 合并coinTo
                mergeCoinTo(mergeCoinToMap, coinData, to, assetsId, value);
            }
        }

        // 最后产生的合并交易，遍历结束后更新它的hash和账户的nonce
        this.updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);

        List<ContractMergedTransfer> mergerdTransferList = this.contractTransfer2mergedTransfer(orginHash, contractTransferList);

        System.out.println(mergerdTransferList.toString());
        System.out.println(contractTransferList.toString());
        byte[] serialize = contractTransferList.get(2).getHash().serialize();
        byte[] end8 = Arrays.copyOfRange(serialize, serialize.length - 8, serialize.length);
        String nonce = tempBalanceManager.get(asString(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3})).getNonce();
        byte[] compareNonceBytes = Hex.decode(nonce);
        Assert.assertTrue(Arrays.equals(end8, compareNonceBytes));
    }

    private List<ContractMergedTransfer> contractTransfer2mergedTransfer(NulsDigestData hash, List<ContractTransferTransaction> transferList) throws NulsException {
        List<ContractMergedTransfer> resultList = new ArrayList<>();
        for(ContractTransferTransaction transfer : transferList) {
            resultList.add(this.transformMergedTransfer(hash, transfer));
        }
        return resultList;
    }

    private ContractMergedTransfer transformMergedTransfer(NulsDigestData orginHash, ContractTransferTransaction transfer) throws NulsException {
        ContractMergedTransfer result = new ContractMergedTransfer();
        CoinData coinData = transfer.getCoinDataObj();
        CoinFrom coinFrom = coinData.getFrom().get(0);
        result.setFrom(coinFrom.getAddress());
        result.setValue(coinFrom.getAmount());
        List<CoinTo> toList = coinData.getTo();
        List<Output> outputs = result.getOutputs();
        Output output;
        for(CoinTo to : toList) {
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
        NulsDigestData hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        byte[] hashBytes = hash.serialize();
        byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
        balance.setNonce(Hex.toHexString(currentNonceBytes));
        tx.setHash(hash);
    }

    private ContractTransferTransaction createContractTransferTx(CoinData coinData, ContractTransferData txData) {
        long blockTime = 12235346436L;
        ContractTransferTransaction contractTransferTx = new ContractTransferTransaction();
        contractTransferTx.setCoinDataObj(coinData);
        contractTransferTx.setTxDataObj(txData);
        contractTransferTx.setTime(blockTime);
        return contractTransferTx;
    }

    private void mergeCoinTo(Map<String, CoinTo> mergeCoinToMap, CoinData coinData, byte[] to, int assetsId, BigInteger value) {
        CoinTo coinTo;
        String key = asString(to);
        if((coinTo = mergeCoinToMap.get(key)) != null) {
            coinTo.setAmount(coinTo.getAmount().add(value));
        } else {
            coinTo = new CoinTo(to, 1, assetsId, value, 0L);
            coinData.getTo().add(coinTo);
            mergeCoinToMap.put(key, coinTo);
        }
    }

}