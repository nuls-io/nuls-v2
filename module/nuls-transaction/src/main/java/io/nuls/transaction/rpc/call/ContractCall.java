/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;

import java.util.*;

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
     * @param blockType 该调用的处理模式, 打包:0, 验证区块:1
     * @return
     * @throws NulsException
     */
    public static boolean contractBatchBegin(Chain chain, long blockHeight, long blockTime, String packingAddress, String preStateRoot, int blockType) {
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        params.put("blockTime", blockTime);
        params.put("packingAddress", packingAddress);
        params.put("preStateRoot", preStateRoot);
        params.put("blockType", blockType);
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
     * @param blockType 该调用的处理模式, 打包:0, 验证区块:1
     * @return
     * @throws NulsException
     */
    public static boolean invokeContract(Chain chain, String tx, int blockType) throws NulsException {
        try {
            Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", tx);
            params.put("blockType", blockType);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_invoke_contract", params);
            Boolean value = (Boolean) result.get("value");
            if(null == value){
                chain.getLogger().error("call sc_invoke_contract response value is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                throw new NulsException(TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND);
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 调用智能合约
     * @param chain
     * @param blockHeight
     * @param blockType 该调用的处理模式, 打包:0, 验证区块:1
     * @return
     * @throws NulsException
     */
    public static boolean contractBatchBeforeEnd(Chain chain, long blockHeight, int blockType) {
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        params.put("blockType", blockType);
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
     * 调用智能合约执行 验证区块交易的时候调用
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
            return result;
        }catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }
    /**
     * 调用智能合约执行 打包的时候调用
     * @param chain
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static Map<String, Object> contractPackageBatchEnd(Chain chain, long blockHeight) throws NulsException {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_4);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("blockHeight", blockHeight);
        try {
            Map result = (Map) TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_package_batch_end", params);
            return result;
        }catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 获取智能合约模块生成的系统交易类型（包括共识，跨链等；不包含gas返还交易）
     * @param chain
     * @return
     * @throws NulsException
     */
    public static Set<Integer> getContractGenerateTxTypes(Chain chain) throws NulsException {
        try {
            Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.SC.abbr, "sc_get_tx_type_list_from_contract_generated", params);
            List<Integer> value = (List) result.get("list");
            if(null == value){
                chain.getLogger().error("call sc_get_tx_type_list_from_contract_generated response data is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                throw new NulsException(TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND);
            }
            Set<Integer> contractGenerateTxTypes = new HashSet<>();
            contractGenerateTxTypes.addAll(value);
            return contractGenerateTxTypes;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }
}
