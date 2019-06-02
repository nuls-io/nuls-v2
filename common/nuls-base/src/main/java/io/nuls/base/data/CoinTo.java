package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2018/11/23
 */
public class CoinTo extends Coin {

    /**
     * 解锁时间，-1为永久锁定
     */
    private long lockTime;

    public CoinTo(){}

    public CoinTo(byte[] address,int assetsChainId,int assetsId,BigInteger amount){
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
        this.amount = amount;
    }

    public CoinTo(byte[] address,int assetsChainId,int assetsId, BigInteger amount, long lockTime){
       this(address,assetsChainId,assetsId,amount);
       this.lockTime = lockTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeUint16(assetsChainId);
        stream.writeUint16(assetsId);
        stream.writeBigInteger(amount);
        stream.writeInt64(lockTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readByLengthByte();
        this.assetsChainId = byteBuffer.readUint16();
        this.assetsId = byteBuffer.readUint16();
        this.amount = byteBuffer.readBigInteger();
        this.lockTime = byteBuffer.readInt64();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBytes(address);
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfInt64();
        return size;
    }


    @Override
    public String toString() {
        return "CoinTo{" +
                "address=" + AddressTool.getStringAddressByBytes(address) +
                ", assetsChainId=" + assetsChainId +
                ", assetsId=" + assetsId +
                ", amount=" + amount +
                ", lockTime=" + lockTime +
                '}';
    }


    @Override
    public byte[] getAddress() {
        return address;
    }

    @Override
    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    public int getAssetsChainId() {
        return assetsChainId;
    }

    @Override
    public void setAssetsChainId(int assetsChainId) {
        this.assetsChainId = assetsChainId;
    }

    @Override
    public int getAssetsId() {
        return assetsId;
    }

    @Override
    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    @Override
    public BigInteger getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
