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
package io.nuls.contract.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.util.Log;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import lombok.Data;
import org.junit.Assert;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractCmdConstant.CONTRACT_TX;

/**
 * @author: PierreLuo
 * @date: 2019-05-08
 */
@Data
public class Base {

    protected Chain chain;
    protected static int chainId = 2;
    protected static int assetId = 1;

    protected static String password = "nuls123456";//"nuls123456";

    protected String sender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected String toAddress0 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    protected String toAddress1 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    protected String toAddress2 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    protected String toAddress3 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    protected String toAddress4 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    protected String toAddress5 = "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM";
    protected String toAddress6 = "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29";
    protected String toAddress7 = "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf";
    protected String toAddress8 = "tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S";
    protected String toAddress9 = "tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja";
    protected String toAddress10 = "tNULSeBaMi5yGkDbDgKGGX8TGxYdDttZ4KhpMv";
    protected String toAddress11 = "tNULSeBaMqjttJV62GZ1iXVFDBudet3ey2aYSB";
    protected String toAddress12 = "tNULSeBaMgTcqskhNrE1ZSt3kZpdAv6B83npXE";
    protected String toAddress13 = "tNULSeBaMjcximfy1JEGzjxodNMjrjydWuiffr";
    protected String toAddress14 = "tNULSeBaMfMk3RGzotV3Dw788NFTP52ep7SMnJ";
    protected String toAddress15 = "tNULSeBaMj8XfWDjyKHZ1ybC3ShR8qKGyVKRcb";
    protected String toAddress16 = "tNULSeBaMqwycXLTWtjexSHHfa4jDTrVq9FMWE";
    protected String toAddress17 = "tNULSeBaMoixxbUovqmzPyJ2AwYFAX2evKbuy9";
    protected String toAddress18 = "tNULSeBaMvaRhahBAYkZKQFhiSqcC67UiRzoSA";
    protected String toAddress19 = "tNULSeBaMuk5jx12ZXhaf5HLgcAr3WCwUhRGfT";
    protected String toAddress20 = "tNULSeBaMqjT3y9bGz4gBeJ7FJujmxBDTGdNp1";
    protected String toAddress21 = "tNULSeBaMobzkpUc1zYcT67wheRPLg7cmas5A6";
    protected String toAddress22 = "tNULSeBaMjXxVzqB4T7zFoykRwfSZSD5ptAn4A";
    protected String toAddress23 = "tNULSeBaMpaiBiMHWfAeTzdXhnfJXPfwXwKikc";
    protected String toAddress24 = "tNULSeBaMrL5netZkTo9FZb86xGSk47kq6TRBR";
    protected String toAddress25 = "tNULSeBaMk52mfhacRWkmB98PrwCVXuEzCdQuk";
    protected String toAddress26 = "tNULSeBaMiKWTid5Gj3FoqBFP7WomUzgumVeKc";
    protected String toAddress27 = "tNULSeBaMvGmZSrFyQHptSL9yBCNSDfhWoxEHF";
    protected String toAddress28 = "tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7";
    protected String toAddress29 = "tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN";
    protected String toAddress30 = "tNULSeBaMj7QaB8mYBBvkhaT3jCrXEMCEcRfb1";
    protected String toAddress31 = "tNULSeBaMtgmrSYu98QwP1Mv8G5FwaMDkWSkuy";
    protected String toAddress32 = "tNULSeBaMh4VafNqp5TJSmV5ogdZviq1nbXBSu";
    protected String toAddress33 = "tNULSeBaMfCD8hK8inyEKDBZpuuBUjLdiKgwnG";
    protected String toAddress34 = "tNULSeBaMvQr8dVnk3f3DPvwCYX3ctTRtrTurD";

