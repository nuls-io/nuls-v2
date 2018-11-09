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
package io.nuls.account.model.dto;

import io.nuls.account.model.bo.Account;
import io.nuls.tools.crypto.HexUtil;

/**
 * @author: qinyifeng
 * @date: 2018/11/09
 */
public class AccountOfflineDto {
    /**
     * 账户地址
     */
    private String address;

    /**
     * 公钥Hex.encode(byte[])
     */
    private String pubKey;

    /**
     * 私钥Hex.encode(byte[])
     */
    private String priKey;

    /**
     * 加密后的私钥Hex.encode(byte[])
     */
    private String encryptedPriKey;

    /**
     * 其他信息Hex.encode(byte[])
     */
    private String extend;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 账户是否加密
     */
    private boolean encrypted;

    /**
     * 账户备注
     */
    private String remark;


    public AccountOfflineDto() {

    }

    public AccountOfflineDto(Account account) {
        this.address = account.getAddress().getBase58();
        this.pubKey = HexUtil.encode(account.getPubKey());
        this.createTime = account.getCreateTime();
        if (account.getExtend() != null) {
            this.extend = HexUtil.encode(account.getExtend());
        }
        this.encrypted = account.isEncrypted();
        if (encrypted) {
            this.encryptedPriKey = HexUtil.encode(account.getEncryptedPriKey());
            this.priKey = "";
        } else {
            this.priKey = HexUtil.encode(account.getPriKey());
            this.encryptedPriKey = "";
        }
        this.remark = account.getRemark();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(String encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
