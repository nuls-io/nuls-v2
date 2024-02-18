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
import io.nuls.common.ConfigBean;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.protocol.model.po.StatisticsInfo;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each chainIDCorresponding to one{@link ProtocolContext},Maintain information during chain operation,And responsible for initializing the chain、start-up、cease、Destruction operation
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 morning10:46
 */
public class ProtocolContext {
    /**
     * chainID
     */
    private int chainId;

    /**
     * Latest height
     */
    private long latestHeight;

    /**
     * The current effective protocol version(Main network protocol version)
     */
    private ProtocolVersion currentProtocolVersion;

    /**
     * The current local protocol version(Local Node Protocol Version)
     */
    private ProtocolVersion localProtocolVersion;

    /**
     * Current effective protocol version count
     */
    private int currentProtocolVersionCount;

    /**
     * History of all effective protocol versions,Rollback
     */
    private Deque<ProtocolVersion> protocolVersionHistory;

    /**
     * List of protocol objects read from configuration file
     */
    private List<ProtocolVersion> localVersionList;

    /**
     * The proportion of different protocol versions within the unstatisfied interval of cache
     */
    private Map<ProtocolVersion, Integer> proportionMap;

    /**
     * The number of cached blocks within an unstatisfied interval
     */
    private int count;

    /**
     * Previous cache statistics
     */
    private StatisticsInfo lastValidStatisticsInfo;

    /**
     * The runtime parameters of the chain
     */
    private ConfigBean parameters;

    /**
     * Record General Logs
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

    public ConfigBean getParameters() {
        return parameters;
    }

    public void setParameters(ConfigBean parameters) {
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
