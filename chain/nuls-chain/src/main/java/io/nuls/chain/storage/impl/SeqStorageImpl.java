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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * @author lan
 * @program nuls2.0
 * @description
 * @date 2018/11/26
 **/
@Component
public class SeqStorageImpl implements SeqStorage, InitializingBean {
    private static final String TBL = "seq";
    /**
     * key :chainId  value:current seq
     */
    private static final Map<Integer, Integer> SEQ_MAP = new ConcurrentHashMap<>();

    /**
     * 得到链的下一个序列号
     */
    @Override
    public int nextSeq(int chainId) throws Exception {

        /*
        空，则从1开始
         */
        if (SEQ_MAP.get(chainId) == null) {
            setSeq(chainId, 1);
            return 1;
        }

        /*
        非空，则尝试返回当前值+1
         */
        int nextSeq = SEQ_MAP.get(chainId) + 1;
        if (setSeq(chainId, nextSeq)) {
            /*
            符合规范
             */
            return nextSeq;
        } else {
            /*
            不符合规范，递归调用
             */
            return nextSeq(chainId);
        }
    }

    /**
     * 设置链的序列号
     */
    @Override
    public boolean setSeq(int chainId, int tarSeq) throws Exception {
        synchronized (SEQ_MAP) {
            /*
            空：第一次添加链的序列号信息，直接保存
            非空：待添加序列号大于当前序列号，保存；否则丢弃
             */
            if (SEQ_MAP.get(chainId) == null || tarSeq > SEQ_MAP.get(chainId)) {
                SEQ_MAP.put(chainId, tarSeq);
                return RocksDBService.put(TBL, ByteUtils.intToBytes(chainId), ByteUtils.intToBytes(tarSeq));
            } else {
                return false;
            }
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

            List<byte[]> keyList = RocksDBService.keyList(TBL);
            for (byte[] key : keyList) {
                byte[] value = RocksDBService.get(TBL, key);
                try {
                    SEQ_MAP.put(ByteUtils.bytesToInt(key), ByteUtils.bytesToInt(value));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
