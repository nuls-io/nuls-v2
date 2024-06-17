/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.model;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

/**
 * Created by lanjinsheng on 2019/04/03
 * @author lanjinsheng
 */
@ApiModel(name = "Lock in funds")
public class FreezeLockState {
    /**
     * Transactionalhashvalue
     */
    @ApiModelProperty(description = "transactionhash")
    private String txHash;
    /**
     * Lock in amount
     */
    @ApiModelProperty(description = "Lock in amount")
    private BigInteger amount;

    /**
     * Lock time or height,-1To permanently lock
     */
    @ApiModelProperty(description = "Lock time or height,-1To permanently lock")
    private long lockedValue;

    /**
     * Transaction generation time
     */
    @ApiModelProperty(description = "Transaction generation time,second")
    private long time;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public long getLockedValue() {
        return lockedValue;
    }

    public void setLockedValue(long lockedValue) {
        this.lockedValue = lockedValue;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
