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
package io.nuls.account.model.bo.tx;

/**
 * 交易注册类
 * Transaction registration class
 *
 * @author qinyifeng
 * 2018/11/30
 */
public class TxRegisterDetail {
    /**
     * 交易类型
     * Transaction type
     */
    private int txType;
    /**
     * 交易验证方法
     * Transaction verification method
     */
    private String validator;
    /**
     * 交易提交方法
     * Transaction submission method
     */
    private String commitCmd;
    /**
     * 交易回滚方法
     * Transaction rollback method
     */
    private String rollbackCmd;

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
    private boolean unlockTx;

    /**
     * 该交易是否需要在账本中验证签名，所有系统产生的交易和一些特殊交易，不需要安装普通交易的方式验证签名，会提供额外的逻辑进行验证。
     * If the deal need to verify the signature in the book, all transactions system and some special deal,
     * no need to install the ordinary transaction way to verify the signature, will provide additional validation logic.
     */
    private boolean verifySignature = true;

    /**
     * 默认需要验证签名
     * @param txType
     */
    public TxRegisterDetail(int txType) {
        this.txType = txType;
        this.systemTx = false;
        this.unlockTx = false;
        this.verifySignature = true;
    }

    public TxRegisterDetail(int txType, boolean systemTx, boolean unlockTx, boolean verifySignature) {
        this.txType = txType;
        this.systemTx = systemTx;
        this.unlockTx = unlockTx;
        this.verifySignature = verifySignature;
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

    public String getCommitCmd() {
        return commitCmd;
    }

    public void setCommitCmd(String commitCmd) {
        this.commitCmd = commitCmd;
    }

    public String getRollbackCmd() {
        return rollbackCmd;
    }

    public void setRollbackCmd(String rollbackCmd) {
        this.rollbackCmd = rollbackCmd;
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
}
