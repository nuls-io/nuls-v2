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

}
