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
    protected String contractAddress0  = "tNULSeBaNCUFknSX9yiuQjPrWAfmWCfHePr8Cz";
    protected String contractAddress1  = "tNULSeBaN6rNfEuJATNCYPfHVeDufPAVVW3syZ";
    protected String contractAddress2  = "tNULSeBaNAejwHL9VNmdL8nCC1CrbYGLoVDpvx";
    protected String contractAddress3  = "tNULSeBaMyWpiWE5NNygT3D266ZZVpPLoHwNjx";
    protected String contractAddress4  = "tNULSeBaMxFsx3wutVKMDpNXmazyDZMLXVNLCb";
    protected String contractAddress5  = "tNULSeBaN3LEGtBZsMFfubbqdkXFjxprprEXdk";
    protected String contractAddress6  = "tNULSeBaNAFFACHZn7BWZFDQ8oR16wkHw5sHLw";
    protected String contractAddress7  = "tNULSeBaNCGcsLNz13CEJf5h7gcDUmv89sXArd";
    protected String contractAddress8  = "tNULSeBaMxUqEbtq5YEmmSWvoQhKe3HtGYTdy4";
    protected String contractAddress9  = "tNULSeBaN7qj5GwJz8BXtRqveGPp6QQZmyqC9k";
    protected String contractAddress10 = "tNULSeBaN2kuwrywTL1KDW4THc38LUZvgwhFiD";
    protected String contractAddress11 = "tNULSeBaMxxtmKsWfB2DnVRhG2dnPycTHgR8ef";
    protected String contractAddress12 = "tNULSeBaNBmnoJmfNHWqwfBG8rH93hT3CcDoqD";
    protected String contractAddress13 = "tNULSeBaN1xLJbH9DarRdop55JJsnuM7vHchQd";
    protected String contractAddress14 = "tNULSeBaNAuDKCWsWMBbEZaY8KrsnZiYkecib6";
    protected String contractAddress15 = "tNULSeBaN6Sbj3ev9uFuetd17z7M795Ta2RkEV";
    protected String contractAddress16 = "tNULSeBaN98wUu2Nh1jWAmjnb5pzXQevtWqsGa";
    protected String contractAddress17 = "tNULSeBaMwdhuGeHgdtbMhqrSdKxPKATrfVbeS";
    protected String contractAddress18 = "tNULSeBaMwuTA9kN78AGc9HiceFj6co87s744Y";
    protected String contractAddress19 = "tNULSeBaMy9B1yJ41V5kE6U2TAJVWAwijofS6V";
    protected String contractAddress20 = "tNULSeBaMxLpfz3n9fNTpQVGpp6pWHE5iAvgBJ";
    protected String contractAddress21 = "tNULSeBaMyjeYUXJoFdraAsczfszJBiZkyN3bJ";
    protected String contractAddress22 = "tNULSeBaN3UCy3R2Z9pDisnwHSGCUjhxY9BcNi";
    protected String contractAddress23 = "tNULSeBaN7jQbmtNYQfBsbQARyqmfNLaoDLF7R";
    protected String contractAddress24 = "tNULSeBaMxk3bBUPshA1z8UKWg5ZSKNCa6x8nC";
    protected String contractAddress25 = "tNULSeBaMy2TE4bemsLqkxUGXXG1e3nj3X1D1T";
    protected String contractAddress26 = "tNULSeBaN8jPWLNob7zdSBZHXRbD3TwwrmafSR";
    protected String contractAddress27 = "tNULSeBaNBz3ZebGTUTWXAoqLfLoFQVz7xidcH";
    protected String contractAddress28 = "tNULSeBaN7VHDhALEZSdJQJNwwmxnKif2K6kw2";
    protected String contractAddress29 = "tNULSeBaN4jAJMmXvpd3ZvuBSyrgNWQFemW2Xb";
    protected String contractAddress30 = "tNULSeBaN3wEpJ5s2PcL8MtCiBb66nYAAzH4gP";
    protected String contractAddress31 = "tNULSeBaMyALix8eBDMDBbQ9fVmXYbQyFMsy5H";
    protected String contractAddress32 = "tNULSeBaN3fg7Sodfcs9rCD6zMcJ71juqALShj";
    protected String contractAddress33 = "tNULSeBaN4F1ecZFKufEVxZ2j1MUETxEop9SrG";
    protected String contractAddress34 = "tNULSeBaN5v6kvGenoDxPq7H3uqJpq5Lu2QuNH";
    protected String contractAddress_nrc20   = "";
    protected String contractAddress_nrc200  = "tNULSeBaN5HBecW8UZLRmALd31W639fP1VgRTG";
    protected String contractAddress_nrc201  = "tNULSeBaN1iEsnc8oPsoPNLwabF3tMi6nEUK4R";
    protected String contractAddress_nrc202  = "tNULSeBaN21hu3XKZtPaJuJ1aJauaV4kRsyjxQ";
    protected String contractAddress_nrc203  = "tNULSeBaN9B1QXHV9qbML2jgF2VhaFwjkkeAxa";
    protected String contractAddress_nrc204  = "tNULSeBaMyrHwoarMBHC3V6mUusjMx6wRYPpxt";
    protected String contractAddress_nrc205  = "tNULSeBaMvmRSr74Q8AnWpsAqMaJeNMMQ99BDK";
    protected String contractAddress_nrc206  = "tNULSeBaN1GhAmt472asAHCmHfWCQeihN2AYQF";
    protected String contractAddress_nrc207  = "tNULSeBaNACK9YKYCTezx9dNW8zL49PUXBnh62";
    protected String contractAddress_nrc208  = "tNULSeBaN5zGGhCiNtQzbGXbHZYhH6M9twa3ZB";
    protected String contractAddress_nrc209  = "tNULSeBaN71ZXWTonxoe8bWsYvhbiBQ2kNEteZ";
    protected String contractAddress_nrc2010 = "tNULSeBaN2M4DF47Rw59ViZ7F8KNdLaAe8UpzK";
    protected String contractAddress_nrc2011 = "tNULSeBaMwEwKfapdiDqyUwvuRXasFiceEuYqD";
    protected String contractAddress_nrc2012 = "tNULSeBaN8rWVJ9ZeJVa52pLmUCHrrECQEVjK1";
    protected String contractAddress_nrc2013 = "tNULSeBaN7NmTpY3rZU63eQ954jsgNWKs5UvVN";
    protected String contractAddress_nrc2014 = "tNULSeBaMzmNfD133mvogMVRVX68FjWCbiYtVA";
    protected String contractAddress_nrc2015 = "tNULSeBaMxByGof9ykBGf24nHnTRY28nPZWQGt";
    protected String contractAddress_nrc2016 = "tNULSeBaN4bMAA3b8QnTW3Zboby7ar4DQsNpiy";
    protected String contractAddress_nrc2017 = "tNULSeBaN1wT3AEikRxJG8tcntYkzUgRpVXknZ";
    protected String contractAddress_nrc2018 = "tNULSeBaN5pVnnDWxECTwDAoNii17UbTKk4BcY";
    protected String contractAddress_nrc2019 = "tNULSeBaNAPVWGkrEe1r5w5KG6YQNyxWJxRANZ";
    protected String contractAddress_nrc2020 = "tNULSeBaN8djo29gvgbrbsREWKMvCbQajJg2Q1";
    protected String contractAddress_nrc2021 = "tNULSeBaN8ETnmHeptnN6JuqhKdTxJhuo2d6no";
    protected String contractAddress_nrc2022 = "tNULSeBaN7CBS58sK6xsh42Yrk7hMaXtCJke5U";
    protected String contractAddress_nrc2023 = "tNULSeBaN5DYEyDYzwFDhLptQSwoeGuTKpYk1v";
    protected String contractAddress_nrc2024 = "tNULSeBaN4ypStnpUfzEASTJGSQpMcbA2EfXQw";
    protected String contractAddress_nrc2025 = "tNULSeBaMypZSA7obL5UFN2TzBHn8LrZY1iimG";
    protected String contractAddress_nrc2026 = "tNULSeBaN9Ln7KAbTfmkd5ZybnTqGcZr18JLux";
    protected String contractAddress_nrc2027 = "tNULSeBaNBQdFaZRpceAkEgHPR5zTc8XTcTbxV";
    protected String contractAddress_nrc2028 = "tNULSeBaN6kkCP1JbnkRCCEHk7VP8DwGSZJuwc";
    protected String contractAddress_nrc2029 = "tNULSeBaMwphEBNkDMTMg92jjHTxpierKYcYi9";
    protected String contractAddress_nrc2030 = "tNULSeBaN9eMEdwWhyvcfBfGdo2XsJqfwMmvTc";
    protected String contractAddress_nrc2031 = "tNULSeBaMxj8AuamvHE8Y4ycYF4ztGtduktABg";
    protected String contractAddress_nrc2032 = "tNULSeBaN2c4QmNtgitLkM4RASrV4wpVFC26q2";
    protected String contractAddress_nrc2033 = "tNULSeBaMyHXg73Nm3DUPvy34AwxDuoXJm8jqY";
    protected String contractAddress_nrc2034 = "tNULSeBaN4npYUuNiTmrG1YdYzMRzjxRuvTPka";

    protected String methodName = "";
    protected String tokenReceiver = "";

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
        Map params = this.makeContractInfoParams("tNULSeBaN84zDybqFWimEYDpXu99qo8z1TjDPX");
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
