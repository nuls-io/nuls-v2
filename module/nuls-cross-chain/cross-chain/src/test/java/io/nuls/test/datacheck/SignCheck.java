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
        String signStr = "2102b036e93c63a1e591435e9021b76a9f38db9e766f2a38af0f91a3ded3c42f553746304402203510f127e542125b10ad6b1ff7887aee25ec41cb5e07fa9d62d6c47061518bad022009926a36b9ea7dae33e7c52cc4d3b40262024df623413c0d4458eca450fdc053";

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
