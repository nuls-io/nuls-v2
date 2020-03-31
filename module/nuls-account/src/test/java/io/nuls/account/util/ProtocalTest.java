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

package io.nuls.account.util;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.basic.VarInt;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Niels
 */
public class ProtocalTest {

    @Test
    public void testCD() throws NulsException {
        String hex = "0117010001d6fc56e4dbf5417e9eb6041450872a600feddbe401000100a067f705000000000000000000000000000000000000000000000000000000000801020304050607080001170100019aa6bccb9e3cba60c95b409701b2417989da208b0100010000e1f505000000000000000000000000000000000000000000000000000000000000000000000000";
        CoinData cd = new CoinData();
        cd.parse(HexUtil.decode(hex),0);
        System.out.println(cd);
    }

    @Test
    public void testtx() throws NulsException {
        String hex = "02002d000000177465737420637265617465207472616e73666572207478117465737420657874656e6420646174612e8c0117010001d6fc56e4dbf5417e9eb6041450872a600feddbe401000100a067f705000000000000000000000000000000000000000000000000000000000801020304050607080001170100019aa6bccb9e3cba60c95b409701b2417989da208b0100010000e1f5050000000000000000000000000000000000000000000000000000000000000000000000006a210233dd5281a4e129dafeea8637b54806f667e56f654e098c5faab87fa7fe889d11473045022100c5bea25111c6729846efd9ebf795b5ac633d1614ca7715aaefa2b1522d645d5d02204534e93be867409b3841869070fa1580e3501391a332b0bb7e0f5bb38a97d2a4";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(hex), 0);
        CoinData cd = new CoinData();
        cd.parse(tx.getCoinData(), 0);
        TransactionSignature ts = new TransactionSignature();
        ts.parse(tx.getTransactionSignature(), 0);
        System.out.println(new String(tx.getRemark()));
        boolean b = ECKey.verify(tx.getHash().getBytes(), ts.getP2PHKSignatures().get(0).getSignData().getSignBytes(), ts.getP2PHKSignatures().get(0).getPublicKey());
        System.out.println(b);
        assertTrue(b);
    }

    @Test
    public void testSignData() throws IOException {
        ECKey eckey = new ECKey();
        String pubHex = eckey.getPublicKeyAsHex();
        String priHex = eckey.getPrivateKeyAsHex();
        System.out.println("pubHex:=\"" + pubHex + "\"");
        System.out.println("priHex:=\"" + priHex + "\"");
        byte[] signValue = eckey.sign(Sha256Hash.hash("asdfghjklqwertyuiop1234567890".getBytes()));
        System.out.println("signValue :=\"" + HexUtil.encode(signValue) + "\"");

        TransactionSignature ts = new TransactionSignature();
        List<P2PHKSignature> list = new ArrayList<>();
        P2PHKSignature ps = new P2PHKSignature();
        ps.setPublicKey(eckey.getPubKey());
        NulsSignData sd = new NulsSignData();
        sd.setSignBytes(signValue);
        ps.setSignData(sd);
        list.add(ps);
        ts.setP2PHKSignatures(list);
        System.out.println("allHex:=\"" + HexUtil.encode(ts.serialize()) + "\"");
    }

    @Test
    public void testCoinData() throws IOException {
        CoinData cd = new CoinData();
        cd.setFrom(new ArrayList<>());
        cd.setTo(new ArrayList<>());

        CoinFrom from = new CoinFrom();
        from.setAddress(AddressTool.getAddress("tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1"));
        from.setAssetsChainId(1);
        from.setAssetsId(1);
        from.setLocked((byte) 0);
        from.setAmount(BigInteger.valueOf(1234567890));
        from.setNonce(HexUtil.decode("1111111111111111"));

        CoinFrom from1 = new CoinFrom();
        from1.setAddress(AddressTool.getAddress("tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1"));
        from1.setAssetsChainId(2);
        from1.setAssetsId(1);
        from1.setLocked((byte) 0);
        from1.setAmount(BigInteger.valueOf(143321123));
        from1.setNonce(HexUtil.decode("0000000011111111"));

        cd.getFrom().add(from);
        cd.getFrom().add(from1);

        CoinTo to = new CoinTo();
        to.setAddress(AddressTool.getAddress("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm"));
        to.setAssetsChainId(1);
        to.setAssetsId(1);
        to.setAmount(BigInteger.valueOf(1234567890));
        to.setLockTime(0);
        CoinTo to1 = new CoinTo();
        to1.setAddress(AddressTool.getAddress("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm"));
        to1.setAssetsChainId(2);
        to1.setAssetsId(1);
        to1.setAmount(BigInteger.valueOf(123321123));
        to1.setLockTime(0);
        cd.getTo().add(to);
        cd.getTo().add(to1);

        System.out.println(HexUtil.encode(cd.serialize()));
    }

    @Test
    public void testSerialize() throws IOException {
        BigInteger bi = new BigInteger("123456789987654321123456789987654321123456789987654321");
        byte[] bytes = SerializeUtils.bigInteger2Bytes(bi);
        System.out.println(HexUtil.encode(bytes));
        byte[] bytes1 = new byte[4];
        bytes1 = SerializeUtils.uint64ToByteArray(Long.MAX_VALUE);
        System.out.println(bytes1);

        byte[] bytes2 = ByteUtils.doubleToBytes(1234567890987654321.321d);
        System.out.println(bytes2);

        BaseNulsData data = new BaseNulsData() {
            private String val = "Nuls is a blockchain project.";

            @Override
            protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
                stream.writeString(val);
            }

            @Override
            public void parse(NulsByteBuffer byteBuffer) throws NulsException {
                this.val = byteBuffer.readString();
            }

            @Override
            public int size() {
                return SerializeUtils.sizeOfString(val);
            }
        };
        byte[] bytes3 = data.serialize();
        printBytes(bytes3);

        VarInt vi1 = new VarInt(100);
        VarInt vi2 = new VarInt(65536);
        VarInt vi3 = new VarInt(Integer.MAX_VALUE);
        VarInt vi4 = new VarInt(Long.MAX_VALUE);
        System.out.println("varInt ======================================");
        printBytes(vi1.encode());
        printBytes(vi2.encode());
        printBytes(vi3.encode());
        printBytes(vi4.encode());
        System.out.println("uint64 ======================================");
        printBytes(SerializeUtils.uint64ToByteArray(1));
        printBytes(SerializeUtils.uint64ToByteArray(1000));
        printBytes(SerializeUtils.uint64ToByteArray(65536));
        printBytes(SerializeUtils.uint64ToByteArray(9223372036854775807L));

        System.out.println("double ======================================");
        data = new BaseNulsData() {
            private double val = 123456.789d;

            @Override
            protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
                stream.writeDouble(val);
            }

            @Override
            public void parse(NulsByteBuffer byteBuffer) throws NulsException {
                this.val = byteBuffer.readDouble();
            }

            @Override
            public int size() {
                return SerializeUtils.sizeOfDouble(val);
            }
        };
        bytes3 = data.serialize();
        printBytes(bytes3);
    }

    private void printBytes(byte[] bytes) {
        for (byte b : bytes) {
            int val = b;
            if (b < 0) {
                val = 256 + b;
            }
            System.out.print(val + ",");
        }
        System.out.println();
    }

    @Test
    public void testBlock() throws Exception {
        String blockHex = "7c22abde4455699e537dd9744e8974d4110c7ed67fc8b520b04edd8cd1fa3664587f75680a75d29000cc730f303ec8c9160776d215bea4bf9a98d6c282555ef654ac7d5efded1900020000005cef47000071009ea87d5e5f0004000400500a00200d922d36a6960e649565ceb596176917e6eae1b192955c77e32819d97569be41a54254743cfda20bbd064589495b13cc50b0dc3376324ee07cab5a4ea3d5b111a5985680de2874392102054400152323535143f5702e0845199fd2300ccdecfb8c65e1f091972f02571746304402207745caad4ecf81177d5ddc49f0dc00b26df561c01afca827cc11093e1286e40c022073c6171f86855e466566119d7441614c649fce4c458beca6eed1a58ea52adfcc010054ac7d5e0000fd5601000517010001c6f808e2662c255f3ab036cb663609089a2029ae0100010007eb0c0000000000000000000000000000000000000000000000000000000000000000000000000017010001af72a88ae4c5c3d57d33e84143ea459e94ce0c4d01000100a835b400000000000000000000000000000000000000000000000000000000000000000000000000170100018ccb83ab6df991d8cd364a313794c6754aa3baa9010001008e050b0000000000000000000000000000000000000000000000000000000000000000000000000017010001d35e1ac9e97a2b9a66cff527e05668e49517e2a2010001009506870600000000000000000000000000000000000000000000000000000000000000000000000017010001fd818fe7ff462cf93eb592d7548221e8e3f2862801000100ecb30f000000000000000000000000000000000000000000000000000000000000000000000000000002004aac7d5e1262696e616e6365207769746864726177616c00d001170100013ba3e3c56062266262514c74fafb01852af99fec01000100a0d4c1c55300000000000000000000000000000000000000000000000000000008e3bef9c18c9dfa060002170100014c13afc00b8f51cf8b4a13de866ab6bed9e929ab0100010000a29cc94c000000000000000000000000000000000000000000000000000000000000000000000017010001efef57cb04f1dd4d3d6d3afc6a56cb516a4ca5700100010000ac23fc0600000000000000000000000000000000000000000000000000000000000000000000006921037af860c03801a7453c55fd2a7fe90c5081ac66db79b7a92139f320d4d60fc0bb4630440220326d0a11d0b21710316ec5bed7278cee0a51ba173ec86b3ea45d786172402f0b02204a59ec211516e7d568d305202bc82d486eeacf7fd4605f8eef430cebb9ca6126";
        Block block = new Block();
        block.parse(HexUtil.decode(blockHex), 0);
        System.out.println("transfer tx hex:");
        System.out.println(HexUtil.encode(block.getTxs().get(1).serialize()));
    }

    @Test
    public void testAddress() throws IOException {
        ECKey ecKey = ECKey.fromPrivate(HexUtil.decode("230cb8ebbf3a2c581d27f98f7a38f8b07c1ff170d605ca645db4ffa05ffa5505"));
//        byte[] bytes = AddressTool.getAddress(ecKey.getPubKey(), 2);
//        System.out.println(AddressTool.getStringAddressByBytes(bytes));
        byte[] data = HexUtil.decode("510a54c68cc64c6f131c8c7dc1ac59153d9b81e7dd1dc6f565f23ab2f9a6fcaf");
        byte[] value = ecKey.sign(data);
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        NulsSignData signData = new NulsSignData();
        signData.setSignBytes(value);
        p2PHKSignature.setSignData(signData);
        System.out.println(HexUtil.encode(p2PHKSignature.serialize()));

    }

    @Test
    public void testHash160() {
        byte[] data = HexUtil.decode("2d3aef2c4abba64e23bdff0c626483e7d7062255443cd627989cd35bc2a94dcf");

        byte[] hash256 = Sha256Hash.hash(data);
        System.out.println("hash256:: " + HexUtil.encode(hash256));

        byte[] hash256_2 = Sha256Hash.hashTwice(data);
        System.out.println("hash256 twice:: " + HexUtil.encode(hash256_2));

        byte[] hash160 = SerializeUtils.sha256hash160(data);
        System.out.println("hash160:: " + HexUtil.encode(hash160));

        byte[] ripemd160h = SerializeUtils.ripemd160h(data);
        System.out.println("ripemd160h:: " + HexUtil.encode(ripemd160h));

    }
}
