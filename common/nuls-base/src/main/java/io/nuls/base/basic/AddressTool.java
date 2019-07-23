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

import io.nuls.base.data.Address;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.crypto.Base58;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.SerializeUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: qinyifeng
 */
public class AddressTool {

    public static final String MAINNET_PREFIX = BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX;
    public static final String TESTNET_PREFIX = BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX;

    private static final String ERROR_MESSAGE = "Address prefix can not be null!";
    private static final String[] LENGTHPREFIX = new String[]{"", "a", "b", "c", "d", "e"};
    private static final Map<Integer, byte[]> BLACK_HOLE_ADDRESS_MAP = new HashMap<>();

    public static String getPrefix(String addressString) {
        if (addressString.startsWith(MAINNET_PREFIX)) {
            return MAINNET_PREFIX;
        }
        if (addressString.startsWith(TESTNET_PREFIX)) {
            return TESTNET_PREFIX;
        }
        char[] arr = addressString.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char val = arr[i];
            if (val >= 97) {
                return addressString.substring(0, i);
            }
        }
        throw new RuntimeException(ERROR_MESSAGE);
    }

    private static String getRealAddress(String addressString) {
        if (addressString.startsWith(MAINNET_PREFIX)) {
            return addressString.substring(MAINNET_PREFIX.length() + 1);
        }
        if (addressString.startsWith(TESTNET_PREFIX)) {
            return addressString.substring(TESTNET_PREFIX.length() + 1);
        }
        char[] arr = addressString.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char val = arr[i];
            if (val >= 97) {
                return addressString.substring(i + 1);
            }
        }
        throw new RuntimeException(ERROR_MESSAGE);
    }

    /**
     * 根据地址字符串查询地址字节数组
     *
     * @param addressString
     * @return
     */
    public static byte[] getAddress(String addressString) {
        try {
            return AddressTool.getAddressBytes(addressString);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
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
            chainId = byteBuffer.readUint16();
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
        if (chainId == 1) {
            return getAddress(publicKey, chainId, "NULS");
        } else if (chainId == 2) {
            return getAddress(publicKey, chainId, "tNULS");
        }
        return getAddress(publicKey, chainId, Base58.encode(SerializeUtils.int16ToBytes(chainId)).toUpperCase());
    }

    /**
     * 根据公钥查询地址字节数组
     *
     * @param publicKeyStr
     * @param chainId
     * @return
     */
    public static byte[] getAddressByPubKeyStr(String publicKeyStr, int chainId, String charsetName) {
        byte[] publicKey = ByteUtils.toBytes(publicKeyStr, charsetName);
        return getAddress(publicKey,chainId);
    }

    /**
     * @param blackHolePublicKey
     * @param chainId
     * @param address
     * @return
     */
    public static boolean isBlackHoleAddress(byte[] blackHolePublicKey, int chainId, byte[] address) {
        byte[] blackHoleAddress = BLACK_HOLE_ADDRESS_MAP.computeIfAbsent(chainId, k -> getAddress(blackHolePublicKey, chainId));
        return Arrays.equals(blackHoleAddress, address);
    }

    public static byte[] getAddress(byte[] publicKey, int chainId, String prefix) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        Address address = new Address(chainId, prefix, BaseConstant.DEFAULT_ADDRESS_TYPE, hash160);
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
        byte[] body = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(hashs, 0, body, 0, Address.ADDRESS_LENGTH);

        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        if (xor != hashs[Address.ADDRESS_LENGTH]) {
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
        byte[] body;
        try {
            String subfix = getRealAddress(address);
            body = Base58.decode(subfix);
            bytes = new byte[body.length - 1];
            System.arraycopy(body, 0, bytes, 0, body.length - 1);
            if (body.length != Address.ADDRESS_LENGTH + 1) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        int chainid;
        byte type;
        byte[] hash160Bytes;
        try {
            chainid = byteBuffer.readUint16();
            type = byteBuffer.readByte();
            hash160Bytes = byteBuffer.readBytes(Address.RIPEMD160_LENGTH);
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
            checkXOR(body);
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
            return byteBuffer.readUint16();
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
            chainid = byteBuffer.readUint16();
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
            chainid = byteBuffer.readUint16();
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
        int chainId = getChainIdByAddress(addressBytes);
        if (BaseConstant.MAINNET_CHAIN_ID == chainId) {
            return getStringAddressByBytes(addressBytes, MAINNET_PREFIX);
        } else if (chainId == BaseConstant.TESTNET_CHAIN_ID) {
            return getStringAddressByBytes(addressBytes, TESTNET_PREFIX);
        } else {
            return getStringAddressByBytes(addressBytes, Base58.encode(SerializeUtils.int16ToBytes(chainId)).toUpperCase());
        }
    }

    public static String getStringAddressByBytes(byte[] addressBytes, String prefix) {
        if (addressBytes == null) {
            return null;
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return null;
        }

        byte[] bytes = ByteUtils.concatenate(addressBytes, new byte[]{getXor(addressBytes)});
        return prefix + LENGTHPREFIX[prefix.length()] + Base58.encode(bytes);
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
        byte[] result;
        try {
            String address = getRealAddress(addressString);
            byte[] body = Base58.decode(address);
            result = new byte[body.length - 1];
            System.arraycopy(body, 0, result, 0, body.length - 1);
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

    public static boolean isNormalAddress(String address, int chainId) {
        byte[] bytes;
        byte[] body;
        try {
            String subfix = getRealAddress(address);
            body = Base58.decode(subfix);
            bytes = new byte[body.length - 1];
            System.arraycopy(body, 0, bytes, 0, body.length - 1);
            if (body.length != Address.ADDRESS_LENGTH + 1) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        int chainid;
        byte type;
        byte[] hash160Bytes;
        try {
            chainid = byteBuffer.readUint16();
            type = byteBuffer.readByte();
            hash160Bytes = byteBuffer.readBytes(Address.RIPEMD160_LENGTH);
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
        if (BaseConstant.DEFAULT_ADDRESS_TYPE != type) {
            return false;
        }
        try {
            checkXOR(body);
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
