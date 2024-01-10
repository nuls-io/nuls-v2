package io.nuls.consensus.rpc.cmd;

import io.nuls.common.ConfigBean;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.dto.output.AccountConsensusInfoDTO;
import io.nuls.consensus.model.dto.output.WholeNetConsensusInfoDTO;
import io.nuls.consensus.service.ChainService;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.List;
import java.util.Map;

/**
 * Consensus Chain Related Interface
 *
 * @author tag
 * 2018/11/7
 */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class ChainCmd extends BaseCmd {
    @Autowired
    private ChainService service;

    /**
     * Block fork record
     */
    @CmdAnnotation(cmd = "cs_addEvidenceRecord", version = 1.0, description = "Chain fork evidence record/add evidence record")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Fork block head one")
    @Parameter(parameterName = "evidenceHeader", parameterType = "String", parameterDes = "Fork block head two")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Processing results")
    }))
    public Response addEvidenceRecord(Map<String, Object> params) {
        Result result = service.addEvidenceRecord(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Shuanghua transaction records
     */
    @CmdAnnotation(cmd = "cs_doubleSpendRecord", version = 1.0, description = "Shuanghua transaction records/double spend transaction record ")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "block", parameterType = "String", parameterDes = "Block information")
    @Parameter(parameterName = "tx", parameterType = "String",parameterDes = "Forked transaction")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Processing results")
    }))
    public Response doubleSpendRecord(Map<String, Object> params) {
        Result result = service.doubleSpendRecord(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query penalty list
     */
    @CmdAnnotation(cmd = "cs_getPublishList", version = 1.0, description = "Query red and yellow card records/query punish list")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "address")
    @Parameter(parameterName = "type", requestType = @TypeDescriptor(value = int.class), parameterDes = "Punishment type 0Red and yellow card records 1Red Card Record 2Yellow card record")
    @ResponseData(name = "Return value", description = "Return aMapObject, containing twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "redPunish",valueType = List.class, valueElement = String.class,  description = "List of red cards obtained"),
            @Key(name = "yellowPunish", valueType = List.class, valueElement = String.class, description = "List of yellow card penalties obtained")
    }))
    public Response getPublishList(Map<String, Object> params) {
        Result result = service.getPublishList(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query consensus information across the entire network
     */
    @CmdAnnotation(cmd = "cs_getWholeInfo", version = 1.0, description = "Query consensus data across the entire network/query the consensus information of the whole network")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = WholeNetConsensusInfoDTO.class))
    public Response getWholeInfo(Map<String, Object> params) {
        Result result = service.getWholeInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query consensus information for specified accounts
     */
    @CmdAnnotation(cmd = "cs_getInfo", version = 1.0, description = "Query consensus data for specified accounts/query consensus information for specified accounts")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = AccountConsensusInfoDTO.class))
    public Response getInfo(Map<String, Object> params) {
        Result result = service.getInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Obtain current round information
     */
    @CmdAnnotation(cmd = "cs_getRoundInfo", version = 1.0, description = "Obtain current round information/get current round information")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = MeetingRound.class))
    public Response getCurrentRoundInfo(Map<String, Object> params) {
        Result result = service.getCurrentRoundInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query the member list of the specified block in the round
     */
    @CmdAnnotation(cmd = "cs_getRoundMemberList", version = 1.0, description = "Query the member list of the specified block in the round/Query the membership list of the specified block's rounds")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "extend", parameterType = "String", parameterDes = "Block header extension information")
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "packAddressList",valueType = List.class, valueElement = String.class,  description = "Current block address list")
    }))
    public Response getRoundMemberList(Map<String, Object> params) {
        Result result = service.getRoundMemberList(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Obtain common module recognition configuration information
     */
    @CmdAnnotation(cmd = "cs_getConsensusConfig", version = 1.0, description = "Obtain consensus module configuration information/get consensus config")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "seedNodes", description = "Seed node list"),
            @Key(name = "inflationAmount",valueType = Integer.class, description = "Maximum entrusted amount"),
            @Key(name = "agentAssetId",valueType = Integer.class, description = "Consensus assetsID"),
            @Key(name = "agentChainId",valueType = Integer.class, description = "Consensus Asset ChainID"),
            @Key(name = "awardAssetId",valueType = Integer.class, description = "Reward assetsID（Consensus rewards are assets of this chain）"),
    }))
    @SuppressWarnings("unchecked")
    public Response getConsensusConfig(Map<String, Object> params) {
        Result result = service.getConsensusConfig(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Get Seed Node
     * */
    @CmdAnnotation(cmd = "cs_getAgentChangeInfo", version = 1.0, description = "get seed nodes list")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getAgentChangeInfo(Map<String,Object> params){
        Result result = service.getAgentChangeInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Stop a sub chain
     * */
    @CmdAnnotation(cmd = "cs_stopChain", version = 1.0, description = "stop a chain 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response stopChain(Map<String,Object> params){
        Result result = service.stopChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Run a sub chain
     * */
    @CmdAnnotation(cmd = "cs_runChain", version = 1.0, description = "Running a sub chain 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response runChain(Map<String,Object> params){
        Result result = service.runChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Start the main chain
     * */
    @CmdAnnotation(cmd = "cs_runMainChain", version = 1.0, description = "run main chain 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response runMainChain(Map<String,Object> params){
        Result result = service.runMainChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

}
