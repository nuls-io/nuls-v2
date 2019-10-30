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

package io.nuls.contract.tx.randomseed;


import io.nuls.contract.sdk.annotation.View;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class RandomSeedSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(RandomSeedSendTxTest.class.getResource("/randomseed-test1.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "random";
        Object[] args = new Object[]{};
        Map params = this.makeCreateParams(sender, contractCode, "randomseed", remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     */
    @Test
    public void getRandomSeedByCountAndAlg() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "getRandomSeedByCountAndAlg";
        String methodDesc = "";
        String remark = "getRandomSeedByCountAndAlg";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5, "sha3");
    }

    /**
     */
    @Test
    public void gasTest() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "gasTest";
        String methodDesc = "";
        String remark = "gasTest";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5, "sha3");
    }
    /**
     */
    @Test
    public void dice() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "dice";
        String methodDesc = "";
        String remark = "dice";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5, 12, 6);
    }
    /**
     */
    @Test
    public void diceAnother() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "diceAnother";
        String methodDesc = "";
        String remark = "diceAnother";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5, 12, 6);
    }
    /**
     */
    @Test
    public void getRandomSeedByCount() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "getRandomSeedByCount";
        String methodDesc = "";
        String remark = "getRandomSeedByCount";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5);
    }
    /**
     */
    @Test
    public void getRandomSeedListByCount() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "getRandomSeedListByCount";
        String methodDesc = "";
        String remark = "getRandomSeedListByCount";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 10, 5);
    }
    /**
     */
    @Test
    public void getRandomSeedByHeight() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "getRandomSeedByHeight";
        String methodDesc = "";
        String remark = "getRandomSeedByHeight";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 5, 10);
    }
    /**
     */
    @Test
    public void getRandomSeedListByHeight() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "getRandomSeedListByHeight";
        String methodDesc = "";
        String remark = "getRandomSeedListByHeight";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 5, 10);
    }

    /**
     * 调用视图方法 - 合约整体信息
     */
    @View
    @Test
    public void getContractWholeInfo() throws Exception {
        Log.info("viewRandomSeedByCountAndAlg is {}", invokeView(contractAddress, "viewRandomSeedByCountAndAlg", 10, 5, "sha3"));
        Log.info("viewRandomSeedByCount is {}", invokeView(contractAddress, "viewRandomSeedByCount", 10, 5));
        Log.info("viewRandomSeedByHeightAndAlg is {}", invokeView(contractAddress, "viewRandomSeedByHeightAndAlg", 5, 10, "sha3"));
        Log.info("viewRandomSeedByHeight is {}", invokeView(contractAddress, "viewRandomSeedByHeight", 5, 10));
        Log.info("viewRandomSeedListByCount is {}", invokeView(contractAddress, "viewRandomSeedListByCount", 10, 5));
        Log.info("viewRandomSeedListByHeight is {}", invokeView(contractAddress, "viewRandomSeedListByHeight", 5, 10));
    }

    protected void invokeCall(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) throws Exception {
        super.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, args);
        TimeUnit.SECONDS.sleep(1);
        getContractWholeInfo();
    }

}
