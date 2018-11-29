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
package io.nuls.chain.storage.impl;

import io.nuls.chain.storage.SeqStorage;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

/**
 * @program: nuls2.0
 * @description:
 * @author: lan
 * @create: 2018/11/26
 **/
@Component
public class SeqStorageImpl implements SeqStorage, InitializingBean {
    private static final String TBL = "seq";
    /**
     * 创建assetId
     * create asset id
     * @return
     */
//    @Override
//    public int createSeqAsset(int chainId) {
//        try {
//            int assetId = getSeqAsset(chainId);
//            assetId = assetId+1;
//            RocksDBService.put(TBL, ByteUtils.intToBytes(chainId), ByteUtils.intToBytes(assetId));
//            return assetId;
//        } catch (Exception e) {
//            Log.error(e);
//        }
//        return 0;
//    }

    /**
     * 获取 assetId
     * get asset id
     *
     * @return
     */
    @Override
    public int getSeqAsset(int chainId) {
        try {
            byte[] assetSeq = RocksDBService.get(TBL, ByteUtils.intToBytes(chainId));
            if (null == assetSeq) {
                return 0;
            }
            int currSeq = ByteUtils.bytesToInt(assetSeq);
            setSeqAsset(chainId, currSeq + 1);
            return ByteUtils.bytesToInt(assetSeq);
        } catch (Exception e) {
            Log.error(e);
        }
        return 0;
    }

    @Override
    public void setSeqAsset(int chainId, int seq) {
        try {
            byte[] currSeqByte = RocksDBService.get(TBL, ByteUtils.intToBytes(chainId));
            int currSeq = currSeqByte == null ? 0 : ByteUtils.bytesToInt(currSeqByte);
            if (seq > currSeq) {
                RocksDBService.put(TBL, ByteUtils.intToBytes(chainId), ByteUtils.intToBytes(seq));
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            if (!RocksDBService.existTable(TBL)) {
                RocksDBService.createTable(TBL);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
