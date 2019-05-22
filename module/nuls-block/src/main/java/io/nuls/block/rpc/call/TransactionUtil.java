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

package io.nuls.block.rpc.call;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;

import java.io.IOException;
import java.util.*;

/**
 * 调用交易管理模块的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:44
 */
public class TransactionUtil {

    /**
     * 获取系统交易类型
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static List<Integer> getSystemTypes(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getSystemTypes", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map types = (Map) responseData.get("tx_getSystemTypes");
                return (List<Integer>) types.get("list");
            } else {
                return List.of();
            }
        } catch (Exception e) {
            commonLog.error("", e);
            return List.of();
        }
    }

    /**
     * 批量验证交易
     *
     * @param chainId      链Id/chain id
     * @param transactions
     * @return
     */
    public static boolean verify(int chainId, List<Transaction> transactions, BlockHeader header, BlockHeader lastHeader) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<String> txList = new ArrayList<>();
            for (Transaction transaction : transactions) {
                txList.add(RPCUtil.encode(transaction.serialize()));
            }
            params.put("txList", txList);
            BlockExtendsData lastData = new BlockExtendsData();
            lastData.parse(new NulsByteBuffer(lastHeader.getExtend()));
            params.put("preStateRoot", RPCUtil.encode(lastData.getStateRoot()));
            params.put("blockHeader", RPCUtil.encode(header.serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_batchVerify", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map v = (Map) responseData.get("tx_batchVerify");
                return (Boolean) v.get("value");
            }
            return false;
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId       链Id/chain id
     * @param blockHeaderPo
     * @param txs
     * @param localInit
     * @return
     */
    public static boolean save(int chainId, BlockHeaderPo blockHeaderPo, List<Transaction> txs, boolean localInit) {
        if (localInit) {
            return saveGengsisTransaction(chainId, blockHeaderPo, txs);
        } else {
            return saveNormal(chainId, blockHeaderPo, txs);
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId       链Id/chain id
     * @param blockHeaderPo
     * @return
     */
    public static boolean saveNormal(int chainId, BlockHeaderPo blockHeaderPo, List<Transaction> txs) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<String> txList = new ArrayList<>();
            for (Transaction transaction : txs) {
                txList.add(RPCUtil.encode(transaction.serialize()));
            }
            params.put("txList", txList);
            params.put("blockHeader", RPCUtil.encode(BlockUtil.fromBlockHeaderPo(blockHeaderPo).serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_save", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map data = (Map) responseData.get("tx_save");
                return (Boolean) data.get("value");
            }
            return false;
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 批量回滚交易
     *
     * @param chainId       链Id/chain id
     * @param blockHeaderPo
     * @return
     */
    public static boolean rollback(int chainId, BlockHeaderPo blockHeaderPo) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<NulsHash> txHashList = blockHeaderPo.getTxHashList();
            List<String> list = new ArrayList<>();
            txHashList.forEach(e -> list.add(e.toHex()));
            params.put("txHashList", list);
            params.put("blockHeader", RPCUtil.encode(BlockUtil.fromBlockHeaderPo(blockHeaderPo).serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_rollback", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map data = (Map) responseData.get("tx_rollback");
                return (Boolean) data.get("value");
            }
            return false;
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 批量获取已确认交易
     *
     * @param chainId  链Id/chain id
     * @param hashList
     * @return
     * @throws IOException
     */
    public static List<Transaction> getConfirmedTransactions(int chainId, List<NulsHash> hashList) {
        List<Transaction> transactions = new ArrayList<>();
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<String> t = new ArrayList<>();
            hashList.forEach(e -> t.add(e.toHex()));
            params.put("txHashList", t);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getBlockTxs", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map map = (Map) responseData.get("tx_getBlockTxs");
                List<String> txHexList = (List<String>) map.get("txList");
                if (txHexList == null || txHexList.isEmpty()) {
                    return Collections.emptyList();
                }
                for (String txHex : txHexList) {
                    Transaction transaction = new Transaction();
                    transaction.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    transactions.add(transaction);
                }
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            commonLog.error("", e);
            return Collections.emptyList();
        }
        return transactions;
    }

    /**
     * 批量获取交易
     *
     * @param chainId  链Id/chain id
     * @param hashList
     * @return
     * @throws IOException
     */
    public static List<Transaction> getTransactions(int chainId, List<NulsHash> hashList, boolean allHits) {
        if (hashList == null || hashList.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Transaction> transactions = new ArrayList<>();
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<String> t = new ArrayList<>();
            hashList.forEach(e -> t.add(e.toHex()));
            params.put("txHashList", t);
            params.put("allHits", allHits);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getBlockTxsExtend", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map map = (Map) responseData.get("tx_getBlockTxsExtend");
                List<String> txHexList = (List<String>) map.get("txList");
                if (txHexList == null || txHexList.isEmpty()) {
                    return Collections.emptyList();
                }
                for (String txHex : txHexList) {
                    Transaction transaction = new Transaction();
                    transaction.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    transactions.add(transaction);
                }
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            commonLog.error("", e);
            return Collections.emptyList();
        }
        return transactions;
    }

    /**
     * 获取单个交易
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @return
     */
    public static Transaction getTransaction(int chainId, NulsHash hash) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("txHash", hash.toHex());
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map map = (Map) responseData.get("tx_getTx");
                String txHex = (String) map.get("tx");
                if (txHex == null) {
                    return null;
                }
                Transaction transaction = new Transaction();
                transaction.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                return transaction;
            } else {
                return null;
            }
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }

    /**
     * 获取单个交易
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @return
     */
    private static Transaction getConfirmedTransaction(int chainId, NulsHash hash) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("txHash", hash.toHex());
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map map = (Map) responseData.get("tx_getConfirmedTx");
                String txHex = (String) map.get("tx");
                if (txHex == null) {
                    return null;
                }
                Transaction transaction = new Transaction();
                transaction.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                return transaction;
            } else {
                return null;
            }
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId       链Id/chain id
     * @param blockHeaderPo
     * @return
     */
    public static boolean saveGengsisTransaction(int chainId, BlockHeaderPo blockHeaderPo, List<Transaction> txs) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<String> list = new ArrayList<>();
            txs.forEach(e -> {
                try {
                    list.add(RPCUtil.encode(e.serialize()));
                } catch (Exception e1) {
                    commonLog.error("", e1);
                }
            });
            params.put("txList", list);
            params.put("blockHeader", RPCUtil.encode(BlockUtil.fromBlockHeaderPo(blockHeaderPo).serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_gengsisSave", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map data = (Map) responseData.get("tx_gengsisSave");
                return (Boolean) data.get("value");
            }
            return false;
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 批量保存交易
     *
     * @param chainId       链Id/chain id
     * @param height
     * @return
     */
    public static boolean heightNotice(int chainId, long height) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_blockHeight", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map data = (Map) responseData.get("tx_blockHeight");
                return (Boolean) data.get("value");
            }
            return false;
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }
}
