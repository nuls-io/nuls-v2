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
package io.nuls.contract.tx.base;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.data.BlockHeader;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class Base {

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(assetId, chainId, 100000000L));
    }

    protected Chain chain;
    protected static int chainId = 2;
    protected static int assetId = 1;

    protected static String password = "nuls123456";//"nuls123456";

    protected String sender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected String toAddress = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    protected String toAddress1 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    protected String toAddress2 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    protected String toAddress3 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    protected String toAddress4 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";

    protected String createHash = "002029ca32525f635a15c82c046114657c0d8a96a7163780ac6b425b2383b240bd56";
    protected String contractAddress = "tNULSeBaN2bUyX77RnC1N45iwfvuYEHnmhE6iB";
    protected String contractAddress_nrc20 = "tNULSeBaN1sf8MkWD6QkMm4WA7tdJiEbGGmUox";

    protected String callHash = "002006dc2d87274408d8ec266d439b0f497e6a44a1e50eaeb1f1eaca1d909b7c4612";
    protected String deleteHash = "0020b2c159dbdf784c2860ec97072feb887466aa50fc147a5b50388886caab113f9a";

    @Test
    public void getBlockHeader() throws NulsException, JsonProcessingException {
        BlockHeader blockHeader = BlockCall.getBlockHeader(chainId, 20L);
        Log.info("\nstateRoot is " + Hex.toHexString(ContractUtil.getStateRoot(blockHeader)) + ", " + blockHeader.toString());
    }

    @Test
    public void getBalance() throws Exception {
        Map<String, Object> balance0 = LedgerCall.getBalanceAndNonce(chain, contractAddress);
        Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance0));
        //Map<String, Object> balance = LedgerCall.getBalance(chain, sender);
        //Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance));
        //Map<String, Object> balance1 = LedgerCall.getBalance(chain, toAddress1);
        //Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance1));
        //Map<String, Object> balance2 = LedgerCall.getBalance(chain, toAddress2);
        //Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance2));
    }

    /**
     * 获取账户创建的合约列表
     */
    @Test
    public void accountContracts() throws Exception {
        Map params = this.makeAccountContractsParams(sender, 1, 10);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, ACCOUNT_CONTRACTS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(ACCOUNT_CONTRACTS));
        Assert.assertTrue(null != result);
        Log.info("accountContracts-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeAccountContractsParams(String address, int pageNumber, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("pageNumber", pageNumber);
        params.put("pageSize", pageSize);
        return params;
    }

    /**
     * 获取合约基本信息
     */
    @Test
    public void contractInfo() throws Exception {
        Map params = this.makeContractInfoParams("tNULSeBaN7CuBiTfqnRk6DuUuUP6251ybd1YEk");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_INFO, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_INFO));
        Assert.assertTrue(null != result);
        Log.info("contract_info-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeContractInfoParams(String contractAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("contractAddress", contractAddress);
        return params;
    }

    /**
     * 获取合约执行结果
     */
    @Test
    public void contractResult() throws Exception {
        Map params = this.makeContractResultParams("00206be26d4d20d39c26df5284331e35cf9b63ce1fae8003f06664deb8c0aaa1fd59");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_RESULT));
        Assert.assertTrue(null != result);
        Log.info("contractResult-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeContractResultParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }

    /**
     * 获取合约交易详情
     */
    @Test
    public void contractTx() throws Exception {
        Map params = this.makeContractTxParams("0020e773a8d3938df93898fcb8d8ac9a1d0cb887d71526c437bf2686dd4008e96146");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_TX, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_TX));
        Assert.assertTrue(null != result);
        Log.info("contractTx-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeContractTxParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }


    /**
     * 查交易
     */
    @Test
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", sender);
        params.put("assetChainId", null);
        params.put("assetId", null);
        params.put("type", null);
        params.put("pageSize", null);
        params.put("pageNumber", null);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxs", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info("Page<TransactionPO>:{}", JSONUtils.obj2PrettyJson(record));
    }

}
