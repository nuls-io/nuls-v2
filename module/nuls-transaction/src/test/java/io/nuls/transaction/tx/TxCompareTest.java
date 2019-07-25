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
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
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
import java.util.*;

/**
 * 交易排序测试，主要用于孤儿交易的排序问题
 *
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

    private List<Integer> randomIde(int count) {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        while (true) {
            Random rand = new Random();
            //随机数范取值围应为0到list最大索引之间
            //根据公式rand.nextInt((list.size() - 1) - 0 + 1) + 0;
            int ran = rand.nextInt(count);
            if (set.add(ran)) {
                list.add(ran);
            }
            if (set.size() == count) {
                break;
            }
        }
       /* for(Integer i : list){
            System.out.println(i);
        }*/
        return list;
    }

    //将交易的顺序打乱，再排序，来验证排序是否正确
    @Test
    public void test() throws Exception {
        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//20 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//21 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        for (int y = 0; y < 10; y++) {
            System.out.println("------------------");
            System.out.println("------------------");
            System.out.println("------------------");
            System.out.println("------------------");
            int count = 200;
            int times = 100;
            List<Transaction> txs = new ArrayList<>();
            for (int x = 0; x < times; x++) {
                txs.addAll(createTxs(count));
            }

//            List<Transaction> txs = createTxs(count);
//            System.out.println("正确的顺序");
//            for (Transaction tx : txs) {
//                System.out.println("正确的顺序: " + tx.getHash().toHex());
//            }
        /* 显示交易格式化完整信息
        for(Transaction tx : txs){
            TxUtil.txInformationDebugPrint(tx);
        }*/
            List<TransactionNetPO> txList = new ArrayList<>();
            int total = count * times;
            List<Integer> ide = randomIde(total);
            for (int i = 0; i < total; i++) {
                txList.add(new TransactionNetPO(txs.get(ide.get(i))));
            }
            System.out.println("Size:" + txList.size());
//
//        txList.add(new TransactionNetPO(txs.get(4)));
//        txList.add(new TransactionNetPO(txs.get(7)));
//        txList.add(new TransactionNetPO(txs.get(2)));
//        txList.add(new TransactionNetPO(txs.get(3)));
//        txList.add(new TransactionNetPO(txs.get(0)));
//        txList.add(new TransactionNetPO(txs.get(5)));
//        txList.add(new TransactionNetPO(txs.get(9)));
//        txList.add(new TransactionNetPO(txs.get(1)));
//        txList.add(new TransactionNetPO(txs.get(6)));
//        txList.add(new TransactionNetPO(txs.get(8)));


//            System.out.println("排序前");
//            for (TransactionNetPO tx : txList) {
//                System.out.println("排序前的顺序: " + tx.getTx().getHash().toHex());
//            }

            long start = System.currentTimeMillis();
            //排序
            rank(txList);
            long end = System.currentTimeMillis() - start;
            System.out.println("执行时间：" + end);
//            System.out.println(txList.size());
//            System.out.println("排序后");
//            for (TransactionNetPO tx : txList) {
//                System.out.println("排序后的顺序: " + tx.getTx().getHash().toHex());
//            }

        }
    }

    //排序
    private void rank(List<TransactionNetPO> txList) {
        //分组：相同时间的一组，同时设置排序字段的值（10000*time），用于最终排序
        Map<Long, List<TransactionNetPO>> groupMap = new HashMap<>();
        for (TransactionNetPO tx : txList) {
            long second = tx.getTx().getTime();
            List<TransactionNetPO> subList = groupMap.get(second);
            if (null == subList) {
                subList = new ArrayList<>();
                groupMap.put(second, subList);
            }
            tx.setOriginalSendNanoTime(second * 10000);
            subList.add(tx);
        }
        //相同时间的组，进行细致排序，并更新排序字段的值
        for (List<TransactionNetPO> list : groupMap.values()) {
            this.sameTimeRank(list);
        }
        //重新排序
        Collections.sort(txList, new Comparator<TransactionNetPO>() {
            @Override
            public int compare(TransactionNetPO o1, TransactionNetPO o2) {
                if (o1.getOriginalSendNanoTime() > o2.getOriginalSendNanoTime()) {
                    return 1;
                } else if (o1.getOriginalSendNanoTime() < o2.getOriginalSendNanoTime()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private void sameTimeRank(List<TransactionNetPO> txList) {
        if (txList.size() <= 1) {
            return;
        }
        TxCompareTool.SortResult<TransactionNetPO> result = new TxCompareTool.SortResult<>(txList.size());
        txList.forEach(po -> {
            doRank(result, new TxCompareTool.SortItem<>(po));
        });
        int index = 0;
        for (TransactionNetPO po : result.getList()) {
            po.setOriginalSendNanoTime(po.getOriginalSendNanoTime() + (index++));
        }

    }

    private static void doRank(TxCompareTool.SortResult<TransactionNetPO> result, TxCompareTool.SortItem<TransactionNetPO> thisItem) {
        if (result.getIndex() == -1) {
            result.getArray()[0] = thisItem;
            result.setIndex(0);
            return;
        }
        TxCompareTool.SortItem[] array = result.getArray();
        boolean gotFront = false;
        boolean gotNext = false;
        int gotIndex = -1;
        boolean added = false;
        for (int i = result.getIndex(); i >= 0; i--) {
            TxCompareTool.SortItem<TransactionNetPO> item = array[i];
            int val = TxCompareTool.compareTo(thisItem.getObj(), item.getObj());
            if (val == 1 && !gotNext) {
                item.setFlower(new TxCompareTool.SortItem[]{thisItem});
                item.setHasFlower(true);
                insertArray(i + 1, result, result.getIndex() + 1, thisItem, false);
                gotFront = true;
                gotIndex = i + 1;
                added = true;
            } else if (val == 1 && gotNext) {
//                需要找到之前的一串，挪动到现在的位置
                thisItem = result.getArray()[gotIndex];
                if (i == gotIndex - 1) {
                    item.setHasFlower(true);
                    return;
                }
                boolean hasFlower = thisItem.isHasFlower();
                TxCompareTool.SortItem<TransactionNetPO>[] flower = new TxCompareTool.SortItem[1];
                int count = 0;
                if (hasFlower) {
                    count = 1;
                }
                int realCount = 1;
                for (int x = 1; x <= count; x++) {
                    TxCompareTool.SortItem flr = array[x + gotIndex];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        TxCompareTool.SortItem<TransactionNetPO>[] flower2 = new TxCompareTool.SortItem[flower.length + 1];
                        System.arraycopy(flower, 0, flower2, 0, flower.length);
                        flower = flower2;
                    }
                }
                thisItem.setFlower(flower);
                if (flower.length > 0) {
                    thisItem.setHasFlower(true);
                }
                item.setHasFlower(true);
                // 前移后面的元素
                for (int x = 0; x < result.getIndex() - gotIndex + 1; x++) {
                    int oldIndex = gotIndex + x + realCount;
                    if (oldIndex <= result.getIndex()) {
                        array[gotIndex + x] = array[oldIndex];
                        array[oldIndex] = null;
                    } else {
                        array[gotIndex + x] = null;
                    }
                }
                result.setIndex(result.getIndex() - realCount);
                insertArray(i + 1, result, result.getIndex() + 1, thisItem, true);
                return;
            } else if (val == -1 && !gotFront) {
                thisItem.setHasFlower(true);
                insertArray(i, result, result.getIndex() + 1, thisItem, false);
                gotNext = true;
                gotIndex = i;
                added = true;
            } else if (val == -1 && gotFront) {
                if (gotIndex == i - 1) {
                    return;
                }
                thisItem.setHasFlower(true);
                thisItem = result.getArray()[i];
                boolean hasFlower = thisItem.isHasFlower();
                int count = hasFlower ? 1 : 0;
                int realCount = 1;
                TxCompareTool.SortItem<TransactionNetPO>[] flower = new TxCompareTool.SortItem[count];
                for (int x = 1; x <= count; x++) {
                    TxCompareTool.SortItem flr = array[x + i];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        TxCompareTool.SortItem<TransactionNetPO>[] flower2 = new TxCompareTool.SortItem[flower.length + 1];
                        System.arraycopy(flower, 0, flower2, 0, flower.length);
                        flower = flower2;
                    }
                }
                thisItem.setFlower(flower);
                // 前移后面的元素
                for (int x = 0; x < result.getIndex() - i + 1; x++) {
                    int oldIndex = i + x + realCount;
                    if (oldIndex <= result.getIndex()) {
                        array[i + x] = array[oldIndex];
                        array[oldIndex] = null;
                    } else {
                        array[i + x] = null;
                    }
                }
                result.setIndex(result.getIndex() - realCount);
                insertArray(gotIndex + 1 - realCount, result, result.getIndex() + 1, thisItem, true);
                return;
            }
        }
        if (!added) {
            insertArray(result.getIndex() + 1, result, result.getIndex() + 1, thisItem, false);
        }
    }

    private static void insertArray(int index, TxCompareTool.SortResult result, int length, TxCompareTool.SortItem item, boolean insertFlowers) {
        TxCompareTool.SortItem[] array = result.getArray();
        int count = 1 + item.getFlower().length;
        result.setIndex(result.getIndex() + count);
        if (length >= index) {
            for (int i = length - 1; i >= index; i--) {
                array[i + count] = array[i];
            }
        }
        array[index] = item;
        if (null == item.getFlower() || !insertFlowers) {
            return;
        }
        int add = 1;
        for (TxCompareTool.SortItem f : item.getFlower()) {
            array[index + add] = f;
            add++;
        }
    }

    //组装一些 时间 账户 一致，nonce是连续的交易
    private List<Transaction> createTxs(int count) throws Exception {
        Map map = CreateTx.createTransferTx(address21, address20, new BigInteger("100000"));
        long time = NulsDateUtils.getCurrentTimeSeconds();
//        Long time = null;
        List<Transaction> list = new ArrayList<>();
        NulsHash hash = null;
        for (int i = 0; i < count; i++) {
            Transaction tx = CreateTx.assemblyTransaction((List<CoinDTO>) map.get("inputs"), (List<CoinDTO>) map.get("outputs"), (String) map.get("remark"), hash, time);
            list.add(tx);
            hash = tx.getHash();
        }
        Thread.sleep(1000L);
        return list;
    }

    public void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
//            Log.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
