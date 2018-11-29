package io.nuls.ledger.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.ledger.utils.RLP;
import io.nuls.tools.data.LongUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountState extends BaseNulsData {

    @Setter
    @Getter
    private int chainId;

    @Setter
    @Getter
    private int assetId;

    @Setter
    @Getter
    private long nonce;

    @Setter
    @Getter
    private long balance;

    /**
     * 账户总金额
     */
    private long totalAmount;

    /**
     * 账户冻结的资产
     */
    @Setter
    @Getter
    private FreezeState freezeState;

    @Setter
    @Getter
    private byte[] rlpEncoded;

    public AccountState(int chainId, int assetId, long nonce, long balance) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.nonce = nonce;
        this.balance = balance;
        this.freezeState = new FreezeState();
    }

    public AccountState(byte[] rlpData) {
        this.rlpEncoded = rlpData;
//        RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
//        this.nonce = ByteUtil.bytesToBigInteger(items.get(0).getRLPData());
//        this.balance = ByteUtil.bytesToBigInteger(items.get(1).getRLPData());
//        this.stateRoot = items.get(2).getRLPData();
//        this.codeHash = items.get(3).getRLPData();
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] chainId = RLP.encodeInt(this.chainId);
            byte[] assetId = RLP.encodeInt(this.assetId);
            byte[] nonce = RLP.encodeBigInteger(BigInteger.valueOf(this.nonce));
            byte[] balance = RLP.encodeBigInteger(BigInteger.valueOf(this.balance));
            byte[] freezeState = this.freezeState.getEncoded();
            this.rlpEncoded = RLP.encodeList(chainId, assetId, nonce, balance, freezeState);
        }
        return rlpEncoded;
    }

    /**
     * 获取账户总金额
     *
     * @return
     */
    public long getTotalAmount() {
        return LongUtils.add(balance, freezeState.getTotal());
    }

    public AccountState withNonce(long nonce) {
        return new AccountState(chainId, assetId, nonce, balance);
    }

    public AccountState withIncrementedNonce() {
        return new AccountState(chainId, assetId, nonce + 1, balance);
    }

    public AccountState withBalanceIncrement(long value) {
        return new AccountState(chainId, assetId, nonce, balance + value);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeUint32(nonce);
        stream.writeUint32(balance);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.nonce = byteBuffer.readUint32();
        this.balance = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        return size;
    }
}
