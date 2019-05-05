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

package io.nuls.contract.tx.nrc20;


import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractNRC20TokenSendTxTest extends BaseQuery {

    @Test
    public void importPriKeyTest() {
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", password);//打包地址 tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//25 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//26 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//27 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", password);//28 tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", password);//29 tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", password);//30 tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL

        importPriKey("979c0ceeba6062e46b8eaa0f8435951ce27859581a39d4d2e7c0eef1baac15d3", password);//5 tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM
        importPriKey("edacaeb4ae6836ead7dd61d8ab79444b631274a303f91608472c8f99d646bbdf", password);//6 tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29
        importPriKey("ab69dab113f27ecac4024536c8a72b35f1ad4c8c934e486f7c4edbb14d8b7f9e", password);//7 tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf
        importPriKey("14e86ce16c0a21fe3960e18a86c4ed943d4cf434cb4dc0c761cf811b20486f43", password);//8 tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S
        importPriKey("a17e3161cc2b2a5d8ac2777f4113f6147270cda9ec9ba2ca979c06839f264e39", password);//9 tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja
        importPriKey("2a1eff43919f52370682c527a5932ca43ea0d65ebd3b4686b5823c5087b33355", password);//10 tNULSeBaMi5yGkDbDgKGGX8TGxYdDttZ4KhpMv
        importPriKey("425c9c6e9cf1c6dbb51fe22baf2487b273b0b3fe0f596f6e7b406cbb97775fd0", password);//11 tNULSeBaMqjttJV62GZ1iXVFDBudet3ey2aYSB
        importPriKey("3ba402d5138ff7439fd0187f6359b1b1e1ec0529544dc05bf0072445b5196e2d", password);//12 tNULSeBaMgTcqskhNrE1ZSt3kZpdAv6B83npXE
        importPriKey("1a4bb53eddab9d355c56c840097de6611497b53fc348f4abaa664ea5a5f8829c", password);//13 tNULSeBaMjcximfy1JEGzjxodNMjrjydWuiffr
        importPriKey("2cca1c7f69f929680a00d45298dca7b705d87d34ae1dbbcb4125b5663552db36", password);//14 tNULSeBaMfMk3RGzotV3Dw788NFTP52ep7SMnJ
        importPriKey("1c73a09db8b19b14921f8790ef015ac1ee6137cdb99f967a3f257b21f68bac1d", password);//15 tNULSeBaMj8XfWDjyKHZ1ybC3ShR8qKGyVKRcb
        importPriKey("2d969f5dd4b68089fcb581f9d029fd3bb472c4858b88bcfec96f0575c1310eb5", password);//16 tNULSeBaMqwycXLTWtjexSHHfa4jDTrVq9FMWE
        importPriKey("d60fc83130dbe5537d4f1e1e35c533f1a396b8b7d641d717b2d1eb1245d0d796", password);//17 tNULSeBaMoixxbUovqmzPyJ2AwYFAX2evKbuy9
        importPriKey("d78bbdd20e0166d468d93c6a5bde7950c84427b7e1da307217f7e68583b137b5", password);//18 tNULSeBaMvaRhahBAYkZKQFhiSqcC67UiRzoSA
        importPriKey("63da888abcdfbb20931e88ec1f926e0624f57792e4c41dcde889bf6bbe01f49a", password);//19 tNULSeBaMuk5jx12ZXhaf5HLgcAr3WCwUhRGfT
        importPriKey("c34d3ec20f8134b53f3df9b61bbac0c50d6e368db3dbf0a0069b0206db409643", password);//20 tNULSeBaMqjT3y9bGz4gBeJ7FJujmxBDTGdNp1
        importPriKey("f13d4e1ff9f8e8311072b6cf8cff74f754f38675905bd22a51dd17461ab8946c", password);//21 tNULSeBaMobzkpUc1zYcT67wheRPLg7cmas5A6
        importPriKey("bc6032137bf45ccc7a230cdef655a263a86a69b1d98f3b9567688872afa5af15", password);//22 tNULSeBaMjXxVzqB4T7zFoykRwfSZSD5ptAn4A
        importPriKey("0bf13b6653412e905b06001e4b57d95c113da9fe279db83076a88159b2828d23", password);//23 tNULSeBaMpaiBiMHWfAeTzdXhnfJXPfwXwKikc
        importPriKey("a08afb9b85f54622503b06f26b8883a78a90892d2909071e4e1a3306e283992a", password);//24 tNULSeBaMrL5netZkTo9FZb86xGSk47kq6TRBR
        importPriKey("a9d2e66e4bb78a71a99a0509c05689f394f2ccb77ca88a14670b5fda117d2de7", password);//25 tNULSeBaMk52mfhacRWkmB98PrwCVXuEzCdQuk
        importPriKey("a99470957cc20287b66ff4a9deb70770a698ea1968e7ac8262532c9a644d01c1", password);//26 tNULSeBaMiKWTid5Gj3FoqBFP7WomUzgumVeKc
        importPriKey("6f5a847f5bce1e7bae540daeb024fc05cab6b21c2eea2cdf2c8837e2844af4e2", password);//27 tNULSeBaMvGmZSrFyQHptSL9yBCNSDfhWoxEHF
        importPriKey("f8ae46ce88cf33091eab6068fa39756d4aa2181c49192b3e1531ecb52dad1b1d", password);//28 tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7
        importPriKey("9da23738f9efe1e8271c7a43d45df6087a57782e276c7b4a8e19f7ced04b73c8", password);//29 tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN
        importPriKey("6fec251b4f3b2b48f98c2426587515fee2339a3f9f1e0011d816cd1f54d37be7", password);//30 tNULSeBaMj7QaB8mYBBvkhaT3jCrXEMCEcRfb1
        importPriKey("9f32c23400bcb08ae52c6195ba9167969ee36ac3219bb320e9c7bc3d49efb4ee", password);//31 tNULSeBaMtgmrSYu98QwP1Mv8G5FwaMDkWSkuy
        importPriKey("4ecbcf0768ea880c6001ad46838e5ece4fa5641424f4e0cce5ec412c11d5ae8f", password);//32 tNULSeBaMh4VafNqp5TJSmV5ogdZviq1nbXBSu
        importPriKey("5633c9e3923773a5665c4e8cf5f8e80abb7085f9b30694656dfc1c9f3b7092d2", password);//33 tNULSeBaMfCD8hK8inyEKDBZpuuBUjLdiKgwnG
        importPriKey("b936b61041b6fc84943b46dc0bc8ed79c009fdeb607fce17113800b64a726f0c", password);//34 tNULSeBaMvQr8dVnk3f3DPvwCYX3ctTRtrTurD

    }

    @Test
    public void transfer() {
        TransferReq.TransferReqBuilder builder = new TransferReq.TransferReqBuilder(chain.getChainId(), chain.getConfig().getAssetsId())
                .addForm(sender, password, BigInteger.valueOf(30000000000000L))
                .addTo(toAddress5,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress6,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress7,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress8,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress9,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress10,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress11,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress12,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress13,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress14,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress15,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress16,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress17,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress18,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress19,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress20,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress21,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress22,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress23,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress24,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress25,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress26,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress27,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress28,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress29,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress30,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress31,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress32,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress33,BigInteger.valueOf(1000000000000L))
                .addTo(toAddress34,BigInteger.valueOf(1000000000000L));
        System.out.println(transferService.transfer(builder.build()).getData());
    }

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        //sender = toAddress32;
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 空气币";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeCreateParams(sender, contractCode, remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        Log.info("Create-NRC20-Contract-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(null != result);
    }

    private Map makeCreateParams(String sender, byte[] contractCode, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", HexUtil.encode(contractCode));
        params.put("args", args);
        params.put("remark", remark);
        return params;
    }

    /**
     * 调用合约
     */
    @Test
    public void callContract() throws Exception {
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
        Map params = this.makeCallParams(sender, value, contractAddress_nrc20, methodName, methodDesc, remark, tokenReceiver, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        if(result == null) {
            Log.error("call-response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        }
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeCallParams(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

    /**
     * token转账
     */
    @Test
    public void tokenTransfer() throws Exception {
        BigInteger value = BigInteger.TEN.pow(10);
        String remark = "token transfer to " + contractAddress;
        Map params = this.makeTokenTransferParams(sender, contractAddress, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER));
        Assert.assertTrue(null != result);
        Log.info("tokenTransfer-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeTokenTransferParams(String address, String toAddress, String contractAddress, BigInteger amount, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("toAddress", toAddress);
        params.put("contractAddress", contractAddress);
        params.put("password", password);
        params.put("amount", amount);
        params.put("remark", remark);
        return params;
    }


    /**
     * 删除合约
     */
    @Test
    public void delete() throws Exception {
        contractAddress_nrc20 = "tNULSeBaNBfqo6FC8jJJiXFE1gcSQU2D3UwQ1b";
        String remark = "delete contract";
        Map params = this.makeDeleteParams(sender, contractAddress_nrc20, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, DELETE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(DELETE));
        Assert.assertTrue(null != result);
        Log.info("delete-result:{}", JSONUtils.obj2PrettyJson(result));
    }
    private Map makeDeleteParams(String sender, String contractAddress, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

    /**
     * 向合约地址转账(失败情况，NRC20合约不接受NULS转账)
     */
    @Test
    public void transfer2ContractFailed() throws Exception {
        BigInteger value = BigInteger.TEN.pow(8);
        String remark = "transfer 2 contract";
        Map params = this.makeTransferParams(sender, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TRANSFER));
        Assert.assertTrue(null != result);
        Log.info("transfer2Contract-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }
    private Map makeTransferParams(String address, String toAddress, BigInteger amount, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("toAddress", toAddress);
        params.put("password", password);
        params.put("amount", amount);
        params.put("remark", remark);
        return params;
    }

    private void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            Log.info("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
