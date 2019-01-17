/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.CrossTx;
import io.nuls.transaction.model.bo.CrossTxSignResult;
import io.nuls.transaction.model.bo.CrossTxVerifyResult;
import io.nuls.transaction.model.bo.Node;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 单元测试工具类, 造数据等
 *
 * @author: Charlie
 * @date: 2019-01-09
 */
public class TestConstant {
    public static String address1 = "LU6eNP3pJ5UMn5yn8LeDE3Pxeapsq3930";//pwd:nuls123456"
    public static String address2 = "JcgbDRvBqQ67Uq4Tb52U22ieJdr3G3930";//pwd:nuls123456"
    public static String address3 = "Vxb3xxatcFFTZZe3wynX6CfAsvzAx3930";
    public static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
    public static String address5 = "GmjB8o7sNiQSXZ6aNz5NBP6pdnNrv3930";

    public static String txhashA = "002073c70ba4f851f37bd9a43c51565ed4f988a2faa80d7b5cd378a1149ce1f5001c";
    public static String txhashB = "0020eca68ca82e78d8c22b8f129742e7b392b30a299ba620b308be39c8a2d61a2e03";
    public static String txhashC = "0020082d2bf2bc53a19896d3d8c257ffed5648e9fa88639edd82b9d5d9cc81fc1e50";
    public static String txhashD = "00201019f8d81528bfd927f3a6ffc96030157448c2ad7c5d0d0256f940c2bb9e593f";

    public static NulsDigestData getHashA() {
        try {
            return NulsDigestData.fromDigestHex(txhashA);
        } catch (NulsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NulsDigestData getHashB() {
        try {
            return NulsDigestData.fromDigestHex(txhashB);
        } catch (NulsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NulsDigestData getHashC() {
        try {
            return NulsDigestData.fromDigestHex(txhashC);
        } catch (NulsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NulsDigestData getHashD() {
        try {
            return NulsDigestData.fromDigestHex(txhashD);
        } catch (NulsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CoinFrom getCoinFrom1() {
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(address1));
        coinFrom.setAmount(new BigInteger("200"));
        coinFrom.setAssetsChainId(1);
        coinFrom.setAssetsId(1);
        coinFrom.setLocked((byte) 0);
        byte[] nonce = new byte[8];
        coinFrom.setNonce(nonce);
        return coinFrom;
    }

    public static CoinFrom getCoinFrom2() {
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(address2));
        coinFrom.setAmount(new BigInteger("100"));
        coinFrom.setAssetsChainId(2);
        coinFrom.setAssetsId(1);
        coinFrom.setLocked((byte) 0);
        byte[] nonce = new byte[8];
        coinFrom.setNonce(nonce);
        return coinFrom;
    }

    public static CoinTo getCoinTo1() {
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address3));
        coinTo.setAmount(new BigInteger("200"));
        coinTo.setAssetsChainId(1);
        coinTo.setAssetsId(1);
        coinTo.setLockTime(System.currentTimeMillis());
        return coinTo;
    }

    public static CoinTo getCoinTo2() {
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address4));
        coinTo.setAmount(new BigInteger("100"));
        coinTo.setAssetsChainId(2);
        coinTo.setAssetsId(1);
        coinTo.setLockTime(0);
        return coinTo;
    }

    public static CoinTo getCoinTo3() {
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address5));
        coinTo.setAmount(new BigInteger("100"));
        coinTo.setAssetsChainId(2);
        coinTo.setAssetsId(1);
        coinTo.setLockTime(0);
        return coinTo;
    }

    public static CoinData getCoinData1() {
        CoinData coinData = new CoinData();
        coinData.addFrom(getCoinFrom1());
        coinData.addFrom(getCoinFrom2());
        coinData.addTo(getCoinTo1());
        coinData.addTo(getCoinTo2());
        return coinData;
    }

    public static CoinData getCoinData2() {
        CoinData coinData = new CoinData();
        coinData.addFrom(getCoinFrom1());
        coinData.addTo(getCoinTo2());
        coinData.addTo(getCoinTo3());
        return coinData;
    }

