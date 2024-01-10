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
package io.nuls.contract.tx.customizetx;

import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.util.Log;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastTxTest extends ContractMakeAndBroadcastBase {

    /**
     * Transaction fraud testing for creating contracts
     */

    /**
     * reducecoinDataoffromThe amount of expenses incurred ---> objective：Not paying contract fees and stealing fees from other transactions in the block
     */
    @Test
    public void fakeCreateTx_stealMoney() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        createTxFake1_1((CreateContractTransaction) tx);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * reducecoinDataoffromThe amount of expenses incurred ---> objective：Not paying contract fees
     */
    @Test
    public void fakeCreateTx_notPaying() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        createTxFake1_2((CreateContractTransaction) tx);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * increasecoinDataofto
     */
    @Test
    public void fakeCreateTx_addCoinTo() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    // Test network black hole address - tNULSeBaMhZnRteniCy3UZqPjTbnWKBPHX1a5d
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeAddCoinTo(tx, "tNULSeBaMhZnRteniCy3UZqPjTbnWKBPHX1a5d", 1_0000L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changesender
     */
    @Test
    public void fakeCreateTx_ContractSender() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractSender(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changecontractAddress
     */
    @Test
    public void fakeCreateTx_ContractAddress() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractAddress(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changegasLimit
     */
    @Test
    public void fakeCreateTx_GasLimit() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeGasLimit(tx, 200001L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changeprice
     */
    @Test
    public void fakeCreateTx_Price() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakePrice(tx, 23L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * Creating contracts normally
     */
    @Test
    public void fakeCreateTx_normal() throws Exception {
        new MakeAndBroadcastCreateTxTest()
                .make()
                .sign()
                .validate()
                .broadcast();
    }

    /**
     * reducecoinDataoffromThe amount of expenses incurred ---> objective：Not paying contract fees
     * condition(Choose one from two)：
     *      1_1 Cooperate in sending a transfer transaction, The handling fee setting is greater than the contractGasConsumption expenses
     *          If there is any remaining balance after executing the contractGasSo the remaininggasWill followGas*priceReturn to user(Refund of handling fees)And the forged transaction did not actually cost anythingGas(modifycoinFrom)
     *          So, this portion of the transaction fee will be deducted from the transaction fee of other transactions in the packaged block and refunded to the user
     *          Equivalent to, by forging this transaction, stealing a portion of the transaction fee from the block and not paying for the execution contractGasHandling fees(premise：There are sufficient transaction fees for other transactions in the block)
     *
     *      1_2 set upgaslimitCoincidentally executing the contractGasNumber, there is no contract to refund the amount, and there is no need to cooperate in sending another transaction to offset the handling fee at this time
     *          At this point, the correctly packaged contract transaction only consumes transaction feesSizeHandling fees for
     */
    private void createTxFake1_1(CreateContractTransaction tx) throws NulsException {
        // Cooperate in sending transfer transactions
        TransferReq.TransferReqBuilder builder = new TransferReq.TransferReqBuilder(chain.getChainId(), chain.getConfig().getAssetId())
                .addForm(toAddress0, password, BigInteger.valueOf(10001_0000_0000L))
                .addTo(toAddress5,BigInteger.valueOf(10000_0000_0000L));
        String transferHash = transferService.transfer(builder.build(new TransferReq())).getData();
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
     * Transaction fraud test for calling contracts
     */


    private String tokenContractAddress() {
        return "tNULSeBaN4BSA6qo2J6eEfCx2rNyyY2TRwhhe8";
    }
    private String contractCallContractAddress() {
        return "tNULSeBaN9GKPn7e2ZVDtnYHAaL8bRpvKk66xK";
    }

    /**
     * increasecoinDataofto
     */
    @Test
    public void fakeCallTx_addCoinTo() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastCallTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeAddCoinTo(tx, contractAddress, 1_0000_0000L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changecoinDataofto
     */
    @Test
    public void fakeCallTx_modifyCoinTo() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastCallTxTest()
                .setLongValue(2_0000_0000L)
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeModifyCoinTo(tx, contractAddress, 1_0000_0000L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changesender
     */
    @Test
    public void fakeCallTx_ContractSender() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastCallTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractSender(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changecontractAddress
     */
    @Test
    public void fakeCallTx_ContractAddress() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastCallTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractAddress(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changegasLimit
     */
    @Test
    public void fakeCallTx_GasLimit() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastCallTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeGasLimit(tx, 20001L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changeprice
     */
    @Test
    public void fakeCallTx_Price() throws Exception {
        contractAddress = tokenContractAddress();
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakePrice(tx, 23L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changeprice, gasLimit
     */
    @Test
    public void fakeCallTx_PriceAndGasLimit() throws Exception {
        contractAddress = contractCallContractAddress();
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeGasLimit(tx, 20001L);
                        txFakePrice(tx, 25L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changevalue
     */
    @Test
    public void fakeCallTx_Value() throws Exception {
        contractAddress = contractCallContractAddress();
        sender = toAddress0;
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractValue(tx, 3_0000_0001L);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * Normal Call Contract
     */
    @Test
    public void fakeCallTx_normal() throws Exception {
        contractAddress = contractCallContractAddress();
        sender = toAddress0;
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .sign()
                .validate()
                .broadcast();
    }


    /**
     * Transaction fraud test for deleting contracts
     */

    /**
     * changesender
     */
    @Test
    public void fakeDeleteTx_ContractSender() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastDeleteTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractSender(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * changecontractAddress
     */
    @Test
    public void fakeDeleteTx_ContractAddress() throws Exception {
        contractAddress = tokenContractAddress();
        new MakeAndBroadcastDeleteTxTest()
                .make()
                .fake(new ExecuteFake() {
                    @Override
                    public void execute(ContractBaseTransaction tx) throws Exception {
                        txFakeContractAddress(tx, toAddress6);
                    }
                })
                .signAndBroadcast();
    }

    /**
     * Non contract creators cannot delete contracts
     */
    @Test
    public void fakeDeleteTx_NonCreatorCannotDeleteContract() throws Exception {
        contractAddress = tokenContractAddress();
        sender = toAddress7;
        new MakeAndBroadcastDeleteTxTest()
                .make()
                .signAndBroadcast();
    }

    /**
     * The balance is not0
     */
    @Test
    public void fakeDeleteTx_BalanceRemaining() throws Exception {
        contractAddress = contractCallContractAddress();
        String orginSender = sender;
        methodName = ContractConstant.BALANCE_TRIGGER_METHOD_NAME;
        sender = toAddress0;
        new MakeAndBroadcastCallTxTest(2_0000_0000L, new Object[0])
                .make()
                .signAndBroadcast();

        TimeUnit.SECONDS.sleep(1);

        sender = orginSender;
        new MakeAndBroadcastDeleteTxTest()
                .make()
                .signAndBroadcast();
    }

    /**
     * Normal deletion of contracts
     */
    @Test
    public void fakeDeleteTx_normal() throws Exception {
        contractAddress = tokenContractAddress();
        String orginSender = sender;

        sender = orginSender;
        new MakeAndBroadcastDeleteTxTest()
                .make()
                .sign()
                .validate()
                .broadcast();
    }
}
