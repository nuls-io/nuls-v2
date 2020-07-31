package io.nuls.test.datacheck;

import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.crosschain.base.model.bo.txdata.CrossTransferData;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/30 17:30
 * @Description: 功能描述
 */
public class TransactionCheck {

    public static void main(String[] args) throws NulsException {
        String txHex = "0a005394235f00008c01170100015cd6524cecf1f83de0e626ec7b9a9267487c5bbf0100010040261b5402000000000000000000000000000000000000000000000000000000081aaddd62fd42b0d60001175fea019b2df4ee82fd35e59339f607df8abd69fc224eef0100010000e40b5402000000000000000000000000000000000000000000000000000000000000000000000069210223ba61d911074569ab7a33b94af6115bbf4d55669edf4f3c926bfd829631cffd4630440220332523dbc6ad60cb5f791e6b674510dbc2d978f1a9eb4ade134954065a7b58fc022062eaf1cc4c8444fdb3b99e66c3d774bf59e58946115d58634fc03c9c6bfef796";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex),0);
        TransactionSignature transactionSignature = new TransactionSignature();
        transactionSignature.parse(HexUtil.decode("210223ba61d911074569ab7a33b94af6115bbf4d55669edf4f3c926bfd829631cffd4630440220332523dbc6ad60cb5f791e6b674510dbc2d978f1a9eb4ade134954065a7b58fc022062eaf1cc4c8444fdb3b99e66c3d774bf59e58946115d58634fc03c9c6bfef796"),0);
        Log.info("{}",transactionSignature.getSignersCount());
        byte[] txData = tx.getTxData();
        if(txData != null){
            CrossTransferData crossTransferData = new CrossTransferData();

        }

    }

}
