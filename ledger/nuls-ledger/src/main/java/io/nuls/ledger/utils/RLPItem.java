package io.nuls.ledger.utils;

/**
 * Created by wangkun23 on 2018/11/30.
 */
public class RLPItem implements RLPElement {

    private final byte[] rlpData;

    public RLPItem(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    @Override
    public byte[] getRLPData() {
        if (rlpData.length == 0) {
            return null;
        }
        return rlpData;
    }
}
