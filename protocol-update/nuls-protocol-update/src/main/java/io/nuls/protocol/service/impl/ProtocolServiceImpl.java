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
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.service.StatisticsStorageService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 区块服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
public class ProtocolServiceImpl implements ProtocolService {

    @Autowired
    private StatisticsStorageService service;

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
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        try {
            RocksDBService.createTable(Constant.STATISTICS + chainId);
            //初始化一条新协议统计信息，与区块高度绑定，并存到数据库
            Statistics statistics = new Statistics();
            statistics.setHeight(0);
            statistics.setLastHeight(0);
            statistics.setProtocolVersion(context.getCurrentProtocolVersion());
            Map<ProtocolVersion, Integer> proportionMap = new HashMap<>();
            proportionMap.put(context.getCurrentProtocolVersion(), 1);
            statistics.setProtocolVersionMap(proportionMap);
            statistics.setCount((short) 0);
            boolean b = service.save(chainId, statistics);
            commonLog.info("chainId-" + chainId + ", height-0, save-" + b + ", new statistics-" + statistics);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

}
