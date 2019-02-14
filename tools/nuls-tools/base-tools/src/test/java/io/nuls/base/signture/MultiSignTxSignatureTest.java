package io.nuls.base.signture;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsSignData;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiSignTxSignatureTest {

    @Test
    public void Test(){
        try {
            MultiSignTxSignature multiSignTxSignature = createMultiSignTxSignature();
            byte[] bytes = multiSignTxSignature.serialize();

            MultiSignTxSignature news = new MultiSignTxSignature();
            news.parse(new NulsByteBuffer(bytes));
            System.out.println(news);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    private MultiSignTxSignature createMultiSignTxSignature(){
        String pubkey1 = "02cca469daec43d8dc8c23f5b4c22c8da37563644b57bd19fd95c3d9170003a11d";
        String pubkey2 = "027f6fec2facbdb7cc3e1053d24407aa706cab748b0724c23e40221374bd325edb";
        String pubkey3 = "035f7acd44c3d07542591c7b580a245f0bf6d328dd89232a3704d1112e9cbf1cf1";

        List<byte[]> list = new ArrayList<>();
        list.add(HexUtil.decode(pubkey1));
        list.add(HexUtil.decode(pubkey2));
        list.add(HexUtil.decode(pubkey3));

        List<P2PHKSignature> slist = new ArrayList<>();
        slist.add(createP2PHKSignature(pubkey1));
        slist.add(createP2PHKSignature(pubkey2));
        MultiSignTxSignature mts = new MultiSignTxSignature();
        mts.setM((byte)2);
        mts.setPubKeyList(list);
        mts.setP2PHKSignatures(slist);
        return mts;
    }


    private P2PHKSignature createP2PHKSignature(String pubkey){
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(HexUtil.decode(pubkey));
        p2PHKSignature.setSignData(new NulsSignData());
        return p2PHKSignature;
    }
}