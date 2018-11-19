package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class Seed extends BaseNulsData {

    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(ip);
        stream.writeUint32(port);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.ip = byteBuffer.readString();
        this.port = byteBuffer.readInt32();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(ip);
        size += SerializeUtils.sizeOfInt32();
        return size;
    }
}
