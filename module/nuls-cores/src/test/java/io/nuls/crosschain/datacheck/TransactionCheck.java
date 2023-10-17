package io.nuls.crosschain.datacheck;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.crosschain.base.model.bo.txdata.CrossTransferData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/30 17:30
 * @Description: 功能描述
 */
public class TransactionCheck {

    public static void verifierChangeTx(Transaction tx) throws NulsException {
        byte[] txData = tx.getTxData();
        VerifierChangeData verifierChangeData = new VerifierChangeData();
        verifierChangeData.parse(txData,0);
        Log.info("{}",verifierChangeData);
    }

    public static void main(String[] args) throws NulsException {
        String txHex = "1800fee42b5f002b0200000126744e554c536542614d6f42645946556d366664463867423733636732335955767a635753596d006921037fae74d15153c3b55857ca0abd5c34c865dfa1c0d0232997c545bae5541a086346304402204529682f745e033977a278b7bdc2f2fd400d83182b7f0c0b8e68736810578f6702206f19b18ec0cbfbe5bd7a585fc78b80ef306a5a19902580b9c6f2d70b08a9a0e7";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex),0);
        verifierChangeTx(tx);
        TransactionSignature transactionSignature = new TransactionSignature();
        transactionSignature.parse(tx.getTransactionSignature(),0);
        Log.info("size:{}",transactionSignature.getSignersCount());

//        byte[] txData = tx.getTxData();
//        if(txData != null){
//            CrossTransferData crossTransferData = new CrossTransferData();
//            crossTransferData.parse(txData,0);
//            Log.info("sourceHash:{}",HexUtil.encode(crossTransferData.getSourceHash()));
//        }
//        Log.info("{}",AddressTool.getChainIdByAddress("TNVTdTSPMsBqXoEVtkAyPBVyduftKvZVQopFD"));
    }

}
