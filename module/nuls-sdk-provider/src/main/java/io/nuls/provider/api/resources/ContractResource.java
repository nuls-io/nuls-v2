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
package io.nuls.provider.api.resources;

import io.nuls.base.api.provider.contract.facade.*;
import io.nuls.provider.api.config.Config;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.contract.ContractProvider;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.*;
import io.nuls.provider.model.form.contract.*;
import io.nuls.provider.rpctools.ContractTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.util.NulsSDKTool;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/contract")
@Component
@Api
public class ContractResource {

    ContractProvider contractProvider = ServiceManager.get(ContractProvider.class);
    @Autowired
    ContractTools contractTools;
    @Autowired
    Config config;

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "发布合约",order = 401)
    @Parameters({
            @Parameter(parameterName = "发布合约", parameterDes = "发布合约表单", requestType = @TypeDescriptor(value = ContractCreate.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象，包含两个属性", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "发布合约的交易hash"),
            @Key(name = "contractAddress", description = "生成的合约地址")
    }))
    public RpcClientResult createContract(ContractCreate create) {
        if (create == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (create.getGasLimit() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("gasLimit [%s] is invalid", create.getGasLimit())));
        }
        if (create.getPrice() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("price [%s] is invalid", create.getPrice())));
        }
        CreateContractReq req = new CreateContractReq();
        req.setChainId(config.getChainId());
        req.setSender(create.getSender());
        req.setPassword(create.getPassword());
        req.setPrice(create.getPrice());
        req.setGasLimit(create.getGasLimit());
        req.setContractCode(create.getContractCode());
        req.setAlias(create.getAlias());
        req.setArgs(create.getArgs());
        req.setRemark(create.getRemark());
        Result<Map> result = contractProvider.createContract(req);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "调用合约", order = 402)
    @Parameters({
            @Parameter(parameterName = "调用合约", parameterDes = "调用合约表单", requestType = @TypeDescriptor(value = ContractCall.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "调用合约的交易hash")
    }))
    public RpcClientResult callContract(ContractCall call) {
        if (call == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (call.getValue() == null || call.getValue().compareTo(BigInteger.ZERO) < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "value is invalid"));
        }
        if (call.getGasLimit() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("gasLimit [%s] is invalid", call.getGasLimit())));
        }
        if (call.getPrice() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("price [%s] is invalid", call.getPrice())));
        }
        CallContractReq req = new CallContractReq();
        req.setChainId(config.getChainId());
        req.setSender(call.getSender());
        req.setPassword(call.getPassword());
        req.setPrice(call.getPrice());
        req.setGasLimit(call.getGasLimit());
        req.setValue(call.getValue().longValue());
        req.setMethodName(call.getMethodName());
        req.setMethodDesc(call.getMethodDesc());
        req.setContractAddress(call.getContractAddress());
        req.setArgs(call.getArgs());
        req.setRemark(call.getRemark());
        Result<String> result = contractProvider.callContract(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            return clientResult.resultMap().map("txHash", clientResult.getData()).mapToData();
        }
        return clientResult;
    }


    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "删除合约", order = 403)
    @Parameters({
            @Parameter(parameterName = "删除合约", parameterDes = "删除合约表单", requestType = @TypeDescriptor(value = ContractDelete.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "删除合约的交易hash")
    }))
    public RpcClientResult deleteContract(ContractDelete delete) {
        if (delete == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        DeleteContractReq req = new DeleteContractReq(delete.getSender(), delete.getContractAddress(), delete.getPassword());
        req.setChainId(config.getChainId());
        req.setRemark(delete.getRemark());
        Result<String> result = contractProvider.deleteContract(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            return clientResult.resultMap().map("txHash", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/tokentransfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "合约token转账", order = 404)
    @Parameters({
            @Parameter(parameterName = "token转账", parameterDes = "token转账表单", requestType = @TypeDescriptor(value = ContractTokenTransfer.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "交易hash")
    }))
    public RpcClientResult tokentransfer(ContractTokenTransfer form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (form.getAmount() == null || form.getAmount().compareTo(BigInteger.ZERO) < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "amount is invalid"));
        }
        TokenTransferReq req = new TokenTransferReq();
        req.setChainId(config.getChainId());
        req.setAddress(form.getFromAddress());
        req.setPassword(form.getPassword());
        req.setToAddress(form.getToAddress());
        req.setContractAddress(form.getContractAddress());
        req.setAmount(form.getAmount().toString());
        req.setRemark(form.getRemark());
        Result<String> result = contractProvider.tokenTransfer(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            return clientResult.resultMap().map("txHash", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/transfer2contract")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "从账户地址向合约地址转账(主链资产)的合约交易", order = 405)
    @Parameters({
            @Parameter(parameterName = "向合约地址转账", parameterDes = "向合约地址转账表单", requestType = @TypeDescriptor(value = ContractTransfer.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "交易hash")
    }))
    public RpcClientResult transferTocontract(ContractTransfer form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (form.getAmount() == null || form.getAmount().compareTo(BigInteger.ZERO) < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "amount is invalid"));
        }
        TransferToContractReq req = new TransferToContractReq(
                form.getFromAddress(),
                form.getToAddress(),
                form.getAmount(),
                form.getPassword(),
                form.getRemark());
        req.setChainId(config.getChainId());
        Result<String> result = contractProvider.transferToContract(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            return clientResult.resultMap().map("txHash", clientResult.getData()).mapToData();
        }
        return clientResult;
    }


    @GET
    @Path("/balance/token/{contractAddress}/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取账户地址的指定合约的token余额", order = 406)
    @Parameters({
            @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
            @Parameter(parameterName = "address", parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractTokenInfoDto.class))
    public RpcClientResult getTokenBalance(@PathParam("contractAddress") String contractAddress, @PathParam("address") String address) {
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        Result<ContractTokenInfoDto> result = contractTools.getTokenBalance(config.getChainId(), contractAddress, address);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @GET
    @Path("/info/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取智能合约详细信息", order = 407)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "合约地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractInfoDto.class))
    public RpcClientResult getContractDetailInfo(@PathParam("address") String address) {
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        Result<Map> result = contractTools.getContractInfo(config.getChainId(), address);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @GET
    @Path("/result/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取智能合约执行结果", order = 408)
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "交易hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractResultDto.class))
    public RpcClientResult getContractResult(@PathParam("hash") String hash) {
        if (hash == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is empty"));
        }
        Result<Map> result = contractTools.getContractResult(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }


    @POST
    @Path("/constructor")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取合约代码构造函数", order = 409)
    @Parameters({
        @Parameter(parameterName = "获取合约代码构造函数", parameterDes = "获取合约代码构造函数表单", requestType = @TypeDescriptor(value = ContractCode.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractConstructorInfoDto.class))
    public RpcClientResult getContractConstructor(ContractCode form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        Result<Map> result = contractTools.getContractConstructor(config.getChainId(), form.getContractCode());
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }


    @POST
    @Path("/method")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取已发布合约指定函数的信息", order = 410)
    @Parameters({
        @Parameter(parameterName = "获取已发布合约指定函数的信息", parameterDes = "获取已发布合约指定函数的信息表单", requestType = @TypeDescriptor(value = ContractMethodForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ProgramMethod.class))
    public RpcClientResult getContractMethod(ContractMethodForm form) {
        int chainId = config.getChainId();
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (!AddressTool.validAddress(chainId, form.getContractAddress())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("contractAddress [%s] is invalid", form.getContractAddress())));
        }
        if (StringUtils.isBlank(form.getMethodName())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "methodName is empty"));
        }
        Result<Map> contractInfoDtoResult = contractTools.getContractInfo(chainId, form.getContractAddress());
        if(contractInfoDtoResult.isFailed()) {
            return ResultUtil.getRpcClientResult(contractInfoDtoResult);
        }
        Map contractInfo = contractInfoDtoResult.getData();
        String methodName = form.getMethodName();
        String methodDesc = form.getMethodDesc();
        List<Map<String, Object>> methods =(List<Map<String, Object>>) contractInfo.get("method");
        Map resultMethod = null;
        boolean isEmptyMethodDesc = StringUtils.isBlank(methodDesc);
        for (Map<String, Object> method : methods) {
            if (methodName.equals(method.get("name"))) {
                if (isEmptyMethodDesc) {
                    resultMethod = method;
                    break;
                } else if (methodDesc.equals(method.get("desc"))) {
                    resultMethod = method;
                    break;
                }
            }
        }
        if (resultMethod == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_NOT_FOUND));
        }
        return RpcClientResult.getSuccess(resultMethod);
    }


    @POST
    @Path("/method/argstypes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取已发布合约指定函数的参数类型列表", order = 411)
    @Parameters({
        @Parameter(parameterName = "获取已发布合约指定函数的参数类型列表", parameterDes = "获取已发布合约指定函数的参数类型表单", requestType = @TypeDescriptor(value = ContractMethodForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcClientResult getContractMethodArgsTypes(ContractMethodForm form) {
        RpcClientResult clientResult = this.getContractMethod(form);
        if(clientResult.isFailed()) {
            return clientResult;
        }
        Map resultMethod = (Map) clientResult.getData();
        if (resultMethod == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_NOT_FOUND));
        }
        List<String> argsTypes;
        try {
            List<Map<String, Object>> args = (List<Map<String, Object>>) resultMethod.get("args");
            argsTypes = new ArrayList<>();
            for (Map<String, Object> arg : args) {
                argsTypes.add((String) arg.get("type"));
            }
            return RpcClientResult.getSuccess(argsTypes);
        } catch (Exception e) {
            Log.error(e);
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage()));
        }
    }


    @POST
    @Path("/validate/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "验证发布合约", order = 412)
    @Parameters(value = {
        @Parameter(parameterName = "验证发布合约", parameterDes = "验证发布合约表单", requestType = @TypeDescriptor(value = ContractValidateCreate.class))
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "验证成功与否"),
        @Key(name = "code", description = "验证失败的错误码"),
        @Key(name = "msg", description = "验证失败的错误信息")
    }))
    public RpcClientResult validateContractCreate(ContractValidateCreate form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (form.getGasLimit() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("gasLimit [%s] is invalid", form.getGasLimit())));
        }
        if (form.getPrice() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("price [%s] is invalid", form.getPrice())));
        }
        Result<Map> mapResult = contractTools.validateContractCreate(config.getChainId(),
                form.getSender(),
                form.getGasLimit(),
                form.getPrice(),
                form.getContractCode(),
                form.getArgs());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/validate/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "验证调用合约", order = 413)
    @Parameters(value = {
        @Parameter(parameterName = "验证调用合约", parameterDes = "验证调用合约表单", requestType = @TypeDescriptor(value = ContractValidateCall.class))
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "验证成功与否"),
        @Key(name = "code", description = "验证失败的错误码"),
        @Key(name = "msg", description = "验证失败的错误信息")
    }))
    public RpcClientResult validateContractCall(ContractValidateCall form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        if (form.getGasLimit() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("gasLimit [%s] is invalid", form.getGasLimit())));
        }
        if (form.getPrice() < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), String.format("price [%s] is invalid", form.getPrice())));
        }
        Result<Map> mapResult = contractTools.validateContractCall(config.getChainId(),
                form.getSender(),
                form.getValue(),
                form.getGasLimit(),
                form.getPrice(),
                form.getContractAddress(),
                form.getMethodName(),
                form.getMethodDesc(),
                form.getArgs());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/validate/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "验证删除合约", order = 414)
    @Parameters(value = {
        @Parameter(parameterName = "验证删除合约", parameterDes = "验证删除合约表单", requestType = @TypeDescriptor(value = ContractValidateDelete.class))
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "验证成功与否"),
        @Key(name = "code", description = "验证失败的错误码"),
        @Key(name = "msg", description = "验证失败的错误信息")
    }))
    public RpcClientResult validateContractDelete(ContractValidateDelete form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        Result<Map> mapResult = contractTools.validateContractDelete(config.getChainId(),
                form.getSender(),
                form.getContractAddress());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/imputedgas/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "估算发布合约交易的GAS", order = 415)
    @Parameters(value = {
        @Parameter(parameterName = "估算发布合约交易的GAS", parameterDes = "估算发布合约交易的GAS表单", requestType = @TypeDescriptor(value = ImputedGasContractCreate.class))
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "消耗的gas值，执行失败返回数值1")
    }))
    public RpcClientResult imputedContractCreateGas(ImputedGasContractCreate form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        Result<Map> mapResult = contractTools.imputedContractCreateGas(config.getChainId(),
                form.getSender(),
                form.getContractCode(),
                form.getArgs());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/imputedgas/call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "估算调用合约交易的GAS", order = 416)
    @Parameters(value = {
        @Parameter(parameterName = "估算调用合约交易的GAS", parameterDes = "估算调用合约交易的GAS表单", requestType = @TypeDescriptor(value = ImputedGasContractCall.class))
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "消耗的gas值，执行失败返回数值1")
    }))
    public RpcClientResult imputedContractCallGas(ImputedGasContractCall form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        Result<Map> mapResult = contractTools.imputedContractCallGas(config.getChainId(),
                form.getSender(),
                form.getValue(),
                form.getContractAddress(),
                form.getMethodName(),
                form.getMethodDesc(),
                form.getArgs());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/view")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "调用合约不上链方法", order = 417)
    @Parameters(value = {
        @Parameter(parameterName = "调用合约不上链方法", parameterDes = "调用合约不上链方法表单", requestType = @TypeDescriptor(value = ContractViewCall.class))
    })
    @ResponseData(name = "返回值", description = "返回Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "result", description = "视图方法的调用结果")
    }))
    public RpcClientResult invokeView(ContractViewCall form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        Result<Map> mapResult = contractTools.invokeView(config.getChainId(),
                form.getContractAddress(),
                form.getMethodName(),
                form.getMethodDesc(),
                form.getArgs());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/create/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 发布合约的交易", order = 450)
    @Parameters(value = {
        @Parameter(parameterName = "发布合约离线交易", parameterDes = "发布合约离线交易表单", requestType = @TypeDescriptor(value = ContractCreateOffline.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "交易hash"),
        @Key(name = "txHex", description = "交易序列化字符串"),
        @Key(name = "contractAddress", description = "生成的合约地址")
    }))
    public RpcClientResult createTxOffline(ContractCreateOffline form) {
        io.nuls.core.basic.Result<Map> result = NulsSDKTool.createContractTxOffline(
                form.getSender(),
                form.getSenderBalance(),
                form.getNonce(),
                form.getAlias(),
                form.getContractCode(),
                form.getGasLimit(),
                form.getArgs(),
                form.getArgsType(),
                form.getRemark());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/call/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 调用合约的交易", order = 451)
    @Parameters(value = {
        @Parameter(parameterName = "调用合约离线交易", parameterDes = "调用合约离线交易表单", requestType = @TypeDescriptor(value = ContractCallOffline.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "交易hash"),
        @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcClientResult callTxOffline(ContractCallOffline form) {
        io.nuls.core.basic.Result<Map> result = NulsSDKTool.callContractTxOffline(
                form.getSender(),
                form.getSenderBalance(),
                form.getNonce(),
                form.getValue(),
                form.getContractAddress(),
                form.getGasLimit(),
                form.getMethodName(),
                form.getMethodDesc(),
                form.getArgs(),
                form.getArgsType(),
                form.getRemark());
        return ResultUtil.getRpcClientResult(result);
    }


    @POST
    @Path("/delete/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 删除合约交易", order = 452)
    @Parameters(value = {
        @Parameter(parameterName = "删除合约离线交易", parameterDes = "删除合约离线交易表单", requestType = @TypeDescriptor(value = ContractDeleteOffline.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "交易hash"),
        @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcClientResult deleteTxOffline(ContractDeleteOffline form) {
        io.nuls.core.basic.Result<Map> result = NulsSDKTool.deleteContractTxOffline(
                form.getSender(),
                form.getSenderBalance(),
                form.getNonce(),
                form.getContractAddress(),
                form.getRemark());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/tokentransfer/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 合约token转账交易", order = 453)
    @Parameters(value = {
        @Parameter(parameterName = "token转账离线交易", parameterDes = "token转账离线交易表单", requestType = @TypeDescriptor(value = ContractTokenTransferOffline.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "交易hash"),
        @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcClientResult tokenTransferOffline(ContractTokenTransferOffline form) {
        io.nuls.core.basic.Result<Map> result = NulsSDKTool.tokenTransferTxOffline(
                form.getFromAddress(),
                form.getSenderBalance(),
                form.getNonce(),
                form.getToAddress(),
                form.getContractAddress(),
                form.getGasLimit(),
                form.getAmount(),
                form.getRemark());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/transfer2contract/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 从账户地址向合约地址转账(主链资产)的合约交易", order = 454)
    @Parameters(value = {
        @Parameter(parameterName = "向合约地址转账离线交易", parameterDes = "向合约地址转账离线交易表单", requestType = @TypeDescriptor(value = ContractTransferOffline.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "交易hash"),
        @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcClientResult transferToContractOffline(ContractTransferOffline form) {
        io.nuls.core.basic.Result<Map> result = NulsSDKTool.transferToContractTxOffline(
                form.getFromAddress(),
                form.getSenderBalance(),
                form.getNonce(),
                form.getToAddress(),
                form.getGasLimit(),
                form.getAmount(),
                form.getRemark());
        return ResultUtil.getRpcClientResult(result);
    }
}
