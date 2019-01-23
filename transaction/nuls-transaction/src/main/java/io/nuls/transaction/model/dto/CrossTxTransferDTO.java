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

package io.nuls.transaction.model.dto;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class CrossTxTransferDTO {

    private Integer chainId;

    private List<CoinDTO> listFrom;

    private List<CoinDTO> listTo;

    private String remark;

    public CrossTxTransferDTO() {
    }

    public CrossTxTransferDTO(Integer chainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) {
        this.chainId = chainId;
        this.listFrom = listFrom;
        this.listTo = listTo;
        this.remark = remark;
    }

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public List<CoinDTO> getListFrom() {
        return listFrom;
    }

    public void setListFrom(List<CoinDTO> listFrom) {
        this.listFrom = listFrom;
    }

    public List<CoinDTO> getListTo() {
        return listTo;
    }

    public void setListTo(List<CoinDTO> listTo) {
        this.listTo = listTo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
