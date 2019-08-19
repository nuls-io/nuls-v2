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

package io.nuls.transaction;

import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.nuls.transaction.TestCommonUtil.*;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TestJyc {

    private static Chain chain;

    @BeforeClass
    public static void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(CHAIN_ID, ASSET_ID, 1024 * 1024, 1000, 20, 20000, 60000));
    }

    /**
     * 导入种子节点
     */
    @Test
    public void importSeed() {
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", PASSWORD);//tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", PASSWORD);//tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
//        importPriKey("14a37507d42e474b45e7f2914c4fc317bbf3a428f6d9a398f5719a3be6bb74b1", PASSWORD);//tNULSeBaMjESuVomqR74SbUmTHwQGEKAeE9awT
//        importPriKey("60bdc4d03a10de2f86f351f2e7cecc2d306b7150265e19727148f1c51bec2fd8", PASSWORD);//tNULSeBaMtsumpXhfEZBU2pMEz7SHLcx5b2TQr
//        importPriKey("7769721125746a25ebd8cbd8f2b39c54dfb82eefd918cd6d940580bed2a758d1", PASSWORD);//tNULSeBaMkwmNkUJGBkdAkUaddbTnQ1tzBUqkT
//        importPriKey("6420b85c05334451688dfb5d01926bef98699c9e914dc262fcc3f625c04d2fd5", PASSWORD);//tNULSeBaMhwGMdTsVZC6Gg8ad5XA8CjZpR95MK
//        importPriKey("146b6920c0992bd7f3a434651462fe47f446c385636d35d2085035b843458467", PASSWORD);//tNULSeBaMqt2J3V8TdY69Gwb2yPCpeRaHn5tW6
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
//        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", PASSWORD);//tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
//        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", PASSWORD);//tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
//        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", PASSWORD);//tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
//        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", PASSWORD);//tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
//        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", PASSWORD);//tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
//        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", PASSWORD);//tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
//        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", PASSWORD);//tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
//        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", PASSWORD);//tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
//        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", PASSWORD);//tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn
//        importPriKey("00a6eef7b91c645525bb8410f2a79e1299a69d0d7ef980068434b6aca90ab6d9", PASSWORD);//tNULSeBaMiAQSiqXHBUypfMGZzcroe12W4SFbi
    }

}
