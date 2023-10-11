/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.ResponseUtil;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
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
    @Autowired
    ChainService chainService;

    @Override
    public String getCrossChainSeeds() {
        String seeds = "";
        try {
            Map<String, Object> map = new HashMap<>();
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, RpcConstants.CMD_NW_CROSS_SEEDS, map);
            if (response.isSuccess()) {
                Map rtMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_NW_CROSS_SEEDS);
                if (null != rtMap) {
                    seeds = String.valueOf(rtMap.get("seedsIps"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return seeds;
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
            LoggerUtil.logger().debug("requestCrossIssuingAssets chainId={},assetIds={}", chainId, assetIds);
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
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_CROSS_CHAIN_REGISTER_CHANGE, map, 1000);
            LoggerUtil.logger().info("通知跨链协议模块:crossChainRegisterChange success");
            return response.isSuccess();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
    }

    @Override
    public boolean registerCrossChain(List<BlockChain> blockChains) {
        for (BlockChain blockChain : blockChains) {
            if (!registerCrossChain(blockChain)) {
                return false;
            }
        }
        LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossChain all success size={}", blockChains.size());
        return true;
    }

    @Override
    public boolean registerCrossChain(BlockChain blockChain) {
        try {
            Map<String, Object> map = chainService.getBlockAssetsInfo(blockChain);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_REG_CROSS_CHAIN, map);
            if (!response.isSuccess()) {
                LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossChain fail chainId={},error={}", blockChain.getChainId(), response.getResponseComment());
                return false;
            }
            LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossChain success chainId={}", blockChain.getChainId());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean registerCrossAsset(List<Asset> assets, long registerTime) {
        for (Asset asset : assets) {
            if (!registerCrossAsset(asset, registerTime)) {
                return false;
            }
        }
        LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossAsset all success size={}", assets.size());
        return true;
    }

    @Override
    public boolean registerCrossAsset(Asset asset, long registerTime) {
        try {
            Map<String, Object> assetMap = new HashMap<>();
            assetMap.put("chainId", asset.getChainId());
            assetMap.put("assetId", asset.getAssetId());
            assetMap.put("symbol", asset.getSymbol());
            assetMap.put("assetName", asset.getAssetName());
            assetMap.put("usable", asset.isAvailable());
            assetMap.put("decimalPlaces", asset.getDecimalPlaces());
            assetMap.put("time", registerTime);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_REG_CROSS_ASSET, assetMap);
            if (!response.isSuccess()) {
                LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossAsset fail chainId={},assetId={},error={}", asset.getChainId(), asset.getAssetId(), response.getResponseComment());
                return false;
            }
            LoggerUtil.logger().info("通知跨链协议模块:cmd=registerCrossAsset success chainId={},assetId={}", asset.getChainId(), asset.getAssetId());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean cancelCrossChain(List<Map<String, Object>> chainAssetIds, long cancelTime) {
        for (Map map : chainAssetIds) {
            if (!cancelCrossChain(map, cancelTime)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean cancelCrossChain(Map<String, Object> chainAssetId, long cancelTime) {
        try {
            chainAssetId.put("time", cancelTime);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, RpcConstants.CMD_CANCEL_CROSS_CHAIN, chainAssetId);
            if (!response.isSuccess()) {
                LoggerUtil.logger().info("通知跨链协议模块:cmd=cancelCrossChain fail chainId={},assetId={},error={}", chainAssetId.get("chainId"), chainAssetId.get("assetId"), response.getResponseComment());
                return false;
            }
            LoggerUtil.logger().info("通知跨链协议模块:cmd=cancelCrossChain success. chainId={},assetId={}", chainAssetId.get("chainId"), chainAssetId.get("assetId"));
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean addAcAddressPrefix(List<Map<String, Object>> prefixList) {
        try {
            if (prefixList.size() == 0) {
                return true;
            }
            Map<String, Object> param = new HashMap<>();
            param.put("prefixList", prefixList);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, RpcConstants.CMD_AC_ADDRESS_PREFIX, param, 1000);
            if (!response.isSuccess()) {
                LoggerUtil.logger().info("通知AC模块地址前缀添加失败");
                return false;
            }
            LoggerUtil.logger().info("通知AC模块地址前缀添加成功");
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return false;
        }
        return true;
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

    @Override
    public String getChainPackerInfo(int chainId) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", chainId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, RpcConstants.CMD_CS_GET_SEED_NODE_INFO, map);
            if (response.isSuccess()) {
                Map<String, Object> packerMap = ResponseUtil.getResultMap(response, RpcConstants.CMD_CS_GET_SEED_NODE_INFO);
                List<String> packerList = (List) packerMap.get("packAddressList");
                for (String packer : packerList) {
                    stringBuffer.append(packer);
                    stringBuffer.append(",");
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        if (stringBuffer.length() > 0) {
            return stringBuffer.toString().substring(0, stringBuffer.length() - 1);
        }
        return "";
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
    public ErrorCode transactionSignature(int chainId, String address, String password, Transaction tx) throws
            NulsException {
        try {
            if (Arrays.equals(CmConstants.BLACK_HOLE_ADDRESS, AddressTool.getAddress(address))) {
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
    public Asset getLocalAssetByLedger(int chainId, int assetId) throws NulsException {
        try {
            Map<String, Object> assetMap = new HashMap<>();
            assetMap.put("chainId", chainId);
            assetMap.put("assetId", assetId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, RpcConstants.CMD_LG_GET_ASSETS_REG_INFO_BY_ID, assetMap);
            if (!response.isSuccess()) {
                LoggerUtil.logger().error("获取账本资产信息失败:chainId={},assetId={},error={}", chainId, assetId, response.getResponseComment());
                return null;
            }
            Map<String, Object> result = ResponseUtil.getResultMap(response, RpcConstants.CMD_LG_GET_ASSETS_REG_INFO_BY_ID);
            if (null == result) {
                return null;
            }
            Asset asset = new Asset();
            asset.setAssetName(result.get("assetName").toString());
            asset.setChainId(chainId);
            asset.setAssetId(assetId);
            asset.setSymbol(result.get("assetSymbol").toString());
            asset.setDecimalPlaces(Short.valueOf(result.get("decimalPlace").toString()));
            BigInteger initNumber = new BigInteger(result.get("initNumber").toString());
            long decimal = (long) Math.pow(10, Integer.valueOf(asset.getDecimalPlaces()));
            initNumber = initNumber.multiply(
                    BigInteger.valueOf(decimal));
            asset.setInitNumber(initNumber);
            return asset;
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return null;
    }
}
