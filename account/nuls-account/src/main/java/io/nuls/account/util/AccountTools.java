package io.nuls.account.util;

import io.nuls.tools.crypto.Base58;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.parse.SerializeUtils;

/**
 * @author Niels
 */
public class AccountTools {

    public static final String createAddress(short chainId, byte type, ECKey ecKey) {
        String prefix = Base58.encode(SerializeUtils.int16ToBytes(chainId));
        byte[] useType = SerializeUtils.int16ToBytes(type);
        byte[] hash160 = SerializeUtils.sha256hash160(ecKey.getPubKey());
        String suffix = Base58.encode(concatenate(useType, hash160));
        return prefix + "_" + suffix;
    }

    public static final byte getType(String address) {
        //todo
        return 0;
    }


    /**
     * 按照传入的顺序拼接数组为一个包含所有数组的大数组
     * Splices the array into a large array containing all of the arrays in the incoming order.
     *
     * @param arrays 想要拼接的数组集合、A collection of arrays that you want to concatenate.
     * @return 拼接结果、 the result of the Joining together
     */
    public static final byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] t = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, t, offset, array.length);
            offset += array.length;
        }
        return t;
    }
}
