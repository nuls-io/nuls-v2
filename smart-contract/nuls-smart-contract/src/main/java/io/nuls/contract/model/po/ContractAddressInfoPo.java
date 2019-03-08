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
package io.nuls.contract.model.po;

import io.nuls.contract.util.ContractUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2018/8/15
 */
@Getter
@Setter
public class ContractAddressInfoPo {

    private byte[] contractAddress;
    private byte[] sender;
    private byte[] createTxHash;
    private long createTime;
    private long blockHeight;
    private boolean acceptDirectTransfer;
    private boolean isNrc20;
    private String nrc20TokenName;
    private String nrc20TokenSymbol;
    private long decimals;
    private BigInteger totalSupply;

    public boolean isLock() {
        return ContractUtil.isLockContract(this.blockHeight);
    }

    public int compareTo(long thatTime) {
        if(this.createTime > thatTime) {
            return -1;
        } else if(this.createTime < thatTime) {
            return 1;
        }
        return 0;
    }
}
