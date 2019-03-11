/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.base.basic;

import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.tools.crypto.Base58;
import io.nuls.tools.model.ByteUtils;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.util.Arrays;
import java.util.List;


/**
 * @author: qinyifeng
 */
public class AddressTool {

    public static String UNDERLINE = "_";

    /**
     * 根据地址字符串查询地址字节数组
     *
     * @param addressString
     * @return
     */
    public static byte[] getAddress(String addressString) {
        byte[] result = new byte[Address.ADDRESS_LENGTH];
        try {
            byte[] addressBytes = AddressTool.getAddressBytes(addressString);
            System.arraycopy(addressBytes, 0, result, 0, 23);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return result;
    }

    /**
     * 根据地址字符串查询地址所属链ID
     *
     * @param addressString
     * @return
     */
    public static int getChainIdByAddress(String addressString) {
        int chainId;
        try {
            byte[] addressBytes = AddressTool.getAddressBytes(addressString);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(addressBytes);
            chainId = byteBuffer.readShort();
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return chainId;
    }

    /**
     * 根据公钥查询地址字节数组
     *
     * @param publicKey
     * @param chainId
     * @return
     */
    public static byte[] getAddress(byte[] publicKey, int chainId) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        Address address = new Address(chainId, BaseConstant.DEFAULT_ADDRESS_TYPE, hash160);
        return address.getAddressBytes();
    }

    /**
     * 生成校验位，根据以下字段生成：addressType+hash160(pubKey)
     *
     * @param body
     * @return
     */
    private static byte getXor(byte[] body) {
        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }
        return xor;
    }

    /**
     * 检查校验位是否正确，XOR(addressType+hash160(pubKey))
     *
     * @param hashs
     */
    public static void checkXOR(byte[] hashs) {
        byte[] body = new byte[Address.ADDRESS_ORIGIN_LENGTH];
        System.arraycopy(hashs, 0, body, 0, Address.ADDRESS_ORIGIN_LENGTH);

        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        if (xor != hashs[Address.ADDRESS_ORIGIN_LENGTH]) {
            throw new NulsRuntimeException(new Exception());
        }
    }

