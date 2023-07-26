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

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.protocol.Protocol;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.protocol.model.po.StatisticsInfo;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个链ID对应一个{@link ProtocolContext},维护一些链运行期间的信息,并负责链的初始化、启动、停止、销毁操作
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
public class ProtocolContext {
    /**
     * 链ID
     */
    private int chainId;

    /**
     * 最新高度
     */
    private long latestHeight;

    /**
     * 当前生效的协议版本(主网协议版本)
     */
    private ProtocolVersion currentProtocolVersion;

    /**
     * 当前本地的协议版本(本地节点协议版本)
     */
    private ProtocolVersion localProtocolVersion;

    /**
     * 当前生效的协议版本计数
     */
    private int currentProtocolVersionCount;

    /**
     * 所有生效的协议版本历史记录,回滚用
     */
    private Deque<ProtocolVersion> protocolVersionHistory;

    /**
     * 从配置文件读取的协议对象列表
     */
    private List<ProtocolVersion> localVersionList;

    /**
     * 缓存的未统计区间内各协议版本占比
     */
    private Map<ProtocolVersion, Integer> proportionMap;

    /**
     * 缓存的未统计区间内区块数
     */
    private int count;

    /**
     * 上一条缓存的统计信息
     */
    private StatisticsInfo lastValidStatisticsInfo;

    /**
     * 链的运行时参数
     */
    private ChainParameters parameters;

    /**
     * 记录通用日志
     */
    private NulsLogger logger;

    private Map<Short, List<Map.Entry<String, Protocol>>> protocolMap;

    public ProtocolVersion getLocalProtocolVersion() {
        return localProtocolVersion;
    }

    public void setLocalProtocolVersion(ProtocolVersion localProtocolVersion) {
        this.localProtocolVersion = localProtocolVersion;
    }

    public Map<Short, List<Map.Entry<String, Protocol>>> getProtocolMap() {
        return protocolMap;
    }

    public void setProtocolMap(Map<Short, List<Map.Entry<String, Protocol>>> protocolMap) {
        this.protocolMap = protocolMap;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public long getLatestHeight() {
        return latestHeight;
    }

    public void setLatestHeight(long latestHeight) {
        this.latestHeight = latestHeight;
    }

    public ProtocolVersion getCurrentProtocolVersion() {
        return currentProtocolVersion;
    }

    public void setCurrentProtocolVersion(ProtocolVersion currentProtocolVersion) {
        this.currentProtocolVersion = currentProtocolVersion;
    }

    public int getCurrentProtocolVersionCount() {
        return currentProtocolVersionCount;
    }

    public void setCurrentProtocolVersionCount(int currentProtocolVersionCount) {
        this.currentProtocolVersionCount = currentProtocolVersionCount;
    }

    public Deque<ProtocolVersion> getProtocolVersionHistory() {
        return protocolVersionHistory;
    }

    public void setProtocolVersionHistory(Deque<ProtocolVersion> protocolVersionHistory) {
        this.protocolVersionHistory = protocolVersionHistory;
    }

    public List<ProtocolVersion> getLocalVersionList() {
        return localVersionList;
    }

    public void setLocalVersionList(List<ProtocolVersion> localVersionList) {
        this.localVersionList = localVersionList;
    }

    public Map<ProtocolVersion, Integer> getProportionMap() {
        return proportionMap;
    }

    public void setProportionMap(Map<ProtocolVersion, Integer> proportionMap) {
        this.proportionMap = proportionMap;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public StatisticsInfo getLastValidStatisticsInfo() {
        return lastValidStatisticsInfo;
    }

    public void setLastValidStatisticsInfo(StatisticsInfo lastValidStatisticsInfo) {
        this.lastValidStatisticsInfo = lastValidStatisticsInfo;
    }

    public ChainParameters getParameters() {
        return parameters;
    }

    public void setParameters(ChainParameters parameters) {
        this.parameters = parameters;
    }

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

    public void init() {
        protocolMap = new HashMap<>();
        proportionMap = new HashMap<>();
        localProtocolVersion = localVersionList.get(localVersionList.size() - 1);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }

    @Override
    public String toString() {
        return "ProtocolContext{" +
                "chainId=" + chainId +
                ", latestHeight=" + latestHeight +
                ", currentProtocolVersion=" + currentProtocolVersion +
                ", localProtocolVersion=" + localProtocolVersion +
                ", currentProtocolVersionCount=" + currentProtocolVersionCount +
                ", protocolVersionHistory=" + protocolVersionHistory +
                ", localVersionList=" + localVersionList +
                ", proportionMap=" + proportionMap +
                ", count=" + count +
                ", lastValidStatisticsInfo=" + lastValidStatisticsInfo +
                ", protocolMap=" + protocolMap +
                '}';
    }
}