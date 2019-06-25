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
package io.nuls.chain.rpc.call.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.ChainAssetTotalCirculate;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.ResponseUtil;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.math.BigInteger;
import java.util.*;

/**
 * @program: nuls2.0
 * @description: 远程接口调用
 * @author: lan
 * @create: 2018/11/20
 **/
@Service
public class RpcServiceImpl implements RpcService {
    @Override
    public String getCrossChainSeeds() {
        try {
            Map<String, Object> map = new HashMap<>();
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CROSS_SEEDS, map);
            if (response.isSuccess()) {
                Map rtMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_NW_CROSS_SEEDS);
                if (null != rtMap) {
                    return String.valueOf(rtMap.get("seedsIps"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return null;
    }

    @Override
    public long getMainNetMagicNumber() {
        try {
            Map<String, Object> map = new HashMap<>();
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_GET_MAIN_NET_MAGIC_NUMBER, map);
            if (response.isSuccess()) {
                Map rtMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_NW_GET_MAIN_NET_MAGIC_NUMBER);
                if (null != rtMap) {
                    return Long.valueOf(rtMap.get("value").toString());
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public ErrorCode newTx(Transaction tx) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(RpcConstants.TX_CHAIN_ID, CmRuntimeInfo.getMainIntChainId());
            params.put(RpcConstants.TX_DATA_HEX, RPCUtil.encode(tx.serialize()));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstants.CMD_TX_NEW, params);
            if (!cmdResp.isSuccess()) {
                return ErrorCode.init(cmdResp.getResponseErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return CmErrorCode.ERROR_TX_REG_RPC;
        }
        return null;
    }

    @Override
    public boolean createCrossGroup(BlockChain blockChain) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", blockChain.getChainId());
            map.put("magicNumber", blockChain.getMagicNumber());
            map.put("maxOut", "");
            map.put("maxIn", "");
            map.put("minAvailableCount", blockChain.getMinAvailableNodeNum());
            map.put("seedIps", "");
            map.put("isCrossGroup", "true");
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CREATE_NODEGROUP, map);
            LoggerUtil.logger().info("通知网络模块:createCrossGroup success");
            return response.isSuccess();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }

    }

    @Override
    public boolean destroyCrossGroup(BlockChain blockChain) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", blockChain.getChainId());
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_DELETE_NODEGROUP, map);
            LoggerUtil.logger().info("通知网络模块:destroyCrossGroup success");
            return response.isSuccess();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
    }

    @Override
    public boolean requestCrossIssuingAssets(int chainId, String assetIds) {
        try {
            LoggerUtil.logger().debug("requestCrossIssuingAssets chainId={},assetIds={}",chainId,assetIds);
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", chainId);
            map.put("assetIds", assetIds);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_GET_FRIEND_CHAIN_CIRCULATE, map);
            return response.isSuccess();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
    }

    @Override
    public boolean crossChainRegisterChange(int chainId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", chainId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_CROSS_CHAIN_REGISTER_CHANGE, map, 200);
            LoggerUtil.logger().info("通知跨链协议模块:cancelCrossChain success");
            return response.isSuccess();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
    }
    @Override
    public ErrorCode getCoinData(String address, AccountBalance accountBalance) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", CmRuntimeInfo.getMainIntChainId());
            map.put("assetChainId", CmRuntimeInfo.getMainIntChainId());
            map.put("assetId", CmRuntimeInfo.getMainIntAssetId());
            map.put("address", address);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, RpcConstants.CMD_LG_GET_COINDATA, map);
            if (!response.isSuccess()) {
                return ErrorCode.init(response.getResponseErrorCode());
            } else {
                Map resultMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_LG_GET_COINDATA);
                if (null != resultMap) {
                    String available = resultMap.get("available").toString();
                    byte[] nonce = RPCUtil.decode(resultMap.get("nonce").toString());
                    accountBalance.setNonce(nonce);
                    accountBalance.setAvailable(available);
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error("get AccountBalance error....");
            LoggerUtil.logger().error(e);
            return CmErrorCode.ERROR_LEDGER_BALANCE_RPC;
        }
        return null;
    }

    @Override
    public List<ChainAssetTotalCirculate> getLgAssetsById(int chainId, String assetIds) {
        List<ChainAssetTotalCirculate> list = new ArrayList<>();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", chainId);
            map.put("assetIds", assetIds);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, RpcConstants.CMD_LG_GET_ASSETS_BY_ID, map);
            if (response.isSuccess()) {
                Map<String, Object> assetsMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_LG_GET_ASSETS_BY_ID);
                List<Map<String, Object>> assets = (List) assetsMap.get("assets");
                for (Map<String, Object> asset : assets) {
                    ChainAssetTotalCirculate chainAssetTotalCirculate = new ChainAssetTotalCirculate();
                    chainAssetTotalCirculate.setChainId(chainId);
                    chainAssetTotalCirculate.setFreeze(new BigInteger(asset.get("freeze").toString()));
                    chainAssetTotalCirculate.setAvailableAmount(new BigInteger(asset.get("availableAmount").toString()));
                    chainAssetTotalCirculate.setAssetId(Integer.valueOf(asset.get("assetId").toString()));
                    list.add(chainAssetTotalCirculate);
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error("get AccountBalance error....");
            LoggerUtil.logger().error(e);
        }
        return list;
    }

    /**
     * 账户验证
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public HashMap accountValid(int chainId, String address, String password) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, RpcConstants.CMD_AC_GET_PRI_KEY, callParams);
            if (!cmdResp.isSuccess()) {
                throw new NulsException(CmErrorCode.ERROR_ACCOUNT_VALIDATE);
            }
            HashMap callResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get(RpcConstants.CMD_AC_GET_PRI_KEY);
            if (callResult == null || callResult.size() == 0 || !(boolean) callResult.get(RpcConstants.VALID_RESULT)) {
                throw new NulsException(CmErrorCode.ERROR_ACCOUNT_VALIDATE);
            }
            return callResult;

        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 交易签名
     * transaction signature
     *
     * @param chainId
     * @param address
     * @param password
     * @param tx
     */
    @Override
    public ErrorCode transactionSignature(int chainId, String address, String password, Transaction tx) throws NulsException {
        try {
            if(Arrays.equals(CmConstants.BLACK_HOLE_ADDRESS, AddressTool.getAddress(address))){
                return CmErrorCode.ERROR_ADDRESS_ERROR;
            }
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            callParams.put("data", RPCUtil.encode(tx.getHash().getBytes()));
            Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, RpcConstants.CMD_AC_SIGN_DIGEST, callParams);
            if (!signResp.isSuccess()) {
                LoggerUtil.logger().error("ac_signDigest rpc error....{}=={}", signResp.getResponseErrorCode(), signResp.getResponseComment());
                return ErrorCode.init(signResp.getResponseErrorCode());
            }
            HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signDigest");
            p2PHKSignature.parse(RPCUtil.decode((String) signResult.get("signature")), 0);
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        } catch (NulsException e) {
            LoggerUtil.logger().error(e);
            return CmErrorCode.ERROR_SIGNDIGEST;
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return CmErrorCode.ERROR_SIGNDIGEST;
        }
        return null;
    }

    @Override
    public long getTime() {
        long time = 0;
        Map<String, Object> map = new HashMap<>(1);
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_GET_TIME_CALL, map, 100);
            if (null != response && response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                time = Long.valueOf(((Map) responseData.get(RpcConstants.CMD_NW_GET_TIME_CALL)).get("currentTimeMillis").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (time == 0) {
                time = System.currentTimeMillis();
            }
        }
        return time;
    }
}
