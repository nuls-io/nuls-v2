package io.nuls.chain.test;

import io.nuls.chain.model.po.Asset;
import io.nuls.core.log.Log;
import io.nuls.core.parse.I18nUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class AssetCmdTest {

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        I18nUtils.loadLanguage(AssetCmdTest.class, "languages", "en");
    }

    @Before
    public void init() throws Exception {
        NoUse.mockModule();
    }
    @Test
    public void a() throws Exception{
        BigInteger a=new BigInteger("1000");
        BigInteger b=new BigInteger("4");
        BigInteger c=new BigInteger("10");
        System.out.println(a.multiply(b).divide(c));
    }

    @Test
    public void getAsset() throws Exception {
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("chainId",100);
        parameters.put("assetId",1);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_asset", parameters);
        System.out.println(JSONUtils.obj2json(response));
    }

    @Test
    public void assetReg() throws Exception {
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("chainId",100);
        parameters.put("address","tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");
        parameters.put("assetId",2);
        parameters.put("symbol","ns2");
        parameters.put("assetName","nulson2");
        parameters.put("initNumber",2000000000);
        parameters.put("decimalPlaces",8);
        parameters.put("password","nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_assetReg", parameters);
        System.out.println(JSONUtils.obj2json(response));
    }

    @Test
    public void mainNetAssetReg() throws Exception {
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("address","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        parameters.put("assetId",2);
        parameters.put("password","nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_mainNetAssetReg", parameters);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void assetRegValidator() throws Exception {
//        Asset asset = build();
//        asset.setAssetId(1542092573248L);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setSymbol("showmethemoney");
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setSymbol("￥");
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setName("abnclasjflajsdfljasldfjalsiiwrpqwiefakvcnaskdfjlj");
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setDepositNuls(1);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setInitNumber(10000 - 1);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setInitNumber(100000000 + 1);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setDecimalPlaces((short) 3);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = build();
//        asset.setDecimalPlaces((short) 9);
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
//
//        asset = new Asset();
//        asset.setAssetId(1542092573248L);
//        asset.setSymbol("￥");
//        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
    }

    private Asset build() {
        Asset asset = new Asset();
        asset.setChainId(867);
        asset.setAssetId(3);
        asset.setSymbol("HH");
        asset.setAssetName("HHHHHH");
        asset.setDepositNuls(new BigInteger("20000000000000"));
        asset.setInitNumber(BigInteger.valueOf(95565));
        asset.setDecimalPlaces((short) 8);
        asset.setAvailable(true);
        asset.setCreateTime(System.currentTimeMillis()/1000);
        return asset;
    }

    @Test
    public void assetRegCommit() throws Exception {
        Asset asset = new Asset();
        asset.setChainId((short) 867);
        asset.setAssetId(23);
        asset.setSymbol("B");
        asset.setAssetName("bts");
        asset.setDepositNuls(new BigInteger("20000000000000"));
        asset.setInitNumber(BigInteger.valueOf(32232));
        asset.setDecimalPlaces((short) 8);
        asset.setAvailable(true);
        asset.setCreateTime(System.currentTimeMillis()/1000);
//        System.out.println(CmdDispatcher.call("assetRegCommit", new Object[]{asset}));
    }

    @Test
    public void assetRegRollback() throws Exception {

    }

    @Test
    public void assetEnable() throws Exception {
//        System.out.println(CmdDispatcher.call("assetEnable", new Object[]{1542092573248L}));
//        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }

    @Test
    public void assetDisable() throws Exception {
//        System.out.println(CmdDispatcher.call("assetDisable", new Object[]{1542092573248L}));
//        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }

    @Test
    public void assetDisableValidator() throws Exception {
//        System.out.println(CmdDispatcher.call("assetDisableValidator", new Object[]{1, 1542092573248L}));
//        System.out.println(CmdDispatcher.call("assetDisableValidator", new Object[]{867, 1542092573248L}));
    }

    @Test
    public void assetStringTest() {
        BigDecimal b = new BigDecimal("999999999999999999999999999999999999999999");
        System.out.println(b.toString());
    }
}
