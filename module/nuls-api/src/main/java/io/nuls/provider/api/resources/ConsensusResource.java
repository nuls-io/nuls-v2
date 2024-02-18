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
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.DepositInfoDto;
import io.nuls.provider.model.dto.RandomSeedDTO;
import io.nuls.provider.model.form.consensus.CreateAgentForm;
import io.nuls.provider.model.form.consensus.DepositForm;
import io.nuls.provider.model.form.consensus.StopAgentForm;
import io.nuls.provider.model.form.consensus.WithdrawForm;
import io.nuls.provider.model.form.consensus.*;
import io.nuls.provider.rpctools.ConsensusTools;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.dto.*;
import io.nuls.v2.util.NulsSDKTool;
import io.nuls.v2.util.ValidateUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/consensus")
@Component
@Api
public class ConsensusResource {

    @Autowired
    private Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Autowired
    private ConsensusTools consensusTools;

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Create consensus nodes", order = 501)
    @Parameters({
            @Parameter(parameterName = "CreateAgentForm", parameterDes = "Create consensus node form", requestType = @TypeDescriptor(value = CreateAgentForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
    }))
    public RpcClientResult createAgent(CreateAgentForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        CreateAgentReq req = new CreateAgentReq(
                form.getAgentAddress(),
                form.getPackingAddress(),
                form.getRewardAddress(),
                form.getCommissionRate(),
                new BigInteger(form.getDeposit()),
                form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = consensusProvider.createAgent(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/agent/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Unregister consensus node", order = 502)
    @Parameters({
            @Parameter(parameterName = "StopAgentForm", parameterDes = "Unregister Consensus Node Form", requestType = @TypeDescriptor(value = StopAgentForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
    }))
    public RpcClientResult stopAgent(StopAgentForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        StopAgentReq req = new StopAgentReq(
                form.getAddress(),
                form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = consensusProvider.stopAgent(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/agent/stopCoinData")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Unregister consensus nodecoinData", order = 502)
    @Parameters({
            @Parameter(parameterName = "StopAgentForm", parameterDes = "Unregister Consensus Node Form", requestType = @TypeDescriptor(value = StopAgentForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = String.class))
    public RpcClientResult getStopAgentCoinData(GetStopAgentCoinDataForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        GetStopAgentCoinDataReq req = new GetStopAgentCoinDataReq(
                form.getAgentHash(), 1L);
        req.setChainId(config.getChainId());
        Result<String> result = consensusProvider.getStopAgentCoinData(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Entrusting participation in consensus", order = 503)
    @Parameters({
            @Parameter(parameterName = "DepositForm", parameterDes = "Delegated Participation Consensus Form", requestType = @TypeDescriptor(value = DepositForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
    }))
    public RpcClientResult depositToAgent(DepositForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        DepositToAgentReq req = new DepositToAgentReq(
                form.getAddress(),
                form.getAgentHash(),
                new BigInteger(form.getDeposit()),
                form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = consensusProvider.depositToAgent(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Exit consensus", order = 504)
    @Parameters({
            @Parameter(parameterName = "Exit consensus", parameterDes = "Exit consensus form", requestType = @TypeDescriptor(value = WithdrawForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
    }))
    public RpcClientResult withdraw(WithdrawForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        WithdrawReq req = new WithdrawReq(
                form.getAddress(),
                form.getTxHash(),
                form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = consensusProvider.withdraw(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @GET
    @Path("/list/deposit/{agentHash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query the delegation consensus list of nodes", order = 505)
    @Parameters({
            @Parameter(parameterName = "agentHash", parameterDes = "Create transactions for consensus nodeshash")
    })
    @ResponseData(name = "Return value", description = "Return the delegate consensus set", responseType = @TypeDescriptor(value = List.class, collectionElement = DepositInfoDto.class))
    public RpcClientResult getDepositList(@PathParam("agentHash") String agentHash) {
        if (!ValidateUtil.validHash(agentHash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "agentHash is invalid"));
        }
        GetDepositListReq req = new GetDepositListReq();
        req.setChainId(config.getChainId());
        req.setPageNumber(1);
        req.setPageSize(300);
        req.setAgentHash(agentHash);

        Result<DepositInfo> result = consensusProvider.getDepositList(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (result.isSuccess()) {
            List<DepositInfo> list = result.getList();
            if (list != null && !list.isEmpty()) {
                List<DepositInfoDto> dtoList = list.stream().map(info -> {
                    DepositInfoDto dto = new DepositInfoDto();
                    BeanCopierManager.beanCopier(info, dto);
                    return dto;
                }).collect(Collectors.toList());
                clientResult.setData(dtoList);
            }
        }
        return clientResult;
    }

    @POST
    @Path("/random/seed/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Generate a random seed based on the maximum height and the number of original seeds and return it", order = 506, detailDesc = "Including maximum height backwards1000Find a specified number of original seeds within this block interval, aggregate them to generate a random seed, and return it")
    @Parameters(value = {
            @Parameter(parameterName = "RandomSeedCountForm", parameterDes = "Random Seed Form", requestType = @TypeDescriptor(value = RandomSeedCountForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public RpcClientResult getRandomSeedByCount(RandomSeedCountForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        try {
            Map resultMap = consensusTools.getRandomSeedByCount(config.getChainId(), form.getHeight(), form.getCount(), form.getAlgorithm());
            return RpcClientResult.getSuccess(resultMap);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionRpcClientResult(e);
        }
    }

    @POST
    @Path("/random/seed/height")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Generate a random seed based on the height interval and return it", order = 507, detailDesc = "Find all valid original seeds within this block interval, summarize them to generate a random seed, and return it")
    @Parameters(value = {
            @Parameter(parameterName = "RandomSeedHeightForm", parameterDes = "Random Seed Form", requestType = @TypeDescriptor(value = RandomSeedHeightForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public RpcClientResult getRandomSeedByHeight(RandomSeedHeightForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        try {
            Map resultMap = consensusTools.getRandomSeedByHeight(config.getChainId(), form.getStartHeight(), form.getEndHeight(), form.getAlgorithm());
            return RpcClientResult.getSuccess(resultMap);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionRpcClientResult(e);
        }
    }

    @POST
    @Path("/random/rawseed/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Find the original seed list based on the maximum height and the number of original seeds, and return it", order = 508, detailDesc = "Including maximum height backwards1000Find a specified number of original seeds within this block interval and return them")
    @Parameters(value = {
            @Parameter(parameterName = "RandomRawSeedCountForm", parameterDes = "Original random seed form", requestType = @TypeDescriptor(value = RandomRawSeedCountForm.class))
    })
    @ResponseData(name = "Original Seed List", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcClientResult getRandomRawSeedsByCount(RandomRawSeedCountForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        try {
            List<String> resultList = consensusTools.getRandomRawSeedsByCount(config.getChainId(), form.getHeight(), form.getCount());
            return RpcClientResult.getSuccess(resultList);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionRpcClientResult(e);
        }
    }

    @POST
    @Path("/random/rawseed/height")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Search for the original seed list based on the height interval and return it", order = 509, detailDesc = "Find all valid original seeds within this block interval and return them")
    @Parameters(value = {
            @Parameter(parameterName = "RandomRawSeedHeightForm", parameterDes = "Original random seed form", requestType = @TypeDescriptor(value = RandomRawSeedHeightForm.class))
    })
    @ResponseData(name = "Original Seed List", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcClientResult getRandomRawSeedsByHeight(RandomRawSeedHeightForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        try {
            List<String> resultList = consensusTools.getRandomRawSeedsByHeight(config.getChainId(), form.getStartHeight(), form.getEndHeight());
            return RpcClientResult.getSuccess(resultList);
        } catch (NulsRuntimeException e) {
            return ResultUtil.getNulsRuntimeExceptionRpcClientResult(e);
        }
    }

    @POST
    @Path("/agent/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Create consensus node transactions", order = 550, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "ConsensusDto", parameterDes = "Offline creation of consensus node form", requestType = @TypeDescriptor(value = ConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult createAgentOffline(ConsensusDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createConsensusTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/agent/stop/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Unregister consensus node transactions", order = 551, detailDesc = "Assembly transactionsStopDepositDtoInformation can be obtained by querying the delegated consensus list of nodes,inputofnonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "Offline logout consensus node form", requestType = @TypeDescriptor(value = StopConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult stopAgentOffline(StopConsensusDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createStopConsensusTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/deposit/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Entrusted participation in consensus trading", order = 552, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "DepositDto", parameterDes = "Offline Delegation Participation Consensus Form", requestType = @TypeDescriptor(value = DepositDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult depositToAgentOffline(DepositDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createDepositTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/withdraw/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Exit consensus trading", order = 553, detailDesc = "InterfaceinputData is entrusted for consensus tradingoutputdatanonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "WithDrawDto", parameterDes = "Offline exit consensus form", requestType = @TypeDescriptor(value = WithDrawDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult withdrawOffline(WithDrawDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createWithdrawDepositTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/agent/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly of multiple signed accounts - Create consensus node transactions", order = 554, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "MultiSignConsensusDto", parameterDes = "Offline creation of consensus node form for multiple account signatures", requestType = @TypeDescriptor(value = MultiSignConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult createMultiSignAgentOffline(MultiSignConsensusDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignConsensusTx(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/deposit/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Delegate multiple account signatures to participate in consensus trading", order = 555, detailDesc = "The required assets for participating in consensus can be obtained through the query chain information interface(agentChainIdandagentAssetId)")
    @Parameters({
            @Parameter(parameterName = "MultiSignDepositDto", parameterDes = "Multiple account offline delegation participation consensus form", requestType = @TypeDescriptor(value = MultiSignDepositDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult multiSignDepositToAgentOffline(MultiSignDepositDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignDepositTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/withdraw/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Sign multiple accounts to exit consensus transactions", order = 556, detailDesc = "InterfaceinputData is entrusted for consensus tradingoutputdatanonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "WithDrawDto", parameterDes = "Multiple account offline exit consensus form with multiple signatures", requestType = @TypeDescriptor(value = MultiSignWithDrawDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult MultiSignWithdrawOffline(MultiSignWithDrawDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignWithdrawDepositTxOffline(form);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/agent/stop/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline assembly - Multiple account cancellation consensus node transactions", order = 557, detailDesc = "Assembly transactionsStopDepositDtoInformation can be obtained by querying the delegated consensus list of nodes,inputofnonceValue can be empty")
    @Parameters({
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "Multiple account offline cancellation consensus node form", requestType = @TypeDescriptor(value = MultiSignStopConsensusDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization string")
    }))
    public RpcClientResult multiSignStopAgentOffline(MultiSignStopConsensusDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignStopConsensusTx(form);
        return ResultUtil.getRpcClientResult(result);
    }


}
