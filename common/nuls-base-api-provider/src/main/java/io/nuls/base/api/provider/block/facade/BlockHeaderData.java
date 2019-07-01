package io.nuls.base.api.provider.block.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 18:07
 * @Description: 功能描述
 */
public class BlockHeaderData {

    private String hash;

    private String preHash;

    private String merkleHash;

    private String time;

    private long height;

    private int txCount;

    private String blockSignature;

    private int size;

    private String packingAddress;

    protected long roundIndex;

    protected int consensusMemberCount;

    protected String roundStartTime;

    protected int packingIndexOfRound;

    /**
     * 主网当前生效的版本
     */
    private short mainVersion;

    /**
     * 区块的版本，可以理解为本地钱包的版本
     */
    private short blockVersion;


    private String stateRoot;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockHeaderData)) {
            return false;
        }

        BlockHeaderData that = (BlockHeaderData) o;

        if (height != that.height) {
            return false;
        }
        if (txCount != that.txCount) {
            return false;
        }
        if (size != that.size) {
            return false;
        }
        if (roundIndex != that.roundIndex) {
            return false;
        }
        if (consensusMemberCount != that.consensusMemberCount) {
            return false;
        }
        if (packingIndexOfRound != that.packingIndexOfRound) {
            return false;
        }
        if (mainVersion != that.mainVersion) {
            return false;
        }
        if (blockVersion != that.blockVersion) {
            return false;
        }
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) {
            return false;
        }
        if (preHash != null ? !preHash.equals(that.preHash) : that.preHash != null) {
            return false;
        }
        if (merkleHash != null ? !merkleHash.equals(that.merkleHash) : that.merkleHash != null) {
            return false;
        }
        if (time != null ? !time.equals(that.time) : that.time != null) {
            return false;
        }
        if (blockSignature != null ? !blockSignature.equals(that.blockSignature) : that.blockSignature != null) {
            return false;
        }
        if (packingAddress != null ? !packingAddress.equals(that.packingAddress) : that.packingAddress != null) {
            return false;
        }
        if (roundStartTime != null ? !roundStartTime.equals(that.roundStartTime) : that.roundStartTime != null) {
            return false;
        }
        return stateRoot != null ? stateRoot.equals(that.stateRoot) : that.stateRoot == null;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (preHash != null ? preHash.hashCode() : 0);
        result = 31 * result + (merkleHash != null ? merkleHash.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (int) (height ^ (height >>> 32));
        result = 31 * result + txCount;
        result = 31 * result + (blockSignature != null ? blockSignature.hashCode() : 0);
        result = 31 * result + size;
        result = 31 * result + (packingAddress != null ? packingAddress.hashCode() : 0);
        result = 31 * result + (int) (roundIndex ^ (roundIndex >>> 32));
        result = 31 * result + consensusMemberCount;
        result = 31 * result + (roundStartTime != null ? roundStartTime.hashCode() : 0);
        result = 31 * result + packingIndexOfRound;
        result = 31 * result + (int) mainVersion;
        result = 31 * result + (int) blockVersion;
        result = 31 * result + (stateRoot != null ? stateRoot.hashCode() : 0);
        return result;
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

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"hash\":\"")
                .append(hash).append('\"')
                .append(",\"preHash\":\"")
                .append(preHash).append('\"')
                .append(",\"merkleHash\":\"")
                .append(merkleHash).append('\"')
                .append(",\"time\":\"")
                .append(time).append('\"')
                .append(",\"height\":")
                .append(height)
                .append(",\"txCount\":")
                .append(txCount)
                .append(",\"blockSignature\":\"")
                .append(blockSignature).append('\"')
                .append(",\"size\":")
                .append(size)
                .append(",\"packingAddress\":\"")
                .append(packingAddress).append('\"')
                .append(",\"roundIndex\":")
                .append(roundIndex)
                .append(",\"consensusMemberCount\":")
                .append(consensusMemberCount)
                .append(",\"roundStartTime\":\"")
                .append(roundStartTime).append('\"')
                .append(",\"packingIndexOfRound\":")
                .append(packingIndexOfRound)
                .append(",\"mainVersion\":")
                .append(mainVersion)
                .append(",\"blockVersion\":")
                .append(blockVersion)
                .append(",\"stateRoot\":\"")
                .append(stateRoot).append('\"')
                .append('}').toString();
    }
}
