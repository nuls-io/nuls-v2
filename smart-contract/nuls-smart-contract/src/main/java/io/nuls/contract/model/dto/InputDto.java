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

package io.nuls.contract.model.dto;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import lombok.Getter;
import lombok.Setter;
import org.spongycastle.util.encoders.Hex;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 * @date: 2019-03-14
 */
@Getter
@Setter
public class InputDto {

    private String address;
    private int assetsChainId;
    private int assetsId;
    private String amount;
    private String nonce;
    private byte locked;


    public InputDto(CoinFrom from) {
        this.address = AddressTool.getStringAddressByBytes(from.getAddress());
        this.assetsChainId = from.getAssetsChainId();
        this.assetsId = from.getAssetsId();
        this.amount = bigInteger2String(from.getAmount());
        this.nonce = Hex.toHexString(from.getNonce());
        this.locked = from.getLocked();
    }
}
