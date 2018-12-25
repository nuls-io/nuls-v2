package io.nuls.poc.rpc;

import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.service.ConsensusService;
import io.nuls.poc.utils.annotation.ResisterTx;
import io.nuls.poc.utils.enumeration.TxMethodType;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.Map;

/**
 * 共识
 * @author tag
 * 2018/11/7
 * */
@Component
public class PocConsensusResource extends BaseCmd{
    @Autowired
    private ConsensusService service;

    /**
     * 创建节点
     * */
    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, description = "create agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentAddress", parameterType = "String")
    @Parameter(parameterName = "packingAddress", parameterType = "String")
    @Parameter(parameterName = "rewardAddress", parameterType = "String")
    @Parameter(parameterName = "commissionRate", parameterType = "double")
    @Parameter(parameterName = "deposit", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response createAgent(Map<String,Object> params){
        Result result = service.createAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 节点验证
     * */
    @CmdAnnotation(cmd = "cs_createAgentValid", version = 1.0, description = "create agent transaction validate 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_REGISTER_AGENT,methodType = TxMethodType.VALID,methodName = "cs_createAgentValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createAgentValid(Map<String,Object> params){
        Result result = service.createAgentValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 创建节点交易提交
     * */
    @CmdAnnotation(cmd = "cs_createAgentCommit", version = 1.0, description = "create agent transaction commit 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_REGISTER_AGENT,methodType = TxMethodType.COMMIT,methodName = "cs_createAgentCommit")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response createAgentCommit(Map<String,Object> params){
        Result result = service.createAgentCommit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 创建节点交易回滚
     * */
    @CmdAnnotation(cmd = "cs_createAgentRollBack", version = 1.0, description = "create agent transaction rollback 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_REGISTER_AGENT,methodType = TxMethodType.ROLLBACK,methodName = "cs_createAgentRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createAgentRollBack(Map<String,Object> params){
        Result result = service.createAgentRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopAgent", version = 1.0, description = "stop agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response stopAgent(Map<String,Object> params){
        Result result = service.stopAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点交易验证
     * */
    @CmdAnnotation(cmd = "cs_stopAgentValid", version = 1.0, description = "stop agent transaction validate 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_STOP_AGENT,methodType = TxMethodType.VALID,methodName = "cs_stopAgentValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response stopAgentValid(Map<String,Object> params){
        Result result = service.stopAgentValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点交易提交
     * */
    @CmdAnnotation(cmd = "cs_stopAgentCommit", version = 1.0, description = "stop agent transaction commit 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_STOP_AGENT,methodType = TxMethodType.COMMIT,methodName = "cs_stopAgentCommit")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response stopAgentCommit(Map<String,Object> params){
        Result result = service.stopAgentCommit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点交易回滚
     * */
    @CmdAnnotation(cmd = "cs_stopAgentRollBack", version = 1.0, description = "stop agent transaction rollback 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_STOP_AGENT,methodType = TxMethodType.ROLLBACK,methodName = "cs_stopAgentRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response stopAgentRollBack(Map<String,Object> params){
        Result result = service.stopAgentRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_depositToAgent", version = 1.0, description = "deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    @Parameter(parameterName = "deposit", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response depositToAgent(Map<String,Object> params){
        Result result = service.depositToAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识交易验证
     * */
    @CmdAnnotation(cmd = "cs_depositValid", version = 1.0, description = "deposit agent transaction validate 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_JOIN_CONSENSUS,methodType = TxMethodType.VALID,methodName = "cs_depositValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response depositValid(Map<String,Object> params){
        Result result = service.depositValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识交易提交
     * */
    @CmdAnnotation(cmd = "cs_depositCommit", version = 1.0, description = "deposit agent transaction commit 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_JOIN_CONSENSUS,methodType = TxMethodType.COMMIT,methodName = "cs_depositCommit")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response depositCommit(Map<String,Object> params){
        Result result = service.depositCommit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识交易回滚
     * */
    @CmdAnnotation(cmd = "cs_depositRollBack", version = 1.0, description = "deposit agent transaction rollback 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_JOIN_CONSENSUS,methodType = TxMethodType.ROLLBACK,methodName = "cs_depositRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response depositRollBack(Map<String,Object> params){
        Result result = service.depositRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识
     * */
    @CmdAnnotation(cmd = "cs_withdraw", version = 1.0, description = "withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "txHash", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识交易验证
     * */
    @CmdAnnotation(cmd = "cs_withdrawValid", version = 1.0, description = "withdraw deposit agent transaction validate 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT,methodType = TxMethodType.VALID,methodName = "cs_withdrawValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response withdrawValid(Map<String,Object> params){
        Result result = service.withdrawValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识交易提交
     * */
    @CmdAnnotation(cmd = "cs_withdrawCommit", version = 1.0, description = "withdraw deposit agent transaction commit 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT,methodType = TxMethodType.COMMIT,methodName = "cs_withdrawCommit")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response withdrawCommit(Map<String,Object> params){
        Result result = service.withdrawCommit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识交易回滚
     * */
    @CmdAnnotation(cmd = "cs_withdrawRollBack", version = 1.0, description = "withdraw deposit agent transaction rollback 1.0")
    @ResisterTx(txType = ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT,methodType = TxMethodType.ROLLBACK,methodName = "cs_withdrawRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response withdrawRollBack(Map<String,Object> params){
        Result result = service.withdrawRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询共识节点列表
     * */
    @CmdAnnotation(cmd = "cs_getAgentList", version = 1.0, description = "query consensus node list 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
    @Parameter(parameterName = "keyWord", parameterType = "String")
    public Response getAgentList(Map<String,Object> params){
        Result result = service.getAgentList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询指定节点信息
     * */
    @CmdAnnotation(cmd = "cs_getAgentInfo", version = 1.0, description = "query specified node information 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    public Response getAgentInfo(Map<String,Object> params){
        Result result = service.getAgentInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询惩罚列表
     * */
    @CmdAnnotation(cmd = "cs_getPublishList", version = 1.0, description = "query punish list 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "type", parameterType = "int")
    public Response getPublishList(Map<String,Object> params){
        Result result = service.getPublishList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询委托信息列表
     * */
    @CmdAnnotation(cmd = "cs_getDepositList", version = 1.0, description = "query delegation information list 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    public Response getDepositList(Map<String,Object> params){
        Result result = service.getDepositList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询全网共识信息
     * */
    @CmdAnnotation(cmd = "cs_getWholeInfo", version = 1.0, description = "query the consensus information of the whole network 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getWholeInfo(Map<String,Object> params){
        Result result = service.getWholeInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询指定账户的共识信息
     * */
    @CmdAnnotation(cmd = "cs_getInfo", version = 1.0, description = "query consensus information for specified accounts 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    public Response getInfo(Map<String,Object> params){
        Result result = service.getInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 验证区块正确性
     * */
    @CmdAnnotation(cmd = "cs_validBlock", version = 1.0, description = "verify block correctness 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "download", parameterType = "int")
    @Parameter(parameterName = "block", parameterType = "String")
    public Response validBlock(Map<String,Object> params){
        Result result = service.validBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 批量验证共识模块交易
     * */
    @CmdAnnotation(cmd = "cs_batchValid", version = 1.0, description = "batch Verification Consensus Module Transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response batchValid(Map<String,Object> params){
        Result result = service.batchValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 获取当前轮次信息
     * */
    @CmdAnnotation(cmd = "cs_getRoundInfo", version = 1.0, description = "get current round information 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getCurrentRoundInfo(Map<String,Object> params){
        Result result = service.getCurrentRoundInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询指定共识节点状态
     * */
    @CmdAnnotation(cmd = "cs_getAgentStatus", version = 1.0, description = "query the specified consensus node status 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    public Response getAgentStatus(Map<String,Object> params){
        Result result = service.getAgentStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 修改节点共识状态
     * */
    @CmdAnnotation(cmd = "cs_updateAgentConsensusStatus", version = 1.0, description = "modifying the Node Consensus State 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response updateAgentConsensusStatus(Map<String,Object> params){
        Result result = service.updateAgentConsensusStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 修改节点打包状态
     * */
    @CmdAnnotation(cmd = "cs_updateAgentStatus", version = 1.0, description = "modifying the Packing State of Nodes 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "status", parameterType = "int")
    public Response updateAgentStatus(Map<String,Object> params){
        Result result = service.updateAgentStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 停止一条子链
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
     * 运行一条子链
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
     * 启动主链
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

    /**
     * 缓存新区块头
     * */
    @CmdAnnotation(cmd = "cs_addBlock", version = 1.0, description = "add block 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response addBlock(Map<String,Object> params){
        Result result = service.addBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 区块分叉记录
     * */
    @CmdAnnotation(cmd = "cs_addEvidenceRecord", version = 1.0, description = "add evidence record 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    @Parameter(parameterName = "evidenceHeader", parameterType = "String")
    public Response addEvidenceRecord(Map<String,Object> params){
        Result result = service.addEvidenceRecord(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
