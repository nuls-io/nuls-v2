package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.service.ConsensusService;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.storage.PunihStorageService;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.manager.RoundManager;
import io.nuls.poc.utils.util.PoConvertUtil;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public CmdResponse getAgentList(Map<String,Object> params) {
        int pageNumber = 0;
        if(params.get("pageNumber") != null){
            pageNumber = (Integer) params.get("pageNumber");
        }
        int pageSize = 0;
        if(params.get("pageSize") != null){
            pageSize = (Integer) params.get("pageSize");
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100 || params.get("chain_id") == null) {
            return new CmdResponse(1, ConsensusErrorCode.DATA_ERROR.getCode(),ConsensusErrorCode.DATA_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        int chain_id = (Integer) params.get("chain_id");
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        String keyword = null;
        if(params.get("keyword")!=null){
            keyword = (String)params.get("keyword");
        }
        //todo
        //从区块管理模块获取本地最新高度
        long startBlockHeight = 100;
        Iterator<Agent> agentIterator = agentList.iterator();
        while(agentIterator.hasNext()){
            Agent agent = agentIterator.next();
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                agentIterator.remove();
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                agentIterator.remove();
            } else if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.toUpperCase();
                String agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress()).toUpperCase();
                String packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress()).toUpperCase();
                String agentId = PoConvertUtil.getAgentId(agent.getTxHash()).toUpperCase();
                //todo
                //从账户模块获取账户别名
                String alias = "";
                boolean b = agentId.indexOf(keyword) >= 0;
                b = b || agentAddress.equals(keyword) || packingAddress.equals(keyword);
                if (StringUtils.isNotBlank(alias)) {
                    b = b || alias.toUpperCase().indexOf(keyword) >= 0;
                }
                if (!b) {
                    agentIterator.remove();
                }
            }
        }
        int start = pageNumber * pageSize - pageSize;

        return null;
    }

    @Override
    public CmdResponse getAgentInfo(Map<String,Object> params) {
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
        if(params == null){
            return new CmdResponse(1, ConsensusErrorCode.PARAM_NUMBER_ERROR.getCode(),ConsensusErrorCode.PARAM_NUMBER_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        MeetingRound round = RoundManager.getInstance().getOrResetCurrentRound((Integer)params.get(0),true);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,round);

    }

    @Override
    public CmdResponse getAgentStatus(List<Object> params) {
        //从数据库查询节点信息，返回节点状态
        if(params == null || params.size() != 2){
            return new CmdResponse(1, ConsensusErrorCode.PARAM_NUMBER_ERROR.getCode(),ConsensusErrorCode.PARAM_NUMBER_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        Map<String,Integer> result = new HashMap<>();
        try {
            NulsDigestData agentHash = new NulsDigestData();
            agentHash.parse(new NulsByteBuffer(HexUtil.decode((String) params.get(0))));
            AgentPo agent = agentService.get(agentHash,(Integer)params.get(1));
            if(agent.getDelHeight() > 0){
                result.put("status",0);
            }else{
                result.put("status",1);
            }
        }catch (Exception e){
            Log.error(e);
        }
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,result);
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
