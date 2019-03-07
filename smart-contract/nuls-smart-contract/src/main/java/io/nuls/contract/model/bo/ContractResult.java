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

package io.nuls.contract.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.tools.crypto.HexUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ContractResult {


    private transient ContractWrapperTransaction tx;

    private String hash;

    private long txTime;

    /**
     * 交易创建者
     */
    private byte[] sender;

    /**
     * 合约地址
     */
    private byte[] contractAddress;

    /**
     * 合约执行结果
     */
    private String result;
    /**
     * 已使用Gas
     */
    private long gasUsed;
    /**
     * 单价
     */
    private long price;
    /**
     * 状态根
     */
    private byte[] stateRoot;

    /**
     * 调用者向合约转入的资金
     */
    private long value;

    /**
     * 有错误，还原状态
     */
    private boolean revert;

    /**
     * 有错误，状态改变
     */
    private boolean error;

    /**
     *
     */
    private String errorMessage;

    /**
     *
     */
    private String stackTrace;

    /**
     *
     */
    private BigInteger balance;

    private BigInteger preBalance;

    /**
     *
     */
    private BigInteger nonce;

    private boolean acceptDirectTransfer;

    private boolean isNrc20;

    private String tokenName;
    private String tokenSymbol;
    private int tokenDecimals;
    private BigInteger tokenTotalSupply;

    /**
     * 合约转账(从合约转出)交易
     */
    private List<ProgramTransfer> transfers = new ArrayList<>();

    private List<ContractMergedTransfer> mergedTransferList = new ArrayList<>();

    private transient List<ContractTransferTransaction> contractTransferList = new ArrayList<>();

    /**
     * 消息事件
     */
    private List<String> events = new ArrayList<>();

    private String remark;


    private boolean isTerminated;
    private Set<String> contractAddressInnerCallSet;

    private transient Object txTrack;

    public boolean isSuccess() {
        return !error && !revert;
    }

    @JsonIgnore
    public Object getTxTrack() {
        return txTrack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractResult)) return false;

        ContractResult result = (ContractResult) o;

        if (getHash() != null ? !getHash().equals(result.getHash()) : result.getHash() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        return result;
    }

    public static ContractResult getFailed(ContractData contractData) {
        ContractResult contractResult = new ContractResult();
        contractResult.setContractAddress(contractData.getContractAddress());
        contractResult.setGasUsed(0L);
        contractResult.setPrice(contractData.getPrice());
        contractResult.setSender(contractData.getSender());
        contractResult.setError(true);
        contractResult.setRevert(true);
        return contractResult;
    }

    public static ContractResult getFailed(ContractData contractData, String msg) {
        ContractResult result = getFailed(contractData);
        result.setErrorMessage(msg);
        return result;
    }

    @Override
    public String toString() {
        return "ContractResult{" +
                "contractAddress=" + AddressTool.getStringAddressByBytes(contractAddress) +
                ", result='" + result + '\'' +
                ", gasUsed=" + gasUsed +
                ", stateRoot=" + (stateRoot != null ? HexUtil.encode(stateRoot) : stateRoot) +
                ", value=" + value +
                ", revert=" + revert +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", balance=" + (balance != null ? balance.toString() : 0) +
                ", nonce=" + nonce +
                ", transfersSize=" + (transfers != null ? transfers.size() : 0) +
                ", eventsSize=" + (events != null ? events.size() : 0) +
                ", remark='" + remark + '\'' +
                '}';
    }
}
