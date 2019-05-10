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
package io.nuls.protocol.manager;

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.utils.ConfigLoader;

import java.util.List;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author: PierreLuo
 * @date: 2019-02-26
 */
@Component
public class ChainManager {

    @Autowired
    private ProtocolService protocolService;

    public void initChain() throws Exception {
        //加载配置
        ConfigLoader.load();
        ContextManager.chainIds.forEach(this::initTable);
    }

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain() {
        List<Integer> chainIds = ContextManager.chainIds;
        for (Integer chainId : chainIds) {
            //服务初始化
            protocolService.init(chainId);
            ProtocolContext context = ContextManager.getContext(chainId);
            List<ProtocolVersion> list = context.getLocalVersionList();
            NulsLogger commonLog = context.getCommonLog();
            short localVersion = list.get(list.size() - 1).getVersion();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version > localVersion) {
                commonLog.error("localVersion-" + localVersion);
                commonLog.error("newVersion-" + version);
                commonLog.error("Older versions of the wallet automatically stop working, Please upgrade the latest version of the wallet!");
                System.exit(1);
            }
        }
    }

    /**
     * 停止一条链
     * Delete a chain
     *
     * @param chainId 链ID/chain id
     */
    public void stopChain(int chainId) {

    }

    /**
     * 初始化链相关表
     * Initialization chain correlation table
     *
     * @param chainId
     */
    private void initTable(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            RocksDBService.createTable(Constant.STATISTICS + chainId);
            RocksDBService.createTable(Constant.CACHED_INFO + chainId);
            RocksDBService.createTable(Constant.PROTOCOL_VERSION_PO + chainId);
        } catch (Exception e) {
            commonLog.error(e);
        }
    }

}
