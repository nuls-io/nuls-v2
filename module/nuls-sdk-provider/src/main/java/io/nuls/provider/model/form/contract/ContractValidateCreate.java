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
package io.nuls.provider.model.form.contract;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.model.form.Base;
import io.nuls.v2.util.ContractUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: PierreLuo
 */
@Data
@NoArgsConstructor
@ApiModel(description = "验证发布智能合约表单数据")
public class ContractValidateCreate extends Base {

    @ApiModelProperty(description = "交易创建者", required = true)
    private String sender;
    @ApiModelProperty(description = "最大gas消耗", required = true)
    private long gasLimit;
    @ApiModelProperty(description = "执行合约单价", required = true)
    private long price;
    @ApiModelProperty(description = "智能合约代码(字节码的Hex编码字符串)", required = true)
    private String contractCode;
    @ApiModelProperty(description = "参数列表", required = false)
    private Object[] args;

    public String[][] getArgs(String[] types) {
        return ContractUtil.twoDimensionalArray(args, types);
    }

}
