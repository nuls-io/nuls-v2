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
package io.nuls.contract.crypto.ecies;

import io.nuls.core.crypto.ECIESUtil;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.CryptoException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author: PierreLuo
 * @date: 2019-06-01
 */
public class ECIESFinalTest {

    byte[] userPriKey = HexUtil.decode("1523eb8a85e8bb6641f8ae53c429811ede7ea588c4b8933fed796c667c203c06");
    byte[] userPubKey = HexUtil.decode("02fd82681e79fbe293aef1a48c6c9b1252591340bb46de1444ad5de400ff84a433");

    @Test
    public void encryptAndDecryptTest() throws CryptoException {
        byte[] result = ECIESUtil.encrypt(userPubKey, "Information:Modules \"nuls-base\", \"nuls-core-rockdb\", \"nuls-core-rpc\", \"nuls-base-api-provider\", \"nuls-base-protocol-update\" and 我们认为NFT的使用案例由个人拥有和交易，以及托运给第三方经纪人/钱包/拍卖商（“运营商”）。NFT可以代表对数字或实物资产的所有权。我们考虑了各种各样的资产，我们知道你会想到更多：2 others were fully rebuilt due to project configuration/dependencies changes".getBytes(StandardCharsets.UTF_8));
        int encryptSize = result.length - 65 - 32;
        byte[] encryped = new byte[encryptSize];
        byte[] ephemPublicKey = new byte[65];
        byte[] mac = new byte[32];
        System.arraycopy(result, 0, encryped, 0, encryptSize);
        System.arraycopy(result, encryptSize, ephemPublicKey, 0, 65);
        System.arraycopy(result, encryptSize + 65, mac, 0, 32);
        System.out.println(HexUtil.encode(encryped));
        System.out.println(HexUtil.encode(ephemPublicKey));
        System.out.println(HexUtil.encode(mac));
        System.out.println();
        System.out.println();
        byte[] decrypt = ECIESUtil.decrypt(userPriKey, HexUtil.encode(result));
        System.out.println(new String(decrypt, StandardCharsets.UTF_8));
    }

}
