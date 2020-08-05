package io.nuls.test.datacheck;

import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/28 16:25
 * @Description: 功能描述
 */
public class SignCheck {

    public static void main(String[] args) throws NulsException {
        String signStr = "2102b036e93c63a1e591435e9021b76a9f38db9e766f2a38af0f91a3ded3c42f5537473045022100cf9234366129869b84b2880696937c0caa1607e7c56d67d85957fa357fd42cb1022029db4f15eae5946b23c6fa73ca9b941d3d5cdee5b25868161a5e03a650b5d82f";

        TransactionSignature transactionSignature = new TransactionSignature();
        transactionSignature.parse(HexUtil.decode(signStr),0);
        Log.info("size:{}",transactionSignature.getSignersCount());
        transactionSignature.getP2PHKSignatures().forEach(d->{
            Log.info("{}",HexUtil.encode(d.getPublicKey()));
            if (!ECKey.verify(HexUtil.decode("9811ebbed10ee09b3098a1d33210dc47e9ddf6612753341e4da5bb558969ac5e"), d.getSignData().getSignBytes(), d.getPublicKey())) {
                Log.error("sign fail :{} ",HexUtil.encode(d.getPublicKey()));
            }
        });
    }

}
