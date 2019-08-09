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

package io.nuls.account.model.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/6/25
 */
public class MultiSignTransferDTO {
    /**
     * 链ID
     */
    private Integer chainId;

    /**
     * 交易输入
     */
    private List<BaseCoinDTO> inputs;

    /**
     * 交易输出
     */
    private List<MultiSignCoinToDTO> outputs;

    private String signAddress;

    private String signPassword;

    /**
     * 备注
     */
    private String remark;

    /**
     * 将多签交易客户端参数转换成交易统一处理对象
     * @return
     */
    public List<CoinDTO> inputsConvert(){
        List<CoinDTO> list = new ArrayList<>(inputs.size());
        for(BaseCoinDTO baseCoinDTO : inputs){
            list.add(baseCoinDTO.convert());
        }
        return list;
    }
    /**
     * 将多签交易客户端参数转换成交易统一处理对象
     * @return
     */
    public List<CoinDTO> outputsConvert(){
        List<CoinDTO> list = new ArrayList<>(outputs.size());
        for(MultiSignCoinToDTO multiSignCoinToDTO : outputs){
            list.add(multiSignCoinToDTO.convert());
        }
        return list;
    }
    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }

    public String getSignPassword() {
        return signPassword;
    }

    public void setSignPassword(String signPassword) {
        this.signPassword = signPassword;
    }

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public List<BaseCoinDTO> getInputs() {
        return inputs;
    }

    public void setInputs(List<BaseCoinDTO> inputs) {
        this.inputs = inputs;
    }

    public List<MultiSignCoinToDTO> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MultiSignCoinToDTO> outputs) {
        this.outputs = outputs;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "MultiSignTransferDTO{" +
                "chainId=" + chainId +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", signAddress='" + signAddress + '\'' +
                ", signPassword='" + signPassword + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
