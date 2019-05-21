package io.nuls.contract.callable;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractMergedTransfer;
import io.nuls.contract.model.bo.Output;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteArrayWrapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.asString;

public class ContractMergeContractTransferTest {

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
    }

    static String[] contractAddressSeeds = {"5MR_3PyGq4CgbfoH7sCZ5PoAXPCrzKokBTF", "5MR_3QANzLfMffP22BbyaBnQddkJQArJtJ7", "5MR_3Q7e7a57oWhKLkgjRWYhJCyHbLS8ik9", "5MR_3QAgjbDEjZyzvAoH5NK642m9CZx6NGy", "5MR_3QAbdkWLSbQi5xthL8WtyFAvd4cKeea", "5MR_3Q3E2ByDXf5cwX1abCi4Xb39Kyq6R7q", "5MR_3QDNQCfjP6qM9ZGZDnVy2NzudSWLAW6", "5MR_3Q9S65XtxXeJUmEiPENSSdXLaZSNpbS", "5MR_3Q2wnpwjHKaG9M9rGfyk2YrwThMb57R", "5MR_3QBpHo2nLXMXfysmswjCkfb8PJFVRT8", "5MR_3Q1qQYgNw2URT6Q1G3HMAuxtsWdLrw1", "5MR_3Q79uhPjevBdVMxazc9ncXHXkjiSC3Q", "5MR_3Py5YMtECe4sKuEQrfpEcedVmUXTaBD", "5MR_3Q4T7MqP8uEPKcuNckurf7KCA7yK78V", "5MR_3QD53q4YiszcZqABwoGSuz339MdRsKX", "5MR_3Q2LBUdaZLDFZAHEbtxoC9razeUFa3T", "5MR_3QDNNscDF6RWvkgTrgqMBSZZoz2RGCh", "5MR_3Q6rhNo7FmG94b6dvwDu9SAxQsCj2TX", "5MR_3QDkFRo3tkGagATk8NKkQfeB3NgVoN7", "5MR_3QD9vHcAxDETYevRiSWLPUnYPDWGoKY", "5MR_3QE6m4naDtbQdmJLvf6Vv4cH2q2Zn7V", "5MR_3Q4LrgUoSveo3VShU5iLCJinTdhdc2w", "5MR_3Q5Q9bUQMNh46wrd3WjCtFjy8ub8f31", "5MR_3Q5SzKAqZfMuQH8itmrPayKjMMpoj2a", "5MR_3PzxjVLqrDAfgxV9dp6j8ZfeccN4ugB", "5MR_3QBoJr2Up7xtEv584W8qVo3G5YLREyG", "5MR_3Q8qdssMypCBTxDMt2h4S7x5xmRhgvL", "5MR_3PzCafNm6w8GoEd3tF5KtuNRYQENEVf", "5MR_3QBoW2rFRGztpn3ZYhwaPLVB5tvSG5N", "5MR_3PyvLiZwRKxbE5oPjwFuCGobXkUq5Jy", "5MR_3PymLmXrzSRmsopyjUQSjPT8ddBX5pc", "5MR_3Q33fMwJeCXq2PfuYapjQbAz3cDiJGr", "5MR_3Q5Kq6i8rfubjoktiQCKp7pUB4SzrD4", "5MR_3Q2yPBHjCEy3ukix93AVmYNpWnLjkZA", "5MR_3PyaMBWWj2EvAreGnLZXM6Fkfu986NR", "5MR_3PyBHEuQKeAs4bJpajzSXz6k9VNYP2w", "5MR_3QCRpbhmx9Rrw6WVrjC7ifbtqkUNQx4", "5MR_3QAtLPVLPeqdmYSbwGwFi1HAhnnDKbH", "5MR_3Q1UA633p7n8eo8VTjaopSc5zfPrpbP", "5MR_3Q39PbCdFwcrP5Wg27MbZKB4JFXnpYK", "5MR_3Q643rgT962iQMoXrn6Tw9B7anLMm5z", "5MR_3QBaFvnHN9FtGDekFeWNgG7W9GnphJt", "5MR_3QBGBwPecpjz5x55SXXCTzND9kUSeM6", "5MR_3Q5u1tiyBNXcR8mCFy7M6Nf3KmkGBMq", "5MR_3Q4EG6vgda7w1UNk8XC1VsNk87JL9Tm", "5MR_3Q4nEPa8RQCToidsJH9M2vTektoacSu", "5MR_3QBeEUcF7uNhC4CerF2EpXbAw4Uym6s", "5MR_3QBcfXELZibdvemKdPLdFxFBLxQmbC2", "5MR_3Q2w3eD877yKWp4g9YX7zKhKJSsLE7e", "5MR_3PyVCTAWc35RMqf7j7yBUVsAUdfYUC2"};
    static String[] senderSeeds = {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW", "5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA", "5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw", "5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk", "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM", "5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r", "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF"};
    static String[] methodNameSeeds = {"transfer", "approve", "increaseApproval", "decreaseApproval"};
    static String[][] methodArgsSeeds = {{"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"}, {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"}, {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"}, {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"}};

    private ContractData makeCallContractData(String sender, String contractAddress, long value, String methodName, Object[] args) {
        CallContractData txData = new CallContractData();
        txData.setSender(AddressTool.getAddress(sender));
        txData.setContractAddress(AddressTool.getAddress(contractAddress));
        txData.setValue(BigInteger.valueOf(value));
        txData.setGasLimit(200000L);
        txData.setPrice(25L);
        txData.setMethodName(methodName);
        String[][] args2 = ContractUtil.twoDimensionalArray(args);
        txData.setArgsCount((byte) args.length);
        txData.setArgs(args2);
        return txData;
    }


    @Test
    public void mergeContractTransferTest() throws IOException, NulsException {
        List<ProgramTransfer> transfers = new ArrayList<>();
        transfers.add(new ProgramTransfer(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3}, new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 2, 3, 4}, BigInteger.ONE));
        transfers.add(new ProgramTransfer(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3}, new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 2, 3, 4}, BigInteger.TWO));
        transfers.add(new ProgramTransfer(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 3, 4, 5}, new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 4, 5, 6}, BigInteger.TEN));
        transfers.add(new ProgramTransfer(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3}, new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 4, 5, 6}, BigInteger.TWO));
        transfers.add(new ProgramTransfer(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3}, new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 5, 3, 4}, BigInteger.ONE));
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
        txData.setContractAddress(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3});
        NulsDigestData orginHash = NulsDigestData.calcDigestData(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8});
        txData.setOrginTxHash(orginHash);

        List<ContractTransferTransaction> contractTransferList = new ArrayList<>();
        Map<String, CoinTo> mergeCoinToMap = MapUtil.createHashMap(transfers.size());
        Map<String, ContractBalance> tempBalanceManager = MapUtil.createHashMap(8);
        ContractBalance balance = ContractBalance.newInstance();
        balance.setNonce(HexUtil.encode(new byte[]{1, 2, 3, 1, 2, 3, 1, 2}));
        tempBalanceManager.put(asString(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 1, 2, 3}), balance);
        balance = ContractBalance.newInstance();
        balance.setNonce(HexUtil.encode(new byte[]{8, 2, 3, 8, 2, 3, 8, 2}));
        tempBalanceManager.put(asString(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 8, 8, 3, 4, 5}), balance);

        for (ProgramTransfer transfer : transfers) {
            byte[] from = transfer.getFrom();
            byte[] to = transfer.getTo();
            BigInteger value = transfer.getValue();
            ByteArrayWrapper wrapperFrom = new ByteArrayWrapper(from);
            if (compareFrom == null || !compareFrom.equals(wrapperFrom)) {
                // 产生新交易
                if (compareFrom == null) {
                    // 第一次遍历，获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.get(asString(from));
                    nonceBytes = HexUtil.decode(contractBalance.getNonce());
                } else {
                    // 产生另一个合并交易，更新之前的合并交易的hash和账户的nonce
                    updatePreTxHashAndAccountNonce(contractTransferTx, contractBalance);
                    mergeCoinToMap.clear();
                    // 获取新交易的coinFrom的nonce
                    contractBalance = tempBalanceManager.get(asString(from));
                    nonceBytes = HexUtil.decode(contractBalance.getNonce());
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
        byte[] compareNonceBytes = HexUtil.decode(nonce);
        Assert.assertTrue(Arrays.equals(end8, compareNonceBytes));
    }

    private List<ContractMergedTransfer> contractTransfer2mergedTransfer(NulsDigestData hash, List<ContractTransferTransaction> transferList) throws NulsException {
        List<ContractMergedTransfer> resultList = new ArrayList<>();
        for (ContractTransferTransaction transfer : transferList) {
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
        NulsDigestData hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        byte[] hashBytes = hash.serialize();
        byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
        balance.setNonce(HexUtil.encode(currentNonceBytes));
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
        if ((coinTo = mergeCoinToMap.get(key)) != null) {
            coinTo.setAmount(coinTo.getAmount().add(value));
        } else {
            coinTo = new CoinTo(to, 1, assetsId, value, 0L);
            coinData.getTo().add(coinTo);
            mergeCoinToMap.put(key, coinTo);
        }
    }

}