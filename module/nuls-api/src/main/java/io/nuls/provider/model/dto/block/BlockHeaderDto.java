/*
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
 */

package io.nuls.provider.model.dto.block;


import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.provider.api.config.Context;

import java.util.List;

/**
 * @author: Niels Wang
 */
@ApiModel(description = "blockHeader Block header information, Only return the corresponding partial data")
public class BlockHeaderDto {

    @ApiModelProperty(description = "Blockedhashvalue")
    private String hash;

    @ApiModelProperty(description = "Previous block'shashvalue")
    private String preHash;

    @ApiModelProperty(description = "Merkelhash")
    private String merkleHash;

    @ApiModelProperty(description = "Block generation time")
    private String time;
    @ApiModelProperty(description = "Block generation timestamp")
    private long timestamp;

    @ApiModelProperty(description = "block height")
    private long height;

    @ApiModelProperty(description = "Number of block packaging transactions")
    private int txCount;

    @ApiModelProperty(description = "autographHex.encode(byte[])")
    private String blockSignature;

    @ApiModelProperty(description = "size")
    private int size;

    @ApiModelProperty(description = "Packaging address")
    private String packingAddress;

    @ApiModelProperty(description = "Consensus round")
    private long roundIndex;

    @ApiModelProperty(description = "Number of members participating in consensus")
    private int consensusMemberCount;

    @ApiModelProperty(description = "Current consensus round start time")
    private String roundStartTime;
    @ApiModelProperty(description = "Current consensus round start timestamp")
    private long roundStartTimestamp;

    @ApiModelProperty(description = "The ranking of the blocks packaged in the current round")
    private int packingIndexOfRound;

    @ApiModelProperty(description = "The current effective version of the main network")
    private short mainVersion;

    @ApiModelProperty(description = "The version of the block can be understood as the version of the local wallet")
    private short blockVersion;

    @ApiModelProperty(description = "Smart Contract World State Root")
    private String stateRoot;

    @ApiModelProperty(description = "Block packaged transactionshashaggregate", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> txHashList;

    public BlockHeaderDto() {

    }

    public BlockHeaderDto(BlockHeader header) {
        BlockExtendsData blockExtendsData = header.getExtendsData();
        this.setHash(header.getHash().toString());
        this.setHeight(header.getHeight());
        this.setSize(header.size());
        this.setTime(NulsDateUtils.timeStamp2DateStr(header.getTime() * 1000));
        this.setTimestamp(header.getTime());
        this.setTxCount(header.getTxCount());
        this.setMerkleHash(header.getMerkleHash().toString());
        this.setBlockSignature(header.getBlockSignature().getSignData().toString());
        this.setPreHash(header.getPreHash().toString());
        this.setPackingAddress(AddressTool.getStringAddressByBytes(header.getPackingAddress(Context.getChainId())));
        this.setConsensusMemberCount(blockExtendsData.getConsensusMemberCount());
        this.setMainVersion(blockExtendsData.getMainVersion());
        this.setBlockVersion(blockExtendsData.getBlockVersion());
        this.setPackingIndexOfRound(blockExtendsData.getPackingIndexOfRound());
        this.setRoundIndex(blockExtendsData.getRoundIndex());
        this.setRoundStartTimestamp(blockExtendsData.getRoundStartTime());
        this.setRoundStartTime(NulsDateUtils.timeStamp2DateStr(blockExtendsData.getRoundStartTime() * 1000));
        this.setStateRoot(RPCUtil.encode(blockExtendsData.getStateRoot()));
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public String getBlockSignature() {
        return blockSignature;
    }

    public void setBlockSignature(String blockSignature) {
        this.blockSignature = blockSignature;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public int getConsensusMemberCount() {
        return consensusMemberCount;
    }

    public void setConsensusMemberCount(int consensusMemberCount) {
        this.consensusMemberCount = consensusMemberCount;
    }

    public String getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(String roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public short getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(short mainVersion) {
        this.mainVersion = mainVersion;
    }

    public short getBlockVersion() {
        return blockVersion;
    }

    public void setBlockVersion(short blockVersion) {
        this.blockVersion = blockVersion;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public List<String> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<String> txHashList) {
        this.txHashList = txHashList;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRoundStartTimestamp() {
        return roundStartTimestamp;
    }

    public void setRoundStartTimestamp(long roundStartTimestamp) {
        this.roundStartTimestamp = roundStartTimestamp;
    }
}
