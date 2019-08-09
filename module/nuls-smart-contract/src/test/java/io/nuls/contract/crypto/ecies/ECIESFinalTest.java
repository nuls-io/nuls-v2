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
        //byte[] result = ECIESUtil.encrypt(userPubKey, "Information:Modules \"nuls-base\", \"nuls-core-rockdb\", \"nuls-core-rpc\", \"nuls-base-api-provider\", \"nuls-base-protocol-update\" and 我们认为NFT的使用案例由个人拥有和交易，以及托运给第三方经纪人/钱包/拍卖商（“运营商”）。NFT可以代表对数字或实物资产的所有权。我们考虑了各种各样的资产，我们知道你会想到更多：2 others were fully rebuilt due to project configuration/dependencies changes".getBytes(StandardCharsets.UTF_8));
        byte[] result = ECIESUtil.encrypt(userPubKey, "Information:Modules \"nuls-base\", \"nuls-core-rockdb\", \"nuls-core-rpc\", \"nuls-base-api-provider\", \"nuls-base-protocol-update\" and 我们认为NFT的使用案例由个人拥有和交易，以及托运给第三方经纪人/钱包/拍卖商（“运营商”）。NFT可以代表对数字或实物资产的所有权。我们考虑了各种各样的资产，我们知道你会想到更多：2 others were fully rebuilt due to project configuration/dependencies changes".getBytes(StandardCharsets.UTF_8));
        System.out.println(String.format("result is %s", HexUtil.encode(result)));
        int encryptSize = result.length - 65 -16 - 32;
        byte[] encryped = new byte[encryptSize];
        byte[] ephemPublicKey = new byte[65];
        byte[] iv = new byte[16];
        byte[] mac = new byte[32];

        System.arraycopy(result, 0, ephemPublicKey, 0, 65);
        System.arraycopy(result, 65, iv, 0, 16);
        System.arraycopy(result, 65 + 16, encryped, 0, encryptSize);
        System.arraycopy(result, result.length - 32, mac, 0, 32);

        System.out.println(String.format("ephemPublicKey is %s", HexUtil.encode(ephemPublicKey)));
        System.out.println(String.format("iv is %s", HexUtil.encode(iv)));
        System.out.println(String.format("encryped is %s", HexUtil.encode(encryped)));
        System.out.println(String.format("hmac is %s", HexUtil.encode(mac)));
        System.out.println();
        System.out.println();
        byte[] decrypt = ECIESUtil.decrypt(userPriKey, HexUtil.encode(result));
        System.out.println(new String(decrypt, StandardCharsets.UTF_8));
    }

    @Test
    public void testDecrypt() throws CryptoException {
        byte[] result = HexUtil.decode("04242f3a0069363c92a3448b56003066d0db7d84731363058ff7de613fcf1424dbf48519b3281c675928f8e479a5c3f4f4553c1cf5638fb700337a5c8574f39a34000000000000000000000000000000000bea771b417b69023f68e5129d611779ca4071e2cbef6554982807078a80e4f046638e4a8e4314fbef37f0356d90a340");
        byte[] decrypt = ECIESUtil.decrypt(userPriKey, HexUtil.encode(result));
        System.out.println(new String(decrypt, StandardCharsets.UTF_8));
    }

}
