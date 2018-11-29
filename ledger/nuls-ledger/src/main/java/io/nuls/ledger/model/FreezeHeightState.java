package io.nuls.ledger.model;

import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * account balance lock
 * Created by wangkun23 on 2018/11/21.
 */
@ToString
@NoArgsConstructor
public class FreezeHeightState implements Serializable {

    /**
     * 交易的hash值
     */
    @Setter
    @Getter
    private String txHash;

    /**
     * 锁定金额
     */
    @Setter
    @Getter
    private long amount;

    /**
     * 锁定时间
     */
    @Setter
    @Getter
    private long height;

    @Setter
    @Getter
    private long createTime;

    @Setter
    @Getter
    protected byte[] rlpEncoded;

    public FreezeHeightState(byte[] rawData) {
        this.rlpEncoded = rawData;

        try {
            RLPList decodedList = RLP.decode2(rlpEncoded);
            RLPList items = (RLPList) decodedList.get(0);

            byte[] txHashBytes = items.get(0).getRLPData();
            byte[] amountBytes = items.get(1).getRLPData();
            byte[] heightBytes = items.get(2).getRLPData();
            byte[] createTimeBytes = items.get(3).getRLPData();

            this.txHash = new String(txHashBytes);
            this.amount = ByteUtil.byteArrayToLong(amountBytes);
            this.height = ByteUtil.byteArrayToLong(heightBytes);
            this.createTime = ByteUtil.byteArrayToLong(createTimeBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    public byte[] getEncoded() {
        if (rlpEncoded != null) {
            return rlpEncoded;
        }
        byte[] txHash = RLP.encodeString(this.txHash);
        byte[] amount = RLP.encodeBigInteger(BigInteger.valueOf(this.amount));
        byte[] height = RLP.encodeBigInteger(BigInteger.valueOf(this.height));
        byte[] createTime = RLP.encodeBigInteger(BigInteger.valueOf(this.createTime));
        this.rlpEncoded = RLP.encodeList(txHash, amount, height, createTime);
        return rlpEncoded;
    }
}
