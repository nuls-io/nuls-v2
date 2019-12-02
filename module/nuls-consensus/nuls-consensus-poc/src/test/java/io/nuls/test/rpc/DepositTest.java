package io.nuls.test.rpc;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.tx.txdata.CancelDeposit;
import io.nuls.poc.rpc.call.CallMethodUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
/**
 * 委托相关操作测试
 * Delegate related operation testing
 *
 * @author tag
 * 2018/12/1
 * */
public class DepositTest {
    protected  String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void depositAgent()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address","tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");
        params.put("agentHash","3efea7edbb5b8eed2a4a4e62d3c47bc72306e30551b6c2eaca20319aa0798060");
        params.put("deposit","3000000000000");
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void depositCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", RPCUtil.encode(blockHeader.serialize()));
        params.put("tx","05006eaeecca67010049e09304000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e7017200205d245e366862da82a1bd36745e1719e8b73e45dc320467d8639f9e0c82c397676801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100801a06000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100e0930400000000000000000000000000ffffffff00");
        //params.put("tx","0500bdc742a167010049e09304000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e7017200207d53655ffdb1bd3b5a05bc4d6e14d7c9980ff22e889fa7c2374e2c4b9cd8119f6801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100801a06000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100e0930400000000000000000000000000ffffffff00");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void depositRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", RPCUtil.encode(blockHeader.serialize()));
        params.put("tx","0600f9cae45d0020241e930ee7b0a4912c82e9e3ada08e7b1839d2757411b381d151349965a39d688c01170200018f44b8662e78871f44ef1e1608282fd59560dcd0020001000030ef7dba02000000000000000000000000000000000000000000000000000008d151349965a39d68ff01170200018f44b8662e78871f44ef1e1608282fd59560dcd00200010060a9ed7dba0200000000000000000000000000000000000000000000000000000000000000000000692103295636f1609058c098053b44ebc4ab28627a7015c0576ae24766e0d36f19901d46304402205179cd4f201c13cdfec33b3847641a25d85b008ec98ac3a189828b0ea0f5f67802201db96c994a554a0135028dbb5bbc9dc26de7eeac86ea1b4c8f82f582fc412e66");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void withdraw()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address","tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");
        params.put("txHash","8684f6b03fc4c17ac6ba69dc2e664337b2807e83eecb4a86a7f58978c5b63d56");
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
        //a3ed95750d13eb17b1c744ecadb43e34da538d15c2318469d900a796f13a14ee
    }

    @Test
    public void withdrawCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        params.put("tx","");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdrawCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void withdrawRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        params.put("tx","");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdrawRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getDepositList()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put("agentHash","22b7d4dfbffab1bf5be5f1b63fd8ab8e1fdf84c306c5be297bd9996cc58320c7");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getDepositList", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void paramTest()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("intCount",1);
        params.put("byteCount",125);
        params.put("shortCount",1555);
        params.put("longCount",666666);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.KE.abbr, "paramTestCmd", params);
        System.out.println(cmdResp);
    }


    @Test
    public void tamperTransaction()throws Exception{
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode("0500c3d7e45d00570030ef7dba0200000000000000000000000000000000000000000000000000000200018f44b8662e78871f44ef1e1608282fd59560dcd03efea7edbb5b8eed2a4a4e62d3c47bc72306e30551b6c2eaca20319aa07980608c01170200018f44b8662e78871f44ef1e1608282fd59560dcd002000100a0b6f07dba02000000000000000000000000000000000000000000000000000008a7f58978c5b63d560001170200018f44b8662e78871f44ef1e1608282fd59560dcd0020001000030ef7dba020000000000000000000000000000000000000000000000000000ffffffffffffffff6a2103295636f1609058c098053b44ebc4ab28627a7015c0576ae24766e0d36f19901d473045022100fd3253ea0a3ccf16ad1703d9148ac11c108673564f81f0bfb732123b4d811c15022008ed1fbdaed9fbab9c2929cdec8c5ee6ce1f696f366463f59bcded05746904eb"), 0);

        //修改签名
        /*tx.setTransactionSignature(null);
        HashMap callResult = CallMethodUtils.accountValid(2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456");
        String priKey = (String) callResult.get("priKey");
        CallMethodUtils.transactionSignature(2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", priKey, tx);*/

        //修改CoinData
        CoinData coinData = tx.getCoinDataInstance();
        coinData.getFrom().get(0).setAddress(AddressTool.getAddress("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm"));
        coinData.getTo().get(0).setAddress(AddressTool.getAddress("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
        tx.setCoinData(coinData.serialize());

        tx.setTransactionSignature(null);
        HashMap callResult = CallMethodUtils.accountValid(2, "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "nuls123456");
        String priKey = (String) callResult.get("priKey");
        CallMethodUtils.transactionSignature(2, "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "nuls123456", priKey, tx);

        String txStr = RPCUtil.encode(tx.serialize());
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(2);
        chain.setConfig(configBean);
        CallMethodUtils.sendTx(chain, txStr);
    }

}
