package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Na;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Page;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.dto.output.*;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.PunishLogPo;
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
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;

import java.util.*;

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
        List<Agent> handleList = new ArrayList<>();
        String keyword = null;
        if(params.get("keyword")!=null){
            keyword = (String)params.get("keyword");
        }
        //todo
        //从区块管理模块获取本地最新高度
        long startBlockHeight = 100;
        for (Agent agent:agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if(agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L){
                continue;
            }
            if(StringUtils.isNotBlank(keyword)){
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
                    agent.setAlais(alias);
                }
                if (!b) {
                    continue;
                }
            }
            handleList.add(agent);
        }
        int start = pageNumber * pageSize - pageSize;
        Page<AgentDTO> page = new Page<>(pageNumber, pageSize, handleList.size());
        if (start >= page.getTotal()) {
            return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,page);
        }
        fillAgentList(chain_id, handleList, null);
        //todo
        //是否要添加排序功能
        List<AgentDTO> resultList = new ArrayList<>();
        for (int i = start; i < handleList.size() && i < (start + pageSize); i++) {
            AgentDTO agentDTO = new AgentDTO(handleList.get(i));
            resultList.add(agentDTO);
        }
        page.setList(resultList);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,page);

    }

    @Override
    public CmdResponse getAgentInfo(Map<String,Object> params) {
        if(params.get("agentHash") == null || params.get("chain_id") == null ){
            return new CmdResponse(1, ConsensusErrorCode.DATA_ERROR.getCode(),ConsensusErrorCode.DATA_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        String agentHash = (String)params.get("agentHash");
        if (!NulsDigestData.validHash(agentHash)) {
            return new CmdResponse(1, ConsensusErrorCode.AGENT_NOT_EXIST.getCode(),ConsensusErrorCode.AGENT_NOT_EXIST.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        try {
            NulsDigestData agentHashData = NulsDigestData.fromDigestHex(agentHash);
            int chain_id = (Integer)params.get("chain_id");
            List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
            for (Agent agent:agentList) {
                if (agent.getTxHash().equals(agentHashData)) {
                    MeetingRound round = RoundManager.getInstance().getCurrentRound(chain_id);
                    this.fillAgent(chain_id, agent, round, null);
                    AgentDTO dto = new AgentDTO(agent);
                    return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,dto);
                }
            }
        }catch (Exception e){
            Log.error(e);
        }
        return new CmdResponse(1, ConsensusErrorCode.AGENT_NOT_EXIST.getCode(),ConsensusErrorCode.AGENT_NOT_EXIST.getMsg(), ConsensusConstant.RPC_VERSION,null);
    }

    @Override
    public CmdResponse getPublishList(Map<String,Object> params) {
        if(params.get("chain_id") == null){
            return new CmdResponse(1, ConsensusErrorCode.DATA_ERROR.getCode(),ConsensusErrorCode.DATA_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        int chain_id = (Integer)params.get("chain_id");
        String address = null;
        if(params.get("address") != null){
            address = (String)params.get("address");
        }
        int type = 0;
        if(params.get("type") != null){
            type = (Integer)params.get("type");
        }
        List<PunishLogDTO> yellowPunishList = null;
        List<PunishLogDTO> redPunishList = null;
        //查询红牌交易
        if(type != 1){
            for (PunishLogPo po:ConsensusManager.getInstance().getRedPunishMap().get(chain_id)) {
                if(StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(),AddressTool.getAddress(address))){
                    continue;
                }
                redPunishList.add(new PunishLogDTO(po));
            }
        }else if(type != 2){
            for (PunishLogPo po:ConsensusManager.getInstance().getYellowPunishMap().get(chain_id)) {
                if(StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(),AddressTool.getAddress(address))){
                    continue;
                }
                yellowPunishList.add(new PunishLogDTO(po));
            }
        }
        Map<String,List<PunishLogDTO>> resultMap = new HashMap<>();
        resultMap.put("redPunish",redPunishList);
        resultMap.put("yellowPunish",yellowPunishList);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,resultMap);
    }

    @Override
    public CmdResponse getDepositList(Map<String,Object> params) {
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
        String address = null;
        if(params.get("address") != null){
            address = (String)params.get("address");
        }
        String agentHash = null;
        if(params.get("agentHash") != null){
            agentHash = (String)params.get("agentHash");
        }
        List<Deposit> depositList = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        List<Deposit> handleList = new ArrayList<>();
        //todo
        //从区块管理模块获取本地最新高度
        long startBlockHeight = 0;
        byte[] addressBytes = null;
        if(StringUtils.isNotBlank(address)){
            addressBytes=AddressTool.getAddress(address);
        }
        for (Deposit deposit:depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (addressBytes != null && !Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            if (agentHash != null && !deposit.getAgentHash().getDigestHex().equals(agentHash)) {
                continue;
            }
            handleList.add(deposit);
        }
        int start = pageNumber * pageSize - pageSize;
        Page<DepositDTO> page = new Page<>(pageNumber, pageSize, handleList.size());
        if (start >= handleList.size()) {
            return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,page);
        }
        List<DepositDTO> resultList = new ArrayList<>();
        for (int i = start; i < depositList.size() && i < (start + pageSize); i++) {
            Deposit deposit = depositList.get(i);
            List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getTxHash().equals(deposit.getAgentHash())) {
                    agent = a;
                    break;
                }
            }
            deposit.setStatus(agent == null ? 0 : agent.getStatus());
            resultList.add(new DepositDTO(deposit, agent));
        }
        page.setList(resultList);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,page);
    }

    @Override
    public CmdResponse getWholeInfo(Map<String,Object> params) {
        if (params.get("chain_id") == null) {
            return new CmdResponse(1, ConsensusErrorCode.DATA_ERROR.getCode(),ConsensusErrorCode.DATA_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        int chain_id = (Integer) params.get("chain_id");
        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        if(agentList == null ){
            return new CmdResponse(1, ConsensusErrorCode.DATA_NOT_EXIST.getCode(),ConsensusErrorCode.DATA_NOT_EXIST.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        List<Agent> handleList = new ArrayList<>();
        //todo
        //从区块管理模块获取本地最新高度
        long startBlockHeight = 100;
        for (Agent agent:agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            handleList.add(agent);
        }
        MeetingRound round = RoundManager.getInstance().getCurrentRound(chain_id);
        long totalDeposit = 0;
        int packingAgentCount = 0;
        if (null != round) {
            for (MeetingMember member : round.getMemberList()) {
                totalDeposit += (member.getAgent().getTotalDeposit().getValue() + member.getAgent().getDeposit().getValue());
                if (member.getAgent() != null) {
                    packingAgentCount++;
                }
            }
        }
        dto.setAgentCount(handleList.size());
        dto.setTotalDeposit(totalDeposit);
        dto.setConsensusAccountNumber(handleList.size());
        dto.setPackingAgentCount(packingAgentCount);
        return new CmdResponse(1, ConsensusErrorCode.SUCCESS.getCode(),ConsensusErrorCode.SUCCESS.getMsg(), ConsensusConstant.RPC_VERSION,dto);
    }

    @Override
    public CmdResponse getInfo(Map<String,Object> params) {
        if (params.get("chain_id") == null || params.get("address")==null) {
            return new CmdResponse(1, ConsensusErrorCode.DATA_ERROR.getCode(),ConsensusErrorCode.DATA_ERROR.getMsg(), ConsensusConstant.RPC_VERSION,null);
        }
        int chain_id = (Integer) params.get("chain_id");
        String address = (String)params.get("address");
        AccountConsensusInfoDTO dto = new AccountConsensusInfoDTO();
        //todo
        //从共识模块获取本地最新高度
        long startBlockHeight = 100;
        int agentCount = 0;
        String agentHash = null;
        byte[] addressBytes = AddressTool.getAddress(address);
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        for (Agent agent:agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(agent.getAgentAddress(), addressBytes)) {
                //一个账户最多只能创建一个共识节点
                agentCount = 1;
                agentHash = agent.getTxHash().getDigestHex();
                break;
            }
        }
        List<Deposit> depositList = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        Set<NulsDigestData> agentSet = new HashSet<>();
        long totalDeposit = 0;
        for (Deposit deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            agentSet.add(deposit.getAgentHash());
            totalDeposit += deposit.getDeposit().getValue();
        }
        dto.setAgentCount(agentCount);
        dto.setAgentHash(agentHash);
        dto.setJoinAgentCount(agentSet.size());
        //todo
        //统计账户奖励金
        //dto.setReward(this.rewardCacheService.getReward(address).getValue());
        //dto.setRewardOfDay(rewardCacheService.getRewardToday(address).getValue());
        dto.setTotalDeposit(totalDeposit);
        try {
            //todo
            //从账本模块获取账户可用余额
            //dto.setUsableBalance(accountLedgerService.getBalance(addressBytes).getData().getUsable().getValue());
        } catch (Exception e) {
            Log.error(e);
            dto.setUsableBalance(0L);
        }
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



    private void fillAgentList(int chain_id, List<Agent> agentList, List<Deposit> depositList) {
        MeetingRound round = RoundManager.getInstance().getCurrentRound(chain_id);
        for (Agent agent : agentList) {
            fillAgent(chain_id, agent, round, depositList);
        }
    }

    private void fillAgent(int chain_id, Agent agent, MeetingRound round, List<Deposit> depositList) {
        if (null == depositList || depositList.isEmpty()) {
            depositList = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        }
        Set<String> memberSet = new HashSet<>();
        Na total = Na.ZERO;
        for (int i = 0; i < depositList.size(); i++) {
            Deposit deposit = depositList.get(i);
            if (!agent.getTxHash().equals(deposit.getAgentHash())) {
                continue;
            }
            if (deposit.getDelHeight() >= 0) {
                continue;
            }
            total = total.add(deposit.getDeposit());
            memberSet.add(AddressTool.getStringAddressByBytes(deposit.getAddress()));
        }
        agent.setMemberCount(memberSet.size());
        agent.setTotalDeposit(total);
        if (round == null) {
            return;
        }
        MeetingMember member = round.getMember(agent.getPackingAddress());
        if (null == member) {
            agent.setStatus(0);
            return;
        }
        agent.setStatus(1);
        agent.setCreditVal(member.getAgent().getCreditVal());
    }
}
