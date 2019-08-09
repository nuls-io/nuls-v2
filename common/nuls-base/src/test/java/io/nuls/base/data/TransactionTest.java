package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.crypto.HexUtil;
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
