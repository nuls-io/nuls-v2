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

package io.nuls.block.service.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.block.constant.Constant;
import io.nuls.block.model.po.ChainContextPo;
import io.nuls.block.service.ContextStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

public class ContextStorageServiceImpl implements ContextStorageService {
    @Override
    public boolean save(ChainContextPo chainContextPo, int chainID) {
        byte[] bytes;
        try {
            bytes = chainContextPo.serialize();
            return RocksDBService.put(Constant.CHAIN_CONTEXT, ByteUtils.intToBytes(chainID), bytes);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public ChainContextPo get(int chainID) {
        try {
            ChainContextPo po = new ChainContextPo();
            byte[] bytes = RocksDBService.get(Constant.CHAIN_CONTEXT, ByteUtils.intToBytes(chainID));
            po.parse(new NulsByteBuffer(bytes));
            return po;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainID) {
        try {
            return RocksDBService.delete(Constant.CHAIN_CONTEXT, ByteUtils.intToBytes(chainID));
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public List<ChainContextPo> getList() {
        try {
            var pos = new ArrayList<ChainContextPo>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.CHAIN_CONTEXT);
            for (byte[] bytes : valueList) {
                var po = new ChainContextPo();
                po.parse(new NulsByteBuffer(bytes));
                pos.add(po);
            }
            return pos;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }
}
