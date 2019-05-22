/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.test.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.ledger.utils.LoggerUtil;
import org.apache.commons.codec.DecoderException;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/01/14
 **/
public class HexTest {
    public int chainId = 2;
    int assetChainId = 2;
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    int assetId = 1;
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);

    Transaction buildTransaction() throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(amount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        tx.setBlockHeight(0);
        tx.setTime(500000000000000L);
        return tx;
    }
    @Test
    public void testHex() throws IOException {
        TranList list = new TranList();
        for (int i = 0; i < 10000; i++) {
            Transaction tx = buildTransaction();
            list.getTxs().add(tx);
        }
        byte[] bytes = list.serialize();
        long time1 = System.currentTimeMillis();
        String hex0 = HexUtil.encode(bytes);
        long time2 = System.currentTimeMillis();
        System.out.println("HexUtil.byteToHex useTime=" + (time2 - time1) + "==StringLenght=" + hex0.length());
        String hex = HexUtil.encode(bytes);
        long time3 = System.currentTimeMillis();
        System.out.println("HexUtil.encode useTime=" + (time3 - time2) + "===StringLenght=" + hex.length());
        String s = ByteUtils.asString(bytes);
        long time4 = System.currentTimeMillis();
        System.out.println(" ByteUtils.asString useTime=" + (time4 - time3) + "===StringLenght" + s.length());

    }
    @Test
    public void testBatchHex() throws IOException, DecoderException {
        TranList list = new TranList();
        for (int i = 0; i < 10000; i++) {
            Transaction tx = buildTransaction();
            list.getTxs().add(tx);
        }
        byte[] bytes = list.serialize();

        String hex0 = HexUtil.encode(bytes);
        long time1 = System.currentTimeMillis();
        byte[] bytes0 = HexUtil.decode(hex0);
        TranList txList = new TranList();
        try {
            txList.parse(bytes0,0);
        } catch (NulsException e) {
            Log.error(e);
        }
        long time2 = System.currentTimeMillis();
        Log.info("{} batch list time used - io.nuls.tools.crypto.HexUtil.decode", (time2 - time1));
        List<String> list2= new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Transaction tx = buildTransaction();
            list2.add(HexUtil.encode(tx.serialize()));
        }
        long time3 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Transaction tx =new Transaction();
            try {
                tx.parse(HexUtil.decode(list2.get(i)),0);
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        long time4 = System.currentTimeMillis();
        Log.info("{} list<String> time used - io.nuls.tools.crypto.HexUtil.decode ", (time4 - time3));

    }
        @Test
    public void testHexs() throws IOException, DecoderException {
        TranList list = new TranList();
        for (int i = 0; i < 100000; i++) {
            Transaction tx = buildTransaction();
            list.getTxs().add(tx);
        }
        byte[] bytes = list.serialize();
        long time1 = System.currentTimeMillis();
        String hex0 = HexUtil.encode(bytes);
        byte[] bytes0 = HexUtil.decode(hex0);
        long time2 = System.currentTimeMillis();
        Log.info("{} time used - io.nuls.tools.crypto.HexUtil.encode && decode ===StringLenght= {}", (time2 - time1), hex0.length());

        String hex1 = Hex.toHexString(bytes);
        byte[] bytes1 = Hex.decode(hex1);
        long time3 = System.currentTimeMillis();
        Log.info("{} time used - org.spongycastle.util.encoders.Hex.encode && decode ===StringLenght= {}", (time3 - time2), hex1.length());


        String hex2 = org.apache.commons.codec.binary.Hex.encodeHexString(bytes);
        byte[] bytes2 = org.apache.commons.codec.binary.Hex.decodeHex(hex2.toCharArray());
        long time4 = System.currentTimeMillis();
        Log.info("{} time used - org.apache.commons.codec.binary.Hex.encode && decode ===StringLenght= {}", (time4 - time3), hex2.length());

        String base0 = RPCUtil.encode(bytes);
        byte[] bytes3 = RPCUtil.decode(base0);
        long time5 = System.currentTimeMillis();
        Log.info("{} time used - java.util.Base64.encode && decode ===StringLenght= {}", (time5 - time4), base0.length());

        String base1 = Base64.toBase64String(bytes);
        byte[] bytes5 = Base64.decode(base1);
        long time6 = System.currentTimeMillis();
        Log.info("{} time used - org.spongycastle.util.encoders.Base64.encode && decode ===StringLenght= {}", (time6 - time5), base1.length());

        String base2 = org.apache.commons.net.util.Base64.encodeBase64String(bytes);
        byte[] bytes6 = org.apache.commons.net.util.Base64.decodeBase64(base2);
        long time7 = System.currentTimeMillis();
        Log.info("{} time used - org.apache.commons.net.util.Base64.encode && decode ===StringLenght= {}", (time7 - time6), base2.length());

//        String base3 = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
//        byte[] bytes7 = org.apache.commons.codec.binary.Base64.decodeBase64(base3);
//        long time8 = System.currentTimeMillis();
//        Log.info("{} time used - org.apache.commons.codec.binary.Base64.encode && decode ===StringLenght= {}", (time8 - time7), base3.length());

    }

}
