package io.nuls.test;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.base.model.ResetChainInfoTransaction;
import io.nuls.crosschain.base.model.bo.txdata.ResetChainInfoData;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxSendTester {

    @Test
    public void test() throws Exception {
        NoUse.mockModule();
        String prikey = "";
        ECKey ecKey = ECKey.fromPrivate(HexUtil.decode(prikey));
        byte[] address = AddressTool.getAddress(ecKey.getPubKey(), 2);


        ResetChainInfoTransaction tx = new ResetChainInfoTransaction();
        tx.setTime(System.currentTimeMillis() / 1000);
        ResetChainInfoData txData = new ResetChainInfoData();
        txData.setJson("{\"chainId\":2,\"chainName\":\"nuls2\",\"minAvailableNodeNum\":0,\"maxSignatureCount\":0,\"signatureByzantineRatio\":0,\"addressPrefix\":\"tNULS\",\"assetInfoList\":[{\"assetId\":1,\"symbol\":\"NULS\",\"assetName\":\"\",\"usable\":true,\"decimalPlaces\":8},{\"assetId\":8,\"symbol\":\"T1\",\"assetName\":\"t1\",\"usable\":true,\"decimalPlaces\":9}],\"verifierList\":[],\"registerTime\":0}");
        tx.setTxData(txData.serialize());

        CoinData coinData = new CoinData();
        CoinFrom from = new CoinFrom();
        from.setAddress(address);
        from.setAmount(BigInteger.valueOf(1000000));
        from.setAssetsChainId(2);
        from.setAssetsId(1);
        from.setLocked((byte) 0);
        from.setNonce(HexUtil.decode("ace23d6fad9760d0"));
        coinData.getFrom().add(from);
        CoinTo to = new CoinTo();
        to.setAddress(address);
        to.setAmount(BigInteger.ZERO);
        to.setAssetsId(1);
        to.setAssetsChainId(2);
        to.setLockTime(0);
        coinData.getTo().add(to);

        tx.setCoinData(coinData.serialize());

        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> list = new ArrayList<>();
        P2PHKSignature sig = new P2PHKSignature();
        sig.setPublicKey(ecKey.getPubKey());
        NulsSignData data = new NulsSignData();
        data.setSignBytes(ecKey.sign(tx.getHash().getBytes()));
        sig.setSignData(data);
        list.add(sig);
        transactionSignature.setP2PHKSignatures(list);
        tx.setTransactionSignature(transactionSignature.serialize());
        Log.info(tx.getHash().toHex());
        Log.info(HexUtil.encode(tx.serialize()));
        sendTx(2, HexUtil.encode(tx.serialize()));
    }

    @SuppressWarnings("unchecked")
    public static void sendTx(int chainId, String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", tx);
        try {
            /*boolean ledgerValidResult = commitUnconfirmedTx(chain,tx);
            if(!ledgerValidResult){
                throw new NulsException(ConsensusErrorCode.FAILED);
            }*/
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                //rollBackUnconfirmTx(chain,tx);
                throw new RuntimeException();
            }
        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
