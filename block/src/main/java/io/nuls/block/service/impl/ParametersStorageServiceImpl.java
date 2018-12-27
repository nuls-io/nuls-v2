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
import io.nuls.block.model.ChainParameters;
import io.nuls.block.service.ParametersStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParametersStorageServiceImpl implements ParametersStorageService {
    @Override
    public boolean save(ChainParameters chainParameters, int chainID) {
        byte[] bytes;
        try {
            bytes = chainParameters.serialize();
            return RocksDBService.put(Constant.CHAIN_PARAMETERS, ByteUtils.intToBytes(chainID), bytes);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public ChainParameters get(int chainID) {
        try {
            ChainParameters po = new ChainParameters();
            byte[] bytes = RocksDBService.get(Constant.CHAIN_PARAMETERS, ByteUtils.intToBytes(chainID));
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
            return RocksDBService.delete(Constant.CHAIN_PARAMETERS, ByteUtils.intToBytes(chainID));
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public List<ChainParameters> getList() {
        try {
            var pos = new ArrayList<ChainParameters>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.CHAIN_PARAMETERS);
            for (byte[] bytes : valueList) {
                var po = new ChainParameters();
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
