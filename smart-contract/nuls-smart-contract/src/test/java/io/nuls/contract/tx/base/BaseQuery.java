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
    protected String contractAddress0  = "tNULSeBaNAxpCQbLKYhWhW84ZejejrMgkDbFh6";
    protected String contractAddress1  = "tNULSeBaMzp6pDpTGzTaAftWrUTsuHNwvGnFau";
    protected String contractAddress2  = "tNULSeBaN1xsrTTNtaUW26vXknL74RAfBD5zTX";
    protected String contractAddress3  = "tNULSeBaMwwMhq9cY89cWGEEzdFwtMQcW8YtoM";
    protected String contractAddress4  = "tNULSeBaN5PiQJpjUmzEhuzkCJB3o62DacJnhC";
    protected String contractAddress5  = "tNULSeBaN8LkozPSf6sptqadje1qGUXhysTMJC";
    protected String contractAddress6  = "tNULSeBaN4XqZAUiiT4Y2xExTXFT1sRV6vAssg";
    protected String contractAddress7  = "tNULSeBaMy6zqoNHsHfVKRd4bpzAr7hRX3YNXM";
    protected String contractAddress8  = "tNULSeBaNC1jzeVBWJNEqJZENefewHLvUsAg2K";
    protected String contractAddress9  = "tNULSeBaMwRkT5JtraT3V4sab9pDsKzj2Vy7Gn";
    protected String contractAddress10 = "tNULSeBaMyE6MwRMRKYeRV1vXkGZfJt8TcBEmd";
    protected String contractAddress11 = "tNULSeBaN95ap1X6Avu1Dmn683993kfZdSu3Cm";
    protected String contractAddress12 = "tNULSeBaN5YRGT2TUJoH3eMW8XeQS1qVDh3FAS";
    protected String contractAddress13 = "tNULSeBaMwhuD6LAgY7NSmWfiZVKcSJPtjZCby";
    protected String contractAddress14 = "tNULSeBaN5nDDutvFnxf7zHw9Xy3aLy6sEgSfj";
    protected String contractAddress15 = "tNULSeBaMz9X3AZBKwU7YsRHmaMozE49CraLwb";
    protected String contractAddress16 = "tNULSeBaN5tDGtGMJgi8DXsk4a5gMNVUGG5Kzi";
    protected String contractAddress17 = "tNULSeBaN2iCndkqdCtofGe6FDXBm3TbWtVm5m";
    protected String contractAddress18 = "tNULSeBaN3D9agMDswVj3Bo6oUXaYvGBo9ybka";
    protected String contractAddress19 = "tNULSeBaN2Ffp1Kexmb3c6eYAVcyYsWGckb2t9";
    protected String contractAddress20 = "tNULSeBaN7F7q7KXGb4gP4M91mk7MLd7wcZJe3";
    protected String contractAddress21 = "tNULSeBaNA45UHFHoTtvXBfQaJ5MQPn2vwyRBG";
    protected String contractAddress22 = "tNULSeBaMyFLqs8ZvJ4ARkw1nq6EBc8KXp8D7u";
    protected String contractAddress23 = "tNULSeBaN2deALux2NrqJbcefsP4mzpt3QGeKL";
    protected String contractAddress24 = "tNULSeBaMzuRxT6noT5yJuQ8quBG1JcfU4tbY9";
    protected String contractAddress25 = "tNULSeBaMygEchBxTKjL6JiKZus6jSYy47wB4Z";
    protected String contractAddress26 = "tNULSeBaN4iL7Y8NKFvuTfEXtfNJESJNeLrPdM";
    protected String contractAddress27 = "tNULSeBaN2aHZQnWPDKRVKpKPpUoPLuv7SUgWP";
    protected String contractAddress28 = "tNULSeBaMzviEFNwqX5JXDiwoBKpnmF9FduFyE";
    protected String contractAddress29 = "tNULSeBaMwefTkoqd4m3cv4JNCx4arDZ4zjE8S";
    protected String contractAddress30 = "tNULSeBaMy3i8v1RDz8kBeZv8BmABKSSvkhHbC";
    protected String contractAddress31 = "tNULSeBaN9KM89Me6V2H7gCCdDyFPRMpTAwmN8";
    protected String contractAddress32 = "tNULSeBaMxHP255JFCtuuCtVj1nZg1krgQnryK";
    protected String contractAddress33 = "tNULSeBaN1oAoP3GMNNk6Y4bqHpdSHocpUyCoQ";
    protected String contractAddress34 = "tNULSeBaN3mDa2KfSKw1vSd87NnnoZamE8o6Qt";
    protected String contractAddress_nrc20   = "";
    protected String contractAddress_nrc200   = "tNULSeBaN1o2SDwJnt9WM7HzTFh7eFPB3hiPfB";
    protected String contractAddress_nrc201  = "tNULSeBaN4AZpjVFhCmW8VS8G6cyWKStex4S3V";
    protected String contractAddress_nrc202  = "tNULSeBaN6GbG4WmSdt1LZhLgQkGA8vcZwk7vW";
    protected String contractAddress_nrc203  = "tNULSeBaNBTXRM6hUp9tcVXaqwN1WuHc91RYoP";
    protected String contractAddress_nrc204  = "tNULSeBaN1AikL5Udn6ZXGGXiM7rmcPtavAkiV";
    protected String contractAddress_nrc205  = "tNULSeBaNBk14DV9L9VXL3MQXPkLUGbfh3tdho";
    protected String contractAddress_nrc206  = "tNULSeBaMyieAbonkQKXU8D3GBoX5yGjMgXvnR";
    protected String contractAddress_nrc207  = "tNULSeBaNCAr6EvNviGYkmGQaZrpX6wvXDTYJ4";
    protected String contractAddress_nrc208  = "tNULSeBaN3WEJgmKBWZWXJPc6sghMzWFtp9CZp";
    protected String contractAddress_nrc209  = "tNULSeBaMvokU3t6o2N8rkcErpLJVUhbRUSqMp";
    protected String contractAddress_nrc2010 = "tNULSeBaN8wSwi3ocXzny1NH1ifnLpGqRJD7BA";
    protected String contractAddress_nrc2011 = "tNULSeBaN1SAPL9FTgBV7ebejnvGyCvBpMtYcR";
    protected String contractAddress_nrc2012 = "tNULSeBaN9NKoT2GW2LXD6iVrwLx3kXwmtAKTt";
    protected String contractAddress_nrc2013 = "tNULSeBaMx59gu62cK6yFUZTpr5Qk3Do1TK8Vg";
    protected String contractAddress_nrc2014 = "tNULSeBaMxddLdD4kfMJjUti7r3zGm4W5oJ1mF";
    protected String contractAddress_nrc2015 = "tNULSeBaMzmUYVrhw7zC1Zw4rtjsEaHP5ZAXXc";
    protected String contractAddress_nrc2016 = "tNULSeBaN8XqHxxThx5Wo1NRBxcMwbfuWoYNAM";
    protected String contractAddress_nrc2017 = "tNULSeBaN2F1jSibDkFmESq5tbGCS3p7qpbZf9";
    protected String contractAddress_nrc2018 = "tNULSeBaNAthCUAPucTVG6zUP5mEC3ZAUyJQ1f";
    protected String contractAddress_nrc2019 = "tNULSeBaN7SPfaotM2Dts2yQdCjanNppgfpis8";
    protected String contractAddress_nrc2020 = "tNULSeBaN6fH64dA4DGuDNA8627VjVCYonFVNw";
    protected String contractAddress_nrc2021 = "tNULSeBaMyovqC9D1nDHgbFGM4SUUWQgLzmT9f";
    protected String contractAddress_nrc2022 = "tNULSeBaN5JQbmVuLK49ncJqh2tqRBTfTphXSC";
    protected String contractAddress_nrc2023 = "tNULSeBaMy1wPP2i31WvzaTmHAjq3xwAnhSMR8";
    protected String contractAddress_nrc2024 = "tNULSeBaN3BrKF1SHLXP62gNfuSKTvzQSZwtdZ";
    protected String contractAddress_nrc2025 = "tNULSeBaMzZzXKW4PAXSUcafwRxAxAsmAHD8A5";
    protected String contractAddress_nrc2026 = "tNULSeBaN5P7fcAVQsYMGA9VGv5b1pcvgVJEvV";
    protected String contractAddress_nrc2027 = "tNULSeBaN46FMvxbrHkZoSx7WzkH9iBeC4VDnZ";
    protected String contractAddress_nrc2028 = "tNULSeBaN3A6yj4FTa6ig7GnBVt3Bq6kUcEuT4";
    protected String contractAddress_nrc2029 = "tNULSeBaNA1ThaoMCQxwxE9HKqz6uvrdBqCiUf";
    protected String contractAddress_nrc2030 = "tNULSeBaN2GLcdGUqStGM9DwqVdG17rWaSnfZX";
    protected String contractAddress_nrc2031 = "tNULSeBaNAnQDMdyA8Pu1dbgNvLsaYAp3Vz3a9";
    protected String contractAddress_nrc2032 = "tNULSeBaNBbdirjfcQqQ6G9wkUkm3AdsDXWg6e";
    protected String contractAddress_nrc2033 = "tNULSeBaN8cnwCPXhrt1ew4nqFTnonArUY55YC";
    protected String contractAddress_nrc2034 = "tNULSeBaNBrZZASqjGWkiH81gnu8HbeacnneFB";

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
