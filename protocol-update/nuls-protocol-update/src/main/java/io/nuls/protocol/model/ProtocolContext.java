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

package io.nuls.protocol.model;

import io.nuls.protocol.constant.RunningStatusEnum;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.utils.LoggerUtil;
import io.nuls.protocol.utils.module.BlockUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.logback.NulsLogger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 每个链ID对应一个{@link ProtocolContext},维护一些链运行期间的信息,并负责链的初始化、启动、停止、销毁操作
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
@NoArgsConstructor
public class ProtocolContext {
    /**
     * 代表模块的运行状态
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
     * 最新高度
     */
    @Getter
    @Setter
    private long latestHeight;

    /**
     * 当前生效的协议版本
     */
    @Getter
    @Setter
    private ProtocolVersion protocolVersion;

    /**
     * 缓存的未统计区间协议对象列表
     */
    @Getter
    @Setter
    private List<ProtocolVersion> versionList;

    /**
     * 缓存的未统计区间内各协议版本占比
     */
    @Getter
    @Setter
    private Map<ProtocolVersion, Integer> proportionMap;

    /**
     * 缓存的未统计区间内区块数
     */
    @Getter
    @Setter
    private int count;

    /**
     * 上一条缓存的统计信息
     */
    @Getter
    @Setter
    private Statistics lastValidStatistics;

    /**
     * 链的运行时参数
     */
    @Getter
    @Setter
    private ProtocolConfig config;

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

    public synchronized void setStatus(RunningStatusEnum status) {
        this.status = status;
    }

    public void init() {
        lock = new StampedLock();
        LoggerUtil.init(chainId, config.getLogLevel());
        this.setStatus(RunningStatusEnum.READY);
        //服务初始化
        ProtocolService service = SpringLiteContext.getBean(ProtocolService.class);
        service.init(chainId);
        //各类缓存初始化

        //定时调度接口初始化
        BlockUtil.register(chainId);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }
}