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
package io.nuls.provider.api.resources;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.contract.ContractProvider;
import io.nuls.base.api.provider.contract.facade.CreateContractReq;
import io.nuls.base.api.provider.contract.facade.DeleteContractReq;
import io.nuls.base.api.provider.contract.facade.TokenTransferReq;
import io.nuls.base.api.provider.contract.facade.TransferToContractReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.*;
import io.nuls.provider.model.form.contract.*;
import io.nuls.provider.rpctools.ContractTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.Utils;
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
    @ApiOperation(description = "Publish contract",order = 401)
    @Parameters({
            @Parameter(parameterName = "Publish contract", parameterDes = "Publish Contract Form", requestType = @TypeDescriptor(value = ContractCreate.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing two properties", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Transactions for publishing contractshash"),
            @Key(name = "contractAddress", description = "Generated contract address")
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
    @ApiOperation(description = "Call Contract", order = 402)
    @Parameters({
            @Parameter(parameterName = "Call Contract", parameterDes = "Call Contract Form", requestType = @TypeDescriptor(value = ContractCall.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Transaction calling contracthash")
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
        /*CallContractReq req = new CallContractReq();
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
        }*/
        Result<Map> mapResult = contractTools.contractCall(config.getChainId(),
                call.getSender(),
                call.getPassword(),
                call.getValue(),
                call.getGasLimit(),
                call.getPrice(),
                call.getContractAddress(),
                call.getMethodName(),
                call.getMethodDesc(),
                call.getArgs(),
                call.getRemark(),
                call.getMultyAssetValues()
        );
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Delete contract", order = 403)
    @Parameters({
            @Parameter(parameterName = "Delete contract", parameterDes = "Delete Contract Form", requestType = @TypeDescriptor(value = ContractDelete.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Delete transactions for contractshash")
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
    @ApiOperation(description = "contracttokenTransfer", order = 404)
    @Parameters({
            @Parameter(parameterName = "tokenTransfer", parameterDes = "tokenTransfer Form", requestType = @TypeDescriptor(value = ContractTokenTransfer.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "transactionhash")
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
    @ApiOperation(description = "Transfer from account address to contract address(Main chain assets)Contract transactions", order = 405)
    @Parameters({
            @Parameter(parameterName = "Transfer to the contracted address", parameterDes = "Transfer Form to Contract Address", requestType = @TypeDescriptor(value = ContractTransfer.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "transactionhash")
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
    @ApiOperation(description = "Obtain the specified contract for the account addresstokenbalance", order = 406)
    @Parameters({
            @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
            @Parameter(parameterName = "address", parameterDes = "Account address")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractTokenInfoDto.class))
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
    @ApiOperation(description = "Get detailed information about smart contracts", order = 407)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "Contract address")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractInfoDto.class))
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
    @ApiOperation(description = "Obtain the execution results of smart contracts", order = 408)
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "transactionhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractResultDto.class))
    public RpcClientResult getContractResult(@PathParam("hash") String hash) {
        if (hash == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is empty"));
        }
        Result<Map> result = contractTools.getContractResult(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @POST
    @Path("/result/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Obtain a list of smart contract execution results", order = 409)
    @Parameters({
            @Parameter(parameterName = "Obtain a list of smart contract execution results", parameterDes = "Get the smart contract execution result list form", requestType = @TypeDescriptor(value = ContractResultListForm.class))
    })
    @ResponseData(name = "Return value", description = "Return the list of contract execution results for the transaction", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash1 or hash2 or hash3...", valueType = ContractResultDto.class, description = "TradinghashIn the listhashValue askeyHerekey nameIt is dynamic")
    }))
    public RpcClientResult getContractResultList(ContractResultListForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        Result<Map> result = contractTools.getContractResultList(config.getChainId(), form.getHashList());
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }


    @POST
    @Path("/constructor")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Get contract code constructor", order = 410)
    @Parameters({
        @Parameter(parameterName = "Get contract code constructor", parameterDes = "Get Contract Code Constructor Form", requestType = @TypeDescriptor(value = ContractCode.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractConstructorInfoDto.class))
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
    @ApiOperation(description = "Obtain information on the specified function of the published contract", order = 411)
    @Parameters({
        @Parameter(parameterName = "Obtain information on the specified function of the published contract", parameterDes = "Get the information form for the specified function of the published contract", requestType = @TypeDescriptor(value = ContractMethodForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ProgramMethod.class))
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
    @ApiOperation(description = "Get a list of parameter types for specified functions in published contracts", order = 412)
    @Parameters({
        @Parameter(parameterName = "Get a list of parameter types for specified functions in published contracts", parameterDes = "Get the parameter type form for the function specified in the published contract", requestType = @TypeDescriptor(value = ContractMethodForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
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
    @ApiOperation(description = "Verify release contract", order = 413)
    @Parameters(value = {
        @Parameter(parameterName = "Verify release contract", parameterDes = "Verify and publish the contract form", requestType = @TypeDescriptor(value = ContractValidateCreate.class))
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "Verification success or failure"),
        @Key(name = "code", description = "Error code for verification failure"),
        @Key(name = "msg", description = "Error message for verification failure")
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
    @ApiOperation(description = "Verify Call Contract", order = 414)
    @Parameters(value = {
        @Parameter(parameterName = "Verify Call Contract", parameterDes = "Verify Call Contract Form", requestType = @TypeDescriptor(value = ContractValidateCall.class))
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "Verification success or failure"),
        @Key(name = "code", description = "Error code for verification failure"),
        @Key(name = "msg", description = "Error message for verification failure")
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
                form.getArgs(),
                form.getMultyAssetValues());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/validate/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Verify deletion of contract", order = 415)
    @Parameters(value = {
        @Parameter(parameterName = "Verify deletion of contract", parameterDes = "Verify Delete Contract Form", requestType = @TypeDescriptor(value = ContractValidateDelete.class))
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "success", valueType = boolean.class, description = "Verification success or failure"),
        @Key(name = "code", description = "Error code for verification failure"),
        @Key(name = "msg", description = "Error message for verification failure")
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
    @ApiOperation(description = "Estimate the release of contract transactionsGAS", order = 416)
    @Parameters(value = {
        @Parameter(parameterName = "Estimate the release of contract transactionsGAS", parameterDes = "Estimate the release of contract transactionsGASform", requestType = @TypeDescriptor(value = ImputedGasContractCreate.class))
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "ConsumablegasValue, return value for execution failure1")
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
    @ApiOperation(description = "Estimating the call to contract transactionsGAS", order = 417)
    @Parameters(value = {
        @Parameter(parameterName = "Estimating the call to contract transactionsGAS", parameterDes = "Estimating the call to contract transactionsGASform", requestType = @TypeDescriptor(value = ImputedGasContractCall.class))
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "ConsumablegasValue, return value for execution failure1")
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
                form.getArgs(),
                form.getMultyAssetValues());
        return ResultUtil.getRpcClientResult(mapResult);
    }


    @POST
    @Path("/view")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Call the contract not on chain method", order = 418)
    @Parameters(value = {
        @Parameter(parameterName = "Call the contract not on chain method", parameterDes = "Call contract non chain method form", requestType = @TypeDescriptor(value = ContractViewCall.class))
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "result", description = "The call result of the view method")
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

    @GET
    @Path("/codeHash/{contractAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Obtaining contractscodeHash", order = 419)
    @Parameters(value = {
        @Parameter(parameterName = "contractAddress", parameterDes = "contractAddress")
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "result", description = "ContractualcodeHash")
    }))
    public RpcClientResult codeHash(@PathParam("contractAddress") String contractAddress) {
        if (contractAddress == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "contractAddress data is empty"));
        }
        Result<Map> mapResult = contractTools.codeHash(config.getChainId(), contractAddress);
        return ResultUtil.getRpcClientResult(mapResult);
    }

    @GET
    @Path("/tx/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Get transaction details of smart contracts", order = 420)
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "transactionhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractTransactionDto.class))
    public RpcClientResult getContractTx(@PathParam("hash") String hash) {
        if (hash == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is empty"));
        }
        Result<Map> result = contractTools.getContractTx(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @POST
    @Path("/computeAddress")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Calculate contract address", order = 421)
    @Parameters(value = {
        @Parameter(parameterName = "Calculate contract address", parameterDes = "Calculate contract address", requestType = @TypeDescriptor(value = ContractComputeAddress.class))
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "result", description = "ContractualcodeHash")
    }))
    public RpcClientResult computeAddress(ContractComputeAddress form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form data is empty"));
        }
        Result<Map> mapResult = contractTools.computeAddress(config.getChainId(),
                form.getSender(), form.getCodeHash(), form.getSalt());
        return ResultUtil.getRpcClientResult(mapResult);
    }

    @GET
    @Path("/contractCode/{contractAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Obtaining contractscode", order = 422)
    @Parameters(value = {
            @Parameter(parameterName = "contractAddress", parameterDes = "contractAddress")
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "Contractualcode")
    }))
    public RpcClientResult contractCode(@PathParam("contractAddress") String contractAddress) {
        if (contractAddress == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "contractAddress data is empty"));
        }
        Result<Map> mapResult = contractTools.contractCode(config.getChainId(), contractAddress);
        return ResultUtil.getRpcClientResult(mapResult);
    }

    @POST
    @Path("/create/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Transactions for publishing contracts", order = 450)
    @Parameters(value = {
        @Parameter(parameterName = "Publish offline transactions for contracts", parameterDes = "Publish offline transaction forms for contracts", requestType = @TypeDescriptor(value = ContractCreateOffline.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "transactionhash"),
        @Key(name = "txHex", description = "Transaction serialization string"),
        @Key(name = "contractAddress", description = "Generated contract address")
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
    @ApiOperation(description = "Offline assembly - Transaction calling contract", order = 451)
    @Parameters(value = {
        @Parameter(parameterName = "Call contract offline transaction", parameterDes = "Call contract offline transaction form", requestType = @TypeDescriptor(value = ContractCallOffline.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "transactionhash"),
        @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult callTxOffline(ContractCallOffline form) {
        // Add multi asset transfer parameters
        String[][] multyAssetValues = form.getMultyAssetValues();
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
                form.getRemark(),
                Utils.multyAssetObjectArray(multyAssetValues));
        return ResultUtil.getRpcClientResult(result);
    }


    @POST
    @Path("/delete/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Delete contract transactions", order = 452)
    @Parameters(value = {
        @Parameter(parameterName = "Delete contract offline transaction", parameterDes = "Delete offline transaction form for contract", requestType = @TypeDescriptor(value = ContractDeleteOffline.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "transactionhash"),
        @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - contracttokenTransfer transaction", order = 453)
    @Parameters(value = {
        @Parameter(parameterName = "tokenOffline transfer transaction", parameterDes = "tokenTransfer Offline Transaction Form", requestType = @TypeDescriptor(value = ContractTokenTransferOffline.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "transactionhash"),
        @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Transfer from account address to contract address(Main chain assets)Contract transactions", order = 454)
    @Parameters(value = {
        @Parameter(parameterName = "Transfer offline transaction to contract address", parameterDes = "Transfer offline transaction form to contract address", requestType = @TypeDescriptor(value = ContractTransferOffline.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash", description = "transactionhash"),
        @Key(name = "txHex", description = "Transaction serialization string")
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
