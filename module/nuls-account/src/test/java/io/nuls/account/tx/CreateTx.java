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

package io.nuls.account.tx;

import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.rpc.call.LedgerCmdCall;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * 组装交易
 * @author: Charlie
 * @date: 2019/4/22
 */
public class CreateTx {

    private static Chain chain = new Chain();
    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;
    static String password = "nuls123456";//"nuls123456";


    static {
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetsId(assetId);
        chain.setConfig(configBean);
    }
    /**
     * 创建普通转账交易
     *
     * @return
     */
    public static Map createTransferTx(String addressFrom, String addressTo, BigInteger amount) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "transfer test");
        List<CoinDto> inputs = new ArrayList<>();
        List<CoinDto> outputs = new ArrayList<>();
        CoinDto inputCoin1 = new CoinDto();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100000").add(amount));
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

    static Map<String, String> accMap = new HashMap<>();
    static {
        accMap.put("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b");
        accMap.put("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75");
        accMap.put("tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24", "8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78");
        accMap.put("tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD", "4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530");
        accMap.put("tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL", "bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7");
        accMap.put("tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL", "ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200");
        accMap.put("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a");
        accMap.put("tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1", "3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1");
        accMap.put("tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2", "27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e");
        accMap.put("tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn", "76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b");
    }

    /**
     * 组装交易
     * @param fromList
     * @param toList
     * @param remark
     * @param prehash 前一笔交易hash 用于连续发交易时测试
     * @return
     * @throws NulsException
     */
    public static Transaction assemblyTransaction(List<CoinDto> fromList, List<CoinDto> toList, String remark, NulsHash prehash) throws Exception {
        Transaction tx = new Transaction(2);
        tx.setTime(TimeUtils.getCurrentTimeSeconds());
        tx.setRemark(StringUtils.bytes(remark));
        //组装CoinData中的coinFrom、coinTo数据
        assemblyCoinData(tx, fromList, toList, prehash);
        tx.setHash(NulsHash.calcDigestData(tx.serializeForHash()));
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        for (CoinDto from : fromList) {
            //根据密码获得ECKey get ECKey from Password
            ECKey ecKey =  ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(accMap.get(from.getAddress()))));
            byte[] signBytes = SignatureUtil.signDigest(tx.getHash().getBytes(), ecKey).serialize();
            P2PHKSignature signature = new P2PHKSignature(signBytes, ecKey.getPubKey()); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
//            signature.parse(new NulsByteBuffer(RPCUtil.decode(signatureStr)));
            p2PHKSignatures.add(signature);
        }
        //交易签名
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        tx.setTransactionSignature(transactionSignature.serialize());
        return tx;
    }



    private static Transaction assemblyCoinData(Transaction tx, List<CoinDto> fromList, List<CoinDto> toList, NulsHash hash) throws Exception {
        //组装coinFrom、coinTo数据
        List<CoinFrom> coinFromList = assemblyCoinFrom( fromList, hash);
        List<CoinTo> coinToList = assemblyCoinTo(toList);
        //来源地址或转出地址为空
        if (coinFromList.size() == 0 || coinToList.size() == 0) {
            return null;
        }
        //交易总大小=交易数据大小+签名数据大小
        int txSize = tx.size() + getSignatureSize(coinFromList);
        //组装coinData数据
        CoinData coinData = getCoinData(chainId, coinFromList, coinToList, txSize);
        tx.setCoinData(coinData.serialize());
        return tx;
    }

    private static CoinData getCoinData(int chainId, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * 通过coinfrom计算签名数据的size
     * 如果coinfrom有重复地址则只计算一次；如果有多签地址，只计算m个地址的size
     *
     * @param coinFroms
     * @return
     */
    private static int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> commonAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            commonAddress.add(address);
        }
        size += commonAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    public static byte[] getNonceByPreHash(String address, NulsHash hash) throws NulsException {
        if (hash == null) {
            byte[] nonce = LedgerCmdCall.getNonce(chainId, assetChainId, assetId, address);
            if(null == nonce){
                return HexUtil.decode("0000000000000000");
            }
            return nonce;
        }
        byte[] out = new byte[8];
        byte[] in = hash.getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        String nonce8BytesStr = HexUtil.encode(out);
        return HexUtil.decode(nonce8BytesStr);
    }
    /**
     * 组装coinFrom数据
     * assembly coinFrom data
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    private static List<CoinFrom> assemblyCoinFrom(List<CoinDto> listFrom, NulsHash hash) throws NulsException {
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
            byte[] nonce = getNonceByPreHash(address,hash);
            CoinFrom coinFrom = new CoinFrom(addressByte, assetChainId, assetId, amount, nonce, (byte) 0);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }


    /**
     * 组装coinTo数据
     * assembly coinTo data
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    private static List<CoinTo> assemblyCoinTo(List<CoinDto> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinDto coinDto : listTo) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //检查该链是否有该资产
            int assetsChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(addressByte);
            coinTo.setAssetsChainId(assetsChainId);
            coinTo.setAssetsId(assetId);
            coinTo.setAmount(coinDto.getAmount());
            coinTos.add(coinTo);
        }
        return coinTos;
    }
}
