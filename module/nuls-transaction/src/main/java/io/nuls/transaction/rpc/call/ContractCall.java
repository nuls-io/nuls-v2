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

package io.nuls.transaction.rpc.call;

import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
public class ContractCall {

    /**
     * 打包智能合约通知
     * @param chain
     * @param blockHeight
     * @param blockTime
     * @param packingAddress
     * @param preStateRoot
     * @return
     * @throws NulsException
     */
    public static boolean contractBatchBegin(Chain chain, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        params.put("blockTime", blockTime);
        params.put("packingAddress", packingAddress);
        params.put("preStateRoot", preStateRoot);
        try {
            TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_batch_begin", params);
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }

    }
    /**
     * 调用智能合约, 合约执行成功与否,不影响交易的打包
     * @param chain
     * @param tx
     * @return
     * @throws NulsException
     */
    public static boolean invokeContract(Chain chain, String tx) {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("tx", tx);
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_invoke_contract", params);
            if(!response.isSuccess()){
                return false;
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 调用智能合约
     * @param chain
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static boolean contractBatchBeforeEnd(Chain chain, long blockHeight) {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_batch_before_end", params);
            if(!response.isSuccess()){
                return false;
            }
            return true;
        }catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 调用智能合约
     * @param chain
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static Map<String, Object> contractBatchEnd(Chain chain, long blockHeight) throws NulsException {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_4);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        try {
            Map result = (Map) TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_batch_end", params);
            chain.getLogger().debug("moduleCode:{}, -cmd:{}, -contractProcess -rs: {}",
                    ModuleE.SC.abbr, "sc_batch_end", JSONUtils.obj2json(result));
            return result;
        }catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(e);
        }
    }

    public static List<NulsHash> contractOfflineTxHashList(Chain chain, String blockHash) throws NulsException {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_4);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHash", blockHash);
        try {
            Map result = (Map) TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_contract_offline_tx_hash_list", params);
            chain.getLogger().debug("moduleCode:{}, -cmd:{}, -contractProcess -rs: {}",
                    ModuleE.SC.abbr, "sc_contract_offline_tx_hash_list", JSONUtils.obj2json(result));
            Object obj = result.get("list");
            if(null == obj){
                return new ArrayList<>();
            }
            List<NulsHash> hashList =  new ArrayList<>();
            for(String hashStr : (List<String>) obj){
                hashList.add(NulsHash.fromHex(hashStr));
            }
            return hashList;
        }catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(e);
        }
    }
}
