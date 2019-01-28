package io.nuls.base.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.script.Script;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2018/11/23
 */
public class CoinTo extends Coin {

    /**
     * uint32,解锁高度或解锁时间，-1为永久锁定
     */
    private long lockTime;

    public CoinTo(){}

    public CoinTo(byte[] address,int assetsChainId,int assetsId,BigInteger amount){
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
        this.amount = amount;
    }

    public CoinTo(byte[] address,int assetsChainId,int assetsId, BigInteger amount,long lockTime){
       this(address,assetsChainId,assetsId,amount);
       this.lockTime = lockTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeUint16(assetsChainId);
        stream.writeUint16(assetsId);
        stream.writeBigInteger(amount);
        stream.writeVarInt(lockTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readByLengthByte();
        this.assetsChainId = byteBuffer.readUint16();
        this.assetsId = byteBuffer.readUint16();
        this.amount = byteBuffer.readBigInteger();
        this.lockTime = byteBuffer.readVarInt();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBytes(address);
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfVarInt(lockTime);
        return size;
    }

    @JsonIgnore
    public byte[] getAddresses() {
        byte[] address = new byte[Address.ADDRESS_LENGTH];
        //如果owner不是存放的脚本则直接返回owner
        if (address == null || address.length == Address.ADDRESS_LENGTH) {
            return address;
        } else {
            Script scriptPubkey = new Script(address);
            //如果为P2PKH类型交易则从第四位开始返回23个字节
            if (scriptPubkey.isSentToAddress()) {
                System.arraycopy(address, 3, address, 0, Address.ADDRESS_LENGTH);
            }
            //如果为P2SH或multi类型的UTXO则从第三位开始返回23个字节
            else if (scriptPubkey.isPayToScriptHash()) {
                scriptPubkey.isSentToMultiSig();
                System.arraycopy(address, 2, address, 0, Address.ADDRESS_LENGTH);
            }else{
                throw new NulsRuntimeException(new Exception());
            }
        }
        return address;
    }

    public boolean usable(Long bestHeight) {
        if (lockTime < 0) {
            return false;
        }
        if (lockTime == 0) {
            return true;
        }

        long currentTime = TimeService.currentTimeMillis();

        if (lockTime > BaseConstant.BlOCKHEIGHT_TIME_DIVIDE) {
            if (lockTime <= currentTime) {
                return true;
            } else {
                return false;
            }
        } else {
            if (lockTime <= bestHeight) {
                return true;
            } else {
                return false;
            }
        }
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
