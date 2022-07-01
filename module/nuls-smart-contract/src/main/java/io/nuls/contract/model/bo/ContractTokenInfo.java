/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.model.bo;

import io.nuls.contract.enums.ContractStatus;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2018/8/19
 */
public class ContractTokenInfo {

    private String contractAddress;
    private String name;
    private String symbol;
    private BigInteger amount;
    private BigInteger lockAmount;
    private long decimals;
    private long blockHeight;
    private ContractStatus status;

    public ContractTokenInfo() {
    }

    public ContractTokenInfo(String contractAddress, String name, long decimals, BigInteger amount, String symbol, long blockHeight) {
        this.name = name;
        this.amount = amount;
        this.lockAmount = BigInteger.ZERO;
        this.contractAddress = contractAddress;
        this.decimals = decimals;
        this.symbol = symbol;
        this.blockHeight = blockHeight;
    }

    public BigInteger getLockAmount() {
        return lockAmount;
    }

    public void setLockAmount(BigInteger lockAmount) {
        this.lockAmount = lockAmount;
    }

    public ContractTokenInfo setName(String name) {
        this.name = name;
        return this;
    }

    public ContractTokenInfo setAmount(BigInteger amount) {
        this.amount = amount;
        return this;
    }

    public boolean isLock(int chainId) throws NulsException {
        return ContractUtil.isLockContract(chainId, this.blockHeight);
    }

    public boolean isStop() {
        return ContractUtil.isTerminatedContract(this.status.status());
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
