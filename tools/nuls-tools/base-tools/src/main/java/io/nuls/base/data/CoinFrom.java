package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author: Charlie
 * @date: 2018/11/23
 */
public class CoinFrom extends BaseNulsData {

    /**
     * byte[24] 账户地址
     */
    private byte[] address;

    /**
     * uint16 资产发行链的id
     */
    private int assetsChainId;

    /**
     * uint16 资产id
     */
    private int assetsId;

    /**
     * 转出数量
     */
    private String amount;

    /**
     * uint32 交易顺序号，递增
     */
    private byte[] nonce;

    /**
     * 0普通交易，-1解锁金额交易（退出共识，退出委托）
     */
    private long lockTime;

    public  CoinFrom(){}

    public CoinFrom(byte[] address,int assetsChainId,int assetsId){
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
    }

    public CoinFrom(byte[] address,int assetsChainId,int assetsId,String amount,long lockTime){
        this(address,assetsChainId,assetsId);
        this.amount = amount;
        this.lockTime = lockTime;
    }

    public CoinFrom(byte[] address,int assetsChainId,int assetsId,String amount,byte[] nonce,long lockTime){
        this(address,assetsChainId,assetsId,amount,lockTime);
        this.nonce = nonce;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeUint16(assetsChainId);
        stream.writeUint16(assetsId);
        stream.writeString(amount);
        stream.writeBytesWithLength(nonce);
        stream.writeUint48(lockTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readByLengthByte();
        this.assetsChainId = byteBuffer.readUint16();
        this.assetsId = byteBuffer.readUint16();
        this.amount = byteBuffer.readString();
        this.nonce = byteBuffer.readByLengthByte();
        this.lockTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBytes(address);
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(amount);
        size += SerializeUtils.sizeOfBytes(nonce);
        size += SerializeUtils.sizeOfUint48();
        return size;
    }

    @Override
    public String toString() {
        return "CoinTo{" +
                "address=" + AddressTool.getStringAddressByBytes(address) +
                ", assetsChainId=" + assetsChainId +
                ", assetsId=" + assetsId +
                ", amount=" + amount +
                ", nonce=" + nonce +
                '}';
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public int getAssetsChainId() {
        return assetsChainId;
    }

    public void setAssetsChainId(int assetsChainId) {
        this.assetsChainId = assetsChainId;
    }

    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
