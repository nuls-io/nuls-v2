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
package io.nuls.consensus.model.dto.transaction;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author: PierreLuo
 * @date: 2019-06-29
 */
@ApiModel
public class CoinFromDto extends CoinDto{

    @ApiModelProperty(description = "accountnonceValue ofHexString, prevent double flower transactions, retrieve the previous transactionhashAt the end of8Bytes")
    private String nonce;
    @ApiModelProperty(description = "0Ordinary transactions,-1Unlock amount transaction（Exit consensus, exit delegation）")
    private byte locked;

    public CoinFromDto() {}

    public CoinFromDto(CoinFrom from) {
        this.address = AddressTool.getStringAddressByBytes(from.getAddress());
        this.amount = from.getAmount().toString();
        this.assetsChainId = from.getAssetsChainId();
        this.assetsId = from.getAssetsId();
        this.nonce = HexUtil.encode(from.getNonce());
        this.locked = from.getLocked();
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public byte getLocked() {
        return locked;
    }

    public void setLocked(byte locked) {
        this.locked = locked;
    }
}
