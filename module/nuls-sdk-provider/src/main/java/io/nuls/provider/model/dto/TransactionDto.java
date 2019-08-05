/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2019-06-29
 */
@Data
@NoArgsConstructor
@ApiModel
public class TransactionDto {
    @ApiModelProperty(description = "交易的hash值")
    private String hash;
    @ApiModelProperty(description = "交易类型")
    private int type;
    @ApiModelProperty(description = "交易时间")
    private String time;
    @ApiModelProperty(description = "区块高度")
    private long blockHeight = -1L;
    @ApiModelProperty(description = "交易备注")
    private String remark;
    @ApiModelProperty(description = "交易签名")
    private String transactionSignature;
    @ApiModelProperty(description = "交易业务数据序列化字符串")
    private String txDataHex;
    @ApiModelProperty(description = "交易状态 0:unConfirm(待确认), 1:confirm(已确认)")
    private int status = 0;
    @ApiModelProperty(description = "交易大小")
    private int size;
    @ApiModelProperty(description = "在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序")
    private int inBlockIndex;
    @ApiModelProperty(description = "输入", type = @TypeDescriptor(value = List.class, collectionElement = CoinFromDto.class))
    private List<CoinFromDto> form;
    @ApiModelProperty(description = "输出", type = @TypeDescriptor(value = List.class, collectionElement = CoinToDto.class))
    private List<CoinToDto> to;

    public TransactionDto(Transaction transaction) throws NulsException {
        this.blockHeight = transaction.getBlockHeight();
        this.status = transaction.getStatus().getStatus();
        this.hash = transaction.getHash().toString();
        this.remark = ByteUtils.asString(transaction.getRemark());
        this.inBlockIndex = transaction.getInBlockIndex();
        this.size = transaction.getSize();
        this.time = DateUtils.timeStamp2DateStr(transaction.getTime()*1000);
        this.transactionSignature = RPCUtil.encode(transaction.getTransactionSignature());
        this.txDataHex = RPCUtil.encode(transaction.getTxData());
        this.type = transaction.getType();
        CoinData coinData = transaction.getCoinDataInstance();
        this.form = coinData.getFrom().stream().map(from -> new CoinFromDto(from)).collect(Collectors.toList());
        this.to = coinData.getTo().stream().map(to -> new CoinToDto(to)).collect(Collectors.toList());
    }
}
