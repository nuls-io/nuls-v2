package io.nuls.poc.rpc;

import io.nuls.poc.service.impl.ConsensusServiceImpl;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.List;

/**
 * 共识RPC类
 * @author tag
 * 2018/11/7
 * */
@Component
public class PocConsensusResource extends BaseCmd{
    @Autowired
    private ConsensusServiceImpl service;
    /**
     * 创建节点
     * */
    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, preCompatible = true)
    public CmdResponse createAgent(List<Object> params){
        return service.createAgent(params);
    }

    /**
     * 节点验证
     * */
    @CmdAnnotation(cmd = "cs_createAgentValid", version = 1.0, preCompatible = true)
    public CmdResponse createAgentValid(List<Object> params){
        return null;
    }

    /**
     * 创建节点交易提交
     * */
    @CmdAnnotation(cmd = "cs_createAgentCommit", version = 1.0, preCompatible = true)
    public CmdResponse createAgentCommit(List<Object> params){
        return null;
    }

    /**
     * 创建节点交易回滚
     * */
    @CmdAnnotation(cmd = "cs_createAgentRollBack", version = 1.0, preCompatible = true)
    public CmdResponse createAgentRollBack(List<Object> params){
        return null;
    }

    /**
     * 注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopAgent", version = 1.0, preCompatible = true)
    public CmdResponse stopAgent(List<Object> params){
        return service.stopAgent(params);
    }

    /**
     * 注销节点交易验证
     * */
    @CmdAnnotation(cmd = "cs_stopAgentValid", version = 1.0, preCompatible = true)
    public CmdResponse stopAgentValid(List<Object> params){
        return null;
    }

    /**
     * 注销节点交易提交
     * */
    @CmdAnnotation(cmd = "cs_stopAgentCommit", version = 1.0, preCompatible = true)
    public CmdResponse stopAgentCommit(List<Object> params){
        return null;
    }

    /**
     * 注销节点交易回滚
     * */
    @CmdAnnotation(cmd = "cs_stopAgentRollback", version = 1.0, preCompatible = true)
    public CmdResponse stopAgentRollBack(List<Object> params){
        return null;
    }

    /**
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_depositToAgent", version = 1.0, preCompatible = true)
    public CmdResponse depositToAgent(List<Object> params){
        return service.depositToAgent(params);
    }

    /**
     * 委托共识交易验证
     * */
    @CmdAnnotation(cmd = "cs_depositValid", version = 1.0, preCompatible = true)
    public CmdResponse depositValid(List<Object> params){
        return null;
    }

    /**
     * 委托共识交易提交
     * */
    @CmdAnnotation(cmd = "cs_depositCommit", version = 1.0, preCompatible = true)
    public CmdResponse depositCommit(List<Object> params){
        return null;
    }

    /**
     * 委托共识交易回滚
     * */
    @CmdAnnotation(cmd = "cs_depositRollback", version = 1.0, preCompatible = true)
    public CmdResponse depositRollBack(List<Object> params){
        return null;
    }

    /**
     * 退出共识
     * */
    @CmdAnnotation(cmd = "cs_withdraw", version = 1.0, preCompatible = true)
    public CmdResponse withdraw(List<Object> params){
        return service.withdraw(params);
    }

    /**
     * 退出共识交易验证
     * */
    @CmdAnnotation(cmd = "cs_withdrawValid", version = 1.0, preCompatible = true)
    public CmdResponse withdrawValid(List<Object> params){
        return null;
    }

    /**
     * 退出共识交易提交
     * */
    @CmdAnnotation(cmd = "cs_depositCommit", version = 1.0, preCompatible = true)
    public CmdResponse withdrawCommit(List<Object> params){
        return null;
    }

    /**
     * 退出共识交易回滚
     * */
    @CmdAnnotation(cmd = "cs_withdrawRollback", version = 1.0, preCompatible = true)
    public CmdResponse withdrawRollBack(List<Object> params){
        return null;
    }

    /**
     * 查询共识节点列表
     * */
    @CmdAnnotation(cmd = "cs_getAgentList", version = 1.0, preCompatible = true)
    public CmdResponse getAgentList(List<Object> params){
        return service.getAgentList(params);
    }

    /**
     * 查询指定节点信息
     * */
    @CmdAnnotation(cmd = "cs_getAgent", version = 1.0, preCompatible = true)
    public CmdResponse getAgentInfo(List<Object> params){
        return service.getAgentInfo(params);
    }

    /**
     * 查询黄牌列表
     * */
    @CmdAnnotation(cmd = "cs_getPunishList", version = 1.0, preCompatible = true)
    public CmdResponse getPublishList(List<Object> params){
        return service.getPublishList(params);
    }

    /**
     * 查询委托信息列表
     * */
    @CmdAnnotation(cmd = "cs_getDepositList", version = 1.0, preCompatible = true)
    public CmdResponse getDepositList(List<Object> params){
        return service.getDepositList(params);
    }

    /**
     * 查询全网共识信息
     * */
    @CmdAnnotation(cmd = "cs_getWholeInfo", version = 1.0, preCompatible = true)
    public CmdResponse getWholeInfo(List<Object> params){
        return service.getWholeInfo(params);
    }

    /**
     * 查询指定账户的共识信息
     * */
    @CmdAnnotation(cmd = "cs_getInfo", version = 1.0, preCompatible = true)
    public CmdResponse getInfo(List<Object> params){
        return service.getInfo(params);
    }

    /**
     * 验证区块正确性
     * */
    @CmdAnnotation(cmd = "cs_validSmallBlock", version = 1.0, preCompatible = true)
    public CmdResponse validSmallBlock(List<Object> params){
        return service.validSmallBlock(params);
    }

    /**
     * 批量验证共识模块交易
     * */
    @CmdAnnotation(cmd = "cs_batchValid", version = 1.0, preCompatible = true)
    public CmdResponse batchValid(List<Object> params){
        return service.batchValid(params);
    }

    /**
     * 获取当前轮次信息
     * */
    @CmdAnnotation(cmd = "cs_getRoundInfo", version = 1.0, preCompatible = true)
    public CmdResponse getRoundInfo(List<Object> params){
        return service.getRoundInfo(params);
    }

    /**
     * 查询指定共识节点状态
     * */
    @CmdAnnotation(cmd = "cs_getAgentStatus", version = 1.0, preCompatible = true)
    public CmdResponse getAgentStatus(List<Object> params){
        return service.getAgentStatus(params);
    }

    /**
     * 修改节点打包状态
     * */
    @CmdAnnotation(cmd = "cs_updatePackStatus", version = 1.0, preCompatible = true)
    public CmdResponse updateAgentStatus(List<Object> params){
        return service.updateAgentStatus(params);
    }

    /**
     * 停止一条子链
     * */
    @CmdAnnotation(cmd = "cs_stopChain", version = 1.0, preCompatible = true)
    public CmdResponse stopChain(List<Object> params){
        return service.stopChain(params);
    }

    /**
     * 运行一条子链
     * */
    @CmdAnnotation(cmd = "cs_runChain", version = 1.0, preCompatible = true)
    public CmdResponse runChain(List<Object> params){
        return service.runChain(params);
    }

    /**
     * 启动主链
     * */
    @CmdAnnotation(cmd = "cs_runMainChain", version = 1.0, preCompatible = true)
    public CmdResponse runMainChain(List<Object> params){
        return service.runChain(params);
    }
}
