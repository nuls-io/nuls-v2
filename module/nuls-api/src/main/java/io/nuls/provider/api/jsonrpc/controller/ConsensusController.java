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

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.provider.model.dto.DepositInfoDto;
import io.nuls.provider.model.dto.RandomSeedDTO;
import io.nuls.provider.model.form.consensus.CreateAgentForm;
import io.nuls.provider.model.form.consensus.DepositForm;
import io.nuls.provider.model.form.consensus.StopAgentForm;
import io.nuls.provider.model.form.consensus.WithdrawForm;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.ConsensusTools;
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
    @Autowired
    private ConsensusTools consensusTools;

    @RpcMethod("createAgent")
    @ApiOperation(description = "Create consensus nodes", order = 501)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "CreateAgentForm", parameterDes = "Create consensus node form", requestType = @TypeDescriptor(value = CreateAgentForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
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
    @ApiOperation(description = "Unregister consensus node", order = 502)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "StopAgentForm", parameterDes = "Unregister Consensus Node Form", requestType = @TypeDescriptor(value = StopAgentForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
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

    @RpcMethod("stopAgentCoinData")
    @ApiOperation(description = "Obtain the consensus node for deregistrationcoindata", order = 502)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "agentHash", parameterDes = "nodehash", requestType = @TypeDescriptor(value = String.class))
    })
    @ResponseData(name = "Return value", description = "Return aCoinData", responseType = @TypeDescriptor(value = String.class))
    public RpcResult getStopAgentCoinData(List<Object> params) {
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

        GetStopAgentCoinDataReq req = new GetStopAgentCoinDataReq(agentHash,1L);
        req.setChainId(chainId);
        Result<String> result = consensusProvider.getStopAgentCoinData(req);
        RpcResult rpcResult = ResultUtil.getJsonRpcResult(result);
        return rpcResult;
    }

    @RpcMethod("depositToAgent")
    @ApiOperation(description = "Entrusting participation in consensus", order = 503)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "DepositForm", parameterDes = "Delegated Participation Consensus Form", requestType = @TypeDescriptor(value = DepositForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
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
    @ApiOperation(description = "Exit consensus", order = 504)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "WithdrawForm", parameterDes = "Exit consensus form", requestType = @TypeDescriptor(value = WithdrawForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
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
    @ApiOperation(description = "Query the delegation consensus list of nodes", order = 505)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "agentHash", parameterDes = "Create transactions for consensus nodeshash")
    })
    @ResponseData(name = "Return value", description = "Return the delegate consensus set", responseType = @TypeDescriptor(value = List.class, collectionElement = DepositInfoDto.class))
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

    @RpcMethod("getRandomSeedByCount")
    @ApiOperation(description = "Generate a random seed based on the maximum height and the number of original seeds and return it", order = 506, detailDesc = "Including maximum height backwards1000Find a specified number of original seeds within this block interval, aggregate them to generate a random seed, and return it")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "Maximum height"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "Original number of seeds"),
            @Parameter(parameterName = "algorithm", parameterDes = "Algorithm identification：SHA3, KECCAK, MERKLE", canNull = true)
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public RpcResult getRandomSeedByCount(List<Object> params) {
        int chainId;
        long height;
        int count;
        String algorithm = null;
        try {
            chainId = Integer.parseInt(params.get(0).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            height = Long.parseLong(params.get(1).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[height] is inValid");
        }
        try {
            count = Integer.parseInt(params.get(2).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[count] is inValid");
        }
        if(params.size() > 3) {
            try {
                algorithm = params.get(3).toString();
            } catch (Exception e) {
                return RpcResult.paramError("[algorithm] is inValid");
            }
        }
        try {
            Map resultMap = consensusTools.getRandomSeedByCount(chainId, height, count, algorithm);
            return RpcResult.success(resultMap);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionJsonRpcResult(e);
        }
    }

    @RpcMethod("getRandomSeedByHeight")
    @ApiOperation(description = "Generate a random seed based on the height interval and return it", order = 507, detailDesc = "Find all valid original seeds within this block interval, summarize them to generate a random seed, and return it")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "startHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "Starting height"),
            @Parameter(parameterName = "endHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "Cut-off height"),
            @Parameter(parameterName = "algorithm", parameterDes = "Algorithm identification：SHA3, KECCAK, MERKLE", canNull = true)
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public RpcResult getRandomSeedByHeight(List<Object> params) {
        int chainId;
        long startHeight;
        long endHeight;
        String algorithm = null;
        try {
            chainId = Integer.parseInt(params.get(0).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            startHeight = Long.parseLong(params.get(1).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[startHeight] is inValid");
        }
        try {
            endHeight = Long.parseLong(params.get(2).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[endHeight] is inValid");
        }
        if(params.size() > 3) {
            try {
                algorithm = params.get(3).toString();
            } catch (Exception e) {
                return RpcResult.paramError("[algorithm] is inValid");
            }
        }
        try {
            Map resultMap = consensusTools.getRandomSeedByHeight(chainId, startHeight, endHeight, algorithm);
            return RpcResult.success(resultMap);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionJsonRpcResult(e);
        }
    }

    @RpcMethod("getRandomRawSeedsByCount")
    @ApiOperation(description = "Find the original seed list based on the maximum height and the number of original seeds, and return it", order = 508, detailDesc = "Including maximum height backwards1000Find a specified number of original seeds within this block interval and return them")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "Maximum height"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "Original number of seeds")
    })
    @ResponseData(name = "Original Seed List", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcResult getRandomRawSeedsByCount(List<Object> params) {
        int chainId;
        long height;
        int count;
        try {
            chainId = Integer.parseInt(params.get(0).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            height = Long.parseLong(params.get(1).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[height] is inValid");
        }
        try {
            count = Integer.parseInt(params.get(2).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[count] is inValid");
        }
        try {
            List<String> resultList = consensusTools.getRandomRawSeedsByCount(chainId, height, count);
            return RpcResult.success(resultList);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionJsonRpcResult(e);
        }
    }

    @RpcMethod("getRandomRawSeedsByHeight")
    @ApiOperation(description = "Search for the original seed list based on the height interval and return it", order = 509, detailDesc = "Find all valid original seeds within this block interval and return them")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "startHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "Starting height"),
            @Parameter(parameterName = "endHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "Cut-off height")
    })
    @ResponseData(name = "Original Seed List", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcResult getRandomRawSeedsByHeight(List<Object> params) {
        int chainId;
        long startHeight;
        long endHeight;
        try {
            chainId = Integer.parseInt(params.get(0).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            startHeight = Long.parseLong(params.get(1).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[startHeight] is inValid");
        }
        try {
            endHeight = Long.parseLong(params.get(2).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[endHeight] is inValid");
        }
        try {
            List<String> resultList = consensusTools.getRandomRawSeedsByHeight(chainId, startHeight, endHeight);
            return RpcResult.success(resultList);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionJsonRpcResult(e);
        }
    }

    @RpcMethod("createAgentOffline")
    @ApiOperation(description = "Offline assembly - Create consensus nodes", order = 550, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "ConsensusDto", parameterDes = "Offline creation of consensus node form", requestType = @TypeDescriptor(value = ConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Unregister consensus node", order = 551, detailDesc = "Assembly transactionsStopDepositDtoInformation can be obtained by querying the delegated consensus list of nodes,inputofnonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "Offline logout consensus node form", requestType = @TypeDescriptor(value = StopConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Entrusting participation in consensus", order = 552, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "DepositDto", parameterDes = "Offline Delegation Participation Consensus Form", requestType = @TypeDescriptor(value = DepositDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Exit consensus", order = 553)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "WithDrawDto", parameterDes = "Offline exit consensus form", requestType = @TypeDescriptor(value = WithDrawDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Create consensus nodes by signing multiple accounts", order = 554, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "MultiSignConsensusDto", parameterDes = "Offline creation of consensus node form for multiple account signatures", requestType = @TypeDescriptor(value = MultiSignConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Consensus node for account cancellation with multiple signatures", order = 555, detailDesc = "Assembly transactionsStopDepositDtoInformation can be obtained by querying the delegated consensus list of nodes,inputofnonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "MultiSignStopConsensusDto", parameterDes = "Multiple account offline cancellation consensus node form", requestType = @TypeDescriptor(value = MultiSignStopConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Multiple account delegation participation consensus", order = 556, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "MultiSignDepositDto", parameterDes = "Multiple account offline delegation participation consensus form", requestType = @TypeDescriptor(value = MultiSignDepositDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
    @ApiOperation(description = "Offline assembly - Consensus on account exit with multiple signatures", order = 557)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "MultiSignWithDrawDto", parameterDes = "Multiple account offline exit consensus form with multiple signatures", requestType = @TypeDescriptor(value = MultiSignWithDrawDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
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
