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
package io.nuls.provider.model.dto;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.DateUtils;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2019-06-29
 */
@ApiModel
public class TransactionDto {
    @ApiModelProperty(description = "Transactionalhashvalue")
    private String hash;
    @ApiModelProperty(description = "Transaction type")
    private int type;
    @ApiModelProperty(description = "Transaction time")
    private String time;
    @ApiModelProperty(description = "Transaction timestamp")
    private long timestamp;
    @ApiModelProperty(description = "block height")
    private long blockHeight = -1L;
    @ApiModelProperty(description = "blockhash")
    private String blockHash;
    @ApiModelProperty(description = "Transaction notes")
    private String remark;
    @ApiModelProperty(description = "Transaction signature")
    private String transactionSignature;
    @ApiModelProperty(description = "Transaction business data serialization string")
    private String txDataHex;
    @ApiModelProperty(description = "Transaction status 0:unConfirm(To be confirmed), 1:confirm(Confirmed)")
    private int status = 0;
    @ApiModelProperty(description = "Transaction size")
    private int size;
    @ApiModelProperty(description = "Order in blocks, stored inrocksDBThe middle is unordered, assigned values when saving blocks, sorted based on this value after retrieval")
    private int inBlockIndex;
    @ApiModelProperty(description = "input", type = @TypeDescriptor(value = List.class, collectionElement = CoinFromDto.class))
    private List<CoinFromDto> from;
    @ApiModelProperty(description = "output", type = @TypeDescriptor(value = List.class, collectionElement = CoinToDto.class))
    private List<CoinToDto> to;

    public TransactionDto() {
    }

    public TransactionDto(Transaction transaction) throws NulsException {
        this.blockHeight = transaction.getBlockHeight();
        this.status = transaction.getStatus().getStatus();
        this.hash = transaction.getHash().toString();
        this.remark = ByteUtils.asString(transaction.getRemark());
        this.inBlockIndex = transaction.getInBlockIndex();
        this.size = transaction.getSize();
        this.time = DateUtils.timeStamp2DateStr(transaction.getTime() * 1000);
        this.timestamp = transaction.getTime();
        this.transactionSignature = RPCUtil.encode(transaction.getTransactionSignature());
        this.txDataHex = RPCUtil.encode(transaction.getTxData());
        this.type = transaction.getType();
        if (transaction.getCoinData() != null) {
            CoinData coinData = transaction.getCoinDataInstance();
            this.from = coinData.getFrom().stream().map(from -> new CoinFromDto(from)).collect(Collectors.toList());
            this.to = coinData.getTo().stream().map(to -> new CoinToDto(to)).collect(Collectors.toList());
        }
    }

    public TransactionDto(Transaction transaction, int i) throws NulsException {
        this.blockHeight = transaction.getBlockHeight();
        this.status = transaction.getStatus().getStatus();
        this.hash = transaction.getHash().toString();
        this.remark = ByteUtils.asString(transaction.getRemark());
        this.inBlockIndex = i;
        this.status = 1;
        this.size = transaction.getSize();
        this.time = DateUtils.timeStamp2DateStr(transaction.getTime() * 1000);
        this.timestamp = transaction.getTime();
        this.transactionSignature = RPCUtil.encode(transaction.getTransactionSignature());
        this.txDataHex = RPCUtil.encode(transaction.getTxData());
        this.type = transaction.getType();
        if (transaction.getCoinData() != null) {
            CoinData coinData = transaction.getCoinDataInstance();
            this.from = coinData.getFrom().stream().map(from -> new CoinFromDto(from)).collect(Collectors.toList());
            this.to = coinData.getTo().stream().map(to -> new CoinToDto(to)).collect(Collectors.toList());
        }
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(String transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getTxDataHex() {
        return txDataHex;
    }

    public void setTxDataHex(String txDataHex) {
        this.txDataHex = txDataHex;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getInBlockIndex() {
        return inBlockIndex;
    }

    public void setInBlockIndex(int inBlockIndex) {
        this.inBlockIndex = inBlockIndex;
    }

    public List<CoinFromDto> getFrom() {
        return from;
    }

    public void setFrom(List<CoinFromDto> from) {
        this.from = from;
    }

    public List<CoinToDto> getTo() {
        return to;
    }

    public void setTo(List<CoinToDto> to) {
        this.to = to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
