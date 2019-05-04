package io.nuls.crosschain.nuls.model.bo.config;

import java.io.Serializable;

/**
 * 跨链模块配置类
 * Consensus Module Configuration Class
 *
 * @author tag
 * 2019/4/10
 */
public class ConfigBean implements Serializable {
    /**
     * 资产ID
     * assets id
     */
    private int assetsId;

    /**
     * chain id
     */
    private int chainId;

    /**
     * 最小链接数
     * Minimum number of links
     * */
    private int minNodeAmount;

    /**
     * 最大链接数
     * */
    private int maxNodeAmount;

    /**
     * 最大被链接数
     * */
    private int maxInNode;

    /**
     * 跨链交易被打包多少块之后广播给其他链
     * */
    private int sendHeight;

    /**
     * 拜占庭比例
     * */
    private int byzantineRatio;

    /**默认链接到的跨链节点*/
    private String crossSeedIps;


    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMinNodeAmount() {
        return minNodeAmount;
    }

    public void setMinNodeAmount(int minNodeAmount) {
        this.minNodeAmount = minNodeAmount;
    }

    public int getSendHeight() {
        return sendHeight;
    }

    public void setSendHeight(int sendHeight) {
        this.sendHeight = sendHeight;
    }

    public int getByzantineRatio() {
        return byzantineRatio;
    }

    public void setByzantineRatio(int byzantineRatio) {
        this.byzantineRatio = byzantineRatio;
    }

    public int getMaxNodeAmount() {
        return maxNodeAmount;
    }

    public void setMaxNodeAmount(int maxNodeAmount) {
        this.maxNodeAmount = maxNodeAmount;
    }

    public int getMaxInNode() {
        return maxInNode;
    }

    public void setMaxInNode(int maxInNode) {
        this.maxInNode = maxInNode;
    }

    public String getCrossSeedIps() {
        return crossSeedIps;
    }

    public void setCrossSeedIps(String crossSeedIps) {
        this.crossSeedIps = crossSeedIps;
    }
}
