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
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    protected String contractAddress0  = "tNULSeBaN7ZnfydbGZSDbXKZAqJs1kjucaC6y3";
    protected String contractAddress1  = "tNULSeBaN5nQ4piuib8NuCxwSuLHkQSBDsxoa2";
    protected String contractAddress2  = "tNULSeBaN1em4GXgF91FMzPyJkY3nQPztTaj1N";
    protected String contractAddress3  = "tNULSeBaMzRHxh3SBEdMZJx4wFqB55vKH36eUM";
    protected String contractAddress4  = "tNULSeBaNCMDh4xmZMdv1FFhh6KrdifqbropNP";
    protected String contractAddress5  = "tNULSeBaN5uZzSDxBTuyQ92T7pFk5Ny5FLMQhH";
    protected String contractAddress6  = "tNULSeBaN3L1KHhJZLE2UQDZgvmyxo4w7XKajf";
    protected String contractAddress7  = "tNULSeBaMyyf8VTJkHbvb2Kbd9g7f2og5G9x9h";
    protected String contractAddress8  = "tNULSeBaN475NghSmrkiXDHJRjXB3fUHVGQguW";
    protected String contractAddress9  = "tNULSeBaN12tr5THvn1MCv3ef55dtWNAZoAQvN";
    protected String contractAddress10 = "tNULSeBaN7eMa9pEttNipRNYW9cHXBxAnTDZcS";
    protected String contractAddress11 = "tNULSeBaN5oakfaSEXhKSeynWDrU1pvqvjoPxi";
    protected String contractAddress12 = "tNULSeBaN1twECi8Lh4dem1CNMneobwNwze2Z5";
    protected String contractAddress13 = "tNULSeBaNCPzREHnwEgWH3CRT6h1bbsr4wMJXj";
    protected String contractAddress14 = "tNULSeBaNBZhLfAo1eg6vivQYA9kPDNUsLoA9q";
    protected String contractAddress15 = "tNULSeBaN1FmRsnfcahgiX5tei6CFPHwvfHcvv";
    protected String contractAddress16 = "tNULSeBaNAHVt4zwVtgX25ytv9q1AKG1PiFU93";
    protected String contractAddress17 = "tNULSeBaN9d7PdM5h1BqwD1zQ8t4rN2ytX3XxN";
    protected String contractAddress18 = "tNULSeBaMwjHbgz5kRQhTwxYfNtGUS6wGsj4W6";
    protected String contractAddress19 = "tNULSeBaMxtwWsggGS3HQ1gKALtY9wVorWBfsa";
    protected String contractAddress20 = "tNULSeBaN92rcvmNmjGNQvtbVMh1pm2xVurm3b";
    protected String contractAddress21 = "tNULSeBaMymBqqmtsHVyi3G2nxhewWb2EBpAAe";
    protected String contractAddress22 = "tNULSeBaNAkB6kbqxwzFJd5cpTmE31eqFx52Ec";
    protected String contractAddress23 = "tNULSeBaMzL4keSqFNvHooNvEseS8gxuohEM89";
    protected String contractAddress24 = "tNULSeBaMyLtcPns5xDfgGmmfjDMFwytsGFaMf";
    protected String contractAddress25 = "tNULSeBaMzjwKD79eiLHHsif4wCe2kLwTvao55";
    protected String contractAddress26 = "tNULSeBaN7f1EHZfRPGsuHKi5iLv1f9FKKo9hS";
    protected String contractAddress27 = "tNULSeBaN1V7cqGjmHaAzcZURc8eptn24vAnAa";
    protected String contractAddress28 = "tNULSeBaMwqcj82iBQnwZ8DZMCuq2hgVBTCi4P";
    protected String contractAddress29 = "tNULSeBaN2u7DK5Feh9F6SzP1zg2TjreDWhT8j";
    protected String contractAddress30 = "tNULSeBaN4Qp6F9jN69rSUoyymonHRadzaDACf";
    protected String contractAddress31 = "tNULSeBaN9Z1ncJ9Kc3c6rppw5DwiSxRE5FGK6";
    protected String contractAddress32 = "tNULSeBaNBJrxm4N2zKaZvkyKoDEf5EhB28zhD";
    protected String contractAddress33 = "tNULSeBaMy3w8zyTm7v6tLNxbQgmDW82tStq75";
    protected String contractAddress34 = "tNULSeBaN8HJH5395svtzWpnfUAQUzqSE5x4t2";
    protected String contractAddress_nrc20   = "";
    protected String contractAddress_nrc200  = "tNULSeBaMzCeoukqhMvWYpCZAbKCN2fE2JVzfK";
    protected String contractAddress_nrc201  = "tNULSeBaNBawLTvbi4yokiXsFLkiNe8x8XH7Ac";
    protected String contractAddress_nrc202  = "tNULSeBaNAnkAcECUu4KVjSWRUKiPaCytRh2dZ";
    protected String contractAddress_nrc203  = "tNULSeBaMxLwGsh7dT2V9nten8noBxN2i4bvpA";
    protected String contractAddress_nrc204  = "tNULSeBaN8DZ1RonVCLJsAicpm32tRE5RRH4F8";
    protected String contractAddress_nrc205  = "tNULSeBaNA2pJxdN8Ui5wuEnymFcF91NrxdL3v";
    protected String contractAddress_nrc206  = "tNULSeBaMvtKgRAGsmEE3V91jSttxxsFtMNQ69";
    protected String contractAddress_nrc207  = "tNULSeBaNBs7FmRVrf2925XvUZe6DJwMjYzJPo";
    protected String contractAddress_nrc208  = "tNULSeBaN63UW3syqccMXY1cz4Utajr1pYLvDo";
    protected String contractAddress_nrc209  = "tNULSeBaNBshfGi9eYvzQauMio7jvXK8havrzu";
    protected String contractAddress_nrc2010 = "tNULSeBaN3TUmG6ZDB6hv9yhe1zDALozziTQYb";
    protected String contractAddress_nrc2011 = "tNULSeBaN2KDRw8z4Muid4ayLjQSS3JA3g7dma";
    protected String contractAddress_nrc2012 = "tNULSeBaN7nPwpUiDuMUv8s3KP1qvmrCfNcqeh";
    protected String contractAddress_nrc2013 = "tNULSeBaN8xftpyYR41ukVejwQx1EXv5LrkU1j";
    protected String contractAddress_nrc2014 = "tNULSeBaN5wu9iPmHmo7V8zQHMMFZyCdQVDuzP";
    protected String contractAddress_nrc2015 = "tNULSeBaN7JyE4GmncaTtP7gZmab5yZm4E8xYb";
    protected String contractAddress_nrc2016 = "tNULSeBaN1Y6guJY5dnr4PCbmqFTXNcTstVbhy";
    protected String contractAddress_nrc2017 = "tNULSeBaNAFyLfA9BUh37PYcJoTKmPxoodGG1N";
    protected String contractAddress_nrc2018 = "tNULSeBaMvmYCoPN5sEvjm9u6uQaxKddd3PH1a";
    protected String contractAddress_nrc2019 = "tNULSeBaMwgpXDnh7YZgi7Bi3pHvsX2GRfJgSL";
    protected String contractAddress_nrc2020 = "tNULSeBaN5CudjrrEMMskRF9JaLgzDpvZnN2Fq";
    protected String contractAddress_nrc2021 = "tNULSeBaN6nU1fpF9Uc8NUAqRSdi36jMkL3Rxk";
    protected String contractAddress_nrc2022 = "tNULSeBaN1jtT4pqHmr6wPYSyYnkAa842hKeqM";
    protected String contractAddress_nrc2023 = "tNULSeBaN8mMezWfv8xwS8ywbE4kB13Bg1SHHy";
    protected String contractAddress_nrc2024 = "tNULSeBaMy6gkMZQWFsQhaa4FQtsG2nSrnhFSg";
    protected String contractAddress_nrc2025 = "tNULSeBaMzZomEiwH7pWdzJonekAfvEZFY4oae";
    protected String contractAddress_nrc2026 = "tNULSeBaN71frdeMUeAWZgkitD56xv1omQhRvU";
    protected String contractAddress_nrc2027 = "tNULSeBaN5zm2pVT1b5wxhyeLgWgNTQTgo6pPN";
    protected String contractAddress_nrc2028 = "tNULSeBaMy6yzt6Z3hqPcNTEMa7JoHAeogrH7i";
    protected String contractAddress_nrc2029 = "tNULSeBaMxvoBpyCkXKVWjoXYXeVZsrxUGjFDV";
    protected String contractAddress_nrc2030 = "tNULSeBaN6umjKPhiefmXbbqMXB4BZByCpgmG1";
    protected String contractAddress_nrc2031 = "tNULSeBaMx7jarG7Rntj6uQ6dipTTkrZUQLRxR";
    protected String contractAddress_nrc2032 = "tNULSeBaNBWoEgy5NvaPQyXGRqJt6esWZoWD4s";
    protected String contractAddress_nrc2033 = "tNULSeBaNB49KvHVo3NPXYqjQBPRc7evKgVvnV";
    protected String contractAddress_nrc2034 = "tNULSeBaN5h2fjwyFRS8WJboYBSLtpRrymJLro";

    protected String methodName = "";
    protected String tokenReceiver = "";

    protected String callHash = "0020874dca08dbf4784540e26c0c31f728a2c2fd2e18bf71c896d8f88955d53e77b7";
    protected String deleteHash = "0020b2c159dbdf784c2860ec97072feb887466aa50fc147a5b50388886caab113f9a";

    @Test
    public void getBlockHeader() throws NulsException, JsonProcessingException {
        BlockHeader blockHeader = BlockCall.getBlockHeader(chainId, 20L);
        Log.info("\nstateRoot is " + HexUtil.encode(ContractUtil.getStateRoot(blockHeader)) + ", " + blockHeader.toString());
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
