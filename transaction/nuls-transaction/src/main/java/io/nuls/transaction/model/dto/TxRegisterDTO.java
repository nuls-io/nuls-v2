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

package io.nuls.transaction.model.dto;

/**
 * 各个模块需要向交易管理模块注册自己的每个交易的验证器
 * @author: qinyifeng
 * @date: 2018/11/30
 */
public class TxRegisterDTO {
    /**
     * Transaction type
     */
    private int txType;

    /**
     * Transaction validator cmd name
     */
    private String validator;

    /**
     * 是否是系统产生的交易（打包节点产生，用于出块奖励结算、红黄牌惩罚），该种类型的交易在验证块大小时不计算在内，该类型交易不需要手续费
     * Is a system to produce trading (packaged node generation, for the piece reward settlement, CARDS punishment),
     * trading in the validation of this kind of new type block size is not taken into account, the types of transactions do not need poundage
     */
    private boolean systemTx;


    /**
     * 是否是解锁交易
     * If it's an unlocking transaction
     */
    public boolean unlockTx;

    /**
     * 该交易是否需要在账本中验证签名，所有系统产生的交易和一些特殊交易，不需要安装普通交易的方式验证签名，会提供额外的逻辑进行验证。
     * If the deal need to verify the signature in the book, all transactions system and some special deal,
     * no need to install the ordinary transaction way to verify the signature, will provide additional validation logic.
     */
    public boolean verifySignature;

    public TxRegisterDTO() {

    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public boolean isSystemTx() {
        return systemTx;
    }

    public void setSystemTx(boolean systemTx) {
        this.systemTx = systemTx;
    }

    public boolean isUnlockTx() {
        return unlockTx;
    }

    public void setUnlockTx(boolean unlockTx) {
        this.unlockTx = unlockTx;
    }

    public boolean isVerifySignature() {
        return verifySignature;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    @Override
    public String toString() {
        return "TxRegisterDTO{" +
                "txType=" + txType +
                ", validator='" + validator + '\'' +
                ", systemTx=" + systemTx +
                ", unlockTx=" + unlockTx +
                ", verifySignature=" + verifySignature +
                '}';
    }
}