    /**
     * 验证地址字符串是否是有效地址
     *
     * @param address
     * @param chainId
     * @return
     */
    public static boolean validAddress(int chainId, String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        byte[] bytes;
        try {
            bytes = AddressTool.getAddressBytes(address);
            if (bytes.length != Address.ADDRESS_LENGTH + 1) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        int chainid;
        byte type;
        byte[] hash160Bytes = new byte[Address.ADDRESS_ORIGIN_LENGTH + 1];
        try {
            chainid = byteBuffer.readShort();
            type = byteBuffer.readByte();
            System.arraycopy(bytes, 2, hash160Bytes, 0, Address.ADDRESS_ORIGIN_LENGTH + 1);
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chainId != chainid) {
            return false;
        }
//        if (BaseConstant.MAIN_NET_VERSION <= 1 && BaseConstant.DEFAULT_ADDRESS_TYPE != type) {
//            return false;
//        }
        if (BaseConstant.DEFAULT_ADDRESS_TYPE != type && BaseConstant.CONTRACT_ADDRESS_TYPE != type && BaseConstant.P2SH_ADDRESS_TYPE != type) {
            return false;
        }
        try {
            checkXOR(hash160Bytes);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 通过地址获得chainId
     *
     * @param bytes
     * @return
     */
    public static int getChainIdByAddress(byte[] bytes) {
        if (null == bytes || bytes.length != Address.ADDRESS_LENGTH) {
            return 0;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        try {
            return (int) byteBuffer.readShort();
        } catch (NulsException e) {
            Log.error(e);
            return 0;
        }

    }

    /**
     * 校验是否是普通地址
     *
     * @param bytes
     * @param chainId
     * @return
     */
    public static boolean validNormalAddress(byte[] bytes, int chainId) {
        if (null == bytes || bytes.length != Address.ADDRESS_LENGTH) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        int chainid;
        byte type;
        try {
            chainid = byteBuffer.readShort();
            type = byteBuffer.readByte();
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chainId != chainid) {
            return false;
        }
        if (BaseConstant.DEFAULT_ADDRESS_TYPE != type) {
            return false;
        }
        return true;
    }

    /**
     * 校验是否是智能合约地址
     *
     * @param addressBytes
     * @param chainId
     * @return
     */
    public static boolean validContractAddress(byte[] addressBytes, int chainId) {
        if (addressBytes == null) {
            return false;
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(addressBytes);
        int chainid;
        byte type;
        try {
            chainid = byteBuffer.readShort();
            type = byteBuffer.readByte();
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chainId != chainid) {
            return false;
        }
        if (BaseConstant.CONTRACT_ADDRESS_TYPE != type) {
            return false;
        }
        return true;
    }

    /**
     * 根据地址字节数组生成地址字符串
     * base58(chainId)+_+base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))
     *
     * @param addressBytes
     * @return
     */
    public static String getStringAddressByBytes(byte[] addressBytes) {
        if (addressBytes == null) {
            return null;
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return null;
        }
        byte[] chainIdByte = new byte[2];
        System.arraycopy(addressBytes, 0, chainIdByte, 0, 2);
        byte[] userTypeByte = new byte[2];
        System.arraycopy(addressBytes, 2, userTypeByte, 0, 1);
        byte[] hash160 = new byte[20];
        System.arraycopy(addressBytes, 3, hash160, 0, 20);

        byte[] body = ByteUtils.concatenate(userTypeByte, hash160);
        byte[] bytes = ByteUtils.concatenate(body, new byte[]{getXor(body)});
        String prefix = Base58.encode(chainIdByte);
        String suffix = Base58.encode(bytes);
        return prefix + "_" + suffix;
        //return Base58.encode(bytes) + HexUtil.encode(chainIdByte);
    }

    /**
     * 根据地址字符串解码出地址原始字节数组
     * base58(chainId)+_+base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))
     * addressType在原始数据后补位0
     *
     * @param addressString
     * @return
     */
    private static byte[] getAddressBytes(String addressString) {
        byte[] result;// = new byte[Address.ADDRESS_LENGTH + 1];
        byte[] chainIdBytes;
        byte[] addressTypeBytes;
        byte[] hash160Bytes = new byte[Address.ADDRESS_ORIGIN_LENGTH];
        try {
            chainIdBytes = Base58.decode(addressString.split(UNDERLINE)[0]);
            byte[] body = Base58.decode(addressString.split(UNDERLINE)[1]);
            addressTypeBytes = new byte[]{body[0]};
            System.arraycopy(body, 2, hash160Bytes, 0, 21);
            result = ByteUtils.concatenate(chainIdBytes, addressTypeBytes, hash160Bytes);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return result;
    }

    public static boolean checkPublicKeyHash(byte[] address, byte[] pubKeyHash) {

        if (address == null || pubKeyHash == null) {
            return false;
        }
        int pubKeyHashLength = pubKeyHash.length;
        if (address.length != Address.ADDRESS_LENGTH || pubKeyHashLength != 20) {
            return false;
        }
        for (int i = 0; i < pubKeyHashLength; i++) {
            if (pubKeyHash[i] != address[i + 3]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMultiSignAddress(byte[] addr) {
        if (addr != null && addr.length > 3) {
            return addr[2] == BaseConstant.P2SH_ADDRESS_TYPE;
        }
        return false;
    }

    public static boolean isMultiSignAddress(String address) {
        byte[] addr = AddressTool.getAddress(address);
        return isMultiSignAddress(addr);
    }

    public static boolean isPackingAddress(String address, int chainId) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        byte[] bytes;
        try {
            bytes = AddressTool.getAddressBytes(address);
            if (bytes.length != Address.ADDRESS_LENGTH + 1) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        int chainid;
        byte type;
        byte[] hash160Bytes = new byte[Address.ADDRESS_ORIGIN_LENGTH + 1];
        try {
            chainid = byteBuffer.readShort();
            type = byteBuffer.readByte();
            System.arraycopy(bytes, 2, hash160Bytes, 0, Address.ADDRESS_ORIGIN_LENGTH + 1);
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chainId != chainid) {
            return false;
        }
        if (BaseConstant.DEFAULT_ADDRESS_TYPE != type) {
            return false;
        }
        try {
            checkXOR(hash160Bytes);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean validSignAddress(List<byte[]> bytesList, byte[] bytes) {
        if (bytesList == null || bytesList.size() == 0 || bytes == null) {
            return false;
        } else {
            for (byte[] tempBytes : bytesList) {
                if (Arrays.equals(bytes, tempBytes)) {
                    return true;
                }
            }
        }
        return false;
    }

}
