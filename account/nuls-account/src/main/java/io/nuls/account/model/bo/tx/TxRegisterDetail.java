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
 * */
public class TxRegisterDetail {
    /**
    * 交易类型
    * Transaction type
    * */
    private int txType;
    /**
    * 交易验证方法
    * Transaction verification method
    * */
    private String validateCmd;
    /**
    * 交易提交方法
    * Transaction submission method
    * */
    private String commitCmd;
    /**
    * 交易回滚方法
    * Transaction rollback method
    * */
    private String rollbackCmd;

    public TxRegisterDetail(int txType){
        this.txType = txType;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getValidateCmd() {
        return validateCmd;
    }

    public void setValidateCmd(String validateCmd) {
        this.validateCmd = validateCmd;
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
}
