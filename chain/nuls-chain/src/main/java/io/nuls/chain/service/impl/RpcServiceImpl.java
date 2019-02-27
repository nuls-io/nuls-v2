/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.ChainTxConstants;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.util.ResponseUtil;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * @program: nuls2.0
 * @description: 远程接口调用
 * @author: lan
 * @create: 2018/11/20
 **/
@Service
public class RpcServiceImpl  implements RpcService {
    @Override
    public String getCrossChainSeeds() {
            try {
                Map<String,Object> map = new HashMap<>();
                Response response =  ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CROSS_SEEDS,map );
                if(response.isSuccess()){
                    Map rtMap = ResponseUtil.getResultMap(response,RpcConstants.CMD_NW_CROSS_SEEDS);
                    if(null != rtMap){
                         return String.valueOf(rtMap.get("seedsIps"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    public boolean regTx() {
        try {
            //向交易管理模块注册交易
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, RpcConstants.TX_REGISTER_VERSION);
            params.put(RpcConstants.TX_CHAIN_ID,  CmRuntimeInfo.getMainIntChainId());
            params.put(RpcConstants.TX_MODULE_CODE, ModuleE.CM.abbr);
            params.put(RpcConstants.TX_MODULE_VALIDATE_CMD, RpcConstants.TX_MODULE_VALIDATE_CMD_VALUE);
            params.put(RpcConstants.TX_COMMIT_CMD,RpcConstants.TX_COMMIT_CMD_VALUE);
            params.put(RpcConstants.TX_ROLLBACK_CMD,RpcConstants.TX_ROLLBACK_CMD_VALUE);
            List<Map<String,Object>> txRegisterDetailList = new ArrayList<Map<String,Object>>();
            /*register chain*/
            Map<String,Object>  regChainMap= new HashMap<>();
            regChainMap.put(RpcConstants.TX_TYPE,ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET);
            regChainMap.put(RpcConstants.TX_VALIDATE_CMD,RpcConstants.TX_VALIDATE_CMD_VALUE_CHAIN_REG);
            regChainMap.put(RpcConstants.TX_IS_SYSTEM_CMD,"false");
            regChainMap.put(RpcConstants.TX_UNLOCK_CMD,"false");
            regChainMap.put(RpcConstants.TX_VERIFY_SIGNATURE_CMD,"true");
            txRegisterDetailList.add(regChainMap);
            /*destroy chain*/
            Map<String,Object>  destroyChainMap= new HashMap<>();
            destroyChainMap.put(RpcConstants.TX_TYPE,ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN);
            destroyChainMap.put(RpcConstants.TX_VALIDATE_CMD,RpcConstants.TX_VALIDATE_CMD_VALUE_CHAIN_DESTROY);
            destroyChainMap.put(RpcConstants.TX_IS_SYSTEM_CMD,"false");
            destroyChainMap.put(RpcConstants.TX_UNLOCK_CMD,"false");
            destroyChainMap.put(RpcConstants.TX_VERIFY_SIGNATURE_CMD,"true");
            txRegisterDetailList.add(destroyChainMap);
            /*add asset*/
            Map<String,Object>  addAssetMap= new HashMap<>();
            addAssetMap.put(RpcConstants.TX_TYPE,ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN);
            addAssetMap.put(RpcConstants.TX_VALIDATE_CMD,RpcConstants.TX_VALIDATE_CMD_VALUE_ASSET_REG);
            addAssetMap.put(RpcConstants.TX_IS_SYSTEM_CMD,"false");
            addAssetMap.put(RpcConstants.TX_UNLOCK_CMD,"false");
            addAssetMap.put(RpcConstants.TX_VERIFY_SIGNATURE_CMD,"true");
            txRegisterDetailList.add(addAssetMap);
            /*destroy asset*/
            Map<String,Object>  destroyAssetMap= new HashMap<>();
            destroyAssetMap.put(RpcConstants.TX_TYPE,ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN);
            destroyAssetMap.put(RpcConstants.TX_VALIDATE_CMD,RpcConstants.TX_VALIDATE_CMD_VALUE_ASSET_DESTROY);
            destroyAssetMap.put(RpcConstants.TX_IS_SYSTEM_CMD,"false");
            destroyAssetMap.put(RpcConstants.TX_UNLOCK_CMD,"false");
            destroyAssetMap.put(RpcConstants.TX_VERIFY_SIGNATURE_CMD,"true");
            txRegisterDetailList.add(destroyAssetMap);
            params.put("list", txRegisterDetailList);

            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstants.TX_REGISTER_CMD, params);
            Log.debug("response={}",cmdResp);
            return cmdResp.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean newTx(Transaction tx) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(RpcConstants.TX_CHAIN_ID,  CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID));
            params.put(RpcConstants.TX_DATA_HEX,HexUtil.encode(tx.serialize()));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstants.CMD_TX_NEW, params);
            Log.debug("response={}",cmdResp);
            return cmdResp.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createCrossGroup(BlockChain blockChain) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("chainId",blockChain.getChainId());
            map.put("magicNumber",blockChain.getMagicNumber());
            map.put("maxOut","");
            map.put("maxIn","");
            map.put("minAvailableCount",blockChain.getMinAvailableNodeNum());
            map.put("seedIps","");
            map.put("isMoonNode","1");
           Response response =  ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CREATE_NODEGROUP,map );
           return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean destroyCrossGroup(BlockChain blockChain) {
        try {
                Map<String,Object> map = new HashMap<>();
                map.put("chainId",blockChain.getChainId());
                Response response =  ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_DELETE_NODEGROUP,map );
                return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AccountBalance getCoinData(String address) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("chainId",CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID));
            map.put("assetChainId",CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID));
            map.put("assetId",CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID));
            Response response =  ResponseMessageProcessor.requestAndResponse(CmConstants.MODULE_ROLE, RpcConstants.CMD_NW_CREATE_NODEGROUP,map );
            Map resultMap =  ResponseUtil.getResultMap(response,RpcConstants.CMD_NW_CREATE_NODEGROUP);
            if(null != resultMap){
               String available = resultMap.get("available").toString();
               String nonce = resultMap.get("nonce").toString();
               return new AccountBalance(available,nonce);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
