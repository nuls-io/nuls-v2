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
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.transaction.TransferService;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
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
import lombok.Data;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
@Data
public class BaseQuery {

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        ServiceManager.init(chainId, Provider.ProviderType.RPC);
    }

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(assetId, chainId, 100000000L));
    }

    protected TransferService transferService = ServiceManager.get(TransferService.class);
    protected Chain chain;
    protected static int chainId = 2;
    protected static int assetId = 1;

    protected static String password = "nuls123456";//"nuls123456";

    protected String sender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected String toAddress0  = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    protected String toAddress1  = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    protected String toAddress2  = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    protected String toAddress3  = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    protected String toAddress4  = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    protected String toAddress5  = "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM";
    protected String toAddress6  = "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29";
    protected String toAddress7  = "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf";
    protected String toAddress8  = "tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S";
    protected String toAddress9  = "tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja";
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
    protected String contractAddress   = "";
    protected String contractAddress0  = "tNULSeBaN2SLtt1o9WnvfPJrXiHjTfhJAu7xhd";
    protected String contractAddress1  = "tNULSeBaN7rVLnJpnqcN1cgEd5DUuJhT3j9WbM";
    protected String contractAddress2  = "tNULSeBaMxa5f7jDEiQiUerPHPJ7UsA2wsdZ1b";
    protected String contractAddress3  = "tNULSeBaN9cLbqLywgXjv9nCiZiuWxFRgdW59L";
    protected String contractAddress4  = "tNULSeBaN2Zxr7E4GJpJ3Tb7kSktyF1SGQWfRK";
    protected String contractAddress5  = "tNULSeBaN5XBoKZGoSQpxiJibP2B2AZbsPwXTg";
    protected String contractAddress6  = "tNULSeBaNC83g7oPKGo4DMNGXN5kAaQAoVfyR9";
    protected String contractAddress7  = "tNULSeBaN4XBFZDLj3ZoUMm1eg5bw4di5EqJKu";
    protected String contractAddress8  = "tNULSeBaMyj4B65opTvoRxGti1KHhjePt2ZcG8";
    protected String contractAddress9  = "tNULSeBaN5m2yDX3XiFz9pj5skA4WhMy9iuQ34";
    protected String contractAddress10 = "tNULSeBaN4yALYAJiMgewwnhfC9AoXQ1YAh3Qt";
    protected String contractAddress11 = "tNULSeBaN47ypNjNMbEfgfxYKkya7dKYZLFN7N";
    protected String contractAddress12 = "tNULSeBaN3EzYgNes6XwZBa6xEeDSAEZo41Uek";
    protected String contractAddress13 = "tNULSeBaMwA8MtTXYng3RjYF833Lt99YeRivaT";
    protected String contractAddress14 = "tNULSeBaMxzhiNCf5RK71PGo2Lf2zEYAfFv5CT";
    protected String contractAddress15 = "tNULSeBaMx4W9Ga5PLCBnuNqUQZKkNW6h5qHVh";
    protected String contractAddress16 = "tNULSeBaMxTZSpcPjuENy3Mk1bruHqiGzFQ7Rs";
    protected String contractAddress17 = "tNULSeBaNBmhyoc5nzeNXdfCwwp7USvxDwTtti";
    protected String contractAddress18 = "tNULSeBaN5wcgJ5WXudKxqrdaa5qBbtvw4hAAT";
    protected String contractAddress19 = "tNULSeBaMwYwwiLi3waue2Mqo1F8LGbeLhWVCZ";
    protected String contractAddress20 = "tNULSeBaN4RCgXURSAhvyE9KEGBMRs5gqstWUh";
    protected String contractAddress21 = "tNULSeBaMyaGhGess9rhU3JLW3ihPb9T1ATPSk";
    protected String contractAddress22 = "tNULSeBaMyko58oux3BBikvERBeAMBQ4QPTrBL";
    protected String contractAddress23 = "tNULSeBaMyqzPNhMHTdtg6DTfhohTNrZLNMW5H";
    protected String contractAddress24 = "tNULSeBaN3XH5ksg9HZuouqEYhwY6qPQRoLu13";
    protected String contractAddress25 = "tNULSeBaN7RUwnubGa6CUfXmQWRF3HoJos9iXV";
    protected String contractAddress26 = "tNULSeBaN83abAqfhK9b3zfsJzy6yGduBENEAh";
    protected String contractAddress27 = "tNULSeBaN1Y4fsJ2RXFEQwZDKneXpg9XRkSb2g";
    protected String contractAddress28 = "tNULSeBaN3HZe38dUVKTgm5ZzUPppCzahf1msx";
    protected String contractAddress29 = "tNULSeBaN6FBmfLQX1NNsvYzosXEbM3j5STHr1";
    protected String contractAddress30 = "tNULSeBaN1CvGh7vT8UwhUj8nmuWkyQPdToDSG";
    protected String contractAddress31 = "tNULSeBaMxL9JbkjFCMtbZGhQu5Xy3AVwj8GXF";
    protected String contractAddress32 = "tNULSeBaN6HrxZnoXusKfdp5hxUkJp3L8Eh5o5";
    protected String contractAddress33 = "tNULSeBaN335iwUwpQUFwjsQsEGY5xFnBkF18X";
    protected String contractAddress34 = "tNULSeBaN6xhwfmihq1wCLPJ6sswURUKMp8btU";
    protected String contractAddress_nrc20   = "";
    protected String contractAddress_nrc200  = "tNULSeBaMyX5uw9QMpyRAYr1FN6yrJ7hc98r1w";
    protected String contractAddress_nrc201  = "tNULSeBaMx9UnjtYiWZKqVX7SdX1JbdgJgua6q";
    protected String contractAddress_nrc202  = "tNULSeBaN5yRVt1FCJpzSRZ8LmwQxur8nQpfRR";
    protected String contractAddress_nrc203  = "tNULSeBaN51QGZqwjqwq8sGWn3DtwRyYx5hRag";
    protected String contractAddress_nrc204  = "tNULSeBaN2SzyX2p7Nb6E3NpiLuRcfcFdnk5GZ";
    protected String contractAddress_nrc205  = "tNULSeBaN6ZUSvRjJXwnJ1h9c29XU4mpfH96TB";
    protected String contractAddress_nrc206  = "tNULSeBaN39Tb8zBvF53DgshSC6Z6vWMsdzRkQ";
    protected String contractAddress_nrc207  = "tNULSeBaNARzJ4teTSDtJREyxoJyDpYkTNpMC4";
    protected String contractAddress_nrc208  = "tNULSeBaNCMVza2YVXZMEmEFFuirfQpyRywtJm";
    protected String contractAddress_nrc209  = "tNULSeBaNB8oCXWKhNAcmTV8hv9iW1chNvMX12";
    protected String contractAddress_nrc2010 = "tNULSeBaMwH2zbzW7Cu72DYFkfmLaPgytdibYy";
    protected String contractAddress_nrc2011 = "tNULSeBaN2cZt6hMsdD5hM25KQKAEfjSkS6Dpq";
    protected String contractAddress_nrc2012 = "tNULSeBaN6FAPoQQUSvfjJBVAR7gC3bUkXQ9AS";
    protected String contractAddress_nrc2013 = "tNULSeBaN1pC1Xeyp4QhqzJXRueECfraTj4LQL";
    protected String contractAddress_nrc2014 = "tNULSeBaN1ccaRGHoPr2CXtAZmaJe5pcFwLuHB";
    protected String contractAddress_nrc2015 = "tNULSeBaMyaj3nktq2g1kL7kdUtRLNABCD8z98";
    protected String contractAddress_nrc2016 = "tNULSeBaN26tg339dcvcPN8cCZnSKvZCt39CGX";
    protected String contractAddress_nrc2017 = "tNULSeBaN5VeeCtBCizX2eFgx2gsUWJhpT2yG2";
    protected String contractAddress_nrc2018 = "tNULSeBaN3MeRv6nLoDRjSPJ6RzC1sQj3NWaps";
    protected String contractAddress_nrc2019 = "tNULSeBaN7PuuvtYxZh6rg87JoELkPYkvazyks";
    protected String contractAddress_nrc2020 = "tNULSeBaN8dqL55m7VMTNWSmgf9dJBNnCUNBYk";
    protected String contractAddress_nrc2021 = "tNULSeBaNA4gDNs61zT42cTFrDXSXHsr9LLLTt";
    protected String contractAddress_nrc2022 = "tNULSeBaN7cqD4dw1hHG7uWbsBS1P9FbwC82d1";
    protected String contractAddress_nrc2023 = "tNULSeBaN8PT45PkSHTd4nL1j2zqXo9YCqdaMb";
    protected String contractAddress_nrc2024 = "tNULSeBaN2Q4gg2YJuK3FDnGm7d1ZRSApf344X";
    protected String contractAddress_nrc2025 = "tNULSeBaNCSpkQMpguWy7BiF8qASbiyiS5sJf3";
    protected String contractAddress_nrc2026 = "tNULSeBaMyRRWeJauRgPpzHgffd6cEVZDuRnTc";
    protected String contractAddress_nrc2027 = "tNULSeBaN5pFZ291MoWfBK22jHomwgoEEbRB8N";
    protected String contractAddress_nrc2028 = "tNULSeBaN3pAjbemJ8BXWj6bEmuv6AwwqN4dVX";
    protected String contractAddress_nrc2029 = "tNULSeBaN5KMB4tkhGmENojgjJwSvcMLBKL1Dv";
    protected String contractAddress_nrc2030 = "tNULSeBaN3GYn2dJsvSWop75AZzrncupt1TtHJ";
    protected String contractAddress_nrc2031 = "tNULSeBaN29uBF7w3xCwTsfMApbpM4D9Xa32hK";
    protected String contractAddress_nrc2032 = "tNULSeBaN3VSL9u92dNye9f9rQzbtmbBKmbtEa";
    protected String contractAddress_nrc2033 = "tNULSeBaN1SXicQB2DH8yiwXByM94DVAWsbDbs";
    protected String contractAddress_nrc2034 = "tNULSeBaNAMctKH47UDEBxHEwA79xeoyyymw17";

    protected String methodName = "";

    protected String callHash = "0020874dca08dbf4784540e26c0c31f728a2c2fd2e18bf71c896d8f88955d53e77b7";
    protected String deleteHash = "0020b2c159dbdf784c2860ec97072feb887466aa50fc147a5b50388886caab113f9a";

    @Test
    public void getBlockHeader() throws NulsException, JsonProcessingException {
        BlockHeader blockHeader = BlockCall.getBlockHeader(chainId, 20L);
        Log.info("\nstateRoot is " + Hex.toHexString(ContractUtil.getStateRoot(blockHeader)) + ", " + blockHeader.toString());
    }

    @Test
    public void getBalance() throws Exception {
        Map<String, Object> balance0 = LedgerCall.getBalanceAndNonce(chain, contractAddress0);
        Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance0));
    }

    @Test
    public void getTxClient() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", "00202c7282a29d99daad03be2211ae9d9b648eaa2092fabd077a06183fbf3a280463");
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Map resultMap = (Map) record.get("tx_getTxClient");
        String txHex = (String) resultMap.get("txHex");
        Transaction tx = Transaction.getInstance(txHex);
        Log.info("tx is {}", JSONUtils.obj2PrettyJson(tx));

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
        Map params = this.makeContractResultParams("0020a764748fcf517d5f3e2cda47de67994549d1c0c39036087ceb441ff4748fb22c");
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
        Map params = this.makeContractTxParams("0020c251fe9fd04e78c15dfdf660b5db477df684e6352191e02bd2d6f640774c0309");
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
        params.put("address", "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn");
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
