package io.nuls.crosschain.datacheck;

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
        String signStr = "2103a16be2cb8826ab8ea0c53745ebb7962069d542d278cd64482e2b86c178582fdf483046022100fb21c54f4bdc458489813caf8e8e28713145363f99aa1fd0e842b5f99131e3200221009d46983c3929fe4b5f957f87e4e5c2e2ba2168fb3e1302e5b2085f2247a45c72";

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
