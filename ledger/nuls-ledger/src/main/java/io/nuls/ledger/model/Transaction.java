package io.nuls.ledger.model;

import io.nuls.tools.crypto.ECKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by wangkun23 on 2018/11/28.
 */
@ToString
public class Transaction {

    @Setter
    @Getter
    private byte[] hash;
    @Setter
    @Getter
    private byte[] nonce;
    @Setter
    @Getter
    private byte[] value;
    @Setter
    @Getter
    private byte[] receiveAddress;
    @Setter
    @Getter
    private byte[] data;
    @Setter
    @Getter
    protected byte[] sendAddress;
    @Setter
    @Getter
    private ECKey.ECDSASignature signature;
}
