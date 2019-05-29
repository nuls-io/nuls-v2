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

package io.nuls.transaction.tx;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.TransactionComparator;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 交易排序测试，主要用于孤儿交易的排序问题
 * @author: Charlie
 * @date: 2019/5/6
 */
public class TxCompareTest {

    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;

    static String password = "nuls123456";//"nuls123456";

    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";

    private Chain chain;


    static TransactionComparator transactionComparator = new TransactionComparator();

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 1024 * 1024, 1000, 20, 20000, 60000));
    }


    //将交易的顺序打乱，再排序，来验证排序是否正确
    @Test
    public void test() throws Exception{
        List<Transaction> txs = createTxs();
        System.out.println("正确的顺序");
        for(Transaction tx : txs){
            System.out.println("正确的顺序"+tx.getHash().toHex());
        }
//        for(Transaction tx : txs){
//            TxUtil.txInformationDebugPrint(tx);
//        }
        List<TransactionNetPO> txList = new ArrayList<>();
//          txList.add(new TransactionNetPO(txs.get(3),""));
//        txList.add(new TransactionNetPO(txs.get(8),""));
//        txList.add(new TransactionNetPO(txs.get(2),""));
//        txList.add(new TransactionNetPO(txs.get(4),""));
//        txList.add(new TransactionNetPO(txs.get(1),""));
//        txList.add(new TransactionNetPO(txs.get(7),""));
//        txList.add(new TransactionNetPO(txs.get(6),""));
//        txList.add(new TransactionNetPO(txs.get(9),""));
//        txList.add(new TransactionNetPO(txs.get(0),""));
//        txList.add(new TransactionNetPO(txs.get(5),""));


        txList.add(new TransactionNetPO(txs.get(3),"", NulsDateUtils.getNanoTime()));
        txList.add(new TransactionNetPO(txs.get(4),"", NulsDateUtils.getNanoTime()));
        txList.add(new TransactionNetPO(txs.get(2),"", NulsDateUtils.getNanoTime()));
        txList.add(new TransactionNetPO(txs.get(0),"", NulsDateUtils.getNanoTime()));
        txList.add(new TransactionNetPO(txs.get(1),"", NulsDateUtils.getNanoTime()));

       /* for(int i = txs.size()-1;i>=0;i--){
            txList.add(new TransactionNetPO(txs.get(i),""));
        }*/


        System.out.println(txList.size());
        System.out.println("排序前的顺序");
        for(TransactionNetPO tx : txList){
            System.out.println("排序前的顺序"+tx.getTx().getHash().toHex());
        }
//        for(TransactionNetPO tx : txList){
//            TxUtil.txInformationDebugPrint(tx.getTx());
//        }
        System.out.println(txList.size());
        //txBubbleSort(txList);
        System.out.println("排序后的顺序");
        for(TransactionNetPO tx : txList){
            System.out.println("排序后的顺序"+tx.getTx().getHash().toHex());
        }
//        for(TransactionNetPO tx : txList){
//            TxUtil.txInformationDebugPrint(tx.getTx());
//        }

    }



//    private void txBubbleSort(List<TransactionNetPO> list){
//        int size = list.size();
//        for (int i = 0; i < size - 1; i++){
//            for (int j = 0; j < size - 1 - i; j++){
//                TransactionNetPO txNet = list.get(j);
//                int rs = txNet.compareTo(list.get(j + 1));
//                if(rs == 1){
//                    list.set(j, list.get(j + 1));
//                    list.set(j + 1, txNet);
//                }
//            }
//        }
//    }

    //组装一些 时间 账户 一致，nonce是连续的交易
    private List<Transaction> createTxs() throws Exception{
        Map map = CreateTx.createTransferTx(address21, address20, new BigInteger("100000"));
        long time = System.currentTimeMillis();
        List<Transaction> list = new ArrayList<>();
        NulsHash hash = null;
        for(int i=0;i<5;i++) {
            Transaction tx = CreateTx.assemblyTransaction((List<CoinDTO>) map.get("inputs"), (List<CoinDTO>) map.get("outputs"), (String) map.get("remark"), hash, time);
            list.add(tx);
            hash = tx.getHash();
        }
        return list;
    }


}
