package io.nuls.test.cases.transcation.batch;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.constant.TxConstant;
import io.nuls.test.cases.transcation.batch.fasttx.model.Chain;
import io.nuls.test.cases.transcation.batch.fasttx.model.CoinDto;
import io.nuls.test.cases.transcation.batch.fasttx.model.ConfigBean;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @Author: ljs
 * @Time: 2019-04-25 12:08
 * @Description: 功能描述
 */
@Component
public class BatchTxsCase extends BaseTestCase<String, Map> {

    @Override
    public String title() {
        return "batch tx test";
    }
    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";

    /**
     * 空地址
     * tNULSeBaMm8Kp5u7WU5xnCJqLe8fRFD49aZQdK
     * tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8
     */
    private Chain chain;
    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";


    private ExecutorService signExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, new NulsThreadFactory("THREAD_VERIFIY_BLOCK_TXS_TEST"));


    public void importPriKeyTest() {
//        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", password);//种子出块地址 tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", password);//种子出块地址 tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//20 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//21 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//22 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", password);//23 tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", password);//24 tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", password);//25 tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", password);//26 tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", password);//27 tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", password);//28 tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", password);//29 tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn
    }

    public void importPriKey(String priKey, String pwd) {
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
            Log.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> createAccount(int chainId, int count, String password) {
        List<String> accountList = null;
        Response cmdResp = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", count);
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params, 60000L);
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            Log.error("cmdResp:{}", cmdResp);
            e.printStackTrace();
        }
        return accountList;
    }

    private List<String> createAddress(int count) throws Exception {
        List<String> addressList = new ArrayList<>();
        if (100 <= count) {
            int c1 = count / 100;
            for (int i = 0; i < c1; i++) {
                List<String> list = createAccount(chainId, 100, password);
                addressList.addAll(list);
            }
            int c2 = count % 100;
            if (c2 > 0) {
                List<String> list = createAccount(chainId, c2, password);
                addressList.addAll(list);
            }
        } else if (100 > count) {
            List<String> list = createAccount(chainId, count, password);
            addressList.addAll(list);
        }
        return addressList;
    }

    private List<CoinFrom> assemblyCoinFrom(int chainId, List<CoinDto> listFrom, NulsHash hash) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinDto coinDto : listFrom) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //检查该链是否有该资产
            int assetChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();

            //检查对应资产余额是否足够
            BigInteger amount = coinDto.getAmount();
            //查询账本获取nonce值
            byte[] nonce = getNonceByPreHash(createChain(), address, hash);
            CoinFrom coinFrom = new CoinFrom(addressByte, assetChainId, assetId, amount, nonce, (byte) 0);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
            String nonce = (String) result.get("nonce");
            if (null == nonce) {
                return TxConstant.DEFAULT_NONCE;
            }
            return RPCUtil.decode(nonce);
        } catch (RuntimeException e) {
            throw new NulsException(e);
        }
    }

    public static byte[] getNonceByPreHash(Chain chain, String address, NulsHash hash) throws NulsException {
        if (hash == null) {
            return getNonce(chain, address, assetChainId, assetId);
        }
        byte[] out = new byte[8];
        byte[] in = hash.getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        String nonce8BytesStr = HexUtil.encode(out);
        return HexUtil.decode(nonce8BytesStr);
    }

    private Chain createChain() {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetsId(assetId);
        chain.setConfig(configBean);
        return chain;
    }

    private List<CoinTo> assemblyCoinTo(int chainId, List<CoinDto> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinDto coinDto : listTo) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //转账交易转出地址必须是本链地址
            if (!AddressTool.validAddress(chainId, address)) {
                Log.debug("failed");
            }
            //检查该链是否有该资产
            int assetsChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            //检查金额是否小于0
            BigInteger amount = coinDto.getAmount();
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                Log.debug("failed");
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(addressByte);
            coinTo.setAssetsChainId(assetsChainId);
            coinTo.setAssetsId(assetId);
            coinTo.setAmount(coinDto.getAmount());
            coinTos.add(coinTo);
        }
        return coinTos;
    }

    private Transaction assemblyCoinData(Transaction tx, int chainId, List<CoinDto> fromList, List<CoinDto> toList, NulsHash hash) throws NulsException {
        try {
            //组装coinFrom、coinTo数据
            List<CoinFrom> coinFromList = assemblyCoinFrom(chainId, fromList, hash);
            List<CoinTo> coinToList = assemblyCoinTo(chainId, toList);
            //来源地址或转出地址为空
            if (coinFromList.size() == 0 || coinToList.size() == 0) {
                return null;
            }
            //交易总大小=交易数据大小+签名数据大小
            int txSize = tx.size() + getSignatureSize(coinFromList);
            //组装coinData数据
            CoinData coinData = getCoinData(chainId, coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
        } catch (Exception e) {
        }
        return tx;
    }

    private CoinData getCoinData(int chainId, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    private int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> commonAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            commonAddress.add(address);
        }
        size += commonAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    private Transaction assemblyTransaction(int chainId, List<CoinDto> fromList, List<CoinDto> toList, String remark, NulsHash hash) throws NulsException {
        Transaction tx = new Transaction(2);
        tx.setTime(NulsDateUtils.getCurrentTimeMillis() / 1000);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            //组装CoinData中的coinFrom、coinTo数据
            assemblyCoinData(tx, chainId, fromList, toList, hash);
            //计算交易数据摘要哈希
            byte[] bytes = tx.serializeForHash();
            tx.setHash(NulsHash.calcHash(bytes));
            //创建ECKey用于签名
//            List<ECKey> signEcKeys = new ArrayList<>();
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            for (CoinDto from : fromList) {
//                P2PHKSignature p2PHKSignature = AccountCall.signDigest(from.getAddress(), from.getPassword(), tx.getHash().getBytes());

                Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("address", from.getAddress());
                params.put("password", password);
                params.put("data", RPCUtil.encode(tx.getHash().getBytes()));
                HashMap result = (HashMap) requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
                String signatureStr = (String) result.get("signature");

                P2PHKSignature signature = new P2PHKSignature(); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
                signature.parse(new NulsByteBuffer(RPCUtil.decode(signatureStr)));

                p2PHKSignatures.add(signature);
            }
            //交易签名
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            return tx;

        } catch (Exception e) {
        }
        return tx;
    }

    private List<String> doAccountsCreateAndGiveMoney(int addrCount, BigInteger amount, String richAddr) throws Exception {
        List<String> list = createAddress(addrCount);
        //给新生成账户转账
        NulsHash hash = null;
        Log.info("交易账户余额初始化...");
        for (int i = 0; i < addrCount; i++) {
            String address = list.get(i);
            Map transferMap = this.createTransferTx(richAddr, address, new BigInteger("90000000000"));
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDto>) transferMap.get("inputs"),
                    (List<CoinDto>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            hash = tx.getHash();
            HashMap result = (HashMap) requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
        }
        //睡30秒
        Thread.sleep(30000L);
        return list;
    }

    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object requestAndResponse(String moduleCode, String cmd, Map params) throws NulsException {
        Long timeout = null;
        try {
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            Response response = null;
            try {
                if (null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                } else {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();

                throw new NulsException(ErrorCode.init(errorCode));
            }
            Map data = (Map) response.getResponseData();
            return data.get(cmd);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map createTransferTx(String addressFrom, String addressTo, BigInteger amount) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "abc");
        List<CoinDto> inputs = new ArrayList<>();
        List<CoinDto> outputs = new ArrayList<>();
        CoinDto inputCoin1 = new CoinDto();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("30000000").add(amount));
        inputs.add(inputCoin1);

        CoinDto outputCoin1 = new CoinDto();
        outputCoin1.setAddress(addressTo);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(amount);
        outputs.add(outputCoin1);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }

    private long doTrans(Map<String, NulsHash> preHashMap, List<String> list1, List<String> list2, int count) throws Exception {
        long countTx = 0;
        for (int i = 0; i < count; i++) {
            String address = list1.get(i);
            String addressTo = list2.get(i);
            Map transferMap = this.createTransferTx(address, addressTo, new BigInteger("1000000"));
            Transaction tx = assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDto>) transferMap.get("inputs"),
                    (List<CoinDto>) transferMap.get("outputs"), (String) transferMap.get("remark"), preHashMap.get(address));
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
//                Log.debug("hash:" + tx.getHash().toHex());
            HashMap result = (HashMap) requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            preHashMap.put(address, tx.getHash());
            countTx++;
            Thread.sleep(1L);
        }
        return countTx;

    }

    public void mAddressTransferLjs(String address1, String address2) throws Exception {
        int count = 10000;
        Log.info("创建转账账户...");
        List<String> list1 = doAccountsCreateAndGiveMoney(count, new BigInteger("90000000000"), address1);
        List<String> list2 = doAccountsCreateAndGiveMoney(count, new BigInteger("90000000000"), address2);
        //睡30秒
        Thread.sleep(30000L);
        //新生成账户各执行一笔转账
        Log.debug("{}", System.currentTimeMillis());
        long countTx = 0;
        Map<String, NulsHash> preHashMap = new HashMap<>();
        long x = 0;
        while (true) {
            x++;
            Log.info("start Transfer {} 笔,  * 第 {} 次", countTx, x);
            long startTime = System.currentTimeMillis();
            countTx = countTx + doTrans(preHashMap, list1, list2, count);
            countTx = countTx + doTrans(preHashMap, list2, list1, count);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            Log.info("tx count:{} - execution time:{} milliseconds,  about≈:{}seconds", countTx, executionTime, executionTime / 1000);
            Thread.sleep(10000L);
        }
    }

    @Override
    public String doTest(Map param, int depth) throws TestFailException {
        Log.info("doTest----");
        Log.info("do Test {}",param.get("id").toString());
        ThreadUtils.createAndRunThread("batchTxsCase", () -> {
                importPriKeyTest();
                try {
                    String id = param.get("id").toString();
                    String addr1 = "";
                    String addr2 = "";
                    if (id.contains("135")) {
                        addr1 = address20;
                        addr2 = address21;
                    } else if (id.contains("136")) {
                        addr1 = address22;
                        addr2 = address23;
                    } else if (id.contains("130")) {
                        addr1 = address24;
                        addr2 = address25;
                    }else if (id.contains("148")) {
                        addr1 = address26;
                        addr2 = address27;
                        Log.info("{}{}",addr1,addr2);
                    } else {
                        return;

                    }
                    mAddressTransferLjs(addr1, addr2);

                } catch (Exception e) {
                    Log.error(e);
                    return;
                }
            });
        return "success";
    }
}
