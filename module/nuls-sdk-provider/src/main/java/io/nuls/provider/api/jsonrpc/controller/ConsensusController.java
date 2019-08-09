/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.dto.DepositInfoDto;
import io.nuls.provider.model.form.consensus.CreateAgentForm;
import io.nuls.provider.model.form.consensus.DepositForm;
import io.nuls.provider.model.form.consensus.StopAgentForm;
import io.nuls.provider.model.form.consensus.WithdrawForm;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;
import io.nuls.v2.model.dto.*;
import io.nuls.v2.util.NulsSDKTool;
import io.nuls.v2.util.ValidateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class ConsensusController {

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @RpcMethod("createAgent")
    @ApiOperation(description = "创建共识节点", order = 501)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "CreateAgentForm", parameterDes = "创建共识节点表单", requestType = @TypeDescriptor(value = CreateAgentForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcResult createAgent(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, commissionRate;
        String agentAddress, packingAddress, rewardAddress, deposit, password;

        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentAddress = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        try {
            packingAddress = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        try {
            rewardAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        try {
            commissionRate = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[commissionRate] is inValid");
        }
        try {
            deposit = params.get(5).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            password = (String) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, packingAddress)) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, rewardAddress)) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }

        CreateAgentReq req = new CreateAgentReq(agentAddress, packingAddress, rewardAddress, commissionRate, new BigInteger(deposit), password);
        req.setChainId(chainId);
        Result<String> result = consensusProvider.createAgent(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("stopAgent")
    @ApiOperation(description = "注销共识节点", order = 502)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "StopAgentForm", parameterDes = "注销共识节点表单", requestType = @TypeDescriptor(value = StopAgentForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcResult stopAgent(List<Object> params) {
        int chainId;
        String address, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        StopAgentReq req = new StopAgentReq(address, password);
        req.setChainId(chainId);
        Result<String> result = consensusProvider.stopAgent(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("depositToAgent")
    @ApiOperation(description = "委托参与共识", order = 503)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "DepositForm", parameterDes = "委托参与共识表单", requestType = @TypeDescriptor(value = DepositForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcResult depositToAgent(List<Object> params) {
        int chainId;
        String address, agentHash, deposit, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            agentHash = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        try {
            deposit = params.get(3).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            password = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        DepositToAgentReq req = new DepositToAgentReq(address, agentHash, new BigInteger(deposit), password);
        req.setChainId(chainId);
        Result<String> result = consensusProvider.depositToAgent(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("withdraw")
    @ApiOperation(description = "退出共识", order = 504)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "WithdrawForm", parameterDes = "退出共识表单", requestType = @TypeDescriptor(value = WithdrawForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcResult withdraw(List<Object> params) {
        int chainId;
        String address, txHash, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            txHash = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[txHash] is inValid");
        }
        try {
            password = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(txHash)) {
            return RpcResult.paramError("[txHash] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        WithdrawReq req = new WithdrawReq(address, txHash, password);
        req.setChainId(chainId);
        Result<String> result = consensusProvider.withdraw(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("getDepositList")
    @ApiOperation(description = "查询节点的委托共识列表", order = 505)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "agentHash", parameterDes = "创建共识节点的交易hash")
    })
    @ResponseData(name = "返回值", description = "返回委托共识集合", responseType = @TypeDescriptor(value = List.class, collectionElement = DepositInfoDto.class))
    public RpcResult getDepositList(List<Object> params) {
        int chainId;
        String agentHash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (!ValidateUtil.validHash(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }

        GetDepositListReq req = new GetDepositListReq();
        req.setChainId(chainId);
        req.setPageNumber(1);
        req.setPageSize(300);
        req.setAgentHash(agentHash);

        Result<DepositInfo> result = consensusProvider.getDepositList(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        if (result.isSuccess()) {
            List<DepositInfo> list = result.getList();
            if (list != null && !list.isEmpty()) {
                List<DepositInfoDto> dtoList = list.stream().map(info -> {
                    DepositInfoDto dto = new DepositInfoDto();
                    BeanCopierManager.beanCopier(info, dto);
                    return dto;
                }).collect(Collectors.toList());
                rpcResult.setResult(dtoList);
            }
        }
        return rpcResult;
    }

    @RpcMethod("createAgentOffline")
    @ApiOperation(description = "离线组装 - 创建共识节点", order = 550, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "ConsensusDto", parameterDes = "离线创建共识节点表单", requestType = @TypeDescriptor(value = ConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult createAgentOffline(List<Object> params) {
        String agentAddress, packingAddress, rewardAddress, deposit;
        int chainId, commissionRate;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentAddress = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        try {
            packingAddress = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        try {
            rewardAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        try {
            commissionRate = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[commissionRate] is inValid");
        }
        try {
            deposit = params.get(5).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            map = (Map) params.get(6);
            String amount = map.get("amount").toString();
            map.put("amount", new BigInteger(amount));
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, packingAddress)) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, rewardAddress)) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }

        ConsensusDto form = new ConsensusDto();
        form.setAgentAddress(agentAddress);
        form.setPackingAddress(packingAddress);
        form.setRewardAddress(rewardAddress);
        form.setDeposit(new BigInteger(deposit));
        form.setCommissionRate(commissionRate);
        form.setInput(fromDto);
        io.nuls.core.basic.Result result = NulsSDKTool.createConsensusTxOffline(form);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("stopAgentOffline")
    @ApiOperation(description = "离线组装 - 注销共识节点", order = 551, detailDesc = "组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "离线注销共识节点表单", requestType = @TypeDescriptor(value = StopConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult stopAgentOffline(List<Object> params) {
        int chainId;
        String agentHash, agentAddress, deposit, price;
        List<Map> mapList;
        List<StopDepositDto> depositDtoList = new ArrayList<>();
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        try {
            agentAddress = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        try {
            deposit = params.get(3).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            price = params.get(4).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        try {
            mapList = (List<Map>) params.get(5);
            for (Map map : mapList) {
                StopDepositDto depositDto = new StopDepositDto();
                depositDto.setDepositHash((String) map.get("depositHash"));
                Map inputMap = (Map) map.get("input");
                CoinFromDto fromDto = JSONUtils.map2pojo(inputMap, CoinFromDto.class);
                depositDto.setInput(fromDto);
                depositDtoList.add(depositDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[depositList] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        StopConsensusDto form = new StopConsensusDto();
        form.setAgentAddress(agentAddress);
        form.setAgentHash(agentHash);
        form.setDeposit(new BigInteger(deposit));
        form.setPrice(new BigInteger(price));
        form.setDepositList(depositDtoList);
        io.nuls.core.basic.Result result = NulsSDKTool.createStopConsensusTxOffline(form);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("depositToAgentOffline")
    @ApiOperation(description = "离线组装 - 委托参与共识", order = 552, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "DepositDto", parameterDes = "离线委托参与共识表单", requestType = @TypeDescriptor(value = DepositDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult depositToAgentOffline(List<Object> params) {
        int chainId;
        String address, agentHash, deposit;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            deposit = params.get(2).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            agentHash = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        try {
            map = (Map) params.get(4);
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        DepositDto depositDto = new DepositDto();
        depositDto.setAddress(address);
        depositDto.setAgentHash(agentHash);
        depositDto.setDeposit(new BigInteger(deposit));
        depositDto.setInput(fromDto);

        io.nuls.core.basic.Result result = NulsSDKTool.createDepositTxOffline(depositDto);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("withdrawOffline")
    @ApiOperation(description = "离线组装 - 退出共识", order = 553)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "WithDrawDto", parameterDes = "离线退出共识表单", requestType = @TypeDescriptor(value = WithDrawDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult withdrawOffline(List<Object> params) {
        int chainId;
        String address, depositHash, price;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            depositHash = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[depositHash] is inValid");
        }
        try {
            price = params.get(3).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        try {
            map = (Map) params.get(4);
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(depositHash)) {
            return RpcResult.paramError("[depositHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        WithDrawDto withDrawDto = new WithDrawDto();
        withDrawDto.setAddress(address);
        withDrawDto.setDepositHash(depositHash);
        withDrawDto.setPrice(new BigInteger(price));
        withDrawDto.setInput(fromDto);

        io.nuls.core.basic.Result result = NulsSDKTool.createWithdrawDepositTxOffline(withDrawDto);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("multiSignCreateAgentOffline")
    @ApiOperation(description = "离线组装 - 多签账户创建共识节点", order = 554, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "MultiSignConsensusDto", parameterDes = "多签账户离线创建共识节点表单", requestType = @TypeDescriptor(value = MultiSignConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult multiSignCreateAgentOffline(List<Object> params) {
        List<String> pubKeys;
        String agentAddress, packingAddress, rewardAddress, deposit;
        int chainId, commissionRate, minSigns;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentAddress = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        try {
            packingAddress = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        try {
            rewardAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        try {
            commissionRate = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[commissionRate] is inValid");
        }
        try {
            deposit = params.get(5).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            map = (Map) params.get(6);
            String amount = map.get("amount").toString();
            map.put("amount", new BigInteger(amount));
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        try {
            pubKeys = (List<String>) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(8);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, packingAddress)) {
            return RpcResult.paramError("[packingAddress] is inValid");
        }
        if (!AddressTool.validAddress(chainId, rewardAddress)) {
            return RpcResult.paramError("[rewardAddress] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }

        MultiSignConsensusDto form = new MultiSignConsensusDto();
        form.setPubKeys(pubKeys);
        form.setMinSigns(minSigns);
        form.setAgentAddress(agentAddress);
        form.setPackingAddress(packingAddress);
        form.setRewardAddress(rewardAddress);
        form.setDeposit(new BigInteger(deposit));
        form.setCommissionRate(commissionRate);
        form.setInput(fromDto);
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignConsensusTx(form);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("multiSignStopAgentOffline")
    @ApiOperation(description = "离线组装 - 多签账户注销共识节点", order = 555, detailDesc = "组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "MultiSignStopConsensusDto", parameterDes = "多签账户离线注销共识节点表单", requestType = @TypeDescriptor(value = MultiSignStopConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult multiSignStopAgentOffline(List<Object> params) {
        int chainId, minSigns;
        List<String> pubKeys;
        String agentHash, agentAddress, deposit, price;
        List<Map> mapList;
        List<StopDepositDto> depositDtoList = new ArrayList<>();
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            agentHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        try {
            agentAddress = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        try {
            deposit = params.get(3).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            price = params.get(4).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        try {
            mapList = (List<Map>) params.get(5);
            for (Map map : mapList) {
                StopDepositDto depositDto = new StopDepositDto();
                depositDto.setDepositHash((String) map.get("depositHash"));
                Map inputMap = (Map) map.get("input");
                CoinFromDto fromDto = JSONUtils.map2pojo(inputMap, CoinFromDto.class);
                depositDto.setInput(fromDto);
                depositDtoList.add(depositDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[depositList] is inValid");
        }
        try {
            pubKeys = (List<String>) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[agentAddress] is inValid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        MultiSignStopConsensusDto form = new MultiSignStopConsensusDto();
        form.setPubKeys(pubKeys);
        form.setMinSigns(minSigns);
        form.setAgentAddress(agentAddress);
        form.setAgentHash(agentHash);
        form.setDeposit(new BigInteger(deposit));
        form.setPrice(new BigInteger(price));
        form.setDepositList(depositDtoList);
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignStopConsensusTx(form);
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("multiSignDepositToAgentOffline")
    @ApiOperation(description = "离线组装 - 多签账户委托参与共识", order = 556, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "MultiSignDepositDto", parameterDes = "多签账户离线委托参与共识表单", requestType = @TypeDescriptor(value = MultiSignDepositDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult multiSignDepositToAgentOffline(List<Object> params) {
        int chainId, minSigns;
        List<String> pubKeys;
        String address, agentHash, deposit;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            deposit = params.get(2).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        try {
            agentHash = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        try {
            map = (Map) params.get(4);
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        try {
            pubKeys = (List<String>) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(deposit)) {
            return RpcResult.paramError("[deposit] is inValid");
        }
        MultiSignDepositDto depositDto = new MultiSignDepositDto();
        depositDto.setPubKeys(pubKeys);
        depositDto.setMinSigns(minSigns);
        depositDto.setAddress(address);
        depositDto.setAgentHash(agentHash);
        depositDto.setDeposit(new BigInteger(deposit));
        depositDto.setInput(fromDto);

        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignDepositTxOffline(depositDto);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("multiSignWithdrawOffline")
    @ApiOperation(description = "离线组装 - 多签账户退出共识", order = 557)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "MultiSignWithDrawDto", parameterDes = "多签账户离线退出共识表单", requestType = @TypeDescriptor(value = MultiSignWithDrawDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcResult multiSignWithdrawOffline(List<Object> params) {
        int chainId, minSigns;
        List<String> pubKeys;
        String address, depositHash, price;
        Map map;
        CoinFromDto fromDto;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            depositHash = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[depositHash] is inValid");
        }
        try {
            price = params.get(3).toString();
        } catch (Exception e) {
            return RpcResult.paramError("[price] is inValid");
        }
        try {
            map = (Map) params.get(4);
            fromDto = JSONUtils.map2pojo(map, CoinFromDto.class);
        } catch (Exception e) {
            return RpcResult.paramError("[input] is inValid");
        }
        try {
            pubKeys = (List<String>) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(6);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(depositHash)) {
            return RpcResult.paramError("[depositHash] is inValid");
        }
        if (!ValidateUtil.validateBigInteger(price)) {
            return RpcResult.paramError("[price] is inValid");
        }

        MultiSignWithDrawDto withDrawDto = new MultiSignWithDrawDto();
        withDrawDto.setPubKeys(pubKeys);
        withDrawDto.setMinSigns(minSigns);
        withDrawDto.setAddress(address);
        withDrawDto.setDepositHash(depositHash);
        withDrawDto.setPrice(new BigInteger(price));
        withDrawDto.setInput(fromDto);

        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignWithdrawDepositTxOffline(withDrawDto);
        return ResultUtil.getJsonRpcResult(result);
    }
}
