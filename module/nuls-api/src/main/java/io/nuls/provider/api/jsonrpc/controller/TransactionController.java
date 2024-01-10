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
package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.crosschain.CrossChainProvider;
import io.nuls.base.api.provider.crosschain.facade.CreateCrossTxReq;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.crypto.HexUtil;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.dto.TransactionDto;
import io.nuls.provider.model.jsonrpc.RpcErrorCode;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.ContractTools;
import io.nuls.provider.rpctools.TransactionTools;
import io.nuls.provider.utils.*;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;
import io.nuls.v2.model.dto.*;
import io.nuls.v2.txdata.CallContractData;
import io.nuls.v2.txdata.CreateContractData;
import io.nuls.v2.txdata.DeleteContractData;
import io.nuls.v2.util.CommonValidator;
import io.nuls.v2.util.NulsSDKTool;
import io.nuls.v2.util.ValidateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.core.constant.TxType.*;
import static io.nuls.provider.utils.Utils.extractTxTypeFromTx;

/**
 * @author: PierreLuo
 * @date: 2019-07-02
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class TransactionController {
    @Autowired
    Config config;
    @Autowired
    private TransactionTools transactionTools;
    @Autowired
    private ContractTools contractTools;

    TransferService transferService = ServiceManager.get(TransferService.class);

    CrossChainProvider crossChainProvider = ServiceManager.get(CrossChainProvider.class);

    BlockService blockService = ServiceManager.get(BlockService.class);

    @RpcMethod("getTx")
    @ApiOperation(description = "according tohashObtain transactions", order = 301)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "hash", parameterDes = "transactionhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = TransactionDto.class))
    public RpcResult getTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHash] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(txHash) || !ValidateUtil.validHash(txHash)) {
            return RpcResult.paramError("[txHash] is inValid");
        }
        Result<TransactionDto> result = transactionTools.getTx(chainId, txHash);
        if (result.isSuccess()) {
            TransactionDto txDto = result.getData();
            if (txDto.getBlockHeight() >= 0) {
                GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(txDto.getBlockHeight());
                req.setChainId(chainId);
                Result<BlockHeaderData> blockResult = blockService.getBlockHeaderByHeight(req);
                if (blockResult.isSuccess()) {
                    txDto.setBlockHash(blockResult.getData().getHash());
                }
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("validateTx")
    @ApiOperation(description = "Verify transactions", order = 302, detailDesc = "Verify transactions for offline assembly,Successful verification returns transactionhashvalue,Failure returns error message")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterDes = "Transaction serialization string"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
    }))
    public RpcResult validateTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(txHex)) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        Result result = transactionTools.validateTx(chainId, txHex);
        if (result.isSuccess()) {
            return RpcResult.success(result.getData());
        } else {
            return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
        }
    }

    @RpcMethod("broadcastTx")
    @ApiOperation(description = "Broadcasting transactions", order = 303, detailDesc = "Broadcast offline assembly transactions,Successfully returnedtrue,Failure returns error message")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterDes = "Transaction serialization16Hexadecimal Strings"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful"),
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult broadcastTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!Context.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            int type = extractTxTypeFromTx(txHex);
            //  if(type == CROSS_CHAIN){
            //     return RpcResult.failed(CommonCodeConstanst.PARAMETER_ERROR,"Cross-chain tx pause support");
            // }
            Result result = new Result();
            switch (type) {
                case CREATE_CONTRACT:
                    Transaction tx = new Transaction();
                    tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CreateContractData create = new CreateContractData();
                    create.parse(new NulsByteBuffer(tx.getTxData()));
                    RpcResult createArgsResult = this.validateContractArgs(create.getArgs());
                    if (createArgsResult.getError() != null) {
                        return createArgsResult;
                    }
                    result = contractTools.validateContractCreate(chainId,
                            AddressTool.getStringAddressByBytes(create.getSender()),
                            create.getGasLimit(),
                            create.getPrice(),
                            RPCUtil.encode(create.getCode()),
                            create.getArgs());
                    break;
                case CALL_CONTRACT:
                    Transaction callTx = new Transaction();
                    callTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CallContractData call = new CallContractData();
                    call.parse(new NulsByteBuffer(callTx.getTxData()));
                    RpcResult argsResult = this.validateContractArgs(call.getArgs());
                    if (argsResult.getError() != null) {
                        return argsResult;
                    }
                    String[][] multyAssetValues = Utils.extractMultyAssetInfoFromCallTransaction(call.getContractAddress(), callTx.getCoinDataInstance(), config.getChainId(), config.getAssetsId());
                    result = contractTools.validateContractCall(chainId,
                            AddressTool.getStringAddressByBytes(call.getSender()),
                            call.getValue(),
                            call.getGasLimit(),
                            call.getPrice(),
                            AddressTool.getStringAddressByBytes(call.getContractAddress()),
                            call.getMethodName(),
                            call.getMethodDesc(),
                            call.getArgs(),
                            multyAssetValues);
                    break;
                case DELETE_CONTRACT:
                    Transaction deleteTx = new Transaction();
                    deleteTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    DeleteContractData delete = new DeleteContractData();
                    delete.parse(new NulsByteBuffer(deleteTx.getTxData()));
                    result = contractTools.validateContractDelete(chainId,
                            AddressTool.getStringAddressByBytes(delete.getSender()),
                            AddressTool.getStringAddressByBytes(delete.getContractAddress()));
                    break;
                default:
                    break;
            }
            Map contractMap = (Map) result.getData();
            if (contractMap != null && Boolean.FALSE.equals(contractMap.get("success"))) {
                return RpcResult.failed(CommonCodeConstanst.DATA_ERROR, (String) contractMap.get("msg"));
            }

            result = transactionTools.newTx(chainId, txHex);

            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
            }
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    private RpcResult validateContractArgs(String[][] args) {
        if (args == null || args.length == 0) {
            return RpcResult.success(null);
        }
        try {
            String[] arg;
            for (int i = 0, length = args.length; i < length; i++) {
                arg = args[i];
                if (arg == null || arg.length == 0) {
                    continue;
                }
                for (String str : arg) {
                    if (!this.checkSpaceArg(str)) {
                        return RpcResult.failed(RpcErrorCode.CONTRACT_VALIDATION_FAILED);
                    }
                }
            }
            return RpcResult.success(null);
        } catch (Exception e) {
            io.nuls.core.log.Log.error("parse args error.", e);
            return RpcResult.failed(RpcErrorCode.CONTRACT_VALIDATION_FAILED);
        }
    }

    private boolean checkSpaceArg(String s) {
        if (s == null) {
            return true;
        }
        return s.length() == s.trim().length();
    }

    @RpcMethod("broadcastTxWithNoContractValidation")
    @ApiOperation(description = "Broadcasting transactions(Not verifying contracts)", order = 304, detailDesc = "Broadcast offline assembly transactions(Not verifying contracts),Successfully returnedtrue,Failure returns error message")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterDes = "Transaction serialization16Hexadecimal Strings"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful"),
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult broadcastTxWithNoContractValidation(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!Context.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result result = transactionTools.newTx(chainId, txHex);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
            }
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    @RpcMethod("broadcastTxWithoutAnyValidation")
    @ApiOperation(description = "Broadcasting transactions(Do not verify)", order = 305, detailDesc = "Broadcast offline assembly transactions(Do not verify),Successfully returnedtrue,Failure returns error message")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterDes = "Transaction serialization16Hexadecimal Strings"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful"),
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult broadcastTxWithoutAnyValidation(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!Context.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result result = transactionTools.broadcastTxWithoutAnyValidation(chainId, txHex);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
            }
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }


    @RpcMethod("transferOtherChainAsset")
    @ApiOperation(description = "Single transfer", order = 306, detailDesc = "Initiate transfer transactions for a single account or asset,Transfer assets to other parallel chain assets within the chain")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset Chainid"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetid"),
            @Parameter(parameterName = "address", parameterDes = "Transfer account address"),
            @Parameter(parameterName = "toAddress", parameterDes = "Transfer to account address"),
            @Parameter(parameterName = "password", parameterDes = "Transfer account password"),
            @Parameter(parameterName = "amount", parameterDes = "Transfer amount"),
            @Parameter(parameterName = "remark", parameterDes = "Remarks"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult transferOtherChainAsset(List<Object> params) {
        VerifyUtils.verifyParams(params, 8);
        int chainId, assetChainId, assetId;
        String address, toAddress, password, amount, remark;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            toAddress = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[toAddress] is inValid");
        }
        try {
            password = (String) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        try {
            amount = params.get(6).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[amount] is inValid");
        }
        try {
            remark = (String) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, toAddress)) {
            return RpcResult.paramError("[toAddress] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(amount)) {
            return RpcResult.paramError("[amount] is inValid");
        }
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(chainId, assetId)
                        .addForm(assetChainId, assetId, address, password, new BigInteger(amount))
                        .addTo(assetChainId, assetId, toAddress, new BigInteger(amount)).setRemark(remark);
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        if (result.isSuccess()) {
            Map resultMap = new HashMap(2);
            resultMap.put("hash", result.getData());
            return RpcResult.success(resultMap);
        } else {
            return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
        }
    }

    @RpcMethod("transfer")
    @ApiOperation(description = "Single in chain transfer", order = 306, detailDesc = "Initiate transfer transactions for a single account or asset")
    @Parameters({
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset Chainid"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetid"),
            @Parameter(parameterName = "address", parameterDes = "Transfer account address"),
            @Parameter(parameterName = "toAddress", parameterDes = "Transfer to account address"),
            @Parameter(parameterName = "password", parameterDes = "Transfer account password"),
            @Parameter(parameterName = "amount", parameterDes = "Transfer amount"),
            @Parameter(parameterName = "remark", parameterDes = "Remarks"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult transfer(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetId;
        String address, toAddress, password, amount, remark;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            toAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[toAddress] is inValid");
        }
        try {
            password = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        try {
            amount = params.get(5).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[amount] is inValid");
        }
        try {
            remark = (String) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, toAddress)) {
            return RpcResult.paramError("[toAddress] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(amount)) {
            return RpcResult.paramError("[amount] is inValid");
        }
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(), assetId)
                        .addForm(chainId, assetId, address, password, new BigInteger(amount))
                        .addTo(chainId, assetId, toAddress, new BigInteger(amount)).setRemark(remark);
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        if (result.isSuccess()) {
            Map resultMap = new HashMap(2);
            resultMap.put("hash", result.getData());
            return RpcResult.success(resultMap);
        } else {
            return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
        }
    }

    @RpcMethod("crossTransfer")
    @ApiOperation(description = "Single cross chain transfer", order = 306, detailDesc = "Initiate cross chain transfer transactions for single account and single asset transactions")
    @Parameters({
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset Chainid"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetid"),
            @Parameter(parameterName = "address", parameterDes = "Transfer account address"),
            @Parameter(parameterName = "toAddress", parameterDes = "Transfer to account address"),
            @Parameter(parameterName = "password", parameterDes = "Transfer account password"),
            @Parameter(parameterName = "amount", parameterDes = "Transfer amount"),
            @Parameter(parameterName = "remark", parameterDes = "Remarks"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash")
    }))
    public RpcResult crossTransfer(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetId;
        String address, toAddress, password, amount, remark;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            toAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[toAddress] is inValid");
        }
        try {
            password = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        try {
            amount = params.get(5).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[amount] is inValid");
        }
        try {
            remark = (String) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(amount)) {
            return RpcResult.paramError("[amount] is inValid");
        }

        CreateCrossTxReq.CreateCrossTxReqBuilder builder = new CreateCrossTxReq.CreateCrossTxReqBuilder(config.getChainId())
                .addForm(chainId, assetId, address, password, new BigInteger(amount))
                .addTo(chainId, assetId, toAddress, new BigInteger(amount))
                .setRemark(remark);

        Result<String> result = crossChainProvider.createCrossTx(builder.build());
        if (result.isSuccess()) {
            Map resultMap = new HashMap(2);
            resultMap.put("hash", result.getData());
            return RpcResult.success(resultMap);
        } else {
            return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
        }
    }


    @RpcMethod("createTransferTxOffline")
    @ApiOperation(description = "Offline assembly transfer transaction", order = 350, detailDesc = "according toinputsandoutputsOffline assembly transfer transaction, used for single account or multi account transfer transactions." +
            "The transaction fee isinputsThe total amount of main assets in the Li Ben Chain, minusoutputsThe total amount of main assets in the Li Ben Chain")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "Transfer transaction form", requestType = @TypeDescriptor(value = TransferDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization16Hexadecimal Strings")
    }))
    public RpcResult createTransferTxOffline(List<Object> params) {
        List<Map> inputList, outputList;
        String remark;

        List<CoinFromDto> froms = new ArrayList<>();
        List<CoinToDto> tos = new ArrayList<>();
        try {
            inputList = (List<Map>) params.get(0);
            for (Map map : inputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinFromDto fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
                froms.add(fromDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[inputList] is inValid");
        }
        try {
            outputList = (List<Map>) params.get(1);
            for (Map map : outputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinToDto toDto = JSONUtils.map2pojo(map, CoinToDto.class);
                tos.add(toDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[outputList] is inValid");
        }
        try {
            remark = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }

        try {
            TransferDto transferDto = new TransferDto();
            transferDto.setInputs(froms);
            transferDto.setOutputs(tos);
            transferDto.setRemark(remark);
            CommonValidator.checkTransferDto(transferDto);
            io.nuls.core.basic.Result result = NulsSDKTool.createTransferTxOffline(transferDto);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result.getErrorCode(), result.getMsg());
            }
        } catch (NulsException e) {
            return RpcResult.failed(e.getErrorCode(), e.format());
        }
    }

    @RpcMethod("createCrossTxOffline")
    @ApiOperation(description = "Offline assembly transfer transaction", order = 350, detailDesc = "according toinputsandoutputsOffline assembly of cross chain transfer transactions, used for single account or multi account cross chain transfer transactions." +
            "The transaction fee isinputsThe total amount of main assets in the Li Ben Chain, minusoutputsThe total amount of main assets in the Li Ben Chain, plus cross chain transfer fees（NULS）")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "Transfer transaction form", requestType = @TypeDescriptor(value = TransferDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization16Hexadecimal Strings")
    }))
    public RpcResult createCrossTxOffline(List<Object> params) {
        List<Map> inputList, outputList;
        String remark;

        List<CoinFromDto> froms = new ArrayList<>();
        List<CoinToDto> tos = new ArrayList<>();
        try {
            inputList = (List<Map>) params.get(0);
            for (Map map : inputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinFromDto fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
                froms.add(fromDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[inputList] is inValid");
        }
        try {
            outputList = (List<Map>) params.get(1);
            for (Map map : outputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinToDto toDto = JSONUtils.map2pojo(map, CoinToDto.class);
                tos.add(toDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[outputList] is inValid");
        }
        try {
            remark = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }

        try {
            TransferDto transferDto = new TransferDto();
            transferDto.setInputs(froms);
            transferDto.setOutputs(tos);
            transferDto.setRemark(remark);
            CommonValidator.checkTransferDto(transferDto);
            io.nuls.core.basic.Result result = NulsSDKTool.createCrossTransferTxOffline(transferDto);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result.getErrorCode(), result.getMsg());
            }
        } catch (NulsException e) {
            return RpcResult.failed(e.getErrorCode(), e.format());
        }
    }


    @RpcMethod("calcTransferTxFee")
    @ApiOperation(description = "Calculate the transaction fee required for offline creation of transfer transactions", order = 351)
    @Parameters({
            @Parameter(parameterName = "TransferTxFeeDto", parameterDes = "Transfer transaction fees", requestType = @TypeDescriptor(value = TransferTxFeeDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Transaction fees"),
    }))
    public RpcResult calcTransferTxFee(List<Object> params) {
        int addressCount, fromLength, toLength;
        String remark, price;
        try {
            addressCount = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[addressCount] is inValid");
        }
        try {
            fromLength = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[fromLength] is inValid");
        }
        try {
            toLength = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[toLength] is inValid");
        }
        try {
            remark = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }
        try {
            price = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        TransferTxFeeDto dto = new TransferTxFeeDto();
        dto.setAddressCount(addressCount);
        dto.setFromLength(fromLength);
        dto.setToLength(toLength);
        dto.setRemark(remark);
        dto.setPrice(new BigInteger(price));
        BigInteger fee = NulsSDKTool.calcTransferTxFee(dto);
        Map map = new HashMap();
        map.put("value", fee.toString());

        return RpcResult.success(map);
    }

    @RpcMethod("calcCrossTxFee")
    @ApiOperation(description = "Calculate the transaction fee required for offline creation of cross chain transfer transactions", order = 351)
    @Parameters({
            @Parameter(parameterName = "TransferTxFeeDto", parameterDes = "Transfer transaction fees", requestType = @TypeDescriptor(value = TransferTxFeeDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Transaction fees"),
    }))
    public RpcResult calcCrossTxFee(List<Object> params) {
        int addressCount, fromLength, toLength;
        String remark;
        try {
            addressCount = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[addressCount] is inValid");
        }
        try {
            fromLength = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[fromLength] is inValid");
        }
        try {
            toLength = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[toLength] is inValid");
        }
        try {
            remark = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }

        CrossTransferTxFeeDto dto = new CrossTransferTxFeeDto();
        dto.setAddressCount(addressCount);
        dto.setFromLength(fromLength);
        dto.setToLength(toLength);
        dto.setRemark(remark);

        BigInteger fee = NulsSDKTool.calcCrossTransferNulsTxFee(dto);
        Map map = new HashMap();
        map.put("value", fee.toString());
        return RpcResult.success(map);
    }

    @RpcMethod("createMultiSignTransferTxOffline")
    @ApiOperation(description = "Offline assembly transfer transaction", order = 352, detailDesc = "according toinputsandoutputsOffline assembly transfer transaction, used for single account or multi account transfer transactions." +
            "The transaction fee isinputsThe total amount of main assets in the Li Ben Chain, minusoutputsThe total amount of main assets in the Li Ben Chain")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "Transfer transaction form", requestType = @TypeDescriptor(value = MultiSignTransferDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization16Hexadecimal Strings")
    }))
    public RpcResult createMultiSignTransferTxOffline(List<Object> params) {
        List<String> pubKeys;
        int minSigns;
        List<Map> inputList, outputList;
        String remark;
        List<CoinFromDto> froms = new ArrayList<>();
        List<CoinToDto> tos = new ArrayList<>();

        try {
            pubKeys = (List<String>) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        try {
            inputList = (List<Map>) params.get(2);
            for (Map map : inputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinFromDto fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
                froms.add(fromDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[inputList] is inValid");
        }
        try {
            outputList = (List<Map>) params.get(3);
            for (Map map : outputList) {
                String amount = map.get("amount").toString();
                map.put("amount", new BigInteger(amount));
                CoinToDto toDto = JSONUtils.map2pojo(map, CoinToDto.class);
                tos.add(toDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[outputList] is inValid");
        }
        try {
            remark = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }

        try {
            MultiSignTransferDto transferDto = new MultiSignTransferDto();
            transferDto.setPubKeys(pubKeys);
            transferDto.setMinSigns(minSigns);
            transferDto.setInputs(froms);
            transferDto.setOutputs(tos);
            transferDto.setRemark(remark);
            CommonValidator.checkMultiSignTransferDto(transferDto);
            io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignTransferTxOffline(transferDto);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result.getErrorCode(), result.getMsg());
            }
        } catch (NulsException e) {
            return RpcResult.failed(e.getErrorCode(), e.format());
        }
    }

    @RpcMethod("calcMultiSignTransferTxFee")
    @ApiOperation(description = "Calculate the transaction fee required for offline creation of transfer transactions", order = 353)
    @Parameters({
            @Parameter(parameterName = "MultiSignTransferTxFeeDto", parameterDes = "Transfer transaction fees", requestType = @TypeDescriptor(value = MultiSignTransferTxFeeDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Transaction fees"),
    }))
    public RpcResult calcMultiSignTransferTxFee(List<Object> params) {
        int pubKeyCount, fromLength, toLength;
        String remark, price;
        try {
            pubKeyCount = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[addressCount] is inValid");
        }
        try {
            fromLength = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[fromLength] is inValid");
        }
        try {
            toLength = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[toLength] is inValid");
        }
        try {
            remark = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[remark] is inValid");
        }
        try {
            price = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        MultiSignTransferTxFeeDto dto = new MultiSignTransferTxFeeDto();
        dto.setPubKeyCount(pubKeyCount);
        dto.setFromLength(fromLength);
        dto.setToLength(toLength);
        dto.setRemark(remark);
        dto.setPrice(new BigInteger(price));
        BigInteger fee = NulsSDKTool.calcMultiSignTransferTxFee(dto);
        Map map = new HashMap();
        map.put("value", fee);

        return RpcResult.success(map);
    }

}
