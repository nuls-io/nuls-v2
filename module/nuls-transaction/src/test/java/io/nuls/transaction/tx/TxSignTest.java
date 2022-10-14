/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.tx;

import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
public class TxSignTest {

    public static void main(String[] args) throws Exception {
        //String txSign = "2102c1ba01932d6451c6d64966151fef6ab1aa40f47b855d9576b7dc0412adffdbc2473045022100855d069fbb65f9dd3873fe3abba51246da671b3754ef4de0e55dfc0d176b051102203072e774ab3a66ddf3f140494d1d3f4cff19734aaca94ce8fba7070e879f9d15";
        //String txHash = "8ddbb4a504532defc35f8db6feb63fd205a3a4cde952f74a5ab2537c024ba95d";

        //TransactionSignature sign = new TransactionSignature();
        //sign.parse(HexUtil.decode(txSign),0);
        //byte[] pub = sign.getP2PHKSignatures().get(0).getPublicKey();
        //System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        //ECKey ecKey = ECKey.fromPublicOnly(pub);
        //boolean result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(0).getSignData().getSignBytes());
        //System.out.println(result);

        //pub = sign.getP2PHKSignatures().get(1).getPublicKey();
        //System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        //ecKey = ECKey.fromPublicOnly(pub);
        // result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(1).getSignData().getSignBytes());
        //System.out.println(result);


        //for (P2PHKSignature signature : sign.getP2PHKSignatures()) {
        //    byte[] pub = signature.getPublicKey();
        //    System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        //    ECKey ecKey = ECKey.fromPublicOnly(pub);
        //    boolean result = ecKey.verify(HexUtil.decode(txHash), signature.getSignData().getSignBytes());
        //    System.out.println(result);
        //    //if (!ECKey.verify(HexUtil.decode(txHash), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
        //    //    System.out.println("bbbbbbbbbb");
        //    //    throw new NulsException(new Exception("Transaction signature error !"));
        //    //}
        //    System.out.println("aaaaaaaaa");
        //}

        List<String> list = new ArrayList<>();
        list.add("NULSd6HggxbVpms48DxhAu6X8MdkrMCZLTrDq  265687  4e84a5a3398670920dd7b79e6d3854f88495aa34d22d720346a59dd9589dfdfa");
        list.add("NULSd6HggjUx7Yka54WXJVc82Di62gJ8XzZmV  140123  5038a9820e9560087f07e169717d00de2606b86d69b7474b1781fd97ee81ab4d");
        list.add("NULSd6HgiYsM5S3b1K79R8o7ecoLjQNvfPRus  306132  8dd58cec6636a46d6b45e6c73795c87490efd3c17aa7d40a6c9b185c623a003f");
        list.add("NULSd6HgiQrKehxN6kL9qeNWVdZqhqLfkjHCk  30061.8  b06cc1b6f0af0f44c5a67fd9de4ebf5fe7a0ba80a3536636789662f40257dac7");
        list.add("NULSd6HgakaunpRHdivWPqKFZrYLPgkPBtNtr  258480  99612be08804132c23c6116335811116efd12738eacc17c8af2b707d22cd817e");
        list.add("NULSd6Hgbcn7gdGuapjgM8QsSTBtqsgKZ7Y8v  73758  ac027273c5df327ce23e78bed320fae845b02328e20d5c57252cfa16fb15c2b3");
        list.add("NULSd6HgYFbxkBQY4g5KALyZYnViHC69cYBDJ  365615 8ddbb4a504532defc35f8db6feb63fd205a3a4cde952f74a5ab2537c024ba95d");
        list.add("NULSd6HgYFbxkBQY4g5KALyZYnViHC69cYBDJ  6107   a7341611bc17338709d5587155cc590818b27fd59edad83644d56867a89d68f4");
        list.add("NULSd6HgdsnbbU9UL9vuxszcPqdLE7ADUDwKR  15654  0b5e52c2f4d7772279a00ffb43101cb332c10d6d8d8be4662e744c7eebe1db4f");
        list.add("NULSd6HgYYcsTcjPLphgzGoJ6bWqRjsE6oEbk  105444 8c4215091a11fc5ea4d97a12a982525ae3a7f1cf7801c44faf8a51ba2db9e26c");
        list.add("NULSd6HgbJZ4n4roadePzyf8Zx2YkD8RcoYuc  215272 ba9b2cc987b888f95340e39dcb1942a71cf8dc1be052dea6f0b2199e35821d01");
        list.add("NULSd6HgbJZ4n4roadePzyf8Zx2YkD8RcoYuc  10000  12192e6ad13599b027ac07b0e51eeae948ee193e5d4c0ce75050c205f1f685a9");
        list.add("NULSd6HgXDFjzhjNANSNCH6sXvjP4d1Zu5vrV  15870  3108d428751d0c4ea70ea7b6218b08f78eb28b1b27d897e7623642778353e6df");
        list.add("NULSd6HgbBDcE6chnSfvgPpT3YBy4tHA2v4By  21811  ebc8117c88f859ce271685533e36638d89106457ef53959cddcf34945e95e16c");
        list.add("NULSd6HgYpRtzGgRHy32ayZXZjVx5RuUNN7PD  10299  83359646d1a4bf60685f08d1900c9e5f2ae1c9cf25459fe87d839b2a3cad06af");
        list.add("NULSd6HgUXrV1fkbKwszFhSjhckZbW25TQeWj  12601  0f50082a8cee0e42d547f8c9c346898e3317050364c867e2a51c2c8d305b1904");
        list.add("NULSd6HgUxiPz3EMdZSuMHZf1caN8dVYCxRJx  3885  690661d1d5fc58bfbbdfa90f5874382b0f12ac5aa5382639bb956c14961236b6");
        list.add("NULSd6HgdwgigpqWvDBjt5ptPeioGsp49fTbp  2775  ddbcd47c767df7f89fa42412e9bf9105d498b452e6076852795b1590654473b5");
        //for (String aaa : list) {
        //    String[] aaas = aaa.split("\\s+");
        //    String address = aaas[0].trim();
        //    System.out.println(Arrays.toString(aaas));
        //    String txInfo = HttpClientUtil.get(String.format("http://192.168.1.125:8004/api/tx/%s", aaas[2].trim()));
        //    Map<String, Object> objectMap = JSONUtils.json2map(txInfo);
        //    Map map = (Map) objectMap.get("data");
        //    String txHash = map.get("hash").toString();
        //    String txSign = map.get("transactionSignature").toString();
        //    System.out.println(txHash);
        //    System.out.println(txSign);
        //    TransactionSignature sign = new TransactionSignature();
        //    sign.parse(HexUtil.decode(txSign),0);
        //    //byte[] pub = sign.getP2PHKSignatures().get(0).getPublicKey();
        //    //System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        //    //ECKey ecKey = ECKey.fromPublicOnly(pub);
        //    //boolean result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(0).getSignData().getSignBytes());
        //    //System.out.println(result);
        //
        //    //pub = sign.getP2PHKSignatures().get(1).getPublicKey();
        //    //System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        //    //ecKey = ECKey.fromPublicOnly(pub);
        //    // result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(1).getSignData().getSignBytes());
        //    //System.out.println(result);
        //
        //
        //    for (P2PHKSignature signature : sign.getP2PHKSignatures()) {
        //        byte[] pub = signature.getPublicKey();
        //        String genAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub, 1));
        //        System.out.println(genAddress);
        //        boolean invalid = !address.equals(genAddress);
        //        if (invalid) {
        //            System.err.println("地址不一致");
        //        }
        //        ECKey ecKey = ECKey.fromPublicOnly(pub);
        //        boolean result = ecKey.verify(HexUtil.decode(txHash), signature.getSignData().getSignBytes());
        //        if (!result) {
        //            System.err.println("验证失败");
        //        }
        //        System.out.println(result);
        //        //if (!ECKey.verify(HexUtil.decode(txHash), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
        //        //    System.out.println("bbbbbbbbbb");
        //        //    throw new NulsException(new Exception("Transaction signature error !"));
        //        //}
        //        System.out.println("aaaaaaaaa");
        //    }
        //    System.out.println("---------------------------------------");
        //}



    }
    /*
    list.add("NULSd6HggxbVpms48DxhAu6X8MdkrMCZLTrDq  265687  4e84a5a3398670920dd7b79e6d3854f88495aa34d22d720346a59dd9589dfdfa");
    list.add("NULSd6HggjUx7Yka54WXJVc82Di62gJ8XzZmV  140123  5038a9820e9560087f07e169717d00de2606b86d69b7474b1781fd97ee81ab4d");
    list.add("NULSd6HgiYsM5S3b1K79R8o7ecoLjQNvfPRus  306132  8dd58cec6636a46d6b45e6c73795c87490efd3c17aa7d40a6c9b185c623a003f");
    list.add("NULSd6HgiQrKehxN6kL9qeNWVdZqhqLfkjHCk  30061.8  b06cc1b6f0af0f44c5a67fd9de4ebf5fe7a0ba80a3536636789662f40257dac7");
    list.add("NULSd6HgakaunpRHdivWPqKFZrYLPgkPBtNtr  258480  99612be08804132c23c6116335811116efd12738eacc17c8af2b707d22cd817e");
    list.add("NULSd6Hgbcn7gdGuapjgM8QsSTBtqsgKZ7Y8v  73758  ac027273c5df327ce23e78bed320fae845b02328e20d5c57252cfa16fb15c2b3");
    list.add("NULSd6HgYFbxkBQY4g5KALyZYnViHC69cYBDJ  365615 8ddbb4a504532defc35f8db6feb63fd205a3a4cde952f74a5ab2537c024ba95d");
    list.add("NULSd6HgYFbxkBQY4g5KALyZYnViHC69cYBDJ  6107   a7341611bc17338709d5587155cc590818b27fd59edad83644d56867a89d68f4");
    list.add("NULSd6HgdsnbbU9UL9vuxszcPqdLE7ADUDwKR  15654  0b5e52c2f4d7772279a00ffb43101cb332c10d6d8d8be4662e744c7eebe1db4f");
    list.add("NULSd6HgYYcsTcjPLphgzGoJ6bWqRjsE6oEbk  105444 8c4215091a11fc5ea4d97a12a982525ae3a7f1cf7801c44faf8a51ba2db9e26c");
    list.add("NULSd6HgbJZ4n4roadePzyf8Zx2YkD8RcoYuc  215272 ba9b2cc987b888f95340e39dcb1942a71cf8dc1be052dea6f0b2199e35821d01");
    list.add("NULSd6HgbJZ4n4roadePzyf8Zx2YkD8RcoYuc  10000  12192e6ad13599b027ac07b0e51eeae948ee193e5d4c0ce75050c205f1f685a9");
    list.add("NULSd6HgXDFjzhjNANSNCH6sXvjP4d1Zu5vrV  15870  3108d428751d0c4ea70ea7b6218b08f78eb28b1b27d897e7623642778353e6df");
    list.add("NULSd6HgbBDcE6chnSfvgPpT3YBy4tHA2v4By  21811  ebc8117c88f859ce271685533e36638d89106457ef53959cddcf34945e95e16c");
    list.add("NULSd6HgYpRtzGgRHy32ayZXZjVx5RuUNN7PD  10299  83359646d1a4bf60685f08d1900c9e5f2ae1c9cf25459fe87d839b2a3cad06af");
    list.add("NULSd6HgUXrV1fkbKwszFhSjhckZbW25TQeWj  12601  0f50082a8cee0e42d547f8c9c346898e3317050364c867e2a51c2c8d305b1904");
    list.add("NULSd6HgUxiPz3EMdZSuMHZf1caN8dVYCxRJx  3885  690661d1d5fc58bfbbdfa90f5874382b0f12ac5aa5382639bb956c14961236b6");
    list.add("NULSd6HgdwgigpqWvDBjt5ptPeioGsp49fTbp  2775  ddbcd47c767df7f89fa42412e9bf9105d498b452e6076852795b1590654473b5");
     */

    @Test
    public void signData() throws Exception {
        String txSign = "210232bdaf6573319eba3b433ed88d6d4b0d06ea3ad9a504596fe967a2dbf95fc07c473045022100f4e1f683803a103b79eeb420a6d04089a673f93b2ea3d3012508320846f91c5302205c133ff512d6865fcb47c7fe07c7d930dfe6857cd5d69ab8065609435929a439";
        TransactionSignature sign = new TransactionSignature();
        sign.parse(HexUtil.decode(txSign),0);
        System.out.println(HexUtil.encode(sign.getP2PHKSignatures().get(0).getPublicKey()));
    }
}
