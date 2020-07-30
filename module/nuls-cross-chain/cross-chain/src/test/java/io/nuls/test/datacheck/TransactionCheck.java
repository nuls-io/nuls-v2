package io.nuls.test.datacheck;

import io.nuls.base.data.Transaction;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.model.bo.txdata.CrossTransferData;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/30 17:30
 * @Description: 功能描述
 */
public class TransactionCheck {

    public static void main(String[] args) throws NulsException {
        String txHex = "0a005892225f00008c01170100015cd6524cecf1f83de0e626ec7b9a9267487c5bbf01000100c0d8a7000000000000000000000000000000000000000000000000000000000008b94d88ec1095f9b80001175fea019b2df4ee82fd35e59339f607df8abd69fc224eef01000100809698000000000000000000000000000000000000000000000000000000000000000000000000006921037bec7cda4a8359bf3df2eba389c50ceb75581eda20f149b10a46a35651431cea463044022012c4086c614aac6c21b1248bdc9f58179a60eb65dd7b918a6a4414ada1b912090220496b5cb5e69b6e7c9edf1dcaff65dd46ee00c80ca5a89fbc06291867a1a90f11";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex),0);
        byte[] txData = tx.getTxData();
        if(txData != null){
            CrossTransferData crossTransferData = new CrossTransferData();

        }

    }

}
