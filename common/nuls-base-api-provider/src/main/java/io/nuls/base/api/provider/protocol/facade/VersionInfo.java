package io.nuls.base.api.provider.protocol.facade;


/**
 * @Author: zhoulijun
 * @Time: 2020-01-15 18:16
 * @Description: Function Description
 */
public class VersionInfo {


    private int localProtocolVersion;

    private int currentProtocolVersion;

    public int getLocalProtocolVersion() {
        return localProtocolVersion;
    }

    public void setLocalProtocolVersion(int localProtocolVersion) {
        this.localProtocolVersion = localProtocolVersion;
    }

    public int getCurrentProtocolVersion() {
        return currentProtocolVersion;
    }

    public void setCurrentProtocolVersion(int currentProtocolVersion) {
        this.currentProtocolVersion = currentProtocolVersion;
    }
}
