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
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.contract.base.Base;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class BaseQuery extends Base {

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        ServiceManager.init(chainId, Provider.ProviderType.RPC);
    }

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
                .addForm(sender, password, BigInteger.valueOf(33_1000_0000_0000L))
                .addTo(toAddress5, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress6, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress7, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress8, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress9, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress10, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress11, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress12, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress13, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress14, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress15, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress16, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress17, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress18, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress19, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress20, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress21, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress22, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress23, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress24, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress25, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress26, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress27, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress28, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress29, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress30, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress31, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress32, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress33, BigInteger.valueOf(1000000000000L))
                .addTo(toAddress34, BigInteger.valueOf(1000000000000L))
                .addTo("tNULSeBaMrNuXBLLUS1zJSERqbf3jm5c633fiS", BigInteger.valueOf(3_1000_0000_0000L));
        System.out.println(transferService.transfer(builder.build()).getData());
    }

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(assetId, chainId, 100000000L));
    }

    protected TransferService transferService = ServiceManager.get(TransferService.class);


    @Test
    public void getBlockHeader() throws NulsException, JsonProcessingException {
        BlockHeader blockHeader = BlockCall.getBlockHeader(chainId, 20L);
        Log.info("\nstateRoot is " + HexUtil.encode(ContractUtil.getStateRoot(blockHeader)) + ", " + blockHeader.toString());
    }

    @Test
    public void getBalance() throws Exception {
        Map<String, Object> balance0 = LedgerCall.getBalanceAndNonce(chain, toAddress7);
        Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance0));
    }

    @Test
    public void base64ToBase58() {
        String base64Str = "AgACPJCMF36z+LvlLbuxYRwT48hQmgA=";
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        Log.info("address is {}", AddressTool.getStringAddressByBytes(bytes));

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
        params.put(Constants.CHAIN_ID, chainId);
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
        Map params = this.makeContractInfoParams("tNULSeBaMxtRUjhRLsm2GfDyhHEZbP1yeEz76R");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_INFO, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_INFO));
        Log.info("contract_info-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(null != result);
    }

    private Map makeContractInfoParams(String contractAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        return params;
    }

    /**
     * 获取合约执行结果
     */
    @Test
    public void contractResult() throws Exception {
        Map params = this.makeContractResultParams("25b3f3e9c1cc893efcc6939433283736fa959f3625f2ec28a02ef279ed63f27e");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_RESULT));
        Log.info("contractResult-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(null != result);
    }

    private Map makeContractResultParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash);
        return params;
    }

    /**
     * 获取合约交易
     */
    @Test
    public void contractTx() throws Exception {
        Map params = this.makeContractTxParams("e75a531a0220d5ff4e0386334a21d5a986b79f97bda6373127f84be39ba5dc9b");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_TX, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_TX));
        Assert.assertTrue(null != result);
        Log.info("contractTx-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeContractTxParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash);
        return params;
    }


    /**
     * 查交易
     */
    @Test
    public void getTxClient() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", "3e7faf0939b131ccb018ce5b96761fb9178cbd247d781a8c1315a4e47c08630f");
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Map resultMap = (Map) record.get("tx_getTxClient");
        String txHex = (String) resultMap.get("tx");
        Assert.assertTrue(null != txHex);
        Transaction tx = new Transaction();
        tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
        Log.info("tx is {}", JSONUtils.obj2PrettyJson(tx));

    }

    @Test
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
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

    public TransferService getTransferService() {
        return transferService;
    }

    public void setTransferService(TransferService transferService) {
        this.transferService = transferService;
    }

    private void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

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
