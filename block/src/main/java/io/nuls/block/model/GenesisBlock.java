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

package io.nuls.block.model;

import io.nuls.base.data.*;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;
import org.apache.http.util.Asserts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * todo 链工厂的链创世块
 * 创世块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-13 下午5:11
 */
public final class GenesisBlock extends Block {

    private final static String GENESIS_BLOCK_FILE = "genesis-block.json";
    private static final String CONFIG_FILED_TIME = "time";
    private static final String CONFIG_FILED_HEIGHT = "height";
    private static final String CONFIG_FILED_EXTEND = "extend";
    private static final String CONFIG_FILED_TXS = "txs";
    private static final String CONFIG_FILED_ADDRESS = "address";
    private static final String CONFIG_FILED_AMOUNT = "amount";
    private static final String CONFIG_FILED_LOCK_TIME = "lockTime";
    private static final String CONFIG_FILED_REMARK = "remark";
    private static final String PRIVATE_KEY = "009cf05b6b3fe8c09b84c13783140c0f1958e8841f8b6f894ef69431522bc65712";

    private static GenesisBlock INSTANCE = new GenesisBlock();

    private transient long blockTime;

    private transient int status = 0;

    private GenesisBlock() {

    }

    public static GenesisBlock getInstance() throws Exception {
        if (INSTANCE.status == 0) {
            String json = null;
            try {
                json = IoUtils.read(GENESIS_BLOCK_FILE);
            } catch (NulsException e) {
                e.printStackTrace();
                Log.error(e);
            }
            INSTANCE.init(json);
        }
        return INSTANCE;
    }

    private void init(String json) throws Exception {
        if (status > 0) {
            return;
        }
        Map<String, Object> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
        }
        String time = (String) jsonMap.get(CONFIG_FILED_TIME);
        Asserts.notEmpty(time, BlockErrorCode.DATA_ERROR.getCode());
        blockTime = Long.parseLong(time);
        this.initGengsisTxs(jsonMap);
        this.fillHeader(jsonMap);
        this.status = 1;
    }

    private void initGengsisTxs(Map<String, Object> jsonMap) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get(CONFIG_FILED_TXS);
        if (null == list || list.isEmpty()) {
            throw new NulsRuntimeException(BlockErrorCode.DATA_ERROR);
        }

        CoinData coinData = new CoinData();

        for (Map<String, Object> map : list) {
            String address = (String) map.get(CONFIG_FILED_ADDRESS);
            Asserts.notEmpty(address, BlockErrorCode.DATA_ERROR.getMsg());

            String amount = map.get(CONFIG_FILED_AMOUNT).toString();
            Long lockTime = Long.valueOf("" + map.get(CONFIG_FILED_LOCK_TIME));

            Address ads = Address.fromHashs(address);

            CoinTo coin = new CoinTo();
            coin.setAddress(ads.getAddressBytes());
            coin.setAmount(new BigInteger(amount));
            coin.setAssetsChainId(1);
            coin.setAssetsId(1);
            coin.setLockTime(lockTime == null ? 0 : lockTime.longValue());

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
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
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
        header.setPreHash(NulsDigestData.calcDigestData(new byte[35]));
        header.setTxCount(this.getTxs().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : this.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setExtend(HexUtil.decode(extend));

        header.setHash(NulsDigestData.calcDigestData(header));

        BlockSignature p2PKHScriptSig = new BlockSignature();
        NulsSignData signData = this.signature(header.getHash().getDigestBytes());
        p2PKHScriptSig.setSignData(signData);
        p2PKHScriptSig.setPublicKey(getGenesisPubkey());
        header.setBlockSignature(p2PKHScriptSig);
    }

    private NulsSignData signature(byte[] bytes) {
        return SignatureUtil.signDigest(bytes, ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(PRIVATE_KEY))));
    }

    private byte[] getGenesisPubkey() {
        return ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(PRIVATE_KEY))).getPubKey();
    }
}

