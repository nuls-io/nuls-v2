package io.nuls.ledger.utils;

import java.io.Serializable;

/**
 * Wrapper class for decoded elements from an RLP encoded byte array.
 * <p>
 * Created by wangkun23 on 2018/11/30.
 */
public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
