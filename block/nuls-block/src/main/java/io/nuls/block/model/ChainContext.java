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
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.protocol.Protocol;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
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
     * 是否继续本次下载，中途发生异常置为false
     */
    @Getter
    @Setter
    private boolean doSyn;

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
    @Setter
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

    /**
     * 分叉链、孤儿链中重复hash计数器
     */
    @Getter
    private Map<String, AtomicInteger> duplicateBlockMap;

    /**
     * 记录某个打包地址是否已经进行过分叉通知，每个地址只通知一次
     */
    @Getter
    private List<byte []> packingAddressList;

    public synchronized void setStatus(RunningStatusEnum status) {
        this.status = status;
    }

    public long getLatestHeight() {
        return latestBlock.getHeader().getHeight();
    }

    public void init() {
        this.setStatus(RunningStatusEnum.INITIALIZING);
        packingAddressList = new CopyOnWriteArrayList<>();
        duplicateBlockMap = new HashMap<>();
        systemTransactionType = new ArrayList<>();
        version = 1;
        doSyn = true;
        lock = new StampedLock();
        LoggerUtil.init(chainId, parameters.getLogLevel());
        //各类缓存初始化
        SmallBlockCacher.init(chainId);
        CacheHandler.init(chainId);
        BlockChainManager.init(chainId);
        TxGroupRequestor.init(chainId);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }

    public void printChains() {
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        commonLog.info("masterChain-" + masterChain);
        SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
        forkChains.forEach(e -> commonLog.info("forkChain-" + e));
        SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
        orphanChains.forEach(e -> commonLog.info("orphanChain-" + e));
    }
}