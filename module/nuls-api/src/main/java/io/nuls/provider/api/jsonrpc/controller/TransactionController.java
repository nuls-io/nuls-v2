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
package io.nuls.provider.api.jsonrpc.controller;

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
import io.nuls.provider.model.txdata.CallContractData;
import io.nuls.provider.model.txdata.CreateContractData;
import io.nuls.provider.model.txdata.DeleteContractData;
import io.nuls.provider.rpctools.ContractTools;
import io.nuls.provider.rpctools.TransactionTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;
import io.nuls.v2.model.dto.*;
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
    private TransactionTools transactionTools;
    @Autowired
    private ContractTools contractTools;

    TransferService transferService = ServiceManager.get(TransferService.class);

    @RpcMethod("getTx")
    @ApiOperation(description = "根据hash获取交易", order = 301)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "hash", parameterDes = "交易hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = TransactionDto.class))
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
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("validateTx")
    @ApiOperation(description = "验证交易", order = 302, detailDesc = "验证离线组装的交易,验证成功返回交易hash值,失败返回错误提示信息")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterDes = "交易序列化字符串"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
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
    @ApiOperation(description = "广播交易", order = 303, detailDesc = "广播离线组装的交易,成功返回true,失败返回错误提示信息")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterDes = "交易序列化16进制字符串"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功"),
            @Key(name = "hash", description = "交易hash")
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
            Result result = new Result();
            switch (type) {
                case CREATE_CONTRACT:
                    Transaction tx = new Transaction();
                    tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CreateContractData create = new CreateContractData();
                    create.parse(new NulsByteBuffer(tx.getTxData()));
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
                    result = contractTools.validateContractCall(chainId,
                            AddressTool.getStringAddressByBytes(call.getSender()),
                            call.getValue(),
                            call.getGasLimit(),
                            call.getPrice(),
                            AddressTool.getStringAddressByBytes(call.getContractAddress()),
                            call.getMethodName(),
                            call.getMethodDesc(),
                            call.getArgs());
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

    @RpcMethod("transfer")
    @ApiOperation(description = "单笔转账", order = 304, detailDesc = "发起单账户单资产的转账交易")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产id"),
            @Parameter(parameterName = "address", parameterDes = "转出账户地址"),
            @Parameter(parameterName = "toAddress", parameterDes = "转入账户地址"),
            @Parameter(parameterName = "password", parameterDes = "转出账户密码"),
            @Parameter(parameterName = "amount", parameterDes = "转出金额"),
            @Parameter(parameterName = "remark", parameterDes = "备注"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash")
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
                new TransferReq.TransferReqBuilder(chainId, assetId)
                        .addForm(address, password, new BigInteger(amount))
                        .addTo(toAddress, new BigInteger(amount)).setRemark(remark);
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        if (result.isSuccess()) {
            Map resultMap = new HashMap(2);
            resultMap.put("hash", result.getData());
            return RpcResult.success(resultMap);
        } else {
            return RpcResult.failed(ErrorCode.init(result.getStatus()), result.getMessage());
        }
    }

    @RpcMethod("createTransferTxOffline")
    @ApiOperation(description = "离线组装转账交易", order = 350, detailDesc = "根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。" +
            "交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "转账交易表单", requestType = @TypeDescriptor(value = TransferDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
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

    @RpcMethod("calcTransferTxFee")
    @ApiOperation(description = "计算离线创建转账交易所需手续费", order = 351)
    @Parameters({
            @Parameter(parameterName = "TransferTxFeeDto", parameterDes = "转账交易手续费", requestType = @TypeDescriptor(value = TransferTxFeeDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易手续费"),
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
        map.put("value", fee);

        return RpcResult.success(map);
    }

    @RpcMethod("createMultiSignTransferTxOffline")
    @ApiOperation(description = "离线组装转账交易", order = 352, detailDesc = "根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。" +
            "交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "转账交易表单", requestType = @TypeDescriptor(value = MultiSignTransferDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
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
    @ApiOperation(description = "计算离线创建转账交易所需手续费", order = 353)
    @Parameters({
            @Parameter(parameterName = "MultiSignTransferTxFeeDto", parameterDes = "转账交易手续费", requestType = @TypeDescriptor(value = MultiSignTransferTxFeeDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易手续费"),
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
