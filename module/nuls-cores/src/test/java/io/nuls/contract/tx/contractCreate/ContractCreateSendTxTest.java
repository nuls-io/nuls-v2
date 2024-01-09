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
package io.nuls.contract.tx.contractCreate;

import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.TOKEN_TRANSFER;

/**
 * @author: PierreLuo
 * @date: 2019-12-06
 */
public class ContractCreateSendTxTest extends BaseQuery {

    String contractA = "tNULSeBaN7MCLavwRjJqH28w9NCmahUbybYC6K";

    @Test
    public void createNRC1155() throws Exception {
        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/NULS-NRC1155Demo/target/NULS-NRC1155Demo-1.0-SNAPSHOT.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create 1155 A";
        String create = this.invokeCreate(sender, contractCode, "test_c", remark, "asdasdasd3", "name1155", "SYMBOL1155v6");
        System.out.println(create);
    }
    @Test
    public void createAndInit() throws Exception {
        this.contractA = createContractA();
        System.out.println(contractA);
    }

    @Test
    public void testCreate() throws Exception {
        contractA = "tNULSeBaMznzh3a6QZedEABuMiydtN4QgV7Euh";
        Map resultA = this.invokeCall(sender, null, contractA, "createContract4", null, null,
                new String[]{"tNULSeBaN7MCLavwRjJqH28w9NCmahUbybYC6K", "2"});
        resultCheck(resultA);
    }

    @Test
    public void testInnerCallForCreate() throws Exception {
        contractA = "tNULSeBaMznzh3a6QZedEABuMiydtN4QgV7Euh";
        Map resultA = this.invokeCall(sender, null, contractA, "innerCall", null, null,
                new String[]{"tNULSeBaN8w6ivxm6nqPMrUVV5hp1gVVd3HpDA", "tccc9"});
        resultCheck(resultA);
    }

    @Test
    public void testCreate2() throws Exception {
        Map resultA = this.invokeCall(sender, null, contractA, "createContract2", null, null,
                new Object[]{"tNULSeBaN8KDbAPBme3tPNUd69pWSZsqYPz3ym", "pocm0", new Object[]{"tNULSeBaMy4cmp1uhrDPDLBGhXSixFQ8e32JJp", 0, 0, 20000000, 200000000000000L, 1, 10000000000L, true, false, "a"}});
        resultCheck(resultA);
    }

    @Test
    public void testASD() throws Exception {
        Map resultA = this.invokeCall(sender, null, "tNULSeBaMxq6jYc7TeRfeMb8EHHHaiXqC1A6TQ", "submit", null, null,
                new String[]{"aaa", "111"});
        resultCheck(resultA);
    }

    @Test
    public void invokeViewTest() throws Exception {
        contractA = "tNULSeBaMznzh3a6QZedEABuMiydtN4QgV7Euh";
        String view = this.invokeView(contractA, "decimals", 2, 1);
        System.out.println(view);
    }

    /**
     * token转账
     */
    @Test
    public void tokenTransfer() throws Exception {
        String token = "tNULSeBaN1t29KzTAVQMKaYup5uyK7raQUGoNY";
        String to = "tNULSeBaNRJrWyAfNtA6aiAozaJdemWA5WbBFU";
        BigInteger value = new BigDecimal("10").movePointRight(8).toBigInteger();
        String remark = "token transfer to " + to;
        Map params = this.makeTokenTransferParams(sender, to, token, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    private void resultCheck(Map resultA) {
        Map contractResult = (Map) resultA.get("contractResult");
        if (contractResult.get("flag") != null) {
            contractResult = (Map) contractResult.get("data");
        }
        boolean success = Boolean.parseBoolean(contractResult.get("success").toString());
        Assert.assertTrue("expect success, " + contractResult.get("errorMessage") + ", " + contractResult.get("stackTrace"), success);
    }


    private String createContractA() throws Exception {
        Log.info("开始创建虚拟机测试合约A");
        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testA/target/contract-vm-testA-testA.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create test A";
        return this.invokeCreate(sender, contractCode, "test_a", remark);
    }
}
