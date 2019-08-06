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


import io.nuls.provider.api.config.Context;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.util.NulsDateUtils;

/**
 * @author: Niels Wang
 */
@ApiModel(description = "blockHeader 区块头信息, 只返回对应的部分数据")
public class BlockHeaderDto {

    @ApiModelProperty(description = "区块的hash值")
    private String hash;

    @ApiModelProperty(description = "上一个区块的hash值")
    private String preHash;

    @ApiModelProperty(description = "梅克尔hash")
    private String merkleHash;

    @ApiModelProperty(description = "区块生成时间")
    private String time;

    @ApiModelProperty(description = "区块高度")
    private long height;

    @ApiModelProperty(description = "区块打包交易数量")
    private int txCount;

    @ApiModelProperty(description = "签名Hex.encode(byte[])")
    private String blockSignature;

    @ApiModelProperty(description = "大小")
    private int size;

    @ApiModelProperty(description = "打包地址")
    private String packingAddress;

    @ApiModelProperty(description = "共识轮次")
    private long roundIndex;

    @ApiModelProperty(description = "参与共识成员数量")
    private int consensusMemberCount;

    @ApiModelProperty(description = "当前共识轮开始时间")
    private String roundStartTime;

    @ApiModelProperty(description = "当前轮次打包出块的名次")
    private int packingIndexOfRound;

    @ApiModelProperty(description = "主网当前生效的版本")
    private short mainVersion;

    @ApiModelProperty(description = "区块的版本，可以理解为本地钱包的版本")
    private short blockVersion;

    @ApiModelProperty(description = "智能合约世界状态根")
    private String stateRoot;

    public BlockHeaderDto() {

    }

    public BlockHeaderDto(BlockHeader header) throws NulsException {
        BlockExtendsData blockExtendsData = new BlockExtendsData();
        blockExtendsData.parse(new NulsByteBuffer(header.getExtend()));
        this.setHash(header.getHash().toString());
        this.setHeight(header.getHeight());
        this.setSize(header.size());
        this.setTime(NulsDateUtils.timeStamp2DateStr(header.getTime()));
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
        this.setRoundStartTime(NulsDateUtils.timeStamp2DateStr(blockExtendsData.getRoundStartTime()));
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
}