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
package io.nuls.chain.rpc.cmd;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.ChainDto;
import io.nuls.chain.model.dto.RegChainDto;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.util.ChainManagerUtil;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@Component
@NulsCoresCmd(module = ModuleE.CM)
public class CmmChainCmd extends BaseChainCmd {

    @Autowired
    private ChainService chainService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private RpcService rpcService;
    @Autowired
    NulsCoresConfig nulsChainConfig;

    @CmdAnnotation(cmd = RpcConstants.CMD_CHAIN, version = 1.0,
            description = "View chain information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
    })
    @ResponseData(description = "Return Chain Information", responseType = @TypeDescriptor(value = RegChainDto.class))
    public Response chain(Map params) {
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            BlockChain blockChain = chainService.getChain(chainId);
            if (blockChain == null) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            } else {
                RegChainDto regChainDto = new RegChainDto();
                regChainDto.buildRegChainDto(blockChain);
                regChainDto.setMainNetCrossConnectSeeds(rpcService.getCrossChainSeeds());
                regChainDto.setMainNetVerifierSeeds(rpcService.getChainPackerInfo(CmRuntimeInfo.getMainIntChainId()));
                return success(regChainDto);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = RpcConstants.CMD_CHAIN_REG, version = 1.0,
            description = "Chain registration-Cross chain registration for parallel chains")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[3-65535]", parameterDes = "Asset ChainId,Value range[3-65535]"),
            @Parameter(parameterName = "chainName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Chain Name"),
            @Parameter(parameterName = "addressType", requestType = @TypeDescriptor(value = int.class), parameterDes = "1 applyNULSThe chain of framework construction Within the ecosystem,2Outside the ecosystem"),
            @Parameter(parameterName = "addressPrefix", requestType = @TypeDescriptor(value = String.class), parameterDes = "Chain Address Prefix,1-5character"),
            @Parameter(parameterName = "magicNumber", requestType = @TypeDescriptor(value = long.class), parameterDes = "Network Magic Parameters"),
            @Parameter(parameterName = "minAvailableNodeNum", requestType = @TypeDescriptor(value = int.class), parameterDes = "Minimum number of connections"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "symbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset symbols"),
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset Name"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = String.class), parameterDes = "Initial value of assets"),
            @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = short.class), parameterDes = "Decimal Places of Assets"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Create an account address for the transaction"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "Account password"),
            @Parameter(parameterName = "verifierList", requestType = @TypeDescriptor(value = String.class), parameterDes = "Verifier Address List,Comma division"),
            @Parameter(parameterName = "signatureBFTRatio", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "Byzantine proportion,A value greater than or equal to this is a valid confirmation"),
            @Parameter(parameterName = "maxSignatureCount", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "Maximum number of signatures,Limit the maximum number of verifier signature lists")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue"),
                    @Key(name = "mainNetVerifierList", valueType = String.class, description = "List of main network validators,Comma separated"),
                    @Key(name = "mainNetCrossSeedList", valueType = String.class, description = "Main network verification seed node list,Comma separated")

            })
    )
    public Response chainReg(Map params) {
        /* Send to transaction module (Send to transaction module) */
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            /*Determine whether the chain and assets already exist*/
            /* assembleBlockChain (BlockChain object) */
            BlockChain blockChain = new BlockChain();
            blockChain.map2pojo(params);
            //todo Temporary handling
//            if (blockChain.getChainId() == BaseConstant.MAINNET_CHAIN_ID || blockChain.getChainId() == BaseConstant.TESTNET_CHAIN_ID) {
//                return failed(CmErrorCode.ERROR_CHAIN_SYSTEM_USED);
//            }
            String addressPrefix = (String) params.get("addressPrefix");
            if (StringUtils.isBlank(addressPrefix)) {
                return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
            }
            char[] arr = addressPrefix.toCharArray();
            if (arr.length > 5) {
                return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
            }
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] < 48 || (arr[i] > 57 && arr[i] < 65) || (arr[i] > 90 && arr[i] < 97) || arr[i] > 122) {
                    return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
                }
            }
            /* assembleAsset (Asset object) */
            /* cancelint assetId = seqService.createAssetId(blockChain.getChainId());*/
            Asset asset = new Asset();
            asset.map2pojo(params);
            if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
                return failed(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
            }
            if (!FormatValidUtils.validTokenNameOrSymbolV15(asset.getSymbol())) {
                return failed(CmErrorCode.ERROR_ASSET_SYMBOL);
            }
            if (!FormatValidUtils.validTokenNameOrSymbolV15(asset.getAssetName())) {
                return failed(CmErrorCode.ERROR_ASSET_NAME);
            }

            int version = ProtocolGroupManager.getCurrentVersion(Integer.valueOf(nulsChainConfig.getMainChainId()));
            asset.setChainId(blockChain.getChainId());
            if(version < CmConstants.REMOVE_DEPOSIT_VERSION) {
                asset.setDepositNuls(nulsChainConfig.getAssetDepositNuls());
                asset.setDestroyNuls(nulsChainConfig.getAssetDestroyNuls());
            }else {
                asset.setDepositNuls(BigInteger.ZERO);
                asset.setDestroyNuls(BigInteger.ZERO);
            }

            asset.setAvailable(true);
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (null != dbChain && dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_CHAIN_REG_CMD);
            }
            if (null != dbChain) {
                return failed(CmErrorCode.ERROR_CHAIN_ID_EXIST);
            }
            if (chainService.hadExistMagicNumber(blockChain.getMagicNumber())) {
                return failed(CmErrorCode.ERROR_MAGIC_NUMBER_EXIST);
            }
            if (chainService.hadExistChainName(blockChain.getChainName())) {
                return failed(CmErrorCode.ERROR_CHAIN_NAME_EXIST);
            }
            /* Assembly transaction sending (Send transaction) */
            Transaction tx = new RegisterChainAndAssetTransaction();
            if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                tx.setTxData(TxUtil.parseChainToTxV5(blockChain, asset).serialize());
            } else {
                tx.setTxData(TxUtil.parseChainToTx(blockChain, asset).serialize());
            }

            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData;
            if (version < CmConstants.REMOVE_DEPOSIT_VERSION) {
                coinData = this.getRegCoinData(asset, CmRuntimeInfo.getMainIntChainId(),
                        CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            } else {
                coinData = this.getRegCoinDataV7(asset, CmRuntimeInfo.getMainIntChainId(),
                        CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            }
            tx.setCoinData(coinData.serialize());

            /* Check if the signature is correct (Determine if the signature is correct),Take the main networkchainidSign */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }

            rtMap.put("txHash", tx.getHash().toHex());
            rtMap.put("mainNetCrossConnectSeeds", rpcService.getCrossChainSeeds());
            rtMap.put("mainNetVerifierSeeds", rpcService.getChainPackerInfo(CmRuntimeInfo.getMainIntChainId()));


            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger().error(e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtMap);
    }


    @CmdAnnotation(cmd = RpcConstants.CMD_CHAIN_ACTIVE, version = 1.0,
            description = "Chain update activation-Cross chain update activation for parallel chains（Activate previously logged out chains）")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "chainName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Chain Name"),
            @Parameter(parameterName = "addressType", requestType = @TypeDescriptor(value = int.class), parameterDes = "1 applyNULSThe chain of framework construction Within the ecosystem,2Outside the ecosystem"),
            @Parameter(parameterName = "addressPrefix", requestType = @TypeDescriptor(value = String.class), parameterDes = "Chain Address Prefix,1-5character"),
            @Parameter(parameterName = "magicNumber", requestType = @TypeDescriptor(value = long.class), parameterDes = "Network Magic Parameters"),
            @Parameter(parameterName = "minAvailableNodeNum", requestType = @TypeDescriptor(value = int.class), parameterDes = "Minimum number of connections"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "symbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset symbols"),
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset Name"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = String.class), parameterDes = "Initial value of assets"),
            @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = short.class), parameterDes = "Decimal Places of Assets"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Create an account address for the transaction"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "Account password"),
            @Parameter(parameterName = "verifierList", requestType = @TypeDescriptor(value = String.class), parameterDes = "Verifier Address List,Comma division"),
            @Parameter(parameterName = "signatureBFTRatio", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "Byzantine proportion,A value greater than or equal to this is a valid confirmation"),
            @Parameter(parameterName = "maxSignatureCount", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "Maximum number of signatures,Limit the maximum number of verifier signature lists")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue"),
                    @Key(name = "mainNetVerifierSeeds", valueType = String.class, description = "Main network validator seed list,Comma separated"),
                    @Key(name = "mainNetCrossConnectSeeds", valueType = String.class, description = "Main network verification seed node list,Comma separated")

            })
    )
    public Response chainActive(Map params) {
        /* Send to transaction module (Send to transaction module) */
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            /*Determine whether the chain and assets already exist*/
            /* assembleBlockChain (BlockChain object) */
            BlockChain blockChain = new BlockChain();
            blockChain.map2pojo(params);
//            if (blockChain.getChainId() == BaseConstant.MAINNET_CHAIN_ID || blockChain.getChainId() == BaseConstant.TESTNET_CHAIN_ID) {
//                return failed(CmErrorCode.ERROR_CHAIN_SYSTEM_USED);
//            }
            String addressPrefix = (String) params.get("addressPrefix");
            if (StringUtils.isBlank(addressPrefix)) {
                return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
            }
            char[] arr = addressPrefix.toCharArray();
            if (arr.length > 5) {
                return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
            }
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] < 48 || (arr[i] > 57 && arr[i] < 65) || (arr[i] > 90 && arr[i] < 97) || arr[i] > 122) {
                    return failed(CmErrorCode.ERROR_CHAIN_ADDRESS_PREFIX);
                }
            }
            /* assembleAsset (Asset object) */
            /* cancelint assetId = seqService.createAssetId(blockChain.getChainId());*/
            Asset asset = new Asset();
            asset.map2pojo(params);
            if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
                return failed(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
            }
            if (!FormatValidUtils.validTokenNameOrSymbolV15(asset.getSymbol())) {
                return failed(CmErrorCode.ERROR_ASSET_SYMBOL);
            }
            if (!FormatValidUtils.validTokenNameOrSymbolV15(asset.getAssetName())) {
                return failed(CmErrorCode.ERROR_ASSET_NAME);
            }

            int version = ProtocolGroupManager.getCurrentVersion(Integer.valueOf(nulsChainConfig.getMainChainId()));
            asset.setChainId(blockChain.getChainId());
            if(version < CmConstants.REMOVE_DEPOSIT_VERSION) {
                asset.setDepositNuls(nulsChainConfig.getAssetDepositNuls());
                asset.setDestroyNuls(nulsChainConfig.getAssetDestroyNuls());
            }else {
                asset.setDepositNuls(BigInteger.ZERO);
                asset.setDestroyNuls(BigInteger.ZERO);
            }
            asset.setAvailable(true);
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (null == dbChain) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            }
            if (null != dbChain && !dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_CHAIN_ACTIVE);
            }
            if (chainService.hadExistMagicNumber(blockChain.getMagicNumber()) && !dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_MAGIC_NUMBER_EXIST);
            }
            if (chainService.hadExistChainName(blockChain.getChainName()) && !dbChain.isDelete()) {
                LoggerUtil.COMMON_LOG.debug("######### delete={}", dbChain.isDelete());
                return failed(CmErrorCode.ERROR_CHAIN_NAME_EXIST);
            }
            /* Assembly transaction sending (Send transaction) */
            Transaction tx = new RegisterChainAndAssetTransaction();
            if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                tx.setTxData(TxUtil.parseChainToTxV5(blockChain, asset).serialize());
            } else {
                tx.setTxData(TxUtil.parseChainToTx(blockChain, asset).serialize());
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData;
            if (version < CmConstants.REMOVE_DEPOSIT_VERSION) {
                coinData = this.getRegCoinData(asset, CmRuntimeInfo.getMainIntChainId(),
                        CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            } else {
                coinData = this.getRegCoinDataV7(asset, CmRuntimeInfo.getMainIntChainId(),
                        CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            }
            tx.setCoinData(coinData.serialize());

            /* Check if the signature is correct (Determine if the signature is correct),Take the main networkchainidSign */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }

            rtMap.put("txHash", tx.getHash().toHex());
            rtMap.put("mainNetCrossConnectSeeds", rpcService.getCrossChainSeeds());
            rtMap.put("mainNetVerifierSeeds", rpcService.getChainPackerInfo(CmRuntimeInfo.getMainIntChainId()));


            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger().error(e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_GET_CROSS_CHAIN_INFOS, version = 1.0,
            description = "Obtain cross chain registration asset information")

    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, collectionElement = List.class, mapKeys = {
                    @Key(name = "chainInfos", valueType = List.class, valueElement = ChainDto.class, description = "Registered Chain and Asset Information List")
            })
    )
    public Response getCrossChainInfos(Map params) {
        List<Map<String, Object>> chainInfos = new ArrayList<>();
        Map<String, Object> rtMap = new HashMap<>();
        try {
            List<BlockChain> blockChains = chainService.getBlockList();
            for (BlockChain blockChain : blockChains) {
                chainInfos.add(chainService.getBlockAssetsInfo(blockChain));
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        rtMap.put("chainInfos", chainInfos);
        try {
            LoggerUtil.COMMON_LOG.debug(JSONUtils.obj2json(chainInfos));
        } catch (JsonProcessingException e) {
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_GET_CROSS_CHAIN_SIMPLE_INFOS, version = 1.0,
            description = "Obtain a list of cross chain registered chains")

    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, collectionElement = List.class, mapKeys = {
                    @Key(name = "chainInfos", valueType = List.class, valueElement = Map.class, description = "Return a brief list of chain and asset information")
            })
    )
    public Response getChainAssetsSimpleInfo(Map params) {
        List<Map<String, Object>> chainInfos = new ArrayList<>();
        Map<String, Object> rtMap = new HashMap<>();
        try {
            List<BlockChain> blockChains = chainService.getBlockList();
            for (BlockChain blockChain : blockChains) {
                chainInfos.add(chainService.getChainAssetsSimpleInfo(blockChain));
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        rtMap.put("chainInfos", chainInfos);
        return success(rtMap);
    }

}
