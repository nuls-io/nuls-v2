/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.message;

import com.google.common.collect.Lists;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.block.model.GenesisBlock;
import io.nuls.block.test.BlockGenerator;
import io.nuls.core.crypto.HexUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 消息测试类,测试各种类型的消息序列化与反序列化
 * @author captain
 * @date 18-11-16 下午12:17
 * @version 1.0
 */
public class MessageTest {

    @Test
    public void testEquals() throws Exception {
        HashMessage m1 = new HashMessage();
        {
            m1.setRequestHash(NulsHash.fromHex("00202385c6ea81795592278265e3d42d4c454dcf05fe368e8ba9d6799dc43695f3e6"));
            String hex = HexUtil.encode(m1.serialize());
            System.out.println(hex);
        }

        HashMessage m2 = new HashMessage();
        {
            m2.setRequestHash(NulsHash.fromHex("00202385c6ea81795592278265e3d42d4c454dcf05fe368e8ba9d6799dc43695f3e6"));
            String hex = HexUtil.encode(m2.serialize());
            System.out.println(hex);
        }

        assertEquals(m1, m2);
    }

    @Test
    public void testBlockMessage() throws Exception {
        BlockMessage message = new BlockMessage();

        message.setBlock(GenesisBlock.getInstance(0, 0));
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        BlockMessage message1 = new BlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testBlockMessage1() throws Exception {
        BlockMessage message = new BlockMessage();

        message.setBlock(null);
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        BlockMessage message1 = new BlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
        assertNull(message1.getBlock());
    }

    @Test
    public void testHashMessage() throws Exception {
        HashMessage message = new HashMessage();
        message.setRequestHash(NulsHash.fromHex("0020e643ab908b37ce52b4cdaeb3219846162235b466cb78491832d766ba0a3a5e98"));
        
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        HashMessage message1 = new HashMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testCompleteMessage() throws Exception {
        CompleteMessage message = new CompleteMessage();
        CompleteMessage body = new CompleteMessage();
        
        body.setRequestHash(NulsHash.calcHash("hello".getBytes()));
        body.setSuccess(true);
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        CompleteMessage message1 = new CompleteMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testGetBlocksByHeightMessage() throws Exception {
        HeightRangeMessage message = new HeightRangeMessage();

        message.setStartHeight(111);
        message.setEndHeight(222);
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        HeightRangeMessage message1 = new HeightRangeMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testHashListMessage() throws Exception {
        HashListMessage message = new HashListMessage();
        message.setBlockHash(NulsHash.calcHash("00205f3e3489f3625304a0080ab4a0f5b46d15b68f9bb2bc24b54a6128f59b84b774".getBytes()));
        message.setTxHashList(Lists.newArrayList(NulsHash.calcHash("00204fd8f7316eb324643197dcc466531556231237adf36a2b3098a7b43526e34261".getBytes())));
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        HashListMessage message1 = new HashListMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testSmallBlockMessage() throws Exception {
        SmallBlockMessage message = new SmallBlockMessage();
        Block block = BlockGenerator.generate(null);
        Transaction transaction = buildTransaction("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        smallBlock.addSystemTx(transaction);
        smallBlock.setTxHashList(Lists.newArrayList(transaction.getHash()));
        message.setSmallBlock(smallBlock);
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        SmallBlockMessage message1 = new SmallBlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Test
    public void testTxGroupMessage() throws Exception {
        TxGroupMessage message = new TxGroupMessage();
        message.setBlockHash(NulsHash.calcHash("00205f3e3489f3625304a0080ab4a0f5b46d15b68f9bb2bc24b54a6128f59b84b774".getBytes()));
        Transaction t1 = buildTransaction("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        Transaction t2 = buildTransaction("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(t1);
        transactions.add(t2);
        message.setTransactions(transactions);
        String hex = HexUtil.encode(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        TxGroupMessage message1 = new TxGroupMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getMsgHash(), message.getMsgHash());
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    /**
     * 铸币交易
     *
     * @return
     * @throws IOException
     */
    private static Transaction buildTransaction(String address) throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.TWO);
        coinTo.setAssetsChainId(2);
        coinTo.setAssetsId(1);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        return tx;
    }
}