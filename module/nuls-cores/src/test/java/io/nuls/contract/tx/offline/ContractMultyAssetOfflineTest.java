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
package io.nuls.contract.tx.offline;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramMultyAssetValue;
import io.nuls.core.basic.Result;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.LongUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.v2.NulsSDKBootStrap;
import io.nuls.v2.model.dto.SignDto;
import io.nuls.v2.util.NulsSDKTool;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2020-10-30
 */
public class ContractMultyAssetOfflineTest {

    protected int chainId = 2;
    protected int assetId = 1;
    protected long gasLimit = 200000L;
    protected long gasPrice = 25L;
    protected long minutes_3 = 60 * 3;
    protected String offlineContract = "tNULSeBaN31HBrLhXsWDkSz1bjhw5qGBcjafVJ";
    // "http://localhost:18004/"
    protected String apiURL = "http://beta.api.nuls.io/";



    /**
     * 多账户调用合约 - 转入
     */
    @Test
    public void transferInOfmanyAccountCall() throws Exception {
        NulsSDKBootStrap.init(chainId, apiURL);
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = new BigDecimal("6.6").movePointRight(8).toBigInteger();
        String contractAddress = offlineContract;
        String remark = "";
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri,
                value,
                contractAddress,
                "_payable", "", remark, null, null, null, true);

    }

    /**
     * 多账户调用合约 - 转入其他资产，如 2-2, 2-3
     */
    @Test
    public void transferInOfmanyAccountCallII() throws Exception {
        NulsSDKBootStrap.init(chainId, apiURL);
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = offlineContract;
        String remark = "";
        ProgramMultyAssetValue[] multyAssetValues = new ProgramMultyAssetValue[]{
                new ProgramMultyAssetValue(BigInteger.valueOf(2_0000_0000L), 5, 1),
                new ProgramMultyAssetValue(BigInteger.valueOf(3_0000_0000L), 55, 1)
        };
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri,
                value,
                contractAddress,
                "_payableMultyAsset", "", remark, null, null, multyAssetValues, true);

    }

    /**
     * 多账户调用合约 - 同时转入NULS资产和其他资产，如 2-1, 2-2, 2-3
     */
    @Test
    public void transferInOfmanyAccountCallIII() throws Exception {
        NulsSDKBootStrap.init(chainId, apiURL);
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = new BigDecimal("6.6").movePointRight(8).toBigInteger();
        String contractAddress = offlineContract;
        String remark = "";
        ProgramMultyAssetValue[] multyAssetValues = new ProgramMultyAssetValue[]{
                new ProgramMultyAssetValue(BigInteger.valueOf(2_0000_0000L), 5, 1),
                new ProgramMultyAssetValue(BigInteger.valueOf(3_0000_0000L), 55, 1)
        };
        this.callTxOfflineII(feeAccount, feeAccountPri, sender, senderPri,
                value,
                contractAddress,
                "receiveAllAssets", "", remark, null, null, multyAssetValues, true);

    }

    /**
     * 多账户调用合约 - 转出
     */
    @Test
    public void transferOutOfmanyAccountCall() throws Exception {
        NulsSDKBootStrap.init(chainId, apiURL);
        //importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//25 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        //importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//26 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = offlineContract;
        String methodName = "transferNuls";
        String methodDesc = "";
        String remark = "";
        // 转出 0.1 NULS
        Object[] args = new Object[]{"tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24", new BigDecimal("0.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        String[] argsType = new String[]{"Address", "BigInteger"};
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri, value, contractAddress, methodName, methodDesc, remark, args, argsType, null, true);
    }

    /**
     * 多账户调用合约 - 转出其他资产
     */
    @Test
    public void transferOutOfmanyAccountOfOtherAssetCall() throws Exception {
        NulsSDKBootStrap.init(chainId, apiURL);
        //importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//25 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        //importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//26 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        String feeAccount = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String feeAccountPri = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        String sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String senderPri = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = offlineContract;
        String methodName = "transferDesignatedAsset";
        String methodDesc = "";
        String remark = "";
        // 转出 0.1 NULS
        Object[] args = new Object[]{"tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24", new BigDecimal("3").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 55, 1};
        String[] argsType = new String[]{"Address", "BigInteger", "int", "int"};
        this.callTxOffline(feeAccount, feeAccountPri, sender, senderPri, value, contractAddress, methodName, methodDesc, remark, args, argsType, null, true);
    }

    protected void callTxOffline(String feeAccount, String feeAccountPri,
                                 String contractSender, String contractSenderPri,
                                 BigInteger value, String contractAddress,
                                 String methodName, String methodDesc,
                                 String remark,
                                 Object[] args, String[] argsType, ProgramMultyAssetValue[] multyAssetValues, boolean isBroadcastTx) throws Exception{
        List<SignDto> txSingers = new ArrayList<>();
        SignDto dto1 = new SignDto();
        dto1.setAddress(contractSender);
        dto1.setPriKey(contractSenderPri);
        SignDto dto2 = new SignDto();
        dto2.setAddress(feeAccount);
        dto2.setPriKey(feeAccountPri);
        txSingers.add(dto1);
        txSingers.add(dto2);
        //ContractBalance senderBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, sender);
        //byte[] senderBytes = AddressTool.getAddress(sender);
        byte[] feeAccountBytes = AddressTool.getAddress(feeAccount);
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

        List<CoinFrom> froms = new ArrayList<>();
        List<CoinTo> tos = new ArrayList<>();
        if (value.compareTo(BigInteger.ZERO) > 0) {
            String payAccount = feeAccount;
            //String payAccount = contractSender;
            ContractBalance payAccountBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, payAccount);
            CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress(payAccount), chainId, assetId, value, RPCUtil.decode(payAccountBalance.getNonce()), (byte) 0);
            froms.add(coinFrom);
            CoinTo coinTo = new CoinTo(contractAddressBytes, chainId, assetId, value);
            tos.add(coinTo);
        }
        if (multyAssetValues != null) {
            for (ProgramMultyAssetValue multyAssetValue : multyAssetValues) {
                int assetChainId = multyAssetValue.getAssetChainId();
                int assetId = multyAssetValue.getAssetId();
                BigInteger _value = multyAssetValue.getValue();
                ContractBalance account = getUnConfirmedBalanceAndNonce(assetChainId, assetId, feeAccount);
                CoinFrom coinFrom = new CoinFrom(feeAccountBytes, assetChainId, assetId, _value, RPCUtil.decode(account.getNonce()), (byte) 0);
                froms.add(coinFrom);
                CoinTo coinTo = new CoinTo(contractAddressBytes, assetChainId, assetId, _value);
                tos.add(coinTo);
            }
        }
        this.callTxOfflineBase(txSingers, froms, tos, feeAccount, contractSender, value, contractAddress, methodName, methodDesc, remark,
                args, argsType, isBroadcastTx);

    }

    private ContractBalance getUnConfirmedBalanceAndNonce(int chainId, int assetId, String payAccount) {
        Result accountBalance = NulsSDKTool.getAccountBalance(payAccount, chainId, assetId);
        Map dataMap = (Map) accountBalance.getData();
        ContractBalance contractBalance = ContractBalance.newInstance();
        contractBalance.setBalance(new BigInteger(dataMap.get("available").toString()));
        contractBalance.setFreeze(new BigInteger(dataMap.get("freeze").toString()));
        contractBalance.setNonce(dataMap.get("nonce").toString());
        return contractBalance;
    }

    /**
     * 两个账户支出同一个资产
     */
    protected void callTxOfflineII(String feeAccount, String feeAccountPri,
                                 String contractSender, String contractSenderPri,
                                 BigInteger value, String contractAddress,
                                 String methodName, String methodDesc,
                                 String remark,
                                 Object[] args, String[] argsType, ProgramMultyAssetValue[] multyAssetValues, boolean isBroadcastTx) throws Exception{
        List<SignDto> txSingers = new ArrayList<>();
        SignDto dto1 = new SignDto();
        dto1.setAddress(contractSender);
        dto1.setPriKey(contractSenderPri);
        SignDto dto2 = new SignDto();
        dto2.setAddress(feeAccount);
        dto2.setPriKey(feeAccountPri);
        // importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//27 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        SignDto dto3 = new SignDto();
        dto3.setAddress("tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24");
        dto3.setPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78");
        txSingers.add(dto1);
        txSingers.add(dto2);
        txSingers.add(dto3);
        //ContractBalance senderBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, sender);
        //byte[] senderBytes = AddressTool.getAddress(sender);
        byte[] feeAccountBytes = AddressTool.getAddress(feeAccount);
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

        List<CoinFrom> froms = new ArrayList<>();
        List<CoinTo> tos = new ArrayList<>();
        if (value.compareTo(BigInteger.ZERO) > 0) {
            String payAccount = feeAccount;
            //String payAccount = contractSender;
            ContractBalance payAccountBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, payAccount);
            CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress(payAccount), chainId, assetId, value, RPCUtil.decode(payAccountBalance.getNonce()), (byte) 0);
            froms.add(coinFrom);
            CoinTo coinTo = new CoinTo(contractAddressBytes, chainId, assetId, value);
            tos.add(coinTo);
        }
        if (multyAssetValues != null) {
            String payAccount;
            for (ProgramMultyAssetValue multyAssetValue : multyAssetValues) {
                BigInteger _value = multyAssetValue.getValue();
                BigInteger divide = _value.divide(BigInteger.valueOf(2));
                int assetChainId = multyAssetValue.getAssetChainId();
                int assetId = multyAssetValue.getAssetId();

                payAccount = feeAccount;
                ContractBalance account = getUnConfirmedBalanceAndNonce(assetChainId, assetId, payAccount);
                CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress(payAccount), assetChainId, assetId, _value.subtract(divide), RPCUtil.decode(account.getNonce()), (byte) 0);
                froms.add(coinFrom);


                payAccount = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
                account = getUnConfirmedBalanceAndNonce(assetChainId, assetId, payAccount);
                coinFrom = new CoinFrom(AddressTool.getAddress(payAccount), assetChainId, assetId, divide, RPCUtil.decode(account.getNonce()), (byte) 0);
                froms.add(coinFrom);

                CoinTo coinTo = new CoinTo(contractAddressBytes, assetChainId, assetId, _value);
                tos.add(coinTo);
            }
        }
        this.callTxOfflineBase(txSingers, froms, tos, feeAccount, contractSender, value, contractAddress, methodName, methodDesc, remark,
                args, argsType, isBroadcastTx);

    }


    protected void callTxOfflineBase(List<SignDto> txSingers, List<CoinFrom> froms, List<CoinTo> tos,
                                 String feeAccount,
                                 String contractSender,
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
        byte[] senderBytes = AddressTool.getAddress(contractSender);
        CallContractData callContractData = new CallContractData();
        callContractData.setContractAddress(contractAddressBytes);
        callContractData.setSender(senderBytes);
        callContractData.setValue(value);
        callContractData.setPrice(25);
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
        coinData.setFrom(froms);
        coinData.setTo(tos);

        long gasUsed = callContractData.getGasLimit();
        BigInteger imputedValue = BigInteger.valueOf(LongUtils.mul(gasUsed, callContractData.getPrice()));
        byte[] feeAccountBytes = AddressTool.getAddress(feeAccount);
        BigInteger feeValue = imputedValue;
        CoinFrom feeAccountFrom = null;
        for (CoinFrom from : froms) {
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            if (Arrays.equals(from.getAddress(), feeAccountBytes) && assetChainId == this.chainId && assetId == this.assetId) {
                from.setAmount(from.getAmount().add(feeValue));
                feeAccountFrom = from;
                break;
            }
        }
        if (feeAccountFrom == null) {
            ContractBalance feeAccountBalance = getUnConfirmedBalanceAndNonce(chainId, assetId, feeAccount);
            feeAccountFrom = new CoinFrom(feeAccountBytes, chainId, assetId, feeValue, RPCUtil.decode(feeAccountBalance.getNonce()), (byte) 0);
            coinData.addFrom(feeAccountFrom);
        }
        /*if (value.compareTo(BigInteger.ZERO) > 0) {
            CoinFrom coinFrom = new CoinFrom(callContractData.getSender(), chainId, assetId, sendValue, RPCUtil.decode(senderBalance.getNonce()), (byte) 0);
            coinData.addFrom(coinFrom);

            CoinTo coinTo = new CoinTo(callContractData.getContractAddress(), chainId, assetId, value);
            coinData.addTo(coinTo);
        }*/

        tx.setCoinData(coinData.serialize());
        tx.setTxData(callContractData.serialize());

        BigInteger txSizeFee = TransactionFeeCalculator.getNormalUnsignedTxFee(tx.getSize() + 130 * froms.size());
        feeAccountFrom.setAmount(feeAccountFrom.getAmount().add(txSizeFee));
        /*if (feeAccountBalance.getBalance().compareTo(feeValue) < 0) {
            // Insufficient balance
            throw new RuntimeException("Insufficient balance to pay fee");
        }*/
        tx.setCoinData(coinData.serialize());
        // 签名
        byte[] txBytes = tx.serialize();
        String txHex = HexUtil.encode(txBytes);
        Result<Map> signTxR = NulsSDKTool.sign(txSingers, txHex);

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

}
