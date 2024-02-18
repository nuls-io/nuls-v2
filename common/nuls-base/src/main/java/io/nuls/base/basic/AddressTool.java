/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.data.Address;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.crypto.Base58;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.SerializeUtils;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author: qinyifeng
 */
public class AddressTool {
    private static AddressPrefixInf addressPrefixToolsInf = null;
    private static final String ERROR_MESSAGE = "Address prefix can not be null!";
    private static final String[] LENGTHPREFIX = new String[]{"", "a", "b", "c", "d", "e"};
    private static final Map<Integer, byte[]> BLACK_HOLE_ADDRESS_MAP = new ConcurrentHashMap<>();
    public static Set<String> BLOCK_HOLE_ADDRESS_SET = new HashSet<>();
    public static Set<String> BLOCK_HOLE_ADDRESS_SET_5 = new HashSet<>();

    static {
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgWAwX7MbvcFSLYqMoyn88d5x3AcUww");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgaXkFL7uYEhvC8zqjkYDNY5GQwrHos");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgdZh1GWTN7a6P92zThPC77EuDPt3N2");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgjVAEJi5ZZZs7hyrPt4xMv3uGTucGj");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfSqGPCh97oXmGDD9rQqodNZbLivzc");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgiVWU4LMyQts6YXPFYsuQz8vDZss7J");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfHVsVx1DYv9RouqKUyKnx6qwDtQMr");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgU1DEYKJMwMnvxQmwY9C4CAMFsEGov");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgX6CRCL8PnDU2rWCDWAo45Bv3nnHJa");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HghrwiGsdkvPLk6h3AmgiEayK9XgPcD");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgexB2yTuB3UEvo1z62V2XeHeksWnNf");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgTwwcmqb1AAxXFMjHQtwx5r96bjarV");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZn6h34EdiPZW3uftAwktqaxEP7Jr1");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgYq9bZFPPYAT2AGfskNd2xBocE9DDG");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HggLHaWAgr57BzAujWujK7cnkdfUpDv");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgY2RHpL5qUzSZY3e5dDXTS7kKTwwkE");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZn9PCmNMYEVLktW12NojNymk6JoFD");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgcjP6h7xbVgBzTXEWVKSZrAW5JMgEe");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HghNkGyb8XHcFrsLj9mdxyHB3dwqLYh");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgXjnzZjPQUqhxdsDnGQxinDPJ1wyUU");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HghLcS3B6kAc9929wSSX6F2gxTsCfjF");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfUkXZduCFcWieYA1t9sieEnz7jjxL");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgeLcgDeAU3fJpwwf196kQdtJ6WhAyP");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfTFrdqGCiB1SXZY5WZBjSUtgPLVCJ");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hggmxe4LDEcuYkfUVxJboJFdMA9vS2m");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZfkCMz4oVMm2Dp9qPu53zL9XpMDGo");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgiW5AGRUqshNb6TqEZraRg1QUMaQ44");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgaFggHtSTyBA9H8uCJo14FEcivvXzf");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgiUvUtuyo2AK8xaXKBR7byo5AuK34T");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgiCnpKWd22i3FKqy4EZSRcvyDGKWNb");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgbvthvopoJjwcs8Y9xQbMTTgwLFjoz");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgeLxxtebLWDhTuULMHe1NFTPDo6swg");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hgd9RipQqzW19cpymhbLW1Pjeq4WkJA");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgaAqUVTRWGGsdz3AhzGKAshUbRQiKX");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hgeujp7b2Eox8TZzBHTfQYERzaMmCot");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgWQJbdYVjA3NUsrVP9g8hbvimJsKLr");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgjYpHJXbZ5mpzvRgG923nssNPRP188");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgWCZHGDuAHgRnJSjoLhXoDmPFv5Sbm");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgaYBps2XoqtGTJBp95Q3ET6M87oD36");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgX6tbavRbQBzYdiUAWG1BW8gzSmFq9");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgVtCN6CpceEKhRexa1zCJw6sXdBtqM");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HghSFFmFHPECZXrqv1Jo1ZmUZBbwuFM");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgdeVgSdz6653mWv6VdQeoUMkEuYGgi");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HggqMzNMQ1khS6sHxmPss79pCYPPEgS");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfqCPU5AdwCbxQK62hd4q4USP27nAY");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgVwvoGs69TfvUNcPm5W4x9UYcW1QdR");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgXjfFD5HLYLm7vcNxjCM9x3iVZSvWR");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfY5oVvjkCLz7vyBRdPQXppCcRxkCR");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hgd9mLzsEjGHFQ8igyzUEiGxCzG7mwg");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hgh3NYZAnsEqb83iYzzHrKxpZQNpbyS");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6Hgib3NyJWaGcNeMUyrAwBRhNp2zXYzi");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgaHjr4Z7GdnGc3vNLgFvXWP55hzNNX");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZdGvSnxFPLHrLzpVzda4gtWvHKibT");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZSxNKeFG7AwEL8MVDGZa1KwE39fPP");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgYLpfpPuZV9umGvYjJJX7EkKsUVukS");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgcCf8NfvCBjmdvDWbanWL5cFYiB8DM");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgZwFFYvEKppidUp7irjQFKADspQXaX");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfNf8oRpKL8gsiFsR9ZwXVebHrcDAz");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgfuE7eRNu4wJrQV6fXKupuioL9cKri");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgWZmG6MSNighAjziatrvuhM1LpCF9o");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgbjaWQZrq3CoDZEed9RwAU3zyTeUAP");
        BLOCK_HOLE_ADDRESS_SET.add("NULSd6HgawRRELcuKfvf4TeyidGLqFsHo9hgM");
        BLOCK_HOLE_ADDRESS_SET_5.add("NULSd6HgfYpp9kfgJU5z5q5D4BRHw4rrbGVH3");
        BLOCK_HOLE_ADDRESS_SET_5.add("NULSd6HgjYXoMgRa89jDeHcEV7B5JCEhhWqm2");
    }
    /**
     * chainId-Address Mapping Table
     */
    private static Map<Integer, String> ADDRESS_PREFIX_MAP = new HashMap<Integer, String>();

    public static Map<Integer, String> getAddressPreFixMap() {
        return ADDRESS_PREFIX_MAP;
    }

    public static void addPrefix(int chainId, String prefix) {
        if (chainId == BaseConstant.MAINNET_CHAIN_ID || chainId == BaseConstant.TESTNET_CHAIN_ID) {
            ADDRESS_PREFIX_MAP.put(chainId, prefix);
        } else {
            ADDRESS_PREFIX_MAP.put(chainId, prefix.toUpperCase());
        }
    }

    public static void init(AddressPrefixInf addressPrefixInf) {
        addressPrefixToolsInf = addressPrefixInf;
    }

    public static String getPrefix(int chainId) {
        if (chainId == BaseConstant.MAINNET_CHAIN_ID) {
            return BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX;
        } else if (chainId == BaseConstant.TESTNET_CHAIN_ID) {
            return BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX;
        } else {
            if (null == ADDRESS_PREFIX_MAP.get(chainId) && null != addressPrefixToolsInf) {
                ADDRESS_PREFIX_MAP.putAll(addressPrefixToolsInf.syncAddressPrefix());
            }
            if (null == ADDRESS_PREFIX_MAP.get(chainId)) {
                return Base58.encode(SerializeUtils.int16ToBytes(chainId)).toUpperCase();
            } else {
                return ADDRESS_PREFIX_MAP.get(chainId);
            }
        }
    }

    public static String getPrefix(String address) {
        if (address.startsWith(BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX)) {
            return BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX;
        }
        if (address.startsWith(BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX)) {
            return BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX;
        }
        char[] arr = address.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char val = arr[i];
            if (val >= 97) {
                return address.substring(0, i);
            }
        }
        throw new RuntimeException(ERROR_MESSAGE);
    }

    public static String getRealAddress(String addressString) {
        if (addressString.startsWith(BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX)) {
            return addressString.substring(BaseConstant.TESTNET_DEFAULT_ADDRESS_PREFIX.length() + 1);
        }
        if (addressString.startsWith(BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX)) {
            return addressString.substring(BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX.length() + 1);
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
     * Query address byte array based on address string
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
     * Query address byte array based on public key
     *
     * @param publicKey
     * @param chainId
     * @return
     */
    public static String getAddressString(byte[] publicKey, int chainId) {
        String prefix = getPrefix(chainId);
        byte[] addressByte = getAddress(publicKey, chainId, prefix);
        return getStringAddressByBytes(addressByte);
    }

    /**
     * Decode the original byte array of the address based on the address string
     * base58(chainId)+_+base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))
     * addressTypePlace after the original data0
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

    public static byte[] getAddressByRealAddr(String addressString) {
        byte[] result;
        try {
            byte[] body = Base58.decode(addressString);
            result = new byte[body.length - 1];
            System.arraycopy(body, 0, result, 0, body.length - 1);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return result;
    }

    /**
     * Query the chain to which the address belongs based on the address stringID
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
     * Query address byte array based on public key
     *
     * @param publicKey
     * @param chainId
     * @return
     */
    public static byte[] getAddress(byte[] publicKey, int chainId) {
        String prefix = getPrefix(chainId);
        return getAddress(publicKey, chainId, prefix);
    }

    /**
     * Query address byte array based on public key
     *
     * @param publicKeyStr
     * @param chainId
     * @return
     */
    public static byte[] getAddressByPubKeyStr(String publicKeyStr, int chainId) {
        byte[] publicKey = HexUtil.decode(publicKeyStr);
        return getAddress(publicKey, chainId);
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
     * Generate checksums based on the following fields：addressType+hash160(pubKey)
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
     * Check if the checksum is correct,XOR(addressType+hash160(pubKey))
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
     * Verify if the address string is a valid address
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
     * Obtain through addresschainId
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
     * Verify if it is a regular address
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
     * Verify if it is a smart contract address
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
     * Generate address string based on address byte array
     * base58(chainId)+_+base58(addressType+hash160(pubKey)+XOR(addressType+hash160(pubKey)))
     *
     * @param addressBytes
     * @return
     */
    public static String getStringAddressByBytes(byte[] addressBytes) {
        int chainId = getChainIdByAddress(addressBytes);
        String prefix = getPrefix(chainId);
        return getStringAddressByBytes(addressBytes, prefix);
    }

    public static String getStringAddressNoPrefix(byte[] addressBytes) {
        byte[] bytes = ByteUtils.concatenate(addressBytes, new byte[]{getXor(addressBytes)});
        return Base58.encode(bytes);
    }

    public static String getStringAddressByBytes(byte[] addressBytes, String prefix) {
        if (addressBytes == null) {
            return null;
        }
        if (addressBytes.length != Address.ADDRESS_LENGTH) {
            return null;
        }
        byte[] bytes = ByteUtils.concatenate(addressBytes, new byte[]{getXor(addressBytes)});
        if (null != prefix) {
            return prefix + LENGTHPREFIX[prefix.length()] + Base58.encode(bytes);
        } else {
            return Base58.encode(bytes);
        }
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

    public static byte[] createMultiSigAccountOriginBytes(int chainId, int m, List<String> pubKeys) throws Exception {
        byte[] result = null;
        if (m < 1) {
            throw new RuntimeException();
        }
        HashSet<String> hashSet = new HashSet(pubKeys);
        List<String> pubKeyList = new ArrayList<>();
        pubKeyList.addAll(hashSet);
        if (pubKeyList.size() < m) {
            throw new RuntimeException();
        }
        Collections.sort(pubKeyList, new Comparator<String>() {
            private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

            @Override
            public int compare(String k1, String k2) {
                return comparator.compare(Hex.decode(k1), Hex.decode(k2));
            }
        });
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(chainId);
            byteArrayOutputStream.write(m);
            for (String pubKey : pubKeyList) {
                byteArrayOutputStream.write(HexUtil.decode(pubKey));
            }
            result = byteArrayOutputStream.toByteArray();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
