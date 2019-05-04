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
package io.nuls.contract.model.dto;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.crypto.HexUtil;

/**
 * @author: PierreLuo
 */
public class ContractTokenTransferTransactionDto {


    private String contractAddress;
    private String from;
    private String to;
    private String value;
    private long time;
    private byte status;
    private String txHash;
    private long blockHeight;
    private String name;
    private String symbol;
    private long decimals;
    private String info;

    public ContractTokenTransferTransactionDto(ContractTokenTransferInfoPo po, byte[] address) {
        this.contractAddress = po.getContractAddress();
        if (po.getFrom() != null) {
            this.from = AddressTool.getStringAddressByBytes(po.getFrom());
        }
        if (po.getTo() != null) {
            this.to = AddressTool.getStringAddressByBytes(po.getTo());
        }
        this.value = ContractUtil.bigInteger2String(po.getValue());
        this.time = po.getTime();
        this.status = po.getStatus();
        this.txHash = HexUtil.encode(po.getTxHash());
        this.blockHeight = po.getBlockHeight();
        this.name = po.getName();
        this.symbol = po.getSymbol();
        this.decimals = po.getDecimals();
        this.info = po.getInfo(address);
    }

    public int compareTo(long thatTime) {
        if (this.time > thatTime) {
            return -1;
        } else if (this.time < thatTime) {
            return 1;
        }
        return 0;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
