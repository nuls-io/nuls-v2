package io.nuls.poc.service.impl;

import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.service.ConsensusService;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.storage.PunihStorageService;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.List;

@Component
public class ConsensusServiceImpl implements ConsensusService {
    @Autowired
    private AgentStorageService agentService;
    @Autowired
    private DepositStorageService depositService;
    @Autowired
    private PunihStorageService publishService;

    @Override
    public CmdResponse createAgent(List<Object> params) {
        //1.参数验证
        if(params == null || params.size() != 7){
            return new CmdResponse(1, ConsensusErrorCode.PARAM_NUMBER_ERROR.getCode(),ConsensusErrorCode.PARAM_NUMBER_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }

        //2.账户验证（调用账户模块接口）
        //3.组装创建节点交易
        //3.1.组装共识节点信息
        //3.2.组装coinData(调用账户模块)
        //4.交易签名
        //5.将交易发送给交易管理模块
        return null;
    }

    @Override
    public CmdResponse stopAgent(List<Object> params) {
        //1.参数基础验证
        //2.账户验证（调用账户模块接口）
        //3.组装删除节点交易
        //3.1.组装退出节点信息
        //3.2.组装coinData(调用账户模块)
        //4.交易签名
        //5.将交易发送给交易管理模块
        return null;
    }

    @Override
    public CmdResponse depositToAgent(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse withdraw(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getAgentList(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getAgentInfo(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getPublishList(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getDepositList(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getWholeInfo(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getInfo(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse validSmallBlock(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse batchValid(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getRoundInfo(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse getAgentStatus(List<Object> params) {
        //从数据库查询节点信息，返回节点状态
        return null;
    }

    @Override
    public CmdResponse updateAgentStatus(List<Object> params) {
        if(params == null || params.size() != 1){
            return new CmdResponse(1, ConsensusErrorCode.PARAM_NUMBER_ERROR.getCode(),ConsensusErrorCode.PARAM_NUMBER_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        int chain_id = (Integer)params.get(0);
        ConsensusManager.getInstance().getPacking_status().put(chain_id,true);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,null);
    }

    @Override
    public CmdResponse stopChain(List<Object> params) {
        return null;
    }

    @Override
    public CmdResponse runChain(List<Object> params) {
        return null;
    }
}
