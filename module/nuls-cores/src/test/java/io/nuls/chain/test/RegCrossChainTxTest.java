///*
// * MIT License
// * Copyright (c) 2017-2019 nuls.io
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package io.nuls.chain.test;
//
//import io.nuls.base.data.Address;
//import io.nuls.base.data.Transaction;
//import io.nuls.base.signture.TransactionSignature;
//import io.nuls.chain.model.tx.txdata.TxChain;
//import io.nuls.core.constant.BaseConstant;
//import io.nuls.core.crypto.HexUtil;
//import io.nuls.core.exception.NulsException;
//import io.nuls.core.log.Log;
//import io.nuls.core.parse.SerializeUtils;
//import io.nuls.core.rockdb.service.RocksDBService;
//import org.junit.Test;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author Niels
// */
//public class RegCrossChainTxTest {
//
//    static String DB_PATH = "/Users/zhoulijun/workspace/nuls/resend-cross-tx/chain-manager";
//
//    static final String TABLE = "block_chain";
//
//    public static void readBlockChain() throws NulsException {
//        RocksDBService.init(DB_PATH);
//        byte[] b = RocksDBService.get(TABLE, TABLE.getBytes());
//        byte[] b = RocksDBService.entryList("block_chain");
////        Log.info("{}", HexUtil.encode(b));
//        CtxStatusPO ctx = new CtxStatusPO();
//        try {
//            ctx.parse(b, 0);
//            Transaction tx = ctx.getTx();
//            TransactionSignature signature = new TransactionSignature();
//            signature.parse(tx.getTransactionSignature(), 0);
//            Log.info("{}", HexUtil.encode(tx.serialize()));
//            Log.info("{}",tx.getType());
//            Log.info("{}", signature.getSignersCount());
//            Set<String> addressSets = new HashSet<>();
//            signature.getP2PHKSignatures().forEach(sign -> {
//                Address address = new Address(CHAIN_ID, QZ, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(sign.getPublicKey()));
//                Log.info("{}", address.getBase58());
//                addressSets.add(address.getBase58());
//            });
//            if(addressSets.size() < 61){
//                Log.info("hash : {}:{}",hash,addressSets.size());
//                sets.add(hash);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Test
//    public void test() throws NulsException {
//
//    }
//}
