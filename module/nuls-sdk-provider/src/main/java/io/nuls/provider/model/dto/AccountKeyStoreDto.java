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

package io.nuls.provider.model.dto;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author: Charlie
 */
@Data
@NoArgsConstructor
@ApiModel(description = "账户KeyStoreJSON")
public class AccountKeyStoreDto {

    @ApiModelProperty(description = "账户地址")
    private String address;
    @ApiModelProperty(description = "加密后的私钥")
    private String encryptedPrivateKey;
    @ApiModelProperty(description = "公钥")
    private String pubKey;
    @ApiModelProperty(description = "私钥")
    private String prikey;

    public AccountKeyStoreDto(Map<String, Object> map) {
        this.address = (String) map.get("address");
        this.encryptedPrivateKey = null == map.get("encryptedPrivateKey") ? null : (String) map.get("encryptedPrivateKey");
        this.pubKey = null == map.get("pubKey") ? null : (String) map.get("pubKey");
        this.prikey = null == map.get("prikey") ? null : (String) map.get("prikey");
    }

}
