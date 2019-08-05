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


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: PierreLuo
 */
@Data
@NoArgsConstructor
@ApiModel
public class ContractResultDto {
    @ApiModelProperty(description = "合约执行是否成功")
    private boolean success;
    @ApiModelProperty(description = "执行失败信息")
    private String errorMessage;
    @ApiModelProperty(description = "合约地址")
    private String contractAddress;
    @ApiModelProperty(description = "合约执行结果")
    private String result;
    @ApiModelProperty(description = "GAS限制")
    private long gasLimit;
    @ApiModelProperty(description = "已使用GAS")
    private long gasUsed;
    @ApiModelProperty(description = "GAS单价")
    private long price;
    @ApiModelProperty(description = "交易总手续费")
    private String totalFee;
    @ApiModelProperty(description = "交易大小手续费")
    private String txSizeFee;
    @ApiModelProperty(description = "实际执行合约手续费")
    private String actualContractFee;
    @ApiModelProperty(description = "合约返回的手续费")
    private String refundFee;
    @ApiModelProperty(description = "调用者向合约地址转入的主网资产金额，没有此业务时则为0")
    private String value;
    @ApiModelProperty(description = "异常堆栈踪迹")
    private String stackTrace;
    @ApiModelProperty(description = "合约转账列表（从合约转出）", type = @TypeDescriptor(value = List.class, collectionElement = ContractMergedTransferDto.class))
    private List<ContractMergedTransferDto> transfers;
    @ApiModelProperty(description = "合约事件列表", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> events;
    @ApiModelProperty(description = "合约token转账列表", type = @TypeDescriptor(value = List.class, collectionElement = ContractTokenTransferDto.class))
    private List<ContractTokenTransferDto> tokenTransfers;
    @ApiModelProperty(description = "合约调用外部命令的调用记录列表", type = @TypeDescriptor(value = List.class, collectionElement = ContractInvokeRegisterCmdDto.class))
    private List<ContractInvokeRegisterCmdDto> invokeRegisterCmds;
    @ApiModelProperty(description = "合约生成交易的序列化字符串列表", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> contractTxList;
    @ApiModelProperty(description = "备注")
    private String remark;

}
