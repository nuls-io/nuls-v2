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
package io.nuls.contract.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ApiModelProperty;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-07-15
 */
public class TxSerialization {

    @Test
    public void testType1() throws NulsException, IOException {

        Transaction tx = new Transaction();
        tx.setType(1);
        tx.setTime(System.currentTimeMillis() / 1000);
        tx.setRemark("fake test".getBytes(StandardCharsets.UTF_8));
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo();
        to.setAddress(AddressTool.getAddress("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
        to.setAssetsChainId(2);
        to.setAssetsId(1);
        to.setLockTime(0L);
        to.setAmount(BigInteger.valueOf(9999999999L));
        coinData.getTo().add(to);
        tx.setCoinData(coinData.serialize());
        System.out.println(HexUtil.encode(tx.serialize()));
    }

    @Test
    public void testType18() throws NulsException, IOException {

        Transaction tx = new Transaction();
        tx.setType(18);
        tx.setTime(System.currentTimeMillis() / 1000);
        tx.setRemark("fake test contract transfer".getBytes(StandardCharsets.UTF_8));
        CoinData coinData = new CoinData();
        CoinFrom from = new CoinFrom();
        from.setAddress(AddressTool.getAddress("NULSd6HgpYjcMB5y6dy2dQzhJ7sGjUtdXWPdL"));
        from.setAmount(BigInteger.valueOf(1000000000L));
        from.setNonce(HexUtil.decode("0000000000000000"));
        from.setLocked((byte) 0);
        from.setAssetsChainId(1);
        from.setAssetsId(1);
        coinData.getFrom().add(from);

        CoinTo to = new CoinTo();
        to.setAddress(AddressTool.getAddress("NULSd6HgbjYg869gRWDLnHELGW281LkebDPbL"));
        to.setAssetsChainId(1);
        to.setAssetsId(1);
        to.setLockTime(0L);
        to.setAmount(BigInteger.valueOf(1000000000L));
        coinData.getTo().add(to);
        tx.setCoinData(coinData.serialize());
        System.out.println(HexUtil.encode(tx.serialize()));
    }


}
