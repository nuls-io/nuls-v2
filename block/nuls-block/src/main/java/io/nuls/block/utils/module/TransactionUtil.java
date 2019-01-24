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

package io.nuls.block.utils.module;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.test.TransactionStorageService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.logback.NulsLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用交易管理模块的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:44
 */
public class TransactionUtil {

    private static TransactionStorageService service = SpringLiteContext.getBean(TransactionStorageService.class);

    /**
     * 获取系统交易类型
     *
     * @param chainId
     * @return
     */
    public static List<Integer> getSystemTypes(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_getSystemTypes", params);
            Map responseData = (Map) response.getResponseData();
            return (List<Integer>) responseData.get("tx_getSystemTypes");
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
    }

    /**
     * 批量验证交易
     *
     * @param chainId
     * @param transactions
     * @return
     */
    public static boolean verify(int chainId, List<Transaction> transactions) {
//        transactions.forEach(e -> service.save(chainId, e));
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            List<String> txHashList = new ArrayList<>();
            for (Transaction transaction : transactions) {
                txHashList.add(transaction.hex());
            }
            params.put("txList", txHashList);
            return CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_batchVerify", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId
     * @param blockHeaderPo
     * @param txs
     * @param localInit
     * @return
     */
    public static boolean save(int chainId, BlockHeaderPo blockHeaderPo, List<Transaction> txs, boolean localInit) {
        if (localInit) {
            return saveGengsisTransaction(chainId, blockHeaderPo, txs);
        } else {
            return saveNormal(chainId, blockHeaderPo);
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId
     * @param blockHeaderPo
     * @return
     */
    public static boolean saveNormal(int chainId, BlockHeaderPo blockHeaderPo) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            List<NulsDigestData> txHashList = blockHeaderPo.getTxHashList();
            List<String> list = new ArrayList<>();
            txHashList.forEach(e -> list.add(e.getDigestHex()));
            params.put("txHashList", list);
            BlockHeaderDigest blockHeaderDigest = new BlockHeaderDigest();
            blockHeaderDigest.setBlockHeaderHash(blockHeaderPo.getHash());
            blockHeaderDigest.setHeight(blockHeaderPo.getHeight());
            blockHeaderDigest.setTime(blockHeaderPo.getTime());
            params.put("secondaryDataHex", HexUtil.encode(blockHeaderDigest.serialize()));
            return CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_save", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    /**
     * 批量回滚交易
     *
     * @param chainId
     * @param blockHeaderPo
     * @return
     */
    public static boolean rollback(int chainId, BlockHeaderPo blockHeaderPo) {
//        hashList.forEach(e -> service.remove(chainId, e));
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            List<NulsDigestData> txHashList = blockHeaderPo.getTxHashList();
            List<String> list = new ArrayList<>();
            txHashList.forEach(e -> list.add(e.getDigestHex()));
            params.put("txHashList", list);
            BlockHeaderDigest blockHeaderDigest = new BlockHeaderDigest();
            blockHeaderDigest.setBlockHeaderHash(blockHeaderPo.getHash());
            blockHeaderDigest.setHeight(blockHeaderPo.getHeight());
            blockHeaderDigest.setTime(blockHeaderPo.getTime());
            params.put("secondaryDataHex", HexUtil.encode(blockHeaderDigest.serialize()));
            return CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_rollback", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    /**
     * 批量获取交易
     *
     * @param chainId
     * @param hashList
     * @return
     * @throws IOException
     */
    public static List<Transaction> getTransactions(int chainId, List<NulsDigestData> hashList) {
        List<Transaction> transactions = new ArrayList<>();
//        hashList.forEach(e -> transactions.add(service.query(chainId, e)));
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            hashList.forEach(e -> transactions.add(getTransaction(chainId, e)));
            return transactions;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
    }

    /**
     * 获取单个交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    public static Transaction getTransaction(int chainId, NulsDigestData hash) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("txHash", hash.getDigestHex());
            Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
            Map responseData = (Map) response.getResponseData();
            String txHex = (String) responseData.get("tx_getTx");
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(HexUtil.decode(txHex)));
            return transaction;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
//        return service.query(chainId, hash);
    }

    /**
     * 批量保存交易
     *
     * @param chainId
     * @param blockHeaderPo
     * @return
     */
    public static boolean saveGengsisTransaction(int chainId, BlockHeaderPo blockHeaderPo, List<Transaction> txs) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            List<String> list = new ArrayList<>();
            txs.forEach(e -> {
                try {
                    list.add(e.hex());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            params.put("txHexList", list);
            BlockHeaderDigest blockHeaderDigest = new BlockHeaderDigest();
            blockHeaderDigest.setBlockHeaderHash(blockHeaderPo.getHash());
            blockHeaderDigest.setHeight(blockHeaderPo.getHeight());
            blockHeaderDigest.setTime(blockHeaderPo.getTime());
            params.put("secondaryDataHex", HexUtil.encode(blockHeaderDigest.serialize()));
            return CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_gengsisSave", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }
}
