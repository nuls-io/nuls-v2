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
 */
package io.nuls.account.model.bo;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.base.data.Address;
import io.nuls.core.crypto.AESEncrypt;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.EncryptedData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.CryptoException;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.ObjectUtils;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author: qinyifeng
 */
public class Account implements Serializable {


    /**
     * chain id
     */
    private int chainId;

    /**
     * 账户地址
     */

    private transient Address address;

    /**
     * 账户别名
     */

    private String alias;

    /**
     * is default acct
     */

    private int status;

    /**
     * 账户公钥
     */

    private byte[] pubKey;

    /**
     * 扩展字段
     */

    private byte[] extend;

    /**
     * 创建时间
     */

    private Long createTime;


    private byte[] encryptedPriKey;

    /**
     * Decrypted  prikey
     */

    private byte[] priKey;

    /**
     * local field
     */

    private transient ECKey ecKey;


    private String remark;


    /**
     * 账户是否被加密(是否设置过密码)
     * Whether the account is encrypted (Whether the password is set)
     */
    public boolean isEncrypted() {
        return getEncryptedPriKey() != null && getEncryptedPriKey().length > 0;
    }

    /**
     * 锁定账户
     * Lock account
     */
    public void lock() {
        if (!isEncrypted()) {
            return;
        }

        if (this.getEcKey().getEncryptedPrivateKey() != null) {
            ECKey result = ECKey.fromEncrypted(getEcKey().getEncryptedPrivateKey(), getPubKey());
            this.setPriKey(new byte[0]);
            this.setEcKey(result);
        }
    }

    public byte[] getHash160() {
        return this.getAddress().getHash160();
    }

    /**
     * 根据密码解锁账户
     * Unlock account based on password
     */
    public boolean unlock(String password) throws NulsException {
        decrypt(password);
        return !isLocked();
    }

    /**
     * 账户是否被锁定(是否有明文私钥) 有私钥表示解锁
     * Whether the account is locked (is there a cleartext private key)
     *
     * @return true: Locked, false: not Locked
     */
    public boolean isLocked() {
        return (this.getPriKey() == null) || (this.getPriKey().length == 0);
    }

    /**
     * 验证账户密码是否正确
     * Verify that the account password is correct
     */
    public boolean validatePassword(String password) {
        boolean result = FormatValidUtils.validPassword(password);
        if (!result) {
            return false;
        }
        byte[] unencryptedPrivateKey;
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
        } catch (CryptoException e) {
            return false;
        }
        BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
        ECKey key = ECKey.fromPrivate(newPriv);

        return Arrays.equals(key.getPubKey(), getPubKey());
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     */
    public void encrypt(String password) throws NulsException {
        encrypt(password, false);
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     */
    public void encrypt(String password, boolean isForce) throws NulsException {
        if (this.isEncrypted()) {
            if (isForce) {
                if (isLocked()) {
                    throw new NulsException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED_AND_LOCKED);
                }
            } else {
                throw new NulsException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
            }
        }
        ECKey eckey = this.getEcKey();
        byte[] privKeyBytes = eckey.getPrivKeyBytes();
        EncryptedData encryptedPrivateKey = AESEncrypt.encrypt(privKeyBytes, EncryptedData.DEFAULT_IV, new KeyParameter(Sha256Hash.hash(password.getBytes())));
        eckey.setEncryptedPrivateKey(encryptedPrivateKey);
        ECKey result = ECKey.fromEncrypted(encryptedPrivateKey, getPubKey());
        this.setPriKey(new byte[0]);
        this.setEcKey(result);
        this.setEncryptedPriKey(encryptedPrivateKey.getEncryptedBytes());
    }

    /**
     * 根据解密账户, 包括生成账户明文私钥
     * According to the decryption account, including generating the account plaintext private key
     */
    private boolean decrypt(String password) throws NulsException {
        try {
            byte[] unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
            BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
            ECKey key = ECKey.fromPrivate(newPriv);

            if (!Arrays.equals(key.getPubKey(), getPubKey())) {
                return false;
            }
            key.setEncryptedPrivateKey(new EncryptedData(this.getEncryptedPriKey()));
            this.setPriKey(key.getPrivKeyBytes());
            this.setEcKey(key);
        } catch (Exception e) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return true;
    }

    public Object copy() {
        Account account = new Account();
        account.setChainId(chainId);
        account.setAlias(alias);
        account.setAddress(address);
        account.setStatus(status);
        account.setPubKey(pubKey);
        account.setExtend(extend);
        account.setCreateTime(createTime);
        account.setEncryptedPriKey(encryptedPriKey);
        account.setPriKey(priKey);
        account.setEcKey(ecKey);
        account.setRemark(remark);
        return account;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public byte[] getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(byte[] encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public byte[] getPriKey(String password) throws NulsException {
        if (!FormatValidUtils.validPassword(password)) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        byte[] unencryptedPrivateKey;
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
        } catch (CryptoException e) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
        ECKey key = ECKey.fromPrivate(newPriv);

        if (!Arrays.equals(key.getPubKey(), getPubKey())) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return unencryptedPrivateKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    /**
     * 根据密码获取ECKey
     */
    public ECKey getEcKey(String password) throws NulsException {
        ECKey eckey = null;
        //判断当前账户是否存在私钥，如果不存在私钥这为加密账户
        BigInteger newPriv;
        if (this.isEncrypted()) {
            ObjectUtils.canNotEmpty(password, "the password can not be empty");
            boolean result = FormatValidUtils.validPassword(password);
            if (result) {
                byte[] unencryptedPrivateKey;
                try {
                    unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
                    newPriv = new BigInteger(1, unencryptedPrivateKey);
                    eckey = ECKey.fromPrivate(newPriv);
                    result = Arrays.equals(eckey.getPubKey(), getPubKey());
                } catch (CryptoException e) {
                    result = false;
                }

            }
            if (!result) {
                throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        } else {
            newPriv = new BigInteger(1, this.getPriKey());
            eckey = ECKey.fromPrivate(newPriv);
        }
        return eckey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Account)) {
            return false;
        }
        Account other = (Account) obj;
        return Arrays.equals(pubKey, other.getPubKey());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pubKey);
    }
}
