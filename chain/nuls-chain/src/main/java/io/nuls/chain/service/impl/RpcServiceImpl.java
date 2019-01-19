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
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.util.ResponseUtils;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Response response =  CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CROSS_SEEDS,map );
                if(response.isSuccess()){
                    Map rtMap = ResponseUtils.getResultMap(response,RpcConstants.CMD_NW_CROSS_SEEDS);
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
            params.put(RpcConstants.TX_CHAIN_ID,  CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID));
            params.put(RpcConstants.TX_MODULE_CODE, ModuleE.CM.abbr);
            params.put(RpcConstants.TX_MODULE_VALIDATE_CMD, RpcConstants.TX_MODULE_VALIDATE_CMD_VALUE);
            List<Map<String,Object>> txRegisterDetailList = new ArrayList<Map<String,Object>>();
            /*register chain*/
            Map<String,Object>  regChainMap= new HashMap<>();
            regChainMap.put("txType",ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET);
            regChainMap.put("validateCmd","cm_chainRegValidator");
            regChainMap.put("commitCmd","cm_chainRegCommit");
            regChainMap.put("rollbackCmd","cm_chainRegRollback");
            regChainMap.put("systemTx","false");
            regChainMap.put("unlockTx","false");
            regChainMap.put("verifySignature","true");
            txRegisterDetailList.add(regChainMap);
            /*destroy chain*/
            Map<String,Object>  destroyChainMap= new HashMap<>();
            destroyChainMap.put("txType",ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN);
            destroyChainMap.put("validateCmd","cm_chainDestroyValidator");
            destroyChainMap.put("commitCmd","cm_chainDestroyCommit");
            destroyChainMap.put("rollbackCmd","cm_chainDestroyRollback");
            destroyChainMap.put("systemTx","false");
            destroyChainMap.put("unlockTx","false");
            destroyChainMap.put("verifySignature","true");
            txRegisterDetailList.add(destroyChainMap);
            /*add asset*/
            Map<String,Object>  addAssetMap= new HashMap<>();
            addAssetMap.put("txType",ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN);
            addAssetMap.put("validateCmd","cm_assetRegValidator");
            addAssetMap.put("commitCmd","cm_assetRegCommit");
            addAssetMap.put("rollbackCmd","cm_assetRegRollback");
            addAssetMap.put("systemTx","false");
            addAssetMap.put("unlockTx","false");
            addAssetMap.put("verifySignature","true");
            txRegisterDetailList.add(addAssetMap);
            /*destroy asset*/
            Map<String,Object>  destroyAssetMap= new HashMap<>();
            destroyAssetMap.put("txType",ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN);
            destroyAssetMap.put("validateCmd","cm_assetRegValidator");
            destroyAssetMap.put("commitCmd","cm_assetRegCommit");
            destroyAssetMap.put("rollbackCmd","cm_assetRegRollback");
            destroyAssetMap.put("systemTx","false");
            destroyAssetMap.put("unlockTx","false");
            destroyAssetMap.put("verifySignature","true");
            txRegisterDetailList.add(destroyAssetMap);
            params.put("list", txRegisterDetailList);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, RpcConstants.TX_REGISTER_CMD, params);
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
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, RpcConstants.TX_NEW_CMD, params);
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
           Response response =  CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CREATE_NODEGROUP,map );
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
                Response response =  CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_DELETE_NODEGROUP,map );
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
            Response response =  CmdDispatcher.requestAndResponse(CmConstants.MODULE_ROLE, RpcConstants.CMD_NW_CREATE_NODEGROUP,map );
            Map resultMap =  ResponseUtils.getResultMap(response,RpcConstants.CMD_NW_CREATE_NODEGROUP);
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
