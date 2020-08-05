package io.nuls.test.datacheck;

import io.nuls.base.basic.AddressTool;
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
        String txHex = "0a0018cb275f00008c01170100015cd6524cecf1f83de0e626ec7b9a9267487c5bbf01000100402a8648170000000000000000000000000000000000000000000000000000000810c60b7692c457010001175fea019b2df4ee82fd35e59339f607df8abd69fc224eef0100010000e876481700000000000000000000000000000000000000000000000000000000000000000000006921026cee524ab370d0fa9761e42d6f7c451365d7829fa354f7126b0e93735aff1c8146304402207c90042ab532779f4dcc4bdf3b471e23787276651c4ba090de058793b5cc7c2302202efefd6026523d1292f303ab4bae35ca0709fbdcd37a5c370f169ab8333b14e1";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex),0);
        byte[] txData = tx.getTxData();
        if(txData != null){
            CrossTransferData crossTransferData = new CrossTransferData();
            crossTransferData.parse(txData,0);
            Log.info("sourceHash:{}",HexUtil.encode(crossTransferData.getSourceHash()));
        }
        Log.info("{}",AddressTool.getChainIdByAddress("TNVTdTSPMsBqXoEVtkAyPBVyduftKvZVQopFD"));
    }

}
