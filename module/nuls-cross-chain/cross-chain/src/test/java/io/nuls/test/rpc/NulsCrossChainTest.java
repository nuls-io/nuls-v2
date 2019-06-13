package io.nuls.test.rpc;
import io.nuls.crosschain.nuls.model.dto.input.CoinDTO;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.log.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NulsCrossChainTest {
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";
    static int chainId = 2;
    static String password = "nuls123456";

    static String main_address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String main_address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String main_address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String main_address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String main_address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String main_address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String main_address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String main_address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String main_address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String main_address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";


    static String local_address1 = "8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w";
    static String local_address2 = "8CPcA7kaj56TWAC3Cix64aYCU3XFoNpu1LN1K";
    static String local_address3 = "8CPcA7kaiDAkvVP28GwXR6eP2oDKPcnPnmvLD";
    static String local_address4 = "8CPcA7kaZDdGEzXe8gwQNQg4u4teecArHt9Dy";
    static String local_address5 = "8CPcA7kaW82Eoj9wyLr96g2uBhHtFqD9Vy4yM";
    static String local_address6 = "8CPcA7kaUW98RW3g7erqTNT7b1gyoaqwxFEY3";
    static String local_address7 = "8CPcA7kaZTXgqBR7DYVbsj8yWUD2sZah6kknY";
    static String local_address8 = "8CPcA7kaaw7jvfn93Zrf7vNwtXyRQMY71zdYF";
    static String local_address9 = "8CPcA7kaUvrGb68gYWcceJRY2Mx2KfUTMJmgB";
    static String local_address10 = "8CPcA7kag8NijwHK8eTJVVMGXjfkT3GDAVo7n";

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createCtx(){
        try{
            List<CoinDTO> fromList = new ArrayList<>();
            List<CoinDTO> toList = new ArrayList<>();
            fromList.add(new CoinDTO(main_address26,assetChainId,assetId, BigInteger.valueOf(100000000000L),password));
            toList.add(new CoinDTO(local_address1,assetChainId,assetId, BigInteger.valueOf(100000000000L),password));
            Map paramMap = new HashMap();
            paramMap.put("listFrom", fromList);
            paramMap.put("listTo", toList);
            paramMap.put("chainId", chainId);
            paramMap.put("remark", "transfer test");
            //调用接口
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, "createCrossTx", paramMap);
            if (!cmdResp.isSuccess()) {
                Log.info("接口调用失败！" );
            }
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("createCrossTx"));
            Assert.assertTrue(null != result);
            String hash = (String) result.get("txHash");
            Log.debug("{}", hash);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
