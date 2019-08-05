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

import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.DepositInfoDto;
import io.nuls.provider.model.form.consensus.CreateAgentForm;
import io.nuls.provider.model.form.consensus.DepositForm;
import io.nuls.provider.model.form.consensus.StopAgentForm;
import io.nuls.provider.model.form.consensus.WithdrawForm;
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

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "创建共识节点", order = 501)
    @Parameters({
            @Parameter(parameterName = "CreateAgentForm", parameterDes = "创建共识节点表单", requestType = @TypeDescriptor(value = CreateAgentForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
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
    @ApiOperation(description = "注销共识节点", order = 502)
    @Parameters({
            @Parameter(parameterName = "StopAgentForm", parameterDes = "注销共识节点表单", requestType = @TypeDescriptor(value = StopAgentForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
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
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "委托参与共识", order = 503)
    @Parameters({
            @Parameter(parameterName = "DepositForm", parameterDes = "委托参与共识表单", requestType = @TypeDescriptor(value = DepositForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
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
    @ApiOperation(description = "退出共识", order = 504)
    @Parameters({
            @Parameter(parameterName = "退出共识", parameterDes = "退出共识表单", requestType = @TypeDescriptor(value = WithdrawForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
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
    @ApiOperation(description = "查询节点的委托共识列表", order = 505)
    @Parameters({
            @Parameter(parameterName = "agentHash", parameterDes = "创建共识节点的交易hash")
    })
    @ResponseData(name = "返回值", description = "返回委托共识集合", responseType = @TypeDescriptor(value = List.class, collectionElement = DepositInfoDto.class))
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
    @Path("/agent/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装 - 创建共识节点交易", order = 550, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "ConsensusDto", parameterDes = "离线创建共识节点表单", requestType = @TypeDescriptor(value = ConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 注销共识节点交易", order = 551, detailDesc = "组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "离线注销共识节点表单", requestType = @TypeDescriptor(value = StopConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 委托参与共识交易", order = 552, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "DepositDto", parameterDes = "离线委托参与共识表单", requestType = @TypeDescriptor(value = DepositDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 退出共识交易", order = 553, detailDesc = "接口的input数据，则是委托共识交易的output数据，nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "WithDrawDto", parameterDes = "离线退出共识表单", requestType = @TypeDescriptor(value = WithDrawDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "多签账户离线组装 - 创建共识节点交易", order = 554, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "MultiSignConsensusDto", parameterDes = "多签账户离线创建共识节点表单", requestType = @TypeDescriptor(value = MultiSignConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 多签账户委托参与共识交易", order = 555, detailDesc = "参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)")
    @Parameters({
            @Parameter(parameterName = "MultiSignDepositDto", parameterDes = "多签账户离线委托参与共识表单", requestType = @TypeDescriptor(value = MultiSignDepositDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 多签账户退出共识交易", order = 556, detailDesc = "接口的input数据，则是委托共识交易的output数据，nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "WithDrawDto", parameterDes = "多签账户离线退出共识表单", requestType = @TypeDescriptor(value = MultiSignWithDrawDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
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
    @ApiOperation(description = "离线组装 - 多签账户注销共识节点交易", order = 557, detailDesc = "组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空")
    @Parameters({
            @Parameter(parameterName = "StopConsensusDto", parameterDes = "多签账户离线注销共识节点表单", requestType = @TypeDescriptor(value = MultiSignStopConsensusDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化字符串")
    }))
    public RpcClientResult multiSignStopAgentOffline(MultiSignStopConsensusDto form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignStopConsensusTx(form);
        return ResultUtil.getRpcClientResult(result);
    }
}