    public static Transaction getTransaction1() throws Exception {
        Transaction tx = new Transaction();
        tx.setCoinData(getCoinData1().serialize());
        tx.setType(10);
        tx.setTime(System.currentTimeMillis());
        tx.setBlockHeight(100);
        String remark = "这是一笔跨链转账交易";
        tx.setRemark(StringUtils.bytes(remark));
        return tx;
    }

    public static Transaction getTransaction2() throws Exception {
        Transaction tx = new Transaction();
        tx.setCoinData(getCoinData2().serialize());
        tx.setType(2);
        tx.setTime(System.currentTimeMillis());
        tx.setBlockHeight(100);
        String remark = "这是一笔普通转账交易";
        tx.setRemark(StringUtils.bytes(remark));
        return tx;
    }

    public static CrossTx createCrossChainTx() throws Exception {
        CrossTx obj = new CrossTx();
        obj.setState(4);
        obj.setSenderChainId(324);
        obj.setSenderNodeId("23");
        obj.setVerifyNodeList(Arrays.asList(
                TestConstant.getNode1(),
                TestConstant.getNode2()
        ));
        obj.setCtxVerifyResultList(Arrays.asList(
                TestConstant.getCrossTxVerifyResult1(),
                TestConstant.getCrossTxVerifyResult1()
        ));
        obj.setSignRsList(Arrays.asList(
                TestConstant.getCrossTxSignResult1(),
                TestConstant.getCrossTxSignResult2()
        ));
        obj.setTx(TestConstant.getTransaction1());
        return obj;
    }

    public static P2PHKSignature getP2PHKSignature() {
        String P2PHKSignatureObjectHex = "21032da10909c0c5ae8221941b23e3e1cdafccc68a1116471d83417188164c7adb9a00463044022043ea6fd68d10b627b73b88c670caa05275085f25d965bd166f9e0bf4d276b12902204c88c4744b4ec684022e6f932335ee0d1b913d63afb9e7affe69364ee1ffa74a";
        byte[] bytes = HexUtil.decode(P2PHKSignatureObjectHex);
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        try {
            p2PHKSignature.parse(new NulsByteBuffer(bytes));
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return p2PHKSignature;
    }


    public static Node getNode1() {
        Node obj = new Node();
        obj.setId("192.168.1.110:8090");
        obj.setHeight(34567890);
        obj.setHash(TestConstant.getHashA());
        return obj;
    }

    public static Node getNode2() {
        Node obj = new Node();
        obj.setId("192.168.23.210:2");
        obj.setHeight(0);
        obj.setHash(TestConstant.getHashB());
        return obj;
    }

    public static CrossTxVerifyResult getCrossTxVerifyResult1() {
        CrossTxVerifyResult obj = new CrossTxVerifyResult();
        obj.setChainId(TxConstant.NULS_CHAINID);
        obj.setHeight(34);
        obj.setNodeId("1");
        return obj;
    }

    public static CrossTxVerifyResult getCrossTxVerifyResult2() {
        CrossTxVerifyResult obj = new CrossTxVerifyResult();
        obj.setChainId(TxConstant.NULS_CHAINID);
        obj.setHeight(34);
        obj.setNodeId("23");
        return obj;
    }


    public static CrossTxSignResult getCrossTxSignResult1() {
        CrossTxSignResult obj = new CrossTxSignResult();
        obj.setPackingAddress(TestConstant.address1);
        obj.setSignature(TestConstant.getP2PHKSignature());
        obj.setNodeId("1");
        return obj;
    }

    public static CrossTxSignResult getCrossTxSignResult2() {
        CrossTxSignResult obj = new CrossTxSignResult();
        obj.setPackingAddress(TestConstant.address2);
        obj.setSignature(TestConstant.getP2PHKSignature());
        obj.setNodeId("23");
        return obj;
    }

    /**
     * 判断两个list中元素的序列化数据是否相等
     */
    public static boolean equals(List<? extends BaseNulsData> listA, List<? extends BaseNulsData> listB) {
        if (null == listA && null == listB) {
            return true;
        }
        if (null == listA || null == listB) {
            return false;
        }
        if (listA.size() != listB.size()) {
            return false;
        }
        try {
            for (int i = 0; i < listA.size(); i++) {
                if (!Arrays.equals(listA.get(i).serialize(), listB.get(i).serialize())) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
