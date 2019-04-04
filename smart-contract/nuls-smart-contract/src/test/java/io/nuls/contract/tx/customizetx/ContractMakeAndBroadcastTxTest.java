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
package io.nuls.contract.tx.customizetx;

import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.tools.basic.Result;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastTxTest extends ContractMakeAndBroadcastBase {

    /**
     * 创建合约的交易造假测试
     */
    @Test
    public void makeAndBroadcastCreateTxTest() throws IOException {
        Log.info("wait create.");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 空气币$$$$$$$$$$";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        String[][] args = ContractUtil.twoDimensionalArray(new Object[]{name, symbol, amount, decimals});

        Result result = this.makeCreateTx(chainId, sender, 200000L, 25L, contractCode, args, password, remark);
        do {
            if (result.isFailed()) {
                break;
            }
            CreateContractTransaction tx = (CreateContractTransaction) result.getData();
            // 造假
            this.createTxFake(tx);
            tx.setTxData(null);
            tx.setCoinData(null);
            tx.serializeData();
            // 签名、广播交易
            result = this.broadcastCreateTx(tx);
        } while (false);

        Log.info("createContract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private void createTxFake(CreateContractTransaction tx) {
        /**
         * 此刻的tx则可以任意修改原数据，以此造假数据
         */
        //TODO ....
    }


    /**
     * 调用合约的交易造假测试
     */
    @Test
    public void makeAndBroadcastCallTxTest() throws IOException {
        Log.info("wait call.");
        BigInteger value = BigInteger.ZERO;
        if(StringUtils.isBlank(methodName)) {
            methodName = "transfer";
        }
        if(StringUtils.isBlank(tokenReceiver)) {
            tokenReceiver = toAddress1;
        }
        String methodDesc = "";
        String remark = "call contract test - 空气币转账";
        String token = BigInteger.valueOf(800L).toString();
        String[][] args = ContractUtil.twoDimensionalArray(new Object[]{tokenReceiver, token});
        Result result = this.makeCallTx(chainId, sender, value, 20000L, 25L, contractAddress_nrc20,
                methodName, methodDesc, args, password, remark);
        do {
            if (result.isFailed()) {
                break;
            }
            CallContractTransaction tx = (CallContractTransaction) result.getData();
            // 造假
            this.callTxFake(tx);
            tx.setTxData(null);
            tx.setCoinData(null);
            tx.serializeData();
            // 签名、广播交易
            result = this.broadcastCallTx(tx);
        } while (false);

        Log.info("callContract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private void callTxFake(CallContractTransaction tx) {
        /**
         * 此刻的tx则可以任意修改原数据，以此造假数据
         */
        //TODO ....
    }


    /**
     * 删除合约的交易造假测试
     */
    @Test
    public void makeAndBroadcastDeleteTxTest() throws IOException {
        Log.info("wait delete.");
        String remark = "delete contract";
        Result result = this.makeDeleteTx(chainId, sender, contractAddress, password, remark);
        do {
            if (result.isFailed()) {
                break;
            }
            DeleteContractTransaction tx = (DeleteContractTransaction) result.getData();
            // 造假
            this.deleteTxFake(tx);
            tx.setTxData(null);
            tx.setCoinData(null);
            tx.serializeData();
            // 签名、广播交易
            result = this.broadcastDeleteTx(tx);
        } while (false);

        Log.info("callContract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private void deleteTxFake(DeleteContractTransaction tx) {
        /**
         * 此刻的tx则可以任意修改原数据，以此造假数据
         */
        //TODO ....
    }

}
