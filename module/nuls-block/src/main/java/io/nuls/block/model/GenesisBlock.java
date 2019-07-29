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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.core.basic.VarInt;
import io.nuls.core.constant.ToolsConstant;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.io.IoUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    /**
     * 创世块生成时间,相比1970-01-01 08:00:00过去了多少秒
     */
    private static final String CONFIG_FILED_TIME = "time";
    /**
     * 创世块高度,默认为0
     */
    private static final String CONFIG_FILED_HEIGHT = "height";
    /**
     * 扩展字段,详细注释见${@link BlockExtendsData},如果不清楚如何设置extend的值,可以参考${@link BlockExtendsDataTest}
     */
    private static final String CONFIG_FILED_EXTEND = "extend";
    /**
     * 初始资产分配
     */
    private static final String CONFIG_FILED_TXS = "txs";
    /**
     * 初始别名设定
     */
    private static final String CONFIG_FILED_ALIAS = "alias";
    /**
     * 分配地址
     */
    private static final String CONFIG_FILED_ADDRESS = "address";
    /**
     * 分配金额
     */
    private static final String CONFIG_FILED_AMOUNT = "amount";
    /**
     * 锁定时间
     */
    private static final String CONFIG_FILED_LOCK_TIME = "lockTime";
    /**
     * 创世块中交易的备注
     */
    private static final String CONFIG_FILED_REMARK = "remark";
    /**
     * 私钥,用来对创世块交易进行签名,没有其他用处
     */
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
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        List<Transaction> txlist = new ArrayList<>();
        txlist.add(tx);
        fillAliasTxs(txlist, jsonMap);
        setTxs(txlist);
    }

    private void fillAliasTxs(List<Transaction> txlist, Map<String, Object> jsonMap) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get(CONFIG_FILED_ALIAS);
        if (null == list || list.isEmpty()) {
            return;
        }
        for (Map<String, Object> map : list) {
            Transaction tx = new Transaction();
            tx.setType(TxType.ACCOUNT_ALIAS);
            tx.setTime(this.blockTime);
            String address = (String) map.get("address");
            String alias = (String) map.get("alias");
            byte[] txData;
            try {
                txData = getAliasTxData(address, alias);
            } catch (UnsupportedEncodingException e) {
                LoggerUtil.COMMON_LOG.error(e);
                continue;
            }
            tx.setTxData(txData);
            txlist.add(tx);
        }
    }

    private byte[] getAliasTxData(String address, String alias) throws UnsupportedEncodingException {
        byte[] a = AddressTool.getAddress(address);
        byte[] c = alias.getBytes(ToolsConstant.DEFAULT_ENCODING);
        byte[] b = new VarInt(c.length).encode();
        byte[] data = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, data, 0, a.length);
        System.arraycopy(b, 0, data, a.length, b.length);
        System.arraycopy(c, 0, data, a.length + b.length, c.length);
        return data;
    }


    private void fillHeader(Map<String, Object> jsonMap) {
        Integer height = (Integer) jsonMap.get(CONFIG_FILED_HEIGHT);
        String extend = (String) jsonMap.get(CONFIG_FILED_EXTEND);
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(height);
        header.setTime(blockTime);
        header.setPreHash(NulsHash.calcHash(new byte[35]));
        header.setTxCount(this.getTxs().size());
        List<NulsHash> txHashList = new ArrayList<>();
        for (Transaction tx : this.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsHash.calcMerkleHash(txHashList));
        header.setExtend(HexUtil.decode(extend));

        BlockSignature p2PKHScriptSig = new BlockSignature();
        priKey = new BigInteger(1, HexUtil.decode((String) jsonMap.get(CONFIG_FILED_PRIVATE_KEY)));
        NulsSignData signData = this.signature(header.getHash().getBytes());
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

