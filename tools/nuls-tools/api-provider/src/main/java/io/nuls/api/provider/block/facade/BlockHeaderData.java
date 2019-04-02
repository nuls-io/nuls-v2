package io.nuls.api.provider.block.facade;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 18:07
 * @Description: 功能描述
 */
@Data
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
        if (this == o) return true;
        if (!(o instanceof BlockHeaderData)) return false;

        BlockHeaderData that = (BlockHeaderData) o;

        if (height != that.height) return false;
        if (txCount != that.txCount) return false;
        if (size != that.size) return false;
        if (roundIndex != that.roundIndex) return false;
        if (consensusMemberCount != that.consensusMemberCount) return false;
        if (packingIndexOfRound != that.packingIndexOfRound) return false;
        if (mainVersion != that.mainVersion) return false;
        if (blockVersion != that.blockVersion) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        if (preHash != null ? !preHash.equals(that.preHash) : that.preHash != null) return false;
        if (merkleHash != null ? !merkleHash.equals(that.merkleHash) : that.merkleHash != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (blockSignature != null ? !blockSignature.equals(that.blockSignature) : that.blockSignature != null)
            return false;
        if (packingAddress != null ? !packingAddress.equals(that.packingAddress) : that.packingAddress != null)
            return false;
        if (roundStartTime != null ? !roundStartTime.equals(that.roundStartTime) : that.roundStartTime != null)
            return false;
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
}
