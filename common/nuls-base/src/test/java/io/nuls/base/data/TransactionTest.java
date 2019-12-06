package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
public class TransactionTest {

    public static void main(String[] args) throws NulsException {
        String txHex = "0b000823c25d00b204746573740131035858587027000001030000000300055ba2bb3832055ba2bb3832055ba2bb38324200c80064000100035858580358585800205fa01200000000000000000000000000000000000000000000000000000000c817a80400000000000000000000000000000000000000000000000000000000a0724e180900000000000000000000000000000000000000000000000000000800170200019a7c5f842094a73eb489d5678e446e3be57d689afd150101170200019a7c5f842094a73eb489d5678e446e3be57d689a02000100a03e66d94500000000000000000000000000000000000000000000000000000008b03a827828b74ace00031702000199092280b81a34b28901654601bbaa764ea0b385020001000040be40250000000000000000000000000000000000000000000000000000000000000000000000170200019a7c5f842094a73eb489d5678e446e3be57d689a0200010000205fa012000000000000000000000000000000000000000000000000000000ffffffffffffffffff1702000129cfc6376255a78451eeb4b129ed8eacffa2feef02000100005847f80d00000000000000000000000000000000000000000000000000000000000000000000006a2102ff3511b8aabd6f598bda6b344a231a2bda8ffd236c13ef746342b0554a9839b747304502202fe4aa5cb760a873bc3ea6e80d778de97db35cfff4fe607454d057f45fd8c5f1022100f922dc3bf369a4f0820e122a79b15047289fa4100fefa8fe76522392810e28d1";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex),0);
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(),0);
        System.out.println(coinData);
    }


    private CoinFrom getCoinFrom() throws Exception{
        CoinFrom coinFrom =  new CoinFrom();

        coinFrom.setAddress(AddressTool.getAddress("WSqyJxB1B83MJaAGYoJDnfqZxNc7o3930"));
        coinFrom.setAmount(new BigInteger("2678"));
        coinFrom.setAssetsChainId(1);
        coinFrom.setAssetsId(2);
        coinFrom.setLocked((byte)0);
        byte[] nonce = new byte[8];
        coinFrom.setNonce(nonce);
        System.out.println(JSONUtils.obj2json(coinFrom));
        return coinFrom;
    }

    @Test
    public void validCoinFrom() throws Exception{
        CoinFrom coinFrom = getCoinFrom();
        CoinFrom testCF = new CoinFrom();
        testCF.parse(new NulsByteBuffer(coinFrom.serialize()));
        Assert.assertTrue(Arrays.equals(coinFrom.getAddress(), testCF.getAddress()));
        Assert.assertTrue(Arrays.equals(coinFrom.getNonce(), testCF.getNonce()));
        Assert.assertEquals(coinFrom.getAmount().longValue(), testCF.getAmount().longValue());
        System.out.println(JSONUtils.obj2json(testCF));

    }

    private CoinTo getCoinTo() throws Exception{
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress("WSqyJxB1B83MJaAGYoJDnfqZxNc7o3930"));
        coinTo.setAmount(new BigInteger("999"));
        coinTo.setAssetsChainId(1);
        coinTo.setAssetsId(2);
        coinTo.setLockTime(System.currentTimeMillis());
        return coinTo;
    }

    private CoinData getCoinData() throws Exception{
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = getCoinFrom();
        coinData.addFrom(coinFrom);
        CoinTo coinTo = getCoinTo();
        coinData.addTo(coinTo);
        return coinData;
    }

    @Test
    public void serialization() throws Exception{
        Transaction tx = new Transaction();
        tx.setType(10);
        tx.setTime(System.currentTimeMillis()/1000);
        tx.setBlockHeight(100);
        String remark = "试一试";
        tx.setRemark(StringUtils.bytes(remark));
        CoinData coinData = getCoinData();

        try {
            tx.setCoinData(coinData.serialize());
            //String hex = HexUtil.encode(tx.serialize());
            String hex = HexUtil.encode(tx.serialize());
            System.out.println(hex);
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            Assert.assertTrue(Arrays.equals(tx.getCoinData(), transaction.getCoinData()));

           /* CoinData cd = new CoinData();
            cd.parse(new NulsByteBuffer(transaction.getCoinData()));

            CoinFrom cf= cd.getFrom().get(0);
            System.out.println(JSONUtils.obj2json(cf));*/
            System.out.println(JSONUtils.obj2json(transaction));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
