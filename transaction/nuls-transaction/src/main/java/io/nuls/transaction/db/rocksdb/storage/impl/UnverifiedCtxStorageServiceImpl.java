/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.db.rocksdb.storage.UnverifiedCtxStorageService;
import io.nuls.transaction.model.bo.CrossTx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018-12-27
 */
@Service
public class UnverifiedCtxStorageServiceImpl implements UnverifiedCtxStorageService {

    @Override
    public boolean putTx(int chainId, CrossTx ctx) {
        if (null == ctx) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = ctx.getTx().getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_UNPROCESSED_CROSSCHAIN + chainId, txHashBytes, ctx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean removeTxList(int chainId, List<CrossTx> ctxList) {
        //check params
        if (ctxList == null || ctxList.size() == 0) {
            return false;
        }
        try {
            List<byte[]> hashList = new ArrayList<>();
            for (CrossTx crossTx : ctxList) {
                hashList.add(crossTx.getTx().getHash().serialize());
            }
            return RocksDBService.deleteKeys(TxDBConstant.DB_UNPROCESSED_CROSSCHAIN + chainId, hashList);
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<CrossTx> getTxList(int chainId) {
        List<CrossTx> ccTxPoList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(TxDBConstant.DB_UNPROCESSED_CROSSCHAIN + chainId);
            if (list != null) {
                for (byte[] value : list) {
                    CrossTx ccTx = new CrossTx();
                    //将byte数组反序列化为Object返回
                    ccTx.parse(value, 0);
                    ccTxPoList.add(ccTx);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return ccTxPoList;
    }

    @Override
    public CrossTx getTx(int chainId, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = hash.serialize();
        } catch (IOException e) {
            Log.error(e);
            return null;
        }
        try {
            byte[] bytes = RocksDBService.get(TxDBConstant.DB_UNPROCESSED_CROSSCHAIN + chainId, txHashBytes);
            if (bytes != null) {
                CrossTx ccTx = new CrossTx();
                ccTx.parse(bytes, 0);
                return ccTx;
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }
}
