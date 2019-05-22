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

package io.nuls.block.model;

import io.nuls.base.data.*;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.io.IoUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 创世块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-13 下午5:11
 */
public final class GenesisBlock extends Block {

    private static final String GENESIS_BLOCK_FILE = "genesis-block.json";
    private static final String CONFIG_FILED_TIME = "time";
    private static final String CONFIG_FILED_HEIGHT = "height";
    private static final String CONFIG_FILED_EXTEND = "extend";
    private static final String CONFIG_FILED_TXS = "txs";
    private static final String CONFIG_FILED_ADDRESS = "address";
    private static final String CONFIG_FILED_AMOUNT = "amount";
    private static final String CONFIG_FILED_LOCK_TIME = "lockTime";
    private static final String CONFIG_FILED_REMARK = "remark";
    private static final String CONFIG_FILED_PRIVATE_KEY = "privateKey";

    private transient long blockTime;
    private int chainId;
    private int assetsId;
    private BigInteger priKey;

    private GenesisBlock(int chainId, int assetsId, String json) throws IOException {
        Map<String, Object> jsonMap;
        jsonMap = JSONUtils.json2map(json);
        String time = (String) jsonMap.get(CONFIG_FILED_TIME);
        blockTime = Long.parseLong(time);
        this.chainId = chainId;
        this.assetsId = assetsId;
        this.initGengsisTxs(jsonMap);
        this.fillHeader(jsonMap);
    }

    public static GenesisBlock getInstance(int chainId, int assetsId, String json) throws IOException {
        return new GenesisBlock(chainId, assetsId, json);
    }

    public static GenesisBlock getInstance(int chainId, int assetsId) throws Exception {
        String json = IoUtils.read(GENESIS_BLOCK_FILE);
        return new GenesisBlock(chainId, assetsId, json);
    }

    private void initGengsisTxs(Map<String, Object> jsonMap) throws IOException {
        List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get(CONFIG_FILED_TXS);
        if (null == list || list.isEmpty()) {
            throw new NulsRuntimeException(BlockErrorCode.DATA_ERROR);
        }
        CoinData coinData = new CoinData();
        for (Map<String, Object> map : list) {
            String address = (String) map.get(CONFIG_FILED_ADDRESS);
            String amount = map.get(CONFIG_FILED_AMOUNT).toString();
            long lockTime = Long.parseLong("" + map.get(CONFIG_FILED_LOCK_TIME));
            Address ads = Address.fromHashs(address);
            CoinTo coin = new CoinTo();
            coin.setAddress(ads.getAddressBytes());
            coin.setAmount(new BigInteger(amount));
            coin.setAssetsChainId(chainId);
            coin.setAssetsId(assetsId);
            coin.setLockTime(lockTime);
            coinData.addTo(coin);
        }

        Transaction tx = new Transaction();
        tx.setType(1);
        tx.setTime(this.blockTime);
        tx.setCoinData(coinData.serialize());
        String remark = (String) jsonMap.get(CONFIG_FILED_REMARK);
        if (StringUtils.isNotBlank(remark)) {
            tx.setRemark(HexUtil.decode(remark));
        }
        tx.setHash(NulsHash.calcDigestData(tx.serializeForHash()));
        List<Transaction> txlist = new ArrayList<>();
        txlist.add(tx);
        setTxs(txlist);
    }


    private void fillHeader(Map<String, Object> jsonMap) {
        Integer height = (Integer) jsonMap.get(CONFIG_FILED_HEIGHT);
        String extend = (String) jsonMap.get(CONFIG_FILED_EXTEND);
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(height);
        header.setTime(blockTime);
        header.setPreHash(NulsHash.calcDigestData(new byte[35]));
        header.setTxCount(this.getTxs().size());
        List<NulsHash> txHashList = new ArrayList<>();
        for (Transaction tx : this.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsHash.calcMerkleDigestData(txHashList));
        header.setExtend(HexUtil.decode(extend));

        BlockSignature p2PKHScriptSig = new BlockSignature();
        priKey = new BigInteger(1, HexUtil.decode((String) jsonMap.get(CONFIG_FILED_PRIVATE_KEY)));
        NulsSignData signData = this.signature(header.getHash().getDigestBytes());
        p2PKHScriptSig.setSignData(signData);
        p2PKHScriptSig.setPublicKey(getGenesisPubkey());
        header.setBlockSignature(p2PKHScriptSig);
    }

    private NulsSignData signature(byte[] bytes) {
        return SignatureUtil.signDigest(bytes, ECKey.fromPrivate(priKey));
    }

    private byte[] getGenesisPubkey() {
        return ECKey.fromPrivate(priKey).getPubKey();
    }
}

