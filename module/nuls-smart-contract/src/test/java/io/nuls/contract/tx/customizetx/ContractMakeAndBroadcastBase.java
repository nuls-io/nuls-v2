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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTxHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.rpc.call.AccountCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.validator.CallContractTxValidator;
import io.nuls.contract.validator.CreateContractTxValidator;
import io.nuls.contract.validator.DeleteContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractErrorCode.FAILED;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-29
 */
public class ContractMakeAndBroadcastBase extends BaseQuery {

    private ContractTxHelper contractTxHelper;
    private ContractHelper contractHelper;
    private ContractAddressStorageService contractAddressStorageService;

    class ContractAddressStorageServiceFake implements ContractAddressStorageService {

        @Override
        public Result<ContractAddressInfoPo> getContractAddressInfo(int chainId, byte[] contractAddressBytes) {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("contractAddress", AddressTool.getStringAddressByBytes(contractAddressBytes));
            try {
                Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_contract_info", params);
                if (!callResp.isSuccess()) {
                    return getFailed();
                }
                HashMap contractInfo = (HashMap) ((HashMap) callResp.getResponseData()).get("sc_contract_info");
                ContractAddressInfoPo po = new ContractAddressInfoPo();
                po.setSender(AddressTool.getAddress(contractInfo.get("creater").toString()));
                return getSuccess().setData(po);
            } catch (Exception e) {
                Log.error(e);
                return getFailed();
            }
        }

        @Override
        public Result saveContractAddress(int chainId, byte[] contractAddressBytes, ContractAddressInfoPo info) { return null; }
        @Override
        public Result deleteContractAddress(int chainId, byte[] contractAddressBytes) throws Exception { return null; }
        @Override
        public boolean isExistContractAddress(int chainId, byte[] contractAddressBytes) {
            return this.getContractAddressInfo(chainId, contractAddressBytes).isSuccess();
        }
        @Override
        public Result<List<ContractAddressInfoPo>> getContractInfoList(int chainId, byte[] creater) { return null; }
        @Override
        public Result<List<ContractAddressInfoPo>> getAllContractInfoList(int chainId) { return null; }
    }

    @Before
    public void init() throws Exception {
        contractHelper = new ContractHelper();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chainId, chain);
        Field field1 = ContractHelper.class.getDeclaredField("chainManager");
        field1.setAccessible(true);
        field1.set(contractHelper, chainManager);

        contractAddressStorageService = new ContractAddressStorageServiceFake();
        Field field2 = ContractHelper.class.getDeclaredField("contractAddressStorageService");
        field2.setAccessible(true);
        field2.set(contractHelper, contractAddressStorageService);

        Field field3 = ContractLedgerUtil.class.getDeclaredField("contractAddressStorageService");
        field3.setAccessible(true);
        field3.set(ContractLedgerUtil.class, contractAddressStorageService);

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
            ContractData contractData = tx.getTxDataObj();
            byte[] contractAddressBytes = contractData.getContractAddress();
            Result result = this.broadcastTx(tx);
            if(result.isFailed()) {
                return result;
            }
            Map<String, String> resultMap = MapUtil.createHashMap(2);
            String txHash = tx.getHash().toHex();
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
        Result result = this.broadcastTx(tx);
        if(result.isFailed()) {
            return result;
        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("txHash", tx.getHash().toHex());
        return getSuccess().setData(resultMap);
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
        Result result = this.broadcastTx(tx);
        if(result.isFailed()) {
            return result;
        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("txHash", tx.getHash().toHex());
        return getSuccess().setData(resultMap);
    }

    private void signContractTx(ContractBaseTransaction tx) throws IOException, NulsException {
        CoinData coinDataObj = tx.getCoinDataObj();
        byte[] txCreator = coinDataObj.getFrom().get(0).getAddress();
        // 生成交易hash
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        // 生成签名
        AccountCall.transactionSignature(chainId, AddressTool.getStringAddressByBytes(txCreator), password, tx);
    }

    private Result broadcastTx(Transaction tx) {
        try {
            String txData = RPCUtil.encode(tx.serialize());

            //// 通知账本
            //int commitStatus = LedgerCall.commitUnconfirmedTx(chainId, txData);
            //if(commitStatus != LedgerUnConfirmedTxStatus.SUCCESS.status()) {
            //    return getFailed().setMsg(LedgerUnConfirmedTxStatus.getStatus(commitStatus).name());
            //}
            // 广播交易
            boolean broadcast = TransactionCall.newTx(chainId, txData);
            if (!broadcast) {
                // 广播失败，回滚账本的未确认交易
                //LedgerCall.rollBackUnconfirmTx(chainId, txData);
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

    protected Result validCreateTx(int chainId, CreateContractTransaction tx) {
        try {
            CreateContractTxValidator validator = new CreateContractTxValidator();
            return validator.validate(chainId, tx);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }
    protected Result validCallTx(int chainId, CallContractTransaction tx) {
        try {
            CallContractTxValidator validator = new CallContractTxValidator();
            return validator.validate(chainId, tx);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }
    protected Result validDeleteTx(int chainId, DeleteContractTransaction tx) {
        try {
            DeleteContractTxValidator validator = new DeleteContractTxValidator();
            Field field = DeleteContractTxValidator.class.getDeclaredField("contractHelper");
            field.setAccessible(true);
            field.set(validator, contractHelper);
            return validator.validate(chainId, tx);
        } catch (Exception e) {
            Log.error(e);
            return getFailed();
        }
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

        MakeAndBroadcastCreateTxTest fake(ExecuteFake fake) throws Exception {
            fake.execute(tx);
            return this;
        }

        MakeAndBroadcastCreateTxTest sign() throws Exception {
            if(tx != null) {
                tx.setTxData(null);
                tx.setCoinData(null);
                tx.serializeData();
                signContractTx(tx);
            } else {
                throw new NullPointerException("tx is null");
            }
            return this;
        }

        MakeAndBroadcastCreateTxTest validate() {
            Result result = validCreateTx(chainId, tx);
            if(result.isFailed()) {
                throw new RuntimeException(result.getMsg());
            }
            return this;
        }

        void broadcast() throws IOException {
            if(tx != null) {
                // 广播交易
                Result result = broadcastCreateTx(tx);
                Log.info("createContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }

        void signAndBroadcast() throws Exception {
            if(tx != null) {
                // 签名
                this.sign();

                // 广播交易
                Result result = broadcastCreateTx(tx);
                Log.info("createContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }
    }


    class MakeAndBroadcastCallTxTest {
        Long longValue;
        Object[] objArgs;
        CallContractTransaction tx;

        MakeAndBroadcastCallTxTest(Long longValue, Object[] objArgs) {
            this.longValue = longValue;
            this.objArgs = objArgs;
        }

        MakeAndBroadcastCallTxTest() {}

        public MakeAndBroadcastCallTxTest setLongValue(Long longValue) {
            this.longValue = longValue;
            return this;
        }

        public MakeAndBroadcastCallTxTest setObjArgs(Object[] objArgs) {
            this.objArgs = objArgs;
            return this;
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

        MakeAndBroadcastCallTxTest fake(ExecuteFake fake) throws Exception {
            fake.execute(tx);
            return this;
        }

        MakeAndBroadcastCallTxTest sign() throws Exception {
            if(tx != null) {
                tx.setTxData(null);
                tx.setCoinData(null);
                tx.serializeData();
                signContractTx(tx);
            } else {
                throw new NullPointerException("tx is null");
            }
            return this;
        }

        MakeAndBroadcastCallTxTest validate() {
            Result result = validCallTx(chainId, tx);
            if(result.isFailed()) {
                throw new RuntimeException(result.getMsg());
            }
            return this;
        }

        void broadcast() throws IOException {
            if(tx != null) {
                // 广播交易
                Result result = broadcastCallTx(tx);
                Log.info("callContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }

        void signAndBroadcast() throws Exception {
            if(tx != null) {
                // 签名
                this.sign();

                // 广播交易
                Result result = broadcastCallTx(tx);
                Log.info("callContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }

    }

    class MakeAndBroadcastDeleteTxTest {
        DeleteContractTransaction tx;

        MakeAndBroadcastDeleteTxTest make() throws Exception {
            Log.info("wait delete.");
            String remark = "delete contract";
            Result result = makeDeleteTx(chainId, sender, contractAddress, password, remark);
            if (result.isFailed()) {
                Log.error("delete make error:{}", JSONUtils.obj2PrettyJson(result));
                throw new RuntimeException(result.getMsg());
            }
            this.tx = (DeleteContractTransaction) result.getData();
            return this;
        }

        MakeAndBroadcastDeleteTxTest fake(ExecuteFake fake) throws Exception {
            fake.execute(tx);
            return this;
        }

        MakeAndBroadcastDeleteTxTest sign() throws Exception {
            if(tx != null) {
                tx.setTxData(null);
                tx.setCoinData(null);
                tx.serializeData();
                signContractTx(tx);
            } else {
                throw new NullPointerException("tx is null");
            }
            return this;
        }

        MakeAndBroadcastDeleteTxTest validate() {
            Result result = validDeleteTx(chainId, tx);
            if(result.isFailed()) {
                throw new RuntimeException(result.getMsg());
            }
            return this;
        }

        void broadcast() throws IOException {
            if(tx != null) {
                // 广播交易
                Result result = broadcastDeleteTx(tx);
                Log.info("deleteContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }

        void signAndBroadcast() throws Exception {
            if(tx != null) {
                // 签名
                this.sign();

                // 广播交易
                Result result = broadcastDeleteTx(tx);
                Log.info("deleteContract-result:{}", JSONUtils.obj2PrettyJson(result));
            } else {
                throw new NullPointerException("tx is null");
            }
        }
    }

    interface ExecuteFake {
        void execute(ContractBaseTransaction tx) throws Exception;
    }

}
