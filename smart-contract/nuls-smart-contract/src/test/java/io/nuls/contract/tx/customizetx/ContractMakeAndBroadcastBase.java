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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.LedgerUnConfirmedTxStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTxHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.rpc.call.AccountCall;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.JSONUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.FAILED;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastBase extends BaseQuery {

    private ContractTxHelper contractTxHelper;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        ContractHelper contractHelper = new ContractHelper();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chainId, chain);
        Field field1 = ContractHelper.class.getDeclaredField("chainManager");
        field1.setAccessible(true);
        field1.set(contractHelper, chainManager);

        contractTxHelper = new ContractTxHelper();
        Field field = ContractTxHelper.class.getDeclaredField("contractHelper");
        field.setAccessible(true);
        field.set(contractTxHelper, contractHelper);

        Log.info("bean init.");
    }


    protected Result<CreateContractTransaction> makeCreateTx(int chainId, String sender, Long gasLimit, Long price,
                                                           byte[] contractCode, String[][] args,
                                                           String password, String remark) {
        try {
            Result accountResult = AccountCall.validationPassword(chainId, sender, password);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            // 生成一个地址作为智能合约地址
            String contractAddress = AccountCall.createContractAddress(chainId);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            byte[] senderBytes = AddressTool.getAddress(sender);
            return contractTxHelper.newCreateTx(chainId, sender, senderBytes, contractAddressBytes, gasLimit, price, contractCode, args, remark);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    protected Result broadcastCreateTx(CreateContractTransaction tx) {
        try {
            CoinData coinDataObj = tx.getCoinDataObj();
            byte[] txCreator = coinDataObj.getFrom().get(0).getAddress();
            ContractData contractData = tx.getTxDataObj();
            byte[] contractAddressBytes = contractData.getContractAddress();
            Result result = this.broadcastTx(chainId, AddressTool.getStringAddressByBytes(txCreator), password, tx);
            if(result.isFailed()) {
                return result;
            }
            Map<String, String> resultMap = MapUtil.createHashMap(2);
            String txHash = tx.getHash().getDigestHex();
            String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddressBytes);
            resultMap.put("txHash", txHash);
            resultMap.put("contractAddress", contractAddressStr);
            return getSuccess().setData(resultMap);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }


    protected Result<CallContractTransaction> makeCallTx(int chainId, String sender, BigInteger value, Long gasLimit, Long price, String contractAddress,
                                                      String methodName, String methodDesc, String[][] args,
                                                      String password, String remark) {
        if (value == null) {
            value = BigInteger.ZERO;
        }
        Result accountResult = AccountCall.validationPassword(chainId, sender, password);
        if (accountResult.isFailed()) {
            return accountResult;
        }
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        byte[] senderBytes = AddressTool.getAddress(sender);
        return contractTxHelper.newCallTx(chainId, sender, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, args, remark);
    }

    protected Result broadcastCallTx(CallContractTransaction tx) {
        try {
            CoinData coinDataObj = tx.getCoinDataObj();
            byte[] txCreator = coinDataObj.getFrom().get(0).getAddress();
            Result result = this.broadcastTx(chainId, AddressTool.getStringAddressByBytes(txCreator), password, tx);
            if(result.isFailed()) {
                return result;
            }
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("txHash", tx.getHash().getDigestHex());
            return getSuccess().setData(resultMap);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }


    protected Result<DeleteContractTransaction> makeDeleteTx(int chainId, String sender, String contractAddress, String password, String remark) {
        Result accountResult = AccountCall.validationPassword(chainId, sender, password);
        if (accountResult.isFailed()) {
            return accountResult;
        }
        byte[] senderBytes = AddressTool.getAddress(sender);
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        return contractTxHelper.newDeleteTx(chainId, sender, senderBytes, contractAddressBytes, remark);
    }

    protected Result broadcastDeleteTx(DeleteContractTransaction tx) {
        try {
            CoinData coinDataObj = tx.getCoinDataObj();
            byte[] txCreator = coinDataObj.getFrom().get(0).getAddress();
            Result result = this.broadcastTx(chainId, AddressTool.getStringAddressByBytes(txCreator), password, tx);
            if(result.isFailed()) {
                return result;
            }
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("txHash", tx.getHash().getDigestHex());
            return getSuccess().setData(resultMap);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    private Result broadcastTx(int chainId, String sender, String password, Transaction tx) {
        try {
            // 生成交易hash
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            // 生成签名
            AccountCall.transactionSignature(chainId, sender, password, tx);
            String txData = RPCUtil.encode(tx.serialize());

            Result validResult = this.validTx(chainId, tx);
            if(validResult.isFailed()) {
                return validResult;
            }
            // 通知账本
            int commitStatus = LedgerCall.commitUnconfirmedTx(chainId, txData);
            if(commitStatus != LedgerUnConfirmedTxStatus.SUCCESS.status()) {
                return getFailed().setMsg(LedgerUnConfirmedTxStatus.getStatus(commitStatus).name());
            }
            // 广播交易
            boolean broadcast = TransactionCall.newTx(chainId, txData);
            if (!broadcast) {
                // 广播失败，回滚账本的未确认交易
                LedgerCall.rollBackUnconfirmTx(chainId, txData);
                return getFailed();
            }
            return getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    private Result validTx(int chainId, Transaction tx) {
        try {
            Result result;
            switch (tx.getType()) {
                case TX_TYPE_CREATE_CONTRACT:
                    result = validCreateTx(chainId, (CreateContractTransaction) tx);
                    break;
                case TX_TYPE_CALL_CONTRACT:
                    result = validCallTx(chainId, (CallContractTransaction) tx);
                    break;
                case TX_TYPE_DELETE_CONTRACT:
                    result = validDeleteTx(chainId, (DeleteContractTransaction) tx);
                    break;
                default:
                    result = getSuccess();
                    break;
            }
            return result;
        } catch (Exception e) {
            return getFailed();
        }
    }

    protected Result validCreateTx(int chainId, CreateContractTransaction tx) {
        return getSuccess();
    }
    protected Result validCallTx(int chainId, CallContractTransaction tx) {
        return getSuccess();
    }
    protected Result validDeleteTx(int chainId, DeleteContractTransaction tx) {
        return getSuccess();
    }

    protected void txFakePrice(ContractBaseTransaction tx, long price) throws Exception{
        BeanUtils.setProperty(tx.getTxDataObj(), "price", price);
    }

    public void txFakeGasLimit(ContractBaseTransaction tx, long gasLimit) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "gasLimit", gasLimit);
    }

    protected void txFakeContractAddress(ContractBaseTransaction tx, String contractAddress) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "contractAddress", AddressTool.getAddress(contractAddress));
    }

    protected void txFakeContractSender(ContractBaseTransaction tx, String sender) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "sender", AddressTool.getAddress(sender));
    }

    protected void txFakeContractValue(ContractBaseTransaction tx, long value) throws Exception {
        BeanUtils.setProperty(tx.getTxDataObj(), "value", BigInteger.valueOf(value));
    }

    protected void txFakeAddCoinTo(ContractBaseTransaction tx, String address, long amount) throws Exception {
        CoinData coinDataObj = tx.getCoinDataObj();
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(address), chainId, chain.getConfig().getAssetsId(),
                BigInteger.valueOf(amount), 0L);
        coinDataObj.getTo().add(coinTo);
    }

    protected void txFakeModifyCoinTo(ContractBaseTransaction tx, String address, long amount) throws Exception {
        CoinData coinDataObj = tx.getCoinDataObj();
        CoinTo coinTo = coinDataObj.getTo().get(0);
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(amount));
    }

    protected void makeAndBroadcastCreateTxTest(String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
        new MakeAndBroadcastCreateTxTest().make().invokeFake(fakeMethodName, params, parameterTypes).broadcast();
    }

    class MakeAndBroadcastCreateTxTest {

        CreateContractTransaction tx;

        MakeAndBroadcastCreateTxTest make() throws Exception {
            Log.info("wait create.");
            InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
            byte[] contractCode = IOUtils.toByteArray(in);
            String remark = "create contract test - 空气币";
            String name = "KQB";
            String symbol = "KongQiBi";
            String amount = BigDecimal.TEN.pow(10).toPlainString();
            String decimals = "2";
            String[][] args = ContractUtil.twoDimensionalArray(new Object[]{name, symbol, amount, decimals});

            Result result = ContractMakeAndBroadcastBase.this.makeCreateTx(chainId, sender, 200000L, 25L, contractCode, args, password, remark);
            if (result.isFailed()) {
                throw new RuntimeException(result.getMsg());
            }
            this.tx = (CreateContractTransaction) result.getData();
            return this;
        }

        MakeAndBroadcastCreateTxTest invokeFake(String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
            // 造假
            params.addFirst(tx);
            Method fakeMethod;
            try {
                fakeMethod = ContractMakeAndBroadcastBase.this.getClass().getDeclaredMethod(fakeMethodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                fakeMethod = ContractMakeAndBroadcastBase.this.getClass().getSuperclass().getDeclaredMethod(fakeMethodName, parameterTypes);
            }
            fakeMethod.invoke(ContractMakeAndBroadcastBase.this, params.toArray());
            return this;
        }

        void broadcast() throws IOException {
            if(tx != null) {
                tx.setTxData(null);
                tx.setCoinData(null);
                tx.serializeData();
                // 签名、广播交易
                Result result = broadcastCreateTx(tx);
                Log.info("createContract-result:{}", JSONUtils.obj2PrettyJson(result));
            }
        }
    }


    protected void makeAndBroadcastCallTxTest(String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
        //this.makeAndBroadcastCallTxTest(null, null, fakeMethodName, params, parameterTypes);
        new MakeAndBroadcastCallTxTest(null, null).make().invokeFake(fakeMethodName, params, parameterTypes).broadcast();
    }

    protected void makeAndBroadcastCallTxTest(Long longValue, String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
        //this.makeAndBroadcastCallTxTest(longValue, null, fakeMethodName, params, parameterTypes);
        new MakeAndBroadcastCallTxTest(longValue, null).make().invokeFake(fakeMethodName, params, parameterTypes).broadcast();
    }

    protected void makeAndBroadcastCallTxTest(Long longValue, Object[] objArgs, String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
        new MakeAndBroadcastCallTxTest(longValue, objArgs).make().invokeFake(fakeMethodName, params, parameterTypes).broadcast();
    }

    class MakeAndBroadcastCallTxTest {
        Long longValue;
        Object[] objArgs;
        CallContractTransaction tx;

        MakeAndBroadcastCallTxTest(Long longValue, Object[] objArgs) {
            this.longValue = longValue;
            this.objArgs = objArgs;
        }

        MakeAndBroadcastCallTxTest make() throws Exception {
            Log.info("wait call.");
            BigInteger value;
            if(longValue == null) {
                value = BigInteger.ZERO;
            } else {
                value = BigInteger.valueOf(longValue);
            }

            if(StringUtils.isBlank(methodName)) {
                methodName = "transfer";
            }
            if(StringUtils.isBlank(tokenReceiver)) {
                tokenReceiver = toAddress1;
            }
            if(StringUtils.isBlank(contractAddress)) {
                contractAddress = contractAddress_nrc20;
            }
            String methodDesc = "";
            String remark = String.format("call contract test - methodName is %s", methodName);
            String token = BigInteger.valueOf(800L).toString();
            String[][] args;
            if(objArgs == null) {
                args = ContractUtil.twoDimensionalArray(new Object[]{tokenReceiver, token});
            } else {
                args = ContractUtil.twoDimensionalArray(objArgs);
            }

            Result result = makeCallTx(chainId, sender, value, 20000L, 50L, contractAddress,
                    methodName, methodDesc, args, password, remark);
            if (result.isFailed()) {
                throw new RuntimeException(result.getMsg());
            }
            this.tx = (CallContractTransaction) result.getData();

            return this;
        }

        MakeAndBroadcastCallTxTest invokeFake(String fakeMethodName, LinkedList params, Class... parameterTypes) throws Exception {
            // 造假
            params.addFirst(tx);
            Method fakeMethod;
            try {
                fakeMethod = ContractMakeAndBroadcastBase.this.getClass().getDeclaredMethod(fakeMethodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                fakeMethod = ContractMakeAndBroadcastBase.this.getClass().getSuperclass().getDeclaredMethod(fakeMethodName, parameterTypes);
            }
            fakeMethod.invoke(ContractMakeAndBroadcastBase.this, params.toArray());
            return this;
        }

        void broadcast() throws IOException {
            if(tx != null) {
                tx.setTxData(null);
                tx.setCoinData(null);
                tx.serializeData();
                // 签名、广播交易
                Result result = broadcastCallTx(tx);
                Log.info("callContract-result:{}", JSONUtils.obj2PrettyJson(result));
            }
        }

    }

}
