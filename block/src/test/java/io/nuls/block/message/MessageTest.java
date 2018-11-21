/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.BlockGenerator;
import io.nuls.block.config.GenesisBlock;
import io.nuls.block.constant.Constant;
import io.nuls.block.message.body.*;
import io.nuls.block.model.CoinBaseTransaction;
import io.nuls.block.model.SmallBlock;
import io.nuls.tools.crypto.HexUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 消息测试类，测试各种类型的消息序列化与反序列化
 * @author captain
 * @date 18-11-16 下午12:17
 * @version 1.0
 */
public class MessageTest {

    @Test
    public void testEquals() throws Exception {
        GetBlockMessage m1 = new GetBlockMessage();
        {
            GetBlockMessageBody body = new GetBlockMessageBody();
            body.setChainID(Constant.CHAIN_ID);
            body.setBlockHash(NulsDigestData.fromDigestHex("00202385c6ea81795592278265e3d42d4c454dcf05fe368e8ba9d6799dc43695f3e6"));
            m1.getHeader().setMagicNumber(68866996);
            m1.getHeader().setPayloadLength(body.size());
            m1.setMsgBody(body);
            String hex = HexUtil.byteToHex(m1.serialize());
            System.out.println(hex);
        }

        GetBlockMessage m2 = new GetBlockMessage();
        {
            GetBlockMessageBody body = new GetBlockMessageBody();
            body.setChainID(Constant.CHAIN_ID);
            body.setBlockHash(NulsDigestData.fromDigestHex("00202385c6ea81795592278265e3d42d4c454dcf05fe368e8ba9d6799dc43695f3e6"));
            m2.getHeader().setMagicNumber(688669963);
            m2.getHeader().setPayloadLength(body.size());
            m2.setMsgBody(body);
            String hex = HexUtil.byteToHex(m2.serialize());
            System.out.println(hex);
        }

        assertEquals(m1, m2);
    }

    @Test
    public void testBlockMessage() throws Exception {
        BlockMessage message = new BlockMessage();
        BlockMessageBody body = new BlockMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setBlock(GenesisBlock.getInstance());
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        BlockMessage message1 = new BlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testGetBlockMessage() throws Exception {
        GetBlockMessage message = new GetBlockMessage();
        GetBlockMessageBody body = new GetBlockMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setBlockHash(NulsDigestData.fromDigestHex("0020e643ab908b37ce52b4cdaeb3219846162235b466cb78491832d766ba0a3a5e98"));
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        GetBlockMessage message1 = new GetBlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testCompleteMessage() throws Exception {
        CompleteMessage message = new CompleteMessage();
        CompleteMessageBody body = new CompleteMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setRequestHash(NulsDigestData.calcDigestData("hello".getBytes()));
        body.setSuccess(true);
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        CompleteMessage message1 = new CompleteMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testForwardSmallBlockMessage() throws Exception {
        ForwardSmallBlockMessage message = new ForwardSmallBlockMessage();
        ForwardSmallBlockMessageBody body = new ForwardSmallBlockMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setBlockHash(NulsDigestData.calcDigestData("hello".getBytes()));
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        ForwardSmallBlockMessage message1 = new ForwardSmallBlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testGetBlocksByHeightMessage() throws Exception {
        GetBlocksByHeightMessage message = new GetBlocksByHeightMessage();
        GetBlocksByHeightMessageBody body = new GetBlocksByHeightMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setStartHeight(111);
        body.setEndHeight(222);
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        GetBlocksByHeightMessage message1 = new GetBlocksByHeightMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testGetSmallBlockMessage() throws Exception {
        GetSmallBlockMessage message = new GetSmallBlockMessage();
        GetSmallBlockMessageBody body = new GetSmallBlockMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setRequestHash(NulsDigestData.calcDigestData("hello".getBytes()));
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        GetSmallBlockMessage message1 = new GetSmallBlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testGetTxGroupMessage() throws Exception {
        GetTxGroupMessage message = new GetTxGroupMessage();
        GetTxGroupMessageBody body = new GetTxGroupMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setTxHashList(Lists.newArrayList(NulsDigestData.calcDigestData("hello".getBytes())));
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        GetTxGroupMessage message1 = new GetTxGroupMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testSmallBlockMessage() throws Exception {
        SmallBlockMessage message = new SmallBlockMessage();
        SmallBlockMessageBody body = new SmallBlockMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        Block block = BlockGenerator.generate(null);
        Transaction transaction = BlockGenerator.getTransaction();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        smallBlock.addBaseTx(transaction);
        smallBlock.setTxHashList(Lists.newArrayList(transaction.getHash()));
        body.setSmallBlock(smallBlock);
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        SmallBlockMessage message1 = new SmallBlockMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Test
    public void testTxGroupMessage() throws Exception {
        TxGroupMessage message = new TxGroupMessage();
        TxGroupMessageBody body = new TxGroupMessageBody();
        body.setChainID(Constant.CHAIN_ID);
        body.setRequestHash(NulsDigestData.calcDigestData("hello".getBytes()));
        body.setTransactions(BlockGenerator.getTransactions());
        message.setMsgBody(body);
        String hex = HexUtil.byteToHex(message.serialize());
        System.out.println(hex);

        byte[] bytes = HexUtil.decode(hex);
        TxGroupMessage message1 = new TxGroupMessage();
        message1.parse(new NulsByteBuffer(bytes));

        assertEquals(message1.getHash(), message.getHash());
    }

    @Before
    public void setUp() throws Exception {
        TransactionManager.putTx(CoinBaseTransaction.class, null);
    }

    @After
    public void tearDown() throws Exception {
    }
}