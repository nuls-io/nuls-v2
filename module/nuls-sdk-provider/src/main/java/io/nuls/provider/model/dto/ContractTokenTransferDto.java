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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: PierreLuo
 */
@Data
@NoArgsConstructor
@ApiModel
public class ContractTokenTransferDto {
    @ApiModelProperty(description = "合约地址")
    private String contractAddress;
    @ApiModelProperty(description = "付款方")
    private String from;
    @ApiModelProperty(description = "收款方")
    private String to;
    @ApiModelProperty(description = "转账金额")
    private String value;
    @ApiModelProperty(description = "token名称")
    private String name;
    @ApiModelProperty(description = "token符号")
    private String symbol;
    @ApiModelProperty(description = "token支持的小数位数")
    private long decimals;

}
