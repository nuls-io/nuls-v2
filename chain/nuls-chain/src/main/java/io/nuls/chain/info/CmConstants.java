package io.nuls.chain.info;

import io.nuls.base.basic.AddressTool;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 */
public class CmConstants {

    private CmConstants() {
    }

    public static final BigInteger ZERO = new BigInteger("0");

    public static final String MODULE_ROLE = "CM";
    public static final String ADDRESS_TYPE_NULS = "nuls";
    public static final String ADDRESS_TYPE_OTHER = "other";


    /**
     * 黑洞地址，该地址的资产无法找回
     */
    public static final byte[] BLACK_HOLE_ADDRESS = AddressTool.getAddress("tNULSeBaMkqeHbTxwKqyquFcbewVTUDHPkF11o");

    public static final int BAK_BLOCK_MAX_COUNT = 500;
}
