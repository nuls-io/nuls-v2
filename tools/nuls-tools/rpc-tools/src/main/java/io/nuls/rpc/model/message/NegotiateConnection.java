package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class NegotiateConnection {
    private String protocolVersion;
    private String compressionAlgorithm;
    private int compressionRate;

    public NegotiateConnection() {
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(String compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public int getCompressionRate() {
        return compressionRate;
    }

    public void setCompressionRate(int compressionRate) {
        this.compressionRate = compressionRate;
    }
}