    protected String createHash = "002029ca32525f635a15c82c046114657c0d8a96a7163780ac6b425b2383b240bd56";
    protected String contractAddress = "tNULSeBaN8cW84rugvTDgSrHNUhZEaWEMERAKZ";
    protected String contractAddress0 = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    protected String contractAddress1 = "tNULSeBaNBhqzwK2yN9FuXmNWago7vLt64xggp";
    protected String contractAddress2 = "tNULSeBaN4ahTXVo5RH1DSnUV9tXpYm3JyBqXc";
    protected String contractAddress3 = "tNULSeBaNCCaTxg3KyWw6xsVpGVicJ2SSRAgEF";
    protected String contractAddress4 = "tNULSeBaMyXYWMvjmddsaLTa3dUwJKJ6Umo7eu";
    protected String contractAddress5 = "tNULSeBaN8YvMDC8bFk7ReHDGMDvnRfhCgYHcM";
    protected String contractAddress6 = "tNULSeBaN54ra3FjKDkMvWKSjEVEAh9UkyvCAC";
    protected String contractAddress7 = "tNULSeBaN7ib5inHZPiSiPf2RFUTAnm7v3zAyy";
    protected String contractAddress8 = "tNULSeBaNAKahVoAFixwdBWJXeA3S913uzha2p";
    protected String contractAddress9 = "tNULSeBaMyR8CTTQiJBawsTsH3xYFDuRktVwEo";
    protected String contractAddress10 = "tNULSeBaNAp4J76TzzpLshj9djAKYrc2aTC6MW";
    protected String contractAddress11 = "tNULSeBaN9Gws3c6ia3EvotK37QnayUQh9KVNV";
    protected String contractAddress12 = "tNULSeBaMwYp39ArJhm85KdDCS8KBVSRdFwCHt";
    protected String contractAddress13 = "tNULSeBaN96u4cGLa9cBjdjWm8Th8U1S4fe52f";
    protected String contractAddress14 = "tNULSeBaN3vgW4VSCbJe3ycApfWFDAdmfPEkob";
    protected String contractAddress15 = "tNULSeBaN4hV2aJ4PtrH4bHQ4uKxwLLbgY9ueV";
    protected String contractAddress16 = "tNULSeBaN7oj8ZsFeENwHvTt229X6SQL1Z5aGz";
    protected String contractAddress17 = "tNULSeBaNASJJ6zswjRkXtLYcuzm6SRT1V8B2p";
    protected String contractAddress18 = "tNULSeBaN14G6bHBfmeqPf1E2hnp3QjxJZpuVT";
    protected String contractAddress19 = "tNULSeBaMzeEEdBVLWg7kTaiKt3DtzfhNs7Lg5";
    protected String contractAddress20 = "tNULSeBaN5tTxQkc6CFs53VgGC5JHUf7VKteMy";
    protected String contractAddress21 = "tNULSeBaMwYCjrkCQxcKaaDRjmQ524HVYNnqAB";
    protected String contractAddress22 = "tNULSeBaN45K8896xgjspzeJWJQg2QrYtHrw4T";
    protected String contractAddress23 = "tNULSeBaNCCrUDnFAykgYM2NLFQow3y9bgg7k2";
    protected String contractAddress24 = "tNULSeBaN3Rx4PkFCwZQRCuSuST8DoHzb9cnH9";
    protected String contractAddress25 = "tNULSeBaMyDmJhSLhTUe9sgbyaeLjJP1gm5jKr";
    protected String contractAddress26 = "tNULSeBaNB27e5ooevy3GHsFmPY87WGJanjJcb";
    protected String contractAddress27 = "tNULSeBaN7XPP7iw6V6GVXXUCiNjYgthUPokgS";
    protected String contractAddress28 = "tNULSeBaMwX4j5T2xGKktm9SFEPDStwM8mUVYe";
    protected String contractAddress29 = "tNULSeBaN5pghSZfZESFuDTkJJc1MYPvvWYw22";
    protected String contractAddress30 = "tNULSeBaMyYNWdn3mbdsSmwN5sxk37JTEqtgRM";
    protected String contractAddress31 = "tNULSeBaMw4LDaSQ2NWiHEPPPdnXVeKHhZV4hz";
    protected String contractAddress32 = "tNULSeBaMx5VtE2EJTHtHueWQ1yA37EP1AGuia";
    protected String contractAddress33 = "tNULSeBaN7gkAGGdnj9hDkKgFDXDf6LnnbWpSG";
    protected String contractAddress34 = "tNULSeBaN4kWaxmgYq2oFMvQ9hq8UEdivvA7i7";
    protected String contractAddress_nrc20 = "tNULSeBaNARcN51M8hoBnxrpAp5zazncj89f6i";
    protected String contractAddress_nrc200 = "tNULSeBaMzThBLi2gwarkgcEdKAT8twK4KF1Uf";
    protected String contractAddress_nrc201 = "tNULSeBaN8LYBqbDhfF7cW11iu9bk1QyjNNVK6";
    protected String contractAddress_nrc202 = "tNULSeBaN9TgWh4hteRMiWKNeEumnKPJCUTh53";
    protected String contractAddress_nrc203 = "tNULSeBaN5qn8B3UB2kyV6Rtv6cqbXnJZm9jAK";
    protected String contractAddress_nrc204 = "tNULSeBaN37SzKyZoERd4KyEMQCKT2sRazUC94";
    protected String contractAddress_nrc205 = "tNULSeBaN6pRoodbstDUem26sR3DcGmNomRmNp";
    protected String contractAddress_nrc206 = "tNULSeBaMzNhv7ekFeX6SS1fxEVZrBynMuFVeR";
    protected String contractAddress_nrc207 = "tNULSeBaMyXor2aJZ4TXjRjhJkw1T4DYxtzH4b";
    protected String contractAddress_nrc208 = "tNULSeBaN2Us7FG6UU2vwjn8winJc7aSvHRQWM";
    protected String contractAddress_nrc209 = "tNULSeBaMw31oFyEyKDUeHFdL7c45HMB5oZyka";
    protected String contractAddress_nrc2010 = "tNULSeBaMzChFy1Q6Ao5oF83oa8cSMEG3ZKfUd";
    protected String contractAddress_nrc2011 = "tNULSeBaN7xxv8xRAqr4QtYZkrHeoLzYTCLmRu";
    protected String contractAddress_nrc2012 = "tNULSeBaNCLwPhph7q4dj1acTgbyxGJd2FPYVT";
    protected String contractAddress_nrc2013 = "tNULSeBaN5SowmpaevWqTscBDAWpRCWPF6ZxGy";
    protected String contractAddress_nrc2014 = "tNULSeBaMy1ETawqkgLG7u1BfSuf5Go3Pc7uk7";
    protected String contractAddress_nrc2015 = "tNULSeBaN5U1GPWmPc8LJ2gpf15KXS5A2VkFaW";
    protected String contractAddress_nrc2016 = "tNULSeBaN8YgGzmJ9qeQFKsMb15HLE1gD2oBxy";
    protected String contractAddress_nrc2017 = "tNULSeBaMw9NQDXCymSjkuybC2haE5gygCLtRR";
    protected String contractAddress_nrc2018 = "tNULSeBaN2wu88LKfVmkoGUTuKXXrttG7QDowe";
    protected String contractAddress_nrc2019 = "tNULSeBaN8NUaHyUGDXQUUenb1iThYRzAqDJwK";
    protected String contractAddress_nrc2020 = "tNULSeBaMwj95C4P8FAoEM4fc5VsCHCKQ3rtZP";
    protected String contractAddress_nrc2021 = "tNULSeBaN1qbx2x15duGVAo5Qni7HScF9etXxQ";
    protected String contractAddress_nrc2022 = "tNULSeBaMzwQ2nhTEEfc2dh3H6JReb22h3HC4v";
    protected String contractAddress_nrc2023 = "tNULSeBaN2QK3vN8vAXynEaPewwztuwzH2NSUC";
    protected String contractAddress_nrc2024 = "tNULSeBaMvpgepaBn7cYRMFEmSdWVjp5ARBXh3";
    protected String contractAddress_nrc2025 = "tNULSeBaN3Y789JGTDTFQmAtyUHSGcZRwPADYD";
    protected String contractAddress_nrc2026 = "tNULSeBaN23MYckd68RvKGW2TazUryPnyYwkHg";
    protected String contractAddress_nrc2027 = "tNULSeBaN4W4U7XFbEyArJAJfNbjNwV8WP8eqo";
    protected String contractAddress_nrc2028 = "tNULSeBaN3Xck3iQ2NvC4nJCyrMQAmRrdeAxJb";
    protected String contractAddress_nrc2029 = "tNULSeBaMxHeQHAZhFi9PWvPLkVqXjDumfAdzJ";
    protected String contractAddress_nrc2030 = "tNULSeBaN4LgXZJ5DmLGGLdh7s14aPfREjs3wA";
    protected String contractAddress_nrc2031 = "tNULSeBaNAByj1ed6ZZas2huKc4pjfy931o7Sd";
    protected String contractAddress_nrc2032 = "tNULSeBaMz314AeVfETvL1ei9EqGPpeoc942W4";
    protected String contractAddress_nrc2033 = "tNULSeBaN3JCvpzFvPBBGA2vVfYWnEgG73AFfZ";
    protected String contractAddress_nrc2034 = "tNULSeBaN99Y5r2nVsguatRF9yuNJdpTvtwajW";

