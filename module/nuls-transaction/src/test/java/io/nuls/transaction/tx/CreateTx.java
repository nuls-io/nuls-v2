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

package io.nuls.transaction.tx;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.TransactionCall;

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
        chain.setConfig(new ConfigBean(chainId, assetId, 1024 * 1024, 1000, 20, 20000, 60000));
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
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100000").add(amount));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
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

    public static Transaction assemblyTransaction(List<CoinDTO> fromList, List<CoinDTO> toList, String remark, NulsHash prehash) throws Exception {
        return assemblyTransaction(fromList, toList, remark, prehash, null);
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
    public static Transaction assemblyTransaction(List<CoinDTO> fromList, List<CoinDTO> toList, String remark, NulsHash prehash, Long txtime) throws Exception {
        Transaction tx = new Transaction(2);
        tx.setTime(null == txtime ? TimeUtils.getCurrentTimeSeconds() : txtime);
        tx.setRemark(StringUtils.bytes(remark));
        //组装CoinData中的coinFrom、coinTo数据
        assemblyCoinData(tx, fromList, toList, prehash);
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        for (CoinDTO from : fromList) {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", from.getAddress());
            params.put("password", password);
            params.put("data", RPCUtil.encode(tx.getHash().getBytes()));
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
            String signatureStr = (String) result.get("signature");
            P2PHKSignature signature = new P2PHKSignature(); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
            signature.parse(new NulsByteBuffer(RPCUtil.decode(signatureStr)));
            p2PHKSignatures.add(signature);
        }
        //交易签名
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        tx.setTransactionSignature(transactionSignature.serialize());
        return tx;
    }



    private static Transaction assemblyCoinData(Transaction tx, List<CoinDTO> fromList, List<CoinDTO> toList, NulsHash hash) throws Exception {
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

    public static byte[] getNonceByPreHash(Chain chain, String address, NulsHash hash) throws NulsException {
        if (hash == null) {
            byte[] nonce = null;
            try {
                return LedgerCall.getNonce(chain, address, assetChainId, assetId);
            } catch (NulsException e) {
                return HexUtil.decode("0000000000000000");
            }
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
    private static List<CoinFrom> assemblyCoinFrom(List<CoinDTO> listFrom, NulsHash hash) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinDTO coinDto : listFrom) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //检查该链是否有该资产
            int assetChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            //检查对应资产余额是否足够
            BigInteger amount = coinDto.getAmount();
            //查询账本获取nonce值
            byte[] nonce = getNonceByPreHash(chain, address,hash);
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
    private static List<CoinTo> assemblyCoinTo(List<CoinDTO> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinDTO coinDto : listTo) {
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
