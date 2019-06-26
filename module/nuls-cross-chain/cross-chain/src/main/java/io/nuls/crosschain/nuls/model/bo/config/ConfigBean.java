package io.nuls.crosschain.nuls.model.bo.config;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 跨链模块配置类
 * Consensus Module Configuration Class
 *
 * @author tag
 * 2019/4/10
 */
public class ConfigBean extends BaseNulsData {
    /**
     * 资产ID
     * assets id
     */
    private int assetId;

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

    /**
     * 最小签名数
     * */
    private int minSignature;

    /**
     * 主网验证人信息
     * */
    private String verifiers;


    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
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

    public int getMinSignature() {
        return minSignature;
    }

    public void setMinSignature(int minSignature) {
        this.minSignature = minSignature;
    }

    public String getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(String verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(assetId);
        stream.writeUint16(chainId);
        stream.writeUint16(minNodeAmount);
        stream.writeUint16(maxNodeAmount);
        stream.writeUint16(maxInNode);
        stream.writeUint16(sendHeight);
        stream.writeUint16(byzantineRatio);
        stream.writeString(crossSeedIps);
        stream.writeUint16(minSignature);
        stream.writeString(verifiers);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assetId = byteBuffer.readUint16();
        this.chainId = byteBuffer.readUint16();
        this.minNodeAmount = byteBuffer.readUint16();
        this.maxNodeAmount = byteBuffer.readUint16();
        this.maxInNode = byteBuffer.readUint16();
        this.sendHeight = byteBuffer.readUint16();
        this.byzantineRatio = byteBuffer.readUint16();
        this.crossSeedIps = byteBuffer.readString();
        this.minNodeAmount = byteBuffer.readUint16();
        this.verifiers = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16() * 8;
        size += SerializeUtils.sizeOfString(crossSeedIps);
        size += SerializeUtils.sizeOfString(verifiers);
        return size;
    }
}
