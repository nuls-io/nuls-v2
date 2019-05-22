package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.dto.input.SearchPunishDTO;
import io.nuls.poc.model.dto.output.AccountConsensusInfoDTO;
import io.nuls.poc.model.dto.output.PunishLogDTO;
import io.nuls.poc.model.dto.output.WholeNetConsensusInfoDTO;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.service.ChainService;
import io.nuls.poc.utils.manager.*;
import io.nuls.poc.utils.validator.BatchValidator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 共识模块RPC接口实现类
 * Consensus Module RPC Interface Implementation Class
 *
 * @author tag
 * 2018/11/7
 */
@Component
public class ChainServiceImpl implements ChainService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private BatchValidator batchValidator;
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private DepositManager depositManager;

    /**
     * 批量验证共识模块交易
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result batchValid(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX_HEX_LIST) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            List<String> txHexList = (List<String>)params.get(ConsensusConstant.PARAM_TX_HEX_LIST);
            List<Transaction> txList = new ArrayList<>();
            for (String txHex : txHexList) {
                Transaction tx = new Transaction();
                tx.parse(RPCUtil.decode(txHex), 0);
                txList.add(tx);
            }
            batchValidator.batchValid(txList, chain);
            List<String> resultTxHashList = new ArrayList<>();
            for (Transaction tx : txList) {
                resultTxHashList.add(tx.getHash().toHex());
            }
            Map<String, Object> result = new HashMap<>(2);
            result.put("list",resultTxHashList);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public Result commitCmd(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX_HEX_LIST) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        Map<String, Object> result = new HashMap<>(2);
        result.put(ConsensusConstant.PARAM_RESULT_VALUE ,false);
        //List<Transaction> commitSuccessList = new ArrayList<>();
        BlockHeader blockHeader = new BlockHeader();
        try {
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX);
            blockHeader.parse(RPCUtil.decode(headerHex), 0);
            List<String> txHexList = (List<String>)params.get(ConsensusConstant.PARAM_TX_HEX_LIST);
            for (String txHex : txHexList) {
                Transaction tx = new Transaction();
                tx.parse(RPCUtil.decode(txHex), 0);
                if(!transactionCommit(tx,chain,blockHeader)){
                    result.put(ConsensusConstant.PARAM_RESULT_VALUE ,false);
                    return Result.getFailed(ConsensusErrorCode.SAVE_FAILED).setData(result);
                }
                /*if(transactionCommit(tx,chain,blockHeader)){
                    commitSuccessList.add(tx);
                }else{
                    transactionBatchRollBack(commitSuccessList,chain,blockHeader);
                    result.put(ConsensusConstant.PARAM_RESULT_VALUE ,false);
                    return Result.getFailed(ConsensusErrorCode.SAVE_FAILED).setData(result);
                }*/
            }
            result.put(ConsensusConstant.PARAM_RESULT_VALUE ,true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            /*try{
                transactionBatchRollBack(commitSuccessList,chain,blockHeader);
            }catch (NulsException re){
                chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(re);
            }*/
            result.put(ConsensusConstant.PARAM_RESULT_VALUE ,false);
            return Result.getFailed(e.getErrorCode()).setData(result);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result rollbackCmd(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX_HEX_LIST) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        Map<String, Object> result = new HashMap<>(2);
        result.put(ConsensusConstant.PARAM_RESULT_VALUE ,false);
        try {
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(headerHex), 0);
            List<String> txHexList = (List<String>)params.get(ConsensusConstant.PARAM_TX_HEX_LIST);
            for (String txHex : txHexList) {
                Transaction tx = new Transaction();
                tx.parse(RPCUtil.decode(txHex), 0);
                transactionRollback(tx,chain,blockHeader);
            }
            result.put(ConsensusConstant.PARAM_RESULT_VALUE ,true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode()).setData(result);
        }
    }

    /**
     * 区块分叉记录
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result addEvidenceRecord(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER) == null || params.get(ConsensusConstant.PARAM_EVIDENCE_HEADER) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            BlockHeader header = new BlockHeader();
            header.parse(RPCUtil.decode((String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER)), 0);
            BlockHeader evidenceHeader = new BlockHeader();
            evidenceHeader.parse(RPCUtil.decode((String) params.get(ConsensusConstant.PARAM_EVIDENCE_HEADER)), 0);
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).info("Received new bifurcation evidence:"+header.getHeight());
            punishManager.addEvidenceRecord(chain, header, evidenceHeader);
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put("value", true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 双花交易记录
     *
     * @param params
     * @return Result
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result doubleSpendRecord(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_BLOCK) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            Block block = new Block();
            block.parse(RPCUtil.decode((String) params.get(ConsensusConstant.PARAM_BLOCK)), 0);
            List<String> txHexList = JSONUtils.json2list((String) params.get(ConsensusConstant.PARAM_TX), String.class);
            List<Transaction> txList = new ArrayList<>();
            for (String txHex : txHexList) {
                Transaction tx = new Transaction();
                tx.parse(RPCUtil.decode(txHex), 0);
                txList.add(tx);
            }
            punishManager.addDoubleSpendRecord(chain, txList, block);
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put("value", true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 获取全网信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getWholeInfo(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
        List<Agent> agentList = chain.getAgentList();
        if (agentList == null) {
            return Result.getFailed(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        List<Agent> handleList = new ArrayList<>();
        //获取本地最新高度
        long startBlockHeight = chain.getNewestHeader().getHeight();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            handleList.add(agent);
        }
        MeetingRound round = roundManager.getCurrentRound(chain);
        BigInteger totalDeposit = BigInteger.ZERO;
        int packingAgentCount = 0;
        if (null != round) {
            for (MeetingMember member : round.getMemberList()) {
                totalDeposit = totalDeposit.add(member.getAgent().getDeposit().add(member.getAgent().getTotalDeposit()));
                if (member.getAgent() != null) {
                    packingAgentCount++;
                }
            }
        }
        dto.setAgentCount(handleList.size());
        dto.setTotalDeposit(String.valueOf(totalDeposit));
        dto.setConsensusAccountNumber(handleList.size());
        dto.setPackingAgentCount(packingAgentCount);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(dto);
    }

    /**
     * 获取指定账户信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getInfo(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_ADDRESS) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        String address = (String) params.get(ConsensusConstant.PARAM_ADDRESS);
        AccountConsensusInfoDTO dto = new AccountConsensusInfoDTO();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        long startBlockHeight = chain.getNewestHeader().getHeight();
        int agentCount = 0;
        String agentHash = null;
        byte[] addressBytes = AddressTool.getAddress(address);
        List<Agent> agentList = chain.getAgentList();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(agent.getAgentAddress(), addressBytes)) {
                //一个账户最多只能创建一个共识节点
                agentCount = 1;
                agentHash = agent.getTxHash().toHex();
                break;
            }
        }
        List<Deposit> depositList = chain.getDepositList();
        Set<NulsHash> agentSet = new HashSet<>();
        BigInteger totalDeposit = BigInteger.ZERO;
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
            totalDeposit = totalDeposit.add(deposit.getDeposit());
        }
        dto.setAgentCount(agentCount);
        dto.setAgentHash(agentHash);
        dto.setJoinAgentCount(agentSet.size());
        //todo 统计账户奖励金

        dto.setTotalDeposit(String.valueOf(totalDeposit));
        try {
            //todo 从账本模块获取账户可用余额
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            dto.setUsableBalance(BigIntegerUtils.ZERO);
        }
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(dto);
    }

    /**
     * 获取惩罚信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getPublishList(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchPunishDTO dto = JSONUtils.map2pojo(params, SearchPunishDTO.class);
        int chainId = dto.getChainId();
        String address = dto.getAddress();
        int type = dto.getType();
        if (chainId == 0 || StringUtils.isBlank(address)) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        List<PunishLogDTO> yellowPunishList = null;
        List<PunishLogDTO> redPunishList = null;
        //查询红牌交易
        if (type != 1) {
            redPunishList = new ArrayList<>();
            for (PunishLogPo po : chain.getRedPunishList()) {
                if (StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(), AddressTool.getAddress(address))) {
                    continue;
                }
                redPunishList.add(new PunishLogDTO(po));
            }
        } else if (type != 2) {
            yellowPunishList = new ArrayList<>();
            for (PunishLogPo po : chain.getYellowPunishList()) {
                if (StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(), AddressTool.getAddress(address))) {
                    continue;
                }
                yellowPunishList.add(new PunishLogDTO(po));
            }
        }
        Map<String, List<PunishLogDTO>> resultMap = new HashMap<>(2);
        resultMap.put("redPunish", redPunishList);
        resultMap.put("yellowPunish", yellowPunishList);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
    }

    /**
     * 获取当前轮次信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getCurrentRoundInfo(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MeetingRound round = roundManager.resetRound(chain, true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(round);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (Exception e){
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }


    /**
     * 获取指定区块轮次
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getRoundMemberList(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_EXTEND) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            BlockExtendsData extendsData = new BlockExtendsData(RPCUtil.decode((String)params.get(ConsensusConstant.PARAM_EXTEND)));
            MeetingRound round = roundManager.getRoundByIndex(chain, extendsData.getRoundIndex());
            if(round == null){
                round = roundManager.getRound(chain, extendsData, false);
            }
            List<String> packAddressList = new ArrayList<>();
            for (MeetingMember meetingMember:round.getMemberList()) {
                packAddressList.add(AddressTool.getStringAddressByBytes(meetingMember.getAgent().getPackingAddress()));
            }
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("packAddressList", packAddressList);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (Exception e){
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 获取种子节点列表
     *
     * @param params
     * @return Result
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getSeedNodeList(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("seedNodeList", chain.getConfig().getSeedNodes().split(","));
        resultMap.put("inflationAmount",chain.getConfig().getInflationAmount().toString());
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
    }

    /**
     * 停止一条链
     */
    @Override
    public Result stopChain(Map<String, Object> params) {
        return null;
    }

    /**
     * 启动一条新链
     */
    @Override
    public Result runChain(Map<String, Object> params) {
        return null;
    }

    @Override
    public Result runMainChain(Map<String, Object> params) {
        return null;
    }
    private boolean transactionCommit(Transaction tx, Chain chain, BlockHeader header)throws NulsException {
        switch (tx.getType()){
            case (TxType.REGISTER_AGENT):
            case (TxType.CONTRACT_CREATE_AGENT):
                return agentManager.createAgentCommit(tx, header, chain);
            case (TxType.STOP_AGENT):
            case (TxType.CONTRACT_STOP_AGENT):
                return agentManager.stopAgentCommit(tx, header, chain);
            case (TxType.DEPOSIT):
            case (TxType.CONTRACT_DEPOSIT):
                return depositManager.depositCommit(tx, header, chain);
            case (TxType.CANCEL_DEPOSIT):
            case (TxType.CONTRACT_CANCEL_DEPOSIT):
                return depositManager.cancelDepositCommit(tx, header, chain);
            case (TxType.YELLOW_PUNISH):
                return punishManager.yellowPunishCommit(tx, chain, header);
            case (TxType.RED_PUNISH):
                return punishManager.redPunishCommit(tx, chain, header);
            case (TxType.COIN_BASE):
                return true;
            default: return false;
        }
    }

    private void transactionBatchRollBack(List<Transaction> txList, Chain chain, BlockHeader header)throws NulsException{
        for (Transaction tx:txList) {
            transactionRollback(tx,chain,header);
        }
    }

    private boolean transactionRollback(Transaction tx,Chain chain,BlockHeader header)throws NulsException{
        switch (tx.getType()){
            case (TxType.REGISTER_AGENT):
            case (TxType.CONTRACT_CREATE_AGENT):
                return agentManager.createAgentRollBack(tx, chain);
            case (TxType.STOP_AGENT):
            case (TxType.CONTRACT_STOP_AGENT):
                return agentManager.stopAgentRollBack(tx, chain, header);
            case (TxType.DEPOSIT):
            case (TxType.CONTRACT_DEPOSIT):
                return depositManager.depositRollBack(tx, chain);
            case (TxType.CANCEL_DEPOSIT):
            case (TxType.CONTRACT_CANCEL_DEPOSIT):
                return depositManager.cancelDepositRollBack(tx, chain, header);
            case (TxType.YELLOW_PUNISH):
                return punishManager.yellowPunishRollback(tx, chain, header);
            case (TxType.RED_PUNISH):
                return punishManager.redPunishRollback(tx, chain, header);
            case (TxType.COIN_BASE):
                return true;
            default: return false;
        }
    }

}