    protected String methodName = "";
    protected String tokenReceiver = "";

    protected String callHash = "0020874dca08dbf4784540e26c0c31f728a2c2fd2e18bf71c896d8f88955d53e77b7";
    protected String deleteHash = "0020b2c159dbdf784c2860ec97072feb887466aa50fc147a5b50388886caab113f9a";

    protected String address(String methodBaseName, int i) throws Exception {
        return this.getClass().getMethod(methodBaseName + i).invoke(this).toString();
    }

    /**
     * 调用合约视图方法
     */
    protected String invokeView(String contractAddress, String methodName, Object... args) throws Exception {
        String methodDesc = "";
        Map params = this.makeInvokeViewParams(contractAddress, methodName, methodDesc, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, INVOKE_VIEW, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(INVOKE_VIEW));
        if(result != null) {
            String viewResult = (String) result.get("result");
            try {
                return JSONUtils.obj2PrettyJson(JSONUtils.json2map(viewResult));
            } catch (Exception e) {
                return viewResult;
            }
        }
        return String.format("invoke_view-result: %s", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeInvokeViewParams(String contractAddress0, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress0);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        return params;
    }

    private Map makeContractResultParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }

    private Map makeContractResultListParams(List<String> hashList) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hashList", hashList);
        return params;
    }

    protected Object[] getContractResult(String hash) throws Exception {
        Map params = this.makeContractResultParams(hash);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT, params);
        Map result = (HashMap) (((HashMap) response.getResponseData()).get(CONTRACT_RESULT));
        return new Object[]{response, result};
    }

    protected Object[] getContractResultList(List<String> hashList) throws Exception {
        Map params = this.makeContractResultListParams(hashList);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT_LIST, params);
        Map result = (HashMap) (((HashMap) response.getResponseData()).get(CONTRACT_RESULT_LIST));
        return new Object[]{response, result};
    }

    protected Map waitGetContractResult(String hash) throws Exception {
        TimeUnit.MILLISECONDS.sleep(800);
        Map map = (Map) getContractResult(hash)[1];
        int i = 0;
        while(map == null) {
            TimeUnit.MILLISECONDS.sleep(800);
            map = (Map) getContractResult(hash)[1];
            if(++i > 32) {
                break;
            }
        }
        return map;
    }

    protected void assertTrue(Response cmdResp2, Map result) throws JsonProcessingException {
        if(null == result) {
            Log.info("Contract-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
            Assert.assertTrue(false);
        }
        Log.info("Contract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    protected Object[] getContractTx(String hash) throws Exception {
        Map params = this.makeContractTxParams(hash);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_TX, params);
        Map result = (HashMap) (((HashMap) response.getResponseData()).get(CONTRACT_TX));
        return new Object[]{response, result};
    }

    protected Map waitGetContractTx(String hash) throws Exception {
        TimeUnit.MILLISECONDS.sleep(800);
        Map map = (Map) getContractTx(hash)[1];
        int i = 0;
        while(map == null) {
            TimeUnit.MILLISECONDS.sleep(800);
            map = (Map) getContractTx(hash)[1];
            if(++i > 32) {
                break;
            }
        }
        return map;
    }

    private Map makeContractTxParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }

    protected Map makeCreateParams(String sender, byte[] contractCode, String alias, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("alias", alias);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", HexUtil.encode(contractCode));
        params.put("args", args);
        params.put("remark", remark);
        return params;
    }

    protected Map makeCallParams(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", 2000000L);
        params.put("price", 25);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

    protected Map makeTransferParams(String address, String toAddress, BigInteger amount, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("toAddress", toAddress);
        params.put("password", password);
        params.put("amount", amount);
        params.put("remark", remark);
        return params;
    }

    protected Map makeTokenTransferParams(String address, String toAddress, String contractAddress, BigInteger amount, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("toAddress", toAddress);
        params.put("contractAddress", contractAddress);
        params.put("password", password);
        params.put("amount", amount);
        params.put("remark", remark);
        return params;
    }
}
