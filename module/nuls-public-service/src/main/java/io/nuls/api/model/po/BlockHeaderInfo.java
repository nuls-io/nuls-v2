/*
 *
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 *
 */
package io.nuls.api.model.po;

import java.math.BigInteger;
import java.util.List;

public class BlockHeaderInfo {

    private String hash;

    private long height;

    private String preHash;

    private String merkleHash;

    private long createTime;

    private String agentHash;

    private String agentId;

    private String packingAddress;

    private String agentAlias;

    private int txCount;

    private long roundIndex;

    private BigInteger totalFee;

    private BigInteger reward;

    private int size;

    private int packingIndexOfRound;

    private String scriptSign;

    private List<String> txHashList;

    private boolean isSeedPacked;

    private long roundStartTime;

    private int agentVersion;

    private int mainVersion;

    public void setByAgentInfo(AgentInfo agentInfo) {
        this.agentHash = agentInfo.getTxHash();
        this.agentId = agentInfo.getAgentId();
        this.agentAlias = agentInfo.getAgentAlias();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(String merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getAgentAlias() {
        return agentAlias;
    }

    public void setAgentAlias(String agentAlias) {
        this.agentAlias = agentAlias;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public BigInteger getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigInteger totalFee) {
        this.totalFee = totalFee;
    }

    public BigInteger getReward() {
        return reward;
    }

    public void setReward(BigInteger reward) {
        this.reward = reward;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public String getScriptSign() {
        return scriptSign;
    }

    public void setScriptSign(String scriptSign) {
        this.scriptSign = scriptSign;
    }

    public List<String> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<String> txHashList) {
        this.txHashList = txHashList;
    }

    public boolean isSeedPacked() {
        return isSeedPacked;
    }

    public void setSeedPacked(boolean seedPacked) {
        isSeedPacked = seedPacked;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(int agentVersion) {
        this.agentVersion = agentVersion;
    }

    public int getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(int mainVersion) {
        this.mainVersion = mainVersion;
    }
}
