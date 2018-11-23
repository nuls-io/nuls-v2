package io.nuls.poc.rpc;

import io.nuls.poc.service.impl.ConsensusServiceImpl;
import io.nuls.rpc.cmd.BaseCmd;
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
    public CmdResponse createAgent(List<Object> params){
        return null;
    }

    /**
     * 节点验证
     * */
    public CmdResponse createAgentValid(List<Object> params){
        return null;
    }

    /**
     * 创建节点交易提交
     * */
    public CmdResponse createAgentCommit(List<Object> params){
        return null;
    }

    /**
     * 创建节点交易回滚
     * */
    public CmdResponse createAgentRollBack(List<Object> params){
        return null;
    }

    /**
     * 注销节点
     * */
    public CmdResponse stopAgent(List<Object> params){
        return null;
    }

    /**
     * 注销节点交易验证
     * */
    public CmdResponse stopAgentValid(List<Object> params){
        return null;
    }

    /**
     * 注销节点交易提交
     * */
    public CmdResponse stopAgentCommit(List<Object> params){
        return null;
    }

    /**
     * 注销节点交易回滚
     * */
    public CmdResponse stopAgentRollBack(List<Object> params){
        return null;
    }

    /**
     * 委托共识
     * */
    public CmdResponse depositToAgent(List<Object> params){
        return null;
    }

    /**
     * 委托共识交易验证
     * */
    public CmdResponse depositValid(List<Object> params){
        return null;
    }

    /**
     * 委托共识交易提交
     * */
    public CmdResponse depositCommit(List<Object> params){
        return null;
    }

    /**
     * 委托共识交易回滚
     * */
    public CmdResponse depositRollBack(List<Object> params){
        return null;
    }

    /**
     * 退出共识
     * */
    public CmdResponse withdraw(List<Object> params){
        return null;
    }

    /**
     * 退出共识交易验证
     * */
    public CmdResponse withdrawValid(List<Object> params){
        return null;
    }

    /**
     * 退出共识交易提交
     * */
    public CmdResponse withdrawCommit(List<Object> params){
        return null;
    }

    /**
     * 退出共识交易回滚
     * */
    public CmdResponse withdrawRollBack(List<Object> params){
        return null;
    }

    /**
     * 查询共识节点列表
     * */
    public CmdResponse getAgentList(List<Object> params){
        //return service.getAgentList(params);
        return null;
    }

    /**
     * 查询指定节点信息
     * */
    public CmdResponse getAgentInfo(List<Object> params){
        //return service.getAgentInfo(params);
        return null;
    }

    /**
     * 查询黄牌列表
     * */
    public CmdResponse getPublishList(List<Object> params){
        return null;
    }

    /**
     * 查询委托信息列表
     * */
    public CmdResponse getDepositList(List<Object> params){
        return null;
    }

    /**
     * 查询全网共识信息
     * */
    public CmdResponse getWholeInfo(List<Object> params){
        return null;
    }

    /**
     * 查询指定账户的共识信息
     * */
    public CmdResponse getInfo(List<Object> params){
        return null;
    }

    /**
     * 验证区块正确性
     * */
    public CmdResponse validSmallBlock(List<Object> params){
        return null;
    }

    /**
     * 批量验证共识模块交易
     * */
    public CmdResponse batchValid(List<Object> params){
        return service.batchValid(params);
    }

    /**
     * 获取当前轮次信息
     * */
    public CmdResponse getRoundInfo(List<Object> params){
        return service.getRoundInfo(params);
    }

    /**
     * 查询指定共识节点状态
     * */
    public CmdResponse getAgentStatus(List<Object> params){
        return service.getAgentStatus(params);
    }

    /**
     * 修改节点打包状态
     * */
    public CmdResponse updateAgentStatus(List<Object> params){
        return service.updateAgentStatus(params);
    }

    /**
     * 停止一条子链
     * */
    public CmdResponse stopChain(List<Object> params){
        return service.stopChain(params);
    }

    /**
     * 运行一条子链
     * */
    public CmdResponse runChain(List<Object> params){
        return service.runChain(params);
    }

    /**
     * 启动主链
     * */
    public CmdResponse runMainChain(List<Object> params){
        return service.runChain(params);
    }
}
