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
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.util.Log;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastTxTest extends ContractMakeAndBroadcastBase {

    private LinkedList params = new LinkedList();
    /**
     * 创建合约的交易造假测试
     */

    /**
     * 减少coinData的from的花费金额 ---> 目的：不支付合约手续费，偷走区块中其他交易的手续费
     */
    @Test
    public void fakeCreateTx_stealMoney() throws Exception {
        this.makeAndBroadcastCreateTxTest("createTxFake1_1", params, CreateContractTransaction.class);
    }

    /**
     * 减少coinData的from的花费金额 ---> 目的：不支付合约手续费
     */
    @Test
    public void fakeCreateTx_notPaying() throws Exception {
        this.makeAndBroadcastCreateTxTest("createTxFake1_2", params, CreateContractTransaction.class);
    }

    /**
     * 增加coinData的to
     */
    @Test
    public void fakeCreateTx_addCoinTo() throws Exception {
        params.add(toAddress9);
        params.add(1_0000L);
        this.makeAndBroadcastCreateTxTest("txFakeAddCoinTo", params, ContractBaseTransaction.class, String.class, long.class);
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
        this.makeAndBroadcastCreateTxTest("txFakeGasLimit", params, ContractBaseTransaction.class, long.class);
    }

    /**
     * 更改price
     */
    @Test
    public void fakeCreateTx_Price() throws Exception {
        params.add(23L);
        this.makeAndBroadcastCreateTxTest("txFakePrice", params, ContractBaseTransaction.class, long.class);
    }

    /**
     * 此刻的tx则可以任意修改原数据，以此造假数据
     */

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

    @Before
    public void callBefore() {
        contractAddress = "tNULSeBaN155SwgcURmRwBMzmjKAH3PwK55tSe";
    }

    /**
     * 增加coinData的to
     */
    @Test
    public void fakeCallTx_addCoinTo() throws Exception {
        params.add(contractAddress);
        params.add(1_00_0000L);
        this.makeAndBroadcastCallTxTest("txFakeAddCoinTo", params, ContractBaseTransaction.class, String.class, long.class);
    }

    /**
     * 更改coinData的to
     */
    @Test
    public void fakeCallTx_modifyCoinTo() throws Exception {
        contractAddress = "tNULSeBaMwfzgrVkRCqREUxSju77wQvsruEJ5d";
        params.add(contractAddress);
        params.add(1_0000_0000L);
        this.makeAndBroadcastCallTxTest(2_0000_0000L, "txFakeModifyCoinTo", params, ContractBaseTransaction.class, String.class, long.class);
    }

    /**
     * 更改sender
     */
    @Test
    public void fakeCallTx_ContractSender() throws Exception {
        params.add(toAddress6);
        this.makeAndBroadcastCallTxTest("txFakeContractSender", params, ContractBaseTransaction.class, String.class);
    }

    /**
     * 更改contractAddress
     */
    @Test
    public void fakeCallTx_ContractAddress() throws Exception {
        params.add(toAddress6);
        this.makeAndBroadcastCallTxTest("txFakeContractAddress", params, ContractBaseTransaction.class, String.class);
    }

    /**
     * 更改gasLimit
     */
    @Test
    public void fakeCallTx_GasLimit() throws Exception {
        params.add(20001L);
        this.makeAndBroadcastCallTxTest("txFakeGasLimit", params, ContractBaseTransaction.class, long.class);
    }

    /**
     * 更改price
     */
    @Test
    public void fakeCallTx_Price() throws Exception {
        params.add(23L);
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        this.makeAndBroadcastCallTxTest(2_0000_0000L, new Object[0], "txFakePrice", params, ContractBaseTransaction.class, long.class);
    }

    /**
     * 更改price, gasLimit
     */
    @Test
    public void fakeCallTx_PriceAndGasLimit() throws Exception {
        LinkedList priceParam = new LinkedList();
        priceParam.add(25L);
        LinkedList gasLimitParam = new LinkedList();
        gasLimitParam.add(20000L);
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .invokeFake("txFakePrice", priceParam, ContractBaseTransaction.class, long.class)
                .invokeFake("txFakeGasLimit", gasLimitParam, ContractBaseTransaction.class, long.class)
                .broadcast();
    }

    /**
     * 更改value
     */
    @Test
    public void fakeCallTx_Value() throws Exception {
        contractAddress = "tNULSeBaN155SwgcURmRwBMzmjKAH3PwK55tSe";
        params.add(3_0000_0001L);
        sender = toAddress0;
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        this.makeAndBroadcastCallTxTest(2_0000_0000L, new Object[0], "txFakeContractValue", params, ContractBaseTransaction.class, long.class);
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
