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

package io.nuls.transaction.storage.rocksdb;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.constant.TxDBConstant;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/7/1
 */
public class RocksDBPerformanceTest {

    @Ignore
    @Test
    public void initTest() throws Exception {
        String dataPath = "E:\\RocksDBTest";
        long start = System.currentTimeMillis();
        RocksDBService.init(dataPath);
        RocksDBService.createTableIfNotExist(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2");
        RocksDBService.createTableIfNotExist(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3");
        long end = System.currentTimeMillis();
        System.out.println("数据库连接初始化测试耗时：" + (end - start) + "ms");
    }

    static List<Transaction> txs = new ArrayList<>();

    @Test
    public void addTx() throws Exception {
        initTest();
        Thread thread = new Thread(){
            public void run(){
                for(int i = 0; i<1000000;i++) {
                    Transaction tx = assemblyTransaction(i);
//                    if(txs.size() < 10000 && (i % 10 == 0)){
//                        txs.add(tx);
//                    }
                    if(txs.size() < 10000){
                        txs.add(tx);
                    }
                    try {
                        RocksDBService.put(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes(), tx.serialize());
                        RocksDBService.put(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", tx.getHash().getBytes(), tx.serialize());
                        Thread.sleep(1L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //System.out.println((i+1) + " : " + tx.getHash().toHex());

                }
                //getTx();
            }
        };
        thread.start();
        List<Transaction> noExistList = new ArrayList<>();
        for(int i = 1000000000; i<1000010000;i++) {
            Transaction tx = assemblyTransaction(i);
            noExistList.add(tx);
        }

        while (true) {
            if(txs.size() < 10000){
                Thread.sleep(10000L);
                continue;
            }
            int count = RocksDBService.keyList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2").size();
            System.out.println("[count]:" + count);
            System.out.println("[keyMayExist 存在]:");
            for (int i = 0; i < 10; i++) {
                long s1 = System.currentTimeMillis();
                for (Transaction tx : txs) {
                    RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes());
                    RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", tx.getHash().getBytes());
                }
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("time:" + s2);
            }
            System.out.println("");
            System.out.println("[keyMayExist 不存在]:");
            for (int i = 0; i < 10; i++) {

                long s1 = System.currentTimeMillis();
                for (Transaction tx : noExistList) {
                    RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes());
                    RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", tx.getHash().getBytes());
                }
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("timenoExist:" + s2);
            }
            System.out.println("");
            System.out.println("[get 存在]:");
            for (int i = 0; i < 10; i++) {
                long s1 = System.currentTimeMillis();
                for (Transaction tx : txs) {
                    RocksDBService.get(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes());
                    RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", tx.getHash().getBytes());
                }
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("time:" + s2);
            }
            System.out.println("");
            System.out.println("[get 不存在]:");
            for (int i = 0; i < 10; i++) {
                long s1 = System.currentTimeMillis();
                for (Transaction tx : noExistList) {
                    RocksDBService.get(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes());
                    RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", tx.getHash().getBytes());
                }
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("timenoExist:" + s2);
            }
            System.out.println("");
            ////////////////////////////////////////////////////////
            System.out.println("[批量取 存在]:");
            for (int i = 0; i < 10; i++) {
                long s1 = System.currentTimeMillis();
                List<byte[]> keys = new ArrayList<>();
                for (Transaction tx : txs) {
                    keys.add(tx.getHash().getBytes());
                }
                List<byte[]> list1 = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", keys);
                List<byte[]> list2 = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", keys);
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("timeno:" + s2 + " count1:" + list1.size() + " count2:" + list2.size());
            }
            System.out.println("");
            System.out.println("[批量取 不存在]:");
            for (int i = 0; i < 10; i++) {
                long s1 = System.currentTimeMillis();
                List<byte[]> keys = new ArrayList<>();
                for (Transaction tx : noExistList) {
                    keys.add(tx.getHash().getBytes());
                }
                List<byte[]> list1 = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", keys);
                List<byte[]> list2 = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + "3", keys);
                long s2 = System.currentTimeMillis() - s1;
                System.out.println("timeno:" + s2 + " count1:" + list1.size() + " count2:" + list2.size());
            }
            System.out.println("");
            System.out.println("------------------------------------");
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


//    public void getTx() {
//        while (true) {
//            int count = RocksDBService.keyList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2").size();
//            System.out.println("count:" + count);
//            for (int i = 0; i < 10; i++) {
//                long s1 = System.nanoTime();
//                for (Transaction tx : txs) {
//                    RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + "2", tx.getHash().getBytes());
//                }
//                long s2 = System.nanoTime() - s1;
//                System.out.println("time:" + s2);
//            }
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private Transaction assemblyTransaction(int i) {
        Transaction tx = new Transaction(2);
        tx.setTime(NulsDateUtils.getCurrentTimeMillis() / 1000);
        tx.setRemark(StringUtils.bytes("测试一下交易测试一下交易测试一下交易测试一下交易测试一下交易测试一下交易测试一下" + i));
        try {
            CoinData coinData = new CoinData();
            CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress("tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn"),
                    2, 1, new BigInteger("80000000000"),  HexUtil.decode("ffffffffffffffff"), (byte)0);
            coinData.setFrom(List.of(coinFrom));
            CoinTo coinTo = new CoinTo(AddressTool.getAddress("tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn"),
                    2,1, new BigInteger("80000000000"), 0L);
            coinData.setTo(List.of(coinTo));
            tx.setCoinData(coinData.serialize());

            byte[] bytes = tx.serializeForHash();
            tx.setHash(NulsHash.calcHash(bytes));
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            P2PHKSignature signature = new P2PHKSignature();
            p2PHKSignatures.add(signature);
            //交易签名
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            return tx;
        } catch (Exception e) {
        }
        return tx;
    }
}
