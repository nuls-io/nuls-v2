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

import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.JSONUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastTxTest extends ContractMakeAndBroadcastBase {

    private LinkedList params = new LinkedList();
    /**
     * 减少coinData的from的花费金额 ---> 目的：不支付合约手续费，偷走区块中其他交易的手续费
     */
    @Test
    public void fakeCreateTx_stealMoney() throws Exception {
        this.makeAndBroadcastCreateTxTest("createTxFake1_1", params, ContractBaseTransaction.class);
    }

    /**
     * 减少coinData的from的花费金额 ---> 目的：不支付合约手续费
     */
    @Test
    public void fakeCreateTx_notPaying() throws Exception {
        this.makeAndBroadcastCreateTxTest("createTxFake1_2", params, ContractBaseTransaction.class);
    }

    /**
     * 增加coinData的to
     */
    @Test
    public void fakeCreateTx_addCoinTo() throws Exception {
        this.makeAndBroadcastCreateTxTest("txFakeAddCoinTo", params, ContractBaseTransaction.class);
    }

    /**
     * 更改sender
     */
    @Test
    public void fakeCreateTx_ContractSender() throws Exception {
        params.add(toAddress6);
        this.makeAndBroadcastCreateTxTest("txFakeContractSender", params, ContractBaseTransaction.class, String.class);
    }

    /**
     * 更改contractAddress
     */
    @Test
    public void fakeCreateTx_ContractAddress() throws Exception {
        params.add(toAddress6);
        this.makeAndBroadcastCreateTxTest("txFakeContractAddress", params, ContractBaseTransaction.class, String.class);
    }

    /**
     * 更改gasLimit
     */
    @Test
    public void fakeCreateTx_GasLimit() throws Exception {
        params.add(200001L);
        this.makeAndBroadcastCreateTxTest("txFakeGasLimit", params, ContractBaseTransaction.class, Long.class);
    }

    /**
     * 更改price
     */
    @Test
    public void fakeCreateTx_Price() throws Exception {
        params.add(23L);
        this.makeAndBroadcastCreateTxTest("txFakePrice", params, ContractBaseTransaction.class, Long.class);
    }

    private void makeAndBroadcastCreateTxTest(String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
        Log.info("wait create.");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 空气币$$$$$$$$$$";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        String[][] args = ContractUtil.twoDimensionalArray(new Object[]{name, symbol, amount, decimals});

        Result result = this.makeCreateTx(chainId, toAddress5, 200000L, 25L, contractCode, args, password, remark);
        do {
            if (result.isFailed()) {
                break;
            }
            CreateContractTransaction tx = (CreateContractTransaction) result.getData();
            // 造假
            params.addFirst(tx);
            Method fakeMethod = this.getClass().getDeclaredMethod(fakeMethodName, parameterTypes);
            fakeMethod.invoke(this, params);

            tx.setTxData(null);
            tx.setCoinData(null);
            tx.serializeData();
            // 签名、广播交易
            result = this.broadcastCreateTx(tx);
        } while (false);

        Log.info("createContract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private void createTxFake(CreateContractTransaction tx) throws Exception {
        /**
         * 此刻的tx则可以任意修改原数据，以此造假数据
         */
        // 减少coinData的from的花费金额 ---> 目的：不支付合约手续费，偷走区块中其他交易的手续费
        this.createTxFake1_1(tx);

        // 减少coinData的from的花费金额 ---> 目的：不支付合约手续费
        this.createTxFake1_2(tx);

        // 增加coinData的to
        this.txFakeAddCoinTo(tx);

        // 更改sender
        this.txFakeContractSender(tx, toAddress6);

        // 更改contractAddress
        this.txFakeContractAddress(tx, toAddress6);

        // 更改gasLimit
        this.txFakeGasLimit(tx, 200001L);

        // 更改price
        this.txFakePrice(tx, 23L);
    }

    private void txFakePrice(ContractBaseTransaction tx, long price) throws Exception{
        BeanUtils.setProperty(tx.getTxDataObj(), "price", price);
    }

    private void txFakeGasLimit(ContractBaseTransaction tx, long gasLimit) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "gasLimit", gasLimit);
    }

    private void txFakeContractAddress(ContractBaseTransaction tx, String contractAddress) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "contractAddress", AddressTool.getAddress(contractAddress));
    }

    private void txFakeContractSender(ContractBaseTransaction tx, String sender) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "sender", AddressTool.getAddress(sender));
    }

    private void txFakeContractValue(ContractBaseTransaction tx, long value) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "value", BigInteger.valueOf(value));
    }

    private void txFakeAddCoinTo(ContractBaseTransaction tx) throws Exception {
        CoinData coinDataObj = tx.getCoinDataObj();
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(toAddress9), chainId, chain.getConfig().getAssetsId(),
                BigInteger.valueOf(1_0000L), 0L);
        coinDataObj.getTo().add(coinTo);

    }

    /**
     * 减少coinData的from的花费金额 ---> 目的：不支付合约手续费
     * 条件(二选一)：
     *      1_1 配合发送一个转账交易, 手续费设置大于合约的Gas消耗费用
     *          如果执行合约后，还有剩余的Gas，那么剩余的gas会按照Gas*price退还给用户(退还的手续费)，而伪造的交易没有真正花费Gas(修改coinFrom)
     *          那么，这部分手续费就会从打包区块中的其他交易的手续费扣出来退还给用户
     *          相当于，通过伪造这笔交易，偷走了区块中的一部分手续费，并且不支付执行合约的Gas手续费(前提：区块中有足够的的其他交易的手续费)
     *
     *      1_2 设置gaslimit刚好是执行合约的Gas数，没有合约退还金额，此时不需要再配合发送另外的交易来冲抵手续费
     *          此时，正确打包后的合约交易，消耗的手续费仅仅只有交易Size的手续费
     */
    private void createTxFake1_1(CreateContractTransaction tx) throws NulsException {
        // 配合发送转账交易
        TransferReq.TransferReqBuilder builder = new TransferReq.TransferReqBuilder(chain.getChainId(), chain.getConfig().getAssetsId())
                .addForm(toAddress0, password, BigInteger.valueOf(10001_0000_0000L))
                .addTo(toAddress5,BigInteger.valueOf(10000_0000_0000L));
        String transferHash = transferService.transfer(builder.build()).getData();
        Log.info("transfer tx hash is {}", transferHash);

        this.createTxFake1_base(tx);
    }

    //@Override
    //protected Result validCreateTx(int chainId, CreateContractTransaction tx) {
    //    try {
    //        CreateContractTxValidator validator = new CreateContractTxValidator();
    //        return validator.validate(chainId, tx);
    //    } catch (NulsException e) {
    //        Log.error(e);
    //        return Result.getFailed(e.getErrorCode());
    //    }
    //}

    private void createTxFake1_2(CreateContractTransaction tx) throws NulsException {
        this.createTxFake1_base(tx);
        tx.getTxDataObj().setGasLimit(15794L);
    }

    private void createTxFake1_base(CreateContractTransaction tx) throws NulsException {
        CoinData coinDataObj = tx.getCoinDataObj();
        CreateContractData contractData = tx.getTxDataObj();
        long gasLimit = contractData.getGasLimit();
        long price = contractData.getPrice();
        CoinFrom coinFrom = coinDataObj.getFrom().get(0);
        BigInteger amount = coinFrom.getAmount();
        amount = amount.subtract(BigInteger.valueOf(gasLimit).multiply(BigInteger.valueOf(price)));
        coinFrom.setAmount(amount);
    }


    /**
     * 调用合约的交易造假测试
     */
    @Test
    public void makeAndBroadcastCallTxTest() throws Exception {
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

    private void callTxFake(CallContractTransaction tx) throws Exception {
        /**
         * 此刻的tx则可以任意修改原数据，以此造假数据
         */
        // 增加coinData的to
        this.txFakeAddCoinTo(tx);

        // 更改sender
        this.txFakeContractSender(tx, toAddress6);

        // 更改转入合约的资金
        this.txFakeContractValue(tx, 1_0000_0000L);

        // 更改contractAddress
        this.txFakeContractAddress(tx, toAddress6);

        // 更改gasLimit
        this.txFakeGasLimit(tx, 200001L);

        // 更改price
        this.txFakePrice(tx, 23L);
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
