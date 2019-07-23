package io.nuls.poc.model.po;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

/**
 * @author Niels
 */
public class RandomSeedStatusPo extends BaseNulsData {

    private byte[] address;
    private long height;

    private byte[] seedHash;

    private byte[] nextSeed;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public byte[] getNextSeed() {
        return nextSeed;
    }

    public void setNextSeed(byte[] nextSeed) {
        this.nextSeed = nextSeed;
    }

    public byte[] getSeedHash() {
        return seedHash;
    }

    public void setSeedHash(byte[] seedHash) {
        this.seedHash = seedHash;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(height);
        stream.write(seedHash);
        if (null != nextSeed) {
            stream.write(nextSeed);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        height = byteBuffer.readInt64();
        seedHash = byteBuffer.readBytes(8);
        if (!byteBuffer.isFinished()) {
            nextSeed = byteBuffer.readBytes(32);
        }
    }

    @Override
    public int size() {
        int size = 48;
        if (null == nextSeed) {
            size = 16;
        }
        return size;
    }

    @Override
    public String toString() {
        String result = "{" +
                "address=" + AddressTool.getStringAddressByBytes(address) +
                ", height=" + height +
                ", seedHash=" + Hex.encode(seedHash);
        if (null != nextSeed) {
            result += ", nextSeed=" + Hex.encode(nextSeed);
        }
        result += '}';
        return result;
    }
}