package io.nuls.test.cases;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:27
 * @Description: 功能描述
 */
public class Constants {

    public static final String PASSWORD = "nuls123456";

    /**
     * 10NULS
     */
    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(1000000000L);

    public static final String REMARK = "test_remark";

    public static String getAlias(String address){
        return address.substring(address.length() - 10).toLowerCase();
    }

}
