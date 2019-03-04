/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.block.model;

import io.nuls.base.data.Block;
import io.nuls.tools.protocol.Protocol;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.block.utils.module.ProtocolUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.logback.NulsLogger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 每个链ID对应一个{@link ChainContext},维护一些链运行期间的信息,并负责链的初始化、启动、停止、销毁操作
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
@NoArgsConstructor
public class ChainContext {
    /**
     * 代表该链的运行状态
     */
    @Getter
    private RunningStatusEnum status;

    /**
     * 链ID
     */
    @Getter
    @Setter
    private int chainId;

    /**
     * 当前协议版本
     */
    @Getter
    @Setter
    private short version;

    /**
     * 所有协议版本(包括消息、交易映射)
     */
    @Getter
    @Setter
    private Map<Short, Protocol> protocolsMap;

    /**
     * 该链的系统交易类型
     */
    @Getter
    private List<Integer> systemTransactionType;

    /**
     * 最新区块
     */
    @Getter
    @Setter
    private Block latestBlock;

    /**
     * 创世区块
     */
    @Getter
    @Setter
    private Block genesisBlock;

    /**
     * 主链
     */
    @Getter
    @Setter
    private Chain masterChain;

    /**
     * 链的运行时参数
     */
    @Getter
    @Setter
    private ChainParameters parameters;

    /**
     * 获取锁对象
     * 清理数据库,区块同步,分叉链维护,孤儿链维护获取该锁
     */
    @Getter
    private StampedLock lock;

    /**
     * 记录通用日志
     */
    @Getter
    @Setter
    private NulsLogger commonLog;

    /**
     * 记录消息收发日志
     */
    @Getter
    @Setter
    private NulsLogger messageLog;

    public synchronized void setStatus(RunningStatusEnum status) {
        this.status = status;
    }

    public long getLatestHeight() {
        return latestBlock.getHeader().getHeight();
    }

    public void init() {
        lock = new StampedLock();
        LoggerUtil.init(chainId, parameters.getLogLevel());
        systemTransactionType = TransactionUtil.getSystemTypes(chainId);
        this.setStatus(RunningStatusEnum.INITIALIZING);
        //服务初始化
        BlockService service = SpringLiteContext.getBean(BlockService.class);
        service.init(chainId);
        //各类缓存初始化
        SmallBlockCacher.init(chainId);
        CacheHandler.init(chainId);
        ChainManager.init(chainId);
        ProtocolUtil.subscribe(chainId);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }
}