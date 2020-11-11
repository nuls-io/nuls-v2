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
package io.nuls.contract.tx.multyasset;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.LongUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.v2.NulsSDKBootStrap;
import io.nuls.v2.model.dto.SignDto;
import io.nuls.v2.util.NulsSDKTool;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2020-10-30
 */
public class ContractMultyAssetTest extends BaseQuery {

    protected long gasLimit = 200000L;
    protected long gasPrice = 1L;
    protected long minutes_3 = 60 * 3;

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/multi-asset-contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "test multy asset";
        Map params = this.makeCreateParams(sender, contractCode, "asset", remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);
        assertTrue(map);
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(map));
    }

    /**
     * 注册一个资产
     */
    @Test
    public void assetRegisterTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("assetSymbol", "MTA");
        params.put("assetName", "MTA");
        params.put("initNumber", 100000000);
        params.put("decimalPlace", 8);
        params.put("txCreatorAddress", sender);
        params.put("assetOwnerAddress", sender);
        params.put("password", "nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "chainAssetTxReg", params);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    /**
     * 转入NULS，转出NULS，转出NULS锁定
     */
    @Test
    public void nulsTest() throws Exception {
        // 转入 3.2 NULS
        this.callByParams("_payable", "3.2", null);
        // 转出 1.1 NULS
        Object[] args = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.callByParams("transferNuls", "0", args);
        // 转出 1.2 NULS
        Object[] argsLock = new Object[]{toAddress17, new BigDecimal("1.2").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.callByParams("transferNulsLock", "0", argsLock);
    }

    /**
     * 其他资产转入、转出、转出锁定
     * <p>
     * 如 2-2, 假设资产decimals=8
     */
    @Test
    public void otherAssetTest() throws Exception {
        // 转入 3.2
        this.callOfDesignatedAssetByParams("_payableMultyAsset", "3.2", null, 2, 2);
        // 转出 1.1
        Object[] args = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2};
        this.callOfDesignatedAssetByParams("transferDesignatedAsset", "0", args, 0, 0);
        // 转出 1.2
        Object[] argsLock = new Object[]{toAddress17, new BigDecimal("1.2").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2, minutes_3};
        this.callOfDesignatedAssetByParams("transferDesignatedAssetLock", "0", argsLock, 0, 0);
    }


    /**
     * 内部调用其他合约, 转入NULS，转出NULS，转出NULS(锁定)
     */
    @Test
    public void innerCall() throws Exception {
        String methodName = "callOtherContract";
        String otherContract = "tNULSeBaN74aQ3n2p9DbyvudrUHMzABQuKAEMH";
        // 转入 6.6 NULS (外部合约)
        this.callByParams("_payable", "6.6", null);
        // 转入 6.6 NULS (内部合约)
        this.innerCallByParams(methodName, otherContract, "_payable", null, "6.6");
        // 转出 3.3 NULS
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.innerCallByParams(methodName, otherContract, "transferNuls", innerArgs, "0");
        // 转出 1.1 NULS(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.innerCallByParams(methodName, otherContract, "transferNulsLock", innerArgsLock, "0");
    }

    /**
     * 内部调用其他合约, 转入NULS，转出NULS，转出NULS(锁定)
     * <p>
     * 内部调用带返回值
     */
    @Test
    public void innerCallWithReturnValue() throws Exception {
        String methodName = "callWithReturnValueOfOtherContract";
        String otherContract = "tNULSeBaN74aQ3n2p9DbyvudrUHMzABQuKAEMH";
        // 转入 6.6 NULS (外部合约)
        this.callByParams("_payable", "6.6", null);
        // 转入 6.6 NULS (内部合约)
        this.innerCallByParams(methodName, otherContract, "_payable", null, "6.6");
        // 转出 3.3 NULS
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.innerCallByParams(methodName, otherContract, "transferNuls", innerArgs, "0");
        // 转出 1.1 NULS(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.innerCallByParams(methodName, otherContract, "transferNulsLock", innerArgsLock, "0");
    }

    /**
     * 内部调用其他合约, 转入、转出、转出锁定 其他资产
     * 如 2-2, 假设资产decimals=8
     * 内部调用带返回值
     */
    @Test
    public void innerCallWithReturnValueOfDesignatedAsset() throws Exception {
        String methodName = "callWithReturnValueOfOtherContractOfDesignatedAsset";
        String otherContract = "tNULSeBaN74aQ3n2p9DbyvudrUHMzABQuKAEMH";
        // 转入 6.6 2-2 (外部合约)
        this.callOfDesignatedAssetByParams("_payableMultyAsset", "6.6", null, 2, 2);
        // 转入 6.6 2-2 (内部合约)
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "_payableMultyAsset", null, "6.6", 2, 2);
        // 转出 3.3 2-2
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2};
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "transferDesignatedAsset", innerArgs, "0", 0, 0);
        // 转出 3.3 2-2(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2, minutes_3};
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "transferDesignatedAssetLock", innerArgsLock, "0", 0, 0);
    }

    /**
     * 根据注册资产的交易hash查询资产信息
     */
    @Test
    public void getAssetRegInfoByHashTest() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", "b51947d09b1eeca55de84703f840faf2638257f6d1b833e46efcc62229383b43");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfoByHash", params);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    /**
     * 多账户调用合约
     */
    @Test
    public void manyAccountCall() throws Exception {
        NulsSDKBootStrap.init(chainId, "http://localhost:18004/");
        //importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//25 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        //importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//26 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = "tNULSeBaN7Fd9yMXAcL5EUhjyq4AUWJ3pw1Gu9";
        //String contractAddress = "tNULSeBaNA1AvcgZh1geK8upzgiabUTSyBy5R2";
        String methodName = "transferNuls";
        String methodDesc = "";
        String remark = "";
        // 转出 0.1 NULS
        Object[] args = new Object[]{toAddress17, new BigDecimal("0.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        String[] argsType = new String[]{"Address", "BigInteger"};
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri,
                new BigDecimal("3.2").multiply(BigDecimal.TEN.pow(8)).toBigInteger(),
                contractAddress,
                "_payable", "", remark, null, null, true);
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri, value, contractAddress, methodName, methodDesc, remark, args, argsType, true);

    }

    protected void callTxOffline(String feeAccount, String feeAccountPri,
                                 String sender, String senderPri,
                                 BigInteger value, String contractAddress,
                                 String methodName, String methodDesc,
                                 String remark,
                                 Object[] args, String[] argsType, boolean isBroadcastTx) throws Exception{
        // 生成参数的二维数组
        String[][] finalArgs = null;
        if (args != null && args.length > 0) {
            if(argsType == null || argsType.length != args.length) {
                Assert.assertTrue("size of 'argsType' array not match 'args' array", false);
            }
            finalArgs = ContractUtil.twoDimensionalArray(args, argsType);
        }

        // 组装交易的txData
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        byte[] senderBytes = AddressTool.getAddress(sender);
        CallContractData callContractData = new CallContractData();
        callContractData.setContractAddress(contractAddressBytes);
        callContractData.setSender(senderBytes);
        callContractData.setValue(value);
        callContractData.setPrice(1);
        callContractData.setGasLimit(gasLimit);
        callContractData.setMethodName(methodName);
        callContractData.setMethodDesc(methodDesc);
        if (finalArgs != null) {
            callContractData.setArgsCount((short) finalArgs.length);
            callContractData.setArgs(finalArgs);
        }

        CallContractTransaction tx = new CallContractTransaction();
        if (StringUtils.isNotBlank(remark)) {
            tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
        }
        tx.setTime(System.currentTimeMillis() / 1000);
        // 计算CoinData
        CoinData coinData = new CoinData();
        ContractBalance feeAccountBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, feeAccount);
        ContractBalance senderBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, sender);

        long gasUsed = callContractData.getGasLimit();
        BigInteger imputedValue = BigInteger.valueOf(LongUtils.mul(gasUsed, callContractData.getPrice()));
        // 总花费
        BigInteger sendValue = callContractData.getValue();
        if (senderBalance.getBalance().compareTo(sendValue) < 0) {
            // Insufficient balance
            throw new RuntimeException("Insufficient balance to transfer to contract");
        }
        BigInteger feeValue = imputedValue;
        if (value.compareTo(BigInteger.ZERO) > 0) {
            CoinFrom coinFrom = new CoinFrom(callContractData.getSender(), chainId, assetId, sendValue, RPCUtil.decode(senderBalance.getNonce()), (byte) 0);
            coinData.addFrom(coinFrom);

            CoinTo coinTo = new CoinTo(callContractData.getContractAddress(), chainId, assetId, value);
            coinData.addTo(coinTo);
        }
        CoinFrom feeCoinFrom = new CoinFrom(AddressTool.getAddress(feeAccount), chainId, assetId, feeValue, RPCUtil.decode(feeAccountBalance.getNonce()), (byte) 0);
        coinData.addFrom(feeCoinFrom);

        tx.setCoinData(coinData.serialize());
        tx.setTxData(callContractData.serialize());

        BigInteger txSizeFee = TransactionFeeCalculator.getNormalUnsignedTxFee(tx.getSize() + 260);
        feeValue = feeValue.add(txSizeFee);
        feeCoinFrom.setAmount(feeValue);
        if (feeAccountBalance.getBalance().compareTo(feeValue) < 0) {
            // Insufficient balance
            throw new RuntimeException("Insufficient balance to pay fee");
        }
        tx.setCoinData(coinData.serialize());
        // 签名
        List<SignDto> signDtoList = new ArrayList<>();
        SignDto signDto = new SignDto();
        signDto.setAddress(sender);
        signDto.setPriKey(senderPri);
        signDtoList.add(signDto);
        signDto = new SignDto();
        signDto.setAddress(feeAccount);
        signDto.setPriKey(feeAccountPri);
        signDtoList.add(signDto);

        byte[] txBytes = tx.serialize();
        String txHex = HexUtil.encode(txBytes);
        Result<Map> signTxR = NulsSDKTool.sign(signDtoList, txHex);

        Assert.assertTrue(JSONUtils.obj2PrettyJson(signTxR), signTxR.isSuccess());
        Map resultData = signTxR.getData();
        String signedTxHex = (String) resultData.get("txHex");
        System.out.println(String.format("signedTxHex: %s", signedTxHex));

        // 在线接口 - 广播交易
        if (!isBroadcastTx) {
            return;
        }
        Result<Map> broadcaseTxR = NulsSDKTool.broadcast(signedTxHex);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(broadcaseTxR), broadcaseTxR.isSuccess());
        Map data = broadcaseTxR.getData();
        String hash = (String) data.get("hash");
        System.out.println(String.format("hash: %s", hash));
    }

    public ContractBalance getUnConfirmedBalanceAndNonce(int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> balance = LedgerCall.getBalanceAndNonce(chain, assetChainId, assetId, address);
            ContractBalance contractBalance = ContractBalance.newInstance();
            contractBalance.setBalance(new BigInteger(balance.get("available").toString()));
            contractBalance.setFreeze(new BigInteger(balance.get("freeze").toString()));
            contractBalance.setNonce((String) balance.get("nonce"));
            return contractBalance;
        } catch (NulsException e) {
            Log.error(e);
            return ContractBalance.newInstance();
        }
    }

    protected void callByParams(String methodName, String valueStr, Object[] args) throws Exception {
        BigInteger value = new BigDecimal(valueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void callOfDesignatedAssetByParams(String methodName, String valueStr, Object[] args, int assetChainId, int assetId) throws Exception {
        BigInteger value = new BigDecimal(valueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Map params = this.makeCallParams(sender, value, gasLimit, gasPrice, contractAddress, methodName, null, "", assetChainId, assetId, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void innerCallByParams(String methodName, String otherContract, String innerMethod, Object[] innerArgs, String innerValueStr) throws Exception {
        BigInteger innerValue = new BigDecimal(innerValueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Object[] args = new Object[]{otherContract, innerMethod, innerArgs, innerValue};
        Map params = this.makeCallParams(sender, BigInteger.ZERO, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void innerCallOfDesignatedAssetByParams(String methodName, String otherContract, String innerMethod, Object[] innerArgs, String innerValueStr, int assetChainId, int assetId) throws Exception {
        BigInteger innerValue = new BigDecimal(innerValueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Object[] args = new Object[]{otherContract, innerMethod, innerArgs, innerValue, assetChainId, assetId};
        Map params = this.makeCallParams(sender, BigInteger.ZERO, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }
}
