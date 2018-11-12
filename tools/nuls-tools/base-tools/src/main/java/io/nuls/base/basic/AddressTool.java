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
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;


/**
 * @author: Niels Wang
 */
public class AddressTool {

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
     * 根据公钥查询地址字节数组
     *
     * @param publicKey
     * @param chain_id
     * @return
     */
    public static byte[] getAddress(byte[] publicKey, short chain_id) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        Address address = new Address(chain_id, BaseConstant.DEFAULT_ADDRESS_TYPE, hash160);
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
     * @param chain_id
     * @return
     */
    public static boolean validAddress(short chain_id, String address) {
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
        short chainId;
        byte type;
        byte[] hash160Bytes = new byte[Address.ADDRESS_ORIGIN_LENGTH + 1];
        try {
            chainId = byteBuffer.readShort();
            type = byteBuffer.readByte();
            System.arraycopy(bytes, 2, hash160Bytes, 0, Address.ADDRESS_ORIGIN_LENGTH + 1);
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chain_id != chainId) {
            return false;
        }
        if (BaseConstant.MAIN_NET_VERSION <= 1 && BaseConstant.DEFAULT_ADDRESS_TYPE != type) {
            return false;
        }
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
     * 校验是否是普通地址
     *
     * @param bytes
     * @param chain_id
     * @return
     */
    public static boolean validNormalAddress(byte[] bytes, short chain_id) {
        if (null == bytes || bytes.length != Address.ADDRESS_LENGTH) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
        short chainId;
        byte type;
        try {
            chainId = byteBuffer.readShort();
            type = byteBuffer.readByte();
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chain_id != chainId) {
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
     * @param chain_id
     * @return
     */
    public static boolean validContractAddress(byte[] addressBytes, short chain_id) {
        if (addressBytes == null) {
            return false;
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return false;
        }
        NulsByteBuffer byteBuffer = new NulsByteBuffer(addressBytes);
        short chainId;
        byte type;
        try {
            chainId = byteBuffer.readShort();
            type = byteBuffer.readByte();
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chain_id != chainId) {
            return false;
        }
        if (BaseConstant.CONTRACT_ADDRESS_TYPE != type) {
            return false;
        }
        return true;
    }

    /**
     * 根据地址字节数组生成地址字符串
     * base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))+Hex(chianId)
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
        byte[] chianIdByte = new byte[2];
        System.arraycopy(addressBytes, 0, chianIdByte, 0, 2);
        byte[] body = new byte[21];
        System.arraycopy(addressBytes, 2, body, 0, 1);
        System.arraycopy(addressBytes, 3, body, 1, 20);

        byte[] bytes = ByteUtils.concatenate(body, new byte[]{getXor(body)});
        return Base58.encode(bytes) + HexUtil.encode(chianIdByte);
    }

    /**
     * 根据地址字符串解码出地址原始字节数组
     * base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))+Hex(chianId)
     *
     * @param addressString
     * @return
     */
    private static byte[] getAddressBytes(String addressString) {
        byte[] result = new byte[Address.ADDRESS_LENGTH + 1];
        byte[] chainIdBytes;
        byte[] hash160Bytes;
        try {
            int length = addressString.length();
            String hash160 = addressString.substring(0, length - 4);
            String chainIdHex = addressString.substring(length - 4, length);
            chainIdBytes = HexUtil.decode(chainIdHex);
            hash160Bytes = Base58.decode(hash160);

            System.arraycopy(chainIdBytes, 0, result, 0, 2);
            System.arraycopy(hash160Bytes, 0, result, 2, 22);
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

    public static boolean isPay2ScriptHashAddress(byte[] addr) {
        if (addr != null && addr.length > 3) {
            return addr[2] == BaseConstant.P2SH_ADDRESS_TYPE;
        }

        return false;
    }

    public static boolean isPackingAddress(String address, short chain_id) {
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
        short chainId;
        byte type;
        byte[] hash160Bytes = new byte[Address.ADDRESS_ORIGIN_LENGTH + 1];
        try {
            chainId = byteBuffer.readShort();
            type = byteBuffer.readByte();
            System.arraycopy(bytes, 2, hash160Bytes, 0, Address.ADDRESS_ORIGIN_LENGTH + 1);
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (chain_id != chainId) {
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

}
