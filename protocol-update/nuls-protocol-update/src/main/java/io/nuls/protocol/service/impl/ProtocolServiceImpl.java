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

package io.nuls.protocol.service.impl;

import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.logback.NulsLogger;

import static io.nuls.protocol.constant.Constant.*;

/**
 * 区块服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
public class ProtocolServiceImpl implements ProtocolService {

    @Override
    public boolean startChain(int chainId) {
        return false;
    }

    @Override
    public boolean stopChain(int chainId, boolean cleanData) {
        return false;
    }

    @Override
    public void init(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
//            RocksDBService.createTable(BLOCK_HEADER + chainId);
//            RocksDBService.createTable(BLOCK_HEADER_INDEX + chainId);
//            if (RocksDBService.existTable(CACHED_BLOCK + chainId)) {
//                RocksDBService.destroyTable(CACHED_BLOCK + chainId);
//            }
//            RocksDBService.createTable(CACHED_BLOCK + chainId);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

}
