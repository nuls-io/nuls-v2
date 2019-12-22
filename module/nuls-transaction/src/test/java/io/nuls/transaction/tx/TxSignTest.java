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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;

/**
 * @author Niels
 */
public class TxSignTest {

    public static void main(String[] args) throws NulsException {
        String txSign = "2102b0a6f555e548a43fb7ddb6560b7edf099b1a649be66b3cebfb3cfc3c589f502a473045022100ae6ab360460d937168a53f176a0ab2aa207d11ff4dc0c45f250f323ca7509d9a022005e2b00839860648078125348f42a86857d71bb7673c94516381bde8e05b38cf2102fc324c8e18dc0ee816bc186b3d4240ca856b80dc5312f96b207b8b84339f185a473045022100ae6ab360460d937168a53f176a0ab2aa207d11ff4dc0c45f250f323ca7509d9a022005e2b00839860648078125348f42a86857d71bb7673c94516381bde8e05b38cf";
        String txHash = "568533e38c472f9e0740f318038b49ebe6b59817ce374c6816bf645744beb6ed";

        TransactionSignature sign = new TransactionSignature();
        sign.parse(HexUtil.decode(txSign),0);
        byte[] pub = sign.getP2PHKSignatures().get(0).getPublicKey();
        System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        ECKey ecKey = ECKey.fromPublicOnly(pub);
        boolean result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(0).getSignData().getSignBytes());
        System.out.println(result);

        pub = sign.getP2PHKSignatures().get(1).getPublicKey();
        System.out.println(AddressTool.getStringAddressByBytes(AddressTool.getAddress(pub,1)));
        ecKey = ECKey.fromPublicOnly(pub);
         result = ecKey.verify(HexUtil.decode(txHash),sign.getP2PHKSignatures().get(1).getSignData().getSignBytes());
        System.out.println(result);


        for (P2PHKSignature signature : sign.getP2PHKSignatures()) {
            if (!ECKey.verify(HexUtil.decode(txHash), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                System.out.println("bbbbbbbbbb");
                throw new NulsException(new Exception("Transaction signature error !"));
            }
            System.out.println("aaaaaaaaa");
        }
    }
}
