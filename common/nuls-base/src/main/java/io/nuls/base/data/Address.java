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

package io.nuls.base.data;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.StringUtils;

/**
 * @author: Chralie
 */
public class Address {

    /**
     * hash length
     */
    public static final int ADDRESS_LENGTH = 23;

    /**
     * RIPEMD160 length
     */
    public static final int RIPEMD160_LENGTH = 20;

    private String prefix;

    /**
     * chain id
     */
    private int chainId;

    /**
     * Address represented in string format
     */
    private String addressStr;

    /**
     * address type
     */
    private byte addressType;

    /**
     * hash160 of public key
     */
    protected byte[] hash160;

    protected byte[] addressBytes;

    public Address(String address) {
        try {
            byte[] bytes = AddressTool.getAddress(address);
            Address addressTmp = Address.fromHashs(bytes);
            this.chainId = addressTmp.getChainId();
            this.addressType = addressTmp.getAddressType();
            this.hash160 = addressTmp.getHash160();
            this.addressBytes = calcAddressbytes();
            this.prefix = AddressTool.getPrefix(address);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public Address(int chainId, byte addressType, byte[] hash160) {
        this.chainId = chainId;
        this.addressType = addressType;
        this.hash160 = hash160;
        this.addressBytes = calcAddressbytes();
        this.prefix = AddressTool.getPrefix(chainId);
    }

    public Address(int chainId, String prefix, byte addressType, byte[] hash160) {
        this.chainId = chainId;
        this.addressType = addressType;
        this.hash160 = hash160;
        this.addressBytes = calcAddressbytes();
        this.prefix = prefix;
    }

    public byte[] getHash160() {
        return hash160;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getChainId() {
        return chainId;
    }

    public static Address fromHashs(String address) {
        byte[] bytes = AddressTool.getAddress(address);
        return fromHashs(bytes);
    }

    public static Address fromHashs(byte[] hashs) {
        if (hashs == null || hashs.length != ADDRESS_LENGTH) {
            throw new NulsRuntimeException(new Exception());
        }

        NulsByteBuffer byteBuffer = new NulsByteBuffer(hashs);
        int chainId;
        try {
            chainId = byteBuffer.readUint16();
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        byte addressType = hashs[2];
        byte[] content = new byte[RIPEMD160_LENGTH];
        System.arraycopy(hashs, 3, content, 0, RIPEMD160_LENGTH);

        return new Address(chainId, addressType, content);
    }

    private byte[] calcAddressbytes() {
        byte[] body = new byte[ADDRESS_LENGTH];
        System.arraycopy(ByteUtils.shortToBytes((short) chainId), 0, body, 0, 2);
        body[2] = this.addressType;
        System.arraycopy(hash160, 0, body, 3, hash160.length);
        return body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Address) {
            Address other = (Address) obj;
            return ByteUtils.arrayEquals(this.addressBytes, other.getAddressBytes());
        }
        return false;
    }

    public byte[] getAddressBytes() {
        return addressBytes;
    }

    public void setAddressBytes(byte[] addressBytes) {
        this.addressBytes = addressBytes;
    }

    public byte getAddressType() {
        return addressType;
    }

    public void setAddressType(byte addressType) {
        this.addressType = addressType;
    }

    public static int size() {
        return ADDRESS_LENGTH;
    }

    /**
     * Default returnbase58Encoded address
     *
     * @return
     */
    @Override
    public String toString() {
        return getBase58();
    }

    public String getBase58() {
        if(StringUtils.isNotBlank(prefix)){
            addressStr = AddressTool.getStringAddressByBytes(this.addressBytes,prefix);
        }else if (StringUtils.isBlank(addressStr)) {
            addressStr = AddressTool.getStringAddressByBytes(this.addressBytes);
        }
        return addressStr;
    }
}
