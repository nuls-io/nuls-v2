package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

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
     * uint128 转出数量
     */
    private BigInteger amount;

    /**
     * byte[8]
     */
    private byte[] nonce;

    /**
     * 0普通交易，-1解锁金额交易（退出共识，退出委托）
     */
    private byte locked;


    public  CoinFrom(){}

    public CoinFrom(byte[] address,int assetsChainId,int assetsId){
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
    }

    public CoinFrom(byte[] address,int assetsChainId,int assetsId, BigInteger amount,byte locked){
        this(address,assetsChainId,assetsId);
        this.amount = amount;
        this.locked = locked;
    }

    public CoinFrom(byte[] address,int assetsChainId,int assetsId, BigInteger amount,byte[] nonce,byte locked){
        this(address,assetsChainId,assetsId,amount,locked);
        this.nonce = nonce;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeUint16(assetsChainId);
        stream.writeUint16(assetsId);
        stream.writeBigInteger(amount);
        stream.writeBytesWithLength(nonce);
        stream.write(locked);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readByLengthByte();
        this.assetsChainId = byteBuffer.readUint16();
        this.assetsId = byteBuffer.readUint16();
        this.amount = byteBuffer.readBigInteger();
        this.nonce = byteBuffer.readByLengthByte();
        this.locked = byteBuffer.readByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += Address.ADDRESS_LENGTH;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfNonce();
        size += 1;
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
                ", locked=" + locked +
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

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {

        this.nonce = nonce;
    }

    public byte getLocked() {
        return locked;
    }

    public void setLocked(byte locked) {
        this.locked = locked;
    }

}
