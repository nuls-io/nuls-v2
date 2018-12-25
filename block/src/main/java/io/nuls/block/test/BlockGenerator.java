/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.test;

import io.nuls.base.data.*;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.model.GenesisBlock;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import lombok.Data;
import org.apache.http.util.Asserts;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.nuls.block.constant.Constant.CHAIN_ID;

/**
 * 区块生成器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-13 下午5:11
 */
@Data
public final class BlockGenerator extends Thread {

    private static final String PRIVATE_KEY = "009cf05b6b3fe8c09b84c13783140c0f1958e8841f8b6f894ef69431522bc65712";
    private static final String ADDRESS = "TTaoMpuLtP4NmiVZWAQpngopcvoDNULS9861";
    private static final String REMARK = "4f70656e2c204c69626572616c2c204175746f6e6f6d6f75732c2053656c662d45766f6c76696e670ae5bc80e694beefbc8ce887aae794b1efbc8ce887aae6b2bbefbc8ce8bf9be58c960a4f75766572742c204c696272652c204175746f6e6f6d652c20c389766f6c757469660ae382aae383bce38397e383b3e38081e38395e383aae383bce38081e887aae6b2bbe38081e980b2e58c960ad09ed182d0bad180d18bd182d18bd0b92c20d0a1d0b2d0bed0b1d0bed0b4d0bdd0b0d18f2c20d090d0b2d182d0bed0bdd0bed0bcd0bdd0b0d18f2c20d18dd0b2d0bed0bbd18ed186d0b8d18f0aeab09cebb0a9eca0812020eca784ebb3b4eca08120ec9e90ec9ca8eca08120ed9881ebaa85eca0810a4162696572746f2c204c696272652c20417574c3b36e6f6d6f2c2045766f6c757469766f0ad981d8aad8ad20d88c20d8add8b120d88c20d985d8b3d8aad982d98420d88c20d8aad8b7d988d8b10a4f6666656e2c20667265692c206175746f6e6f6d2c2045766f6c7574696f6e0a45766f6c75c3a7c3a36f206162657274612c206c69767265206520617574c3b36e6f6d610ac39670706e612c20667269612c206175746f6e6f6d612c2065766f6c7574696f6e0ace91cebdcebfceb9cebacf84ceae2c20ceb5cebbceb5cf8dceb8ceb5cf81ceb72c20ceb1cf85cf84cf8ccebdcebfcebcceb72c20ceb5cebeceadcebbceb9cebeceb70a41c3a7c4b16b2c20c3b67a67c3bc722c20c3b67a65726b2c20657672696d0a4f736361696c2c2073616f7220696e2061697363652c206e65616d6873706c65c3a163682c20c3a96162686cc3b36964";
    private static final long BLOCK_TIME = 1531152000000L;
    private static final long TOTAL = Long.MAX_VALUE;
    private static final int TX_COUNT = 222;

    /**
     * 生成一个区块
     *
     * @param latestBlock
     * @return
     * @throws Exception
     */
    public static Block generate(Block latestBlock) throws Exception {
        return generate(latestBlock, 1, "1");
    }

    /**
     * 生成一个区块
     *
     * @param latestBlock
     * @return
     * @throws Exception
     */
    public static Block generate(Block latestBlock, long seed, String symbol) throws Exception {
        if (latestBlock == null) {
            return GenesisBlock.getInstance();
        }
        Block block = new Block();
        block.setTxs(getTransactions(TX_COUNT, seed, symbol));
        fillHeader(block, latestBlock.getHeader().getHash(), latestBlock.getHeader().getHeight() + 1);
        return block;
    }

    private static void fillHeader(Block block, NulsDigestData previousHash, long height) {
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        header.setHeight(height);
        header.setTime(BLOCK_TIME);
        header.setPreHash(previousHash);
        header.setTxCount(block.getTxs().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setHash(NulsDigestData.calcDigestData(header));

        BlockSignature p2PKHScriptSig = new BlockSignature();
        NulsSignData signData = signature(header.getHash().getDigestBytes());
        p2PKHScriptSig.setSignData(signData);
        p2PKHScriptSig.setPublicKey(getGenesisPubkey());
        header.setBlockSignature(p2PKHScriptSig);
    }

    private static NulsSignData signature(byte[] bytes) {
        return SignatureUtil.signDigest(bytes, ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(PRIVATE_KEY))));
    }

    private static byte[] getGenesisPubkey() {
        return ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(PRIVATE_KEY))).getPubKey();
    }

    public static List<Transaction> getTransactions(int size) throws IOException {
        return getTransactions(size, 1, "1");
    }

    public static List<Transaction> getTransactions(int size, long seed, String symbol) throws IOException {
        List<Transaction> txlist = new ArrayList<>();
        CoinData coinData = new CoinData();
        String address = ADDRESS;
        Asserts.notEmpty(address, BlockErrorCode.DATA_ERROR.getMsg());
        String amount = "10000000" + seed + symbol;
        Address ads = Address.fromHashs(address);
        CoinTo coin = new CoinTo();
        coin.setAddress(ads.getAddressBytes());
        coin.setAmount(new BigInteger(amount));
        coin.setAssetsChainId(CHAIN_ID);
        coin.setAssetsId(1);
        coin.setLockTime(0);
        coinData.addTo(coin);
        byte[] bytes = coinData.serialize();
        byte[] remark = HexUtil.decode(REMARK);
        for (int i = 0; i < size; i++) {
            Transaction tx = new Transaction();
            tx.setTime(BLOCK_TIME);
            tx.setCoinData(bytes);
            tx.setRemark(remark);
            tx.setType(i + 1);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            txlist.add(tx);
        }
        return txlist;
    }
}

