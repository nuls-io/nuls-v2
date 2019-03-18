package io.nuls.api.service;

import io.nuls.api.constant.ApiConstant;
import io.nuls.api.db.*;
import io.nuls.api.model.po.db.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;
import java.util.*;

@Component
public class RollbackService {
    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private PunishService punishService;
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ChainService chainService;

    //记录每个区块打包交易涉及到的账户的余额变动
    private Map<String, AccountInfo> accountInfoMap = new HashMap<>();
    //记录每个账户的资产变动
    private Map<String, AccountLedgerInfo> accountLedgerInfoMap = new HashMap<>();
    //记录每个区块代理节点的变化
    private List<AgentInfo> agentInfoList = new ArrayList<>();
    //记录每个区块交易和账户地址的关系
    private Set<TxRelationInfo> txRelationInfoSet = new HashSet<>();
    //记录每个区块设置别名信息
    private List<AliasInfo> aliasInfoList = new ArrayList<>();
    //记录每个区块委托共识的信息
    private List<DepositInfo> depositInfoList = new ArrayList<>();
    //记录每个区块的红黄牌信息
    private List<PunishLogInfo> punishLogList = new ArrayList<>();

    private List<CoinDataInfo> coinDataList = new ArrayList<>();

    public boolean rollbackBlock(int chainId, long blockHeight) {
        clear();

        BlockInfo blockInfo = queryBlock(chainId, blockHeight);
        if (blockInfo == null) {
            chainService.rollbackComplete(chainId);
            return true;
        }

        findAddProcessAgentOfBlock(chainId, blockInfo);

        processTxs(chainId, blockInfo.getTxList());

        return false;
    }

    private void findAddProcessAgentOfBlock(int chainId, BlockInfo blockInfo) {
        BlockHeaderInfo headerInfo = blockInfo.getHeader();
        AgentInfo agentInfo;
        if (headerInfo.isSeedPacked()) {
            //如果是种子节点打包的区块，则创建一个新的AgentInfo对象，临时使用
            //If it is a block packed by the seed node, create a new AgentInfo object for temporary use.
            agentInfo = new AgentInfo();
            agentInfo.setPackingAddress(headerInfo.getPackingAddress());
            agentInfo.setAgentId(headerInfo.getPackingAddress());
            agentInfo.setRewardAddress(agentInfo.getPackingAddress());
            headerInfo.setByAgentInfo(agentInfo);
        } else {
            //根据区块头的打包地址，查询打包节点的节点信息，修改相关统计数据
            //According to the packed address of the block header, query the node information of the packed node, and modify related statistics.
            agentInfo = queryAgentInfo(chainId, headerInfo.getPackingAddress(), 3);
            agentInfo.setTotalPackingCount(agentInfo.getTotalPackingCount() - 1);
            agentInfo.setLastRewardHeight(headerInfo.getHeight() - 1);
            agentInfo.setVersion(headerInfo.getAgentVersion());
            headerInfo.setByAgentInfo(agentInfo);

            if (blockInfo.getTxList() != null && !blockInfo.getTxList().isEmpty()) {
                calcCommissionReward(agentInfo, blockInfo.getTxList().get(0));
            }
        }
    }

    private void calcCommissionReward(AgentInfo agentInfo, TransactionInfo coinBaseTx) {
        List<CoinToInfo> list = coinBaseTx.getCoinTos();
        if (null == list || list.isEmpty()) {
            return;
        }

        BigInteger agentReward = BigInteger.ZERO, otherReward = BigInteger.ZERO;
        for (CoinToInfo output : list) {
            if (output.getAddress().equals(agentInfo.getRewardAddress())) {
                agentReward = agentReward.add(output.getAmount());
            } else {
                otherReward = otherReward.add(output.getAmount());
            }
        }
        agentInfo.setTotalReward(agentInfo.getTotalReward().subtract(agentReward).subtract(otherReward));
        agentInfo.setAgentReward(agentInfo.getAgentReward().subtract(agentReward));
        agentInfo.setCommissionReward(agentInfo.getCommissionReward().subtract(otherReward));
    }

    private void processTxs(int chainId, List<TransactionInfo> txs) {
        for (int i = 0; i < txs.size(); i++) {
            TransactionInfo tx = txs.get(i);
            if (tx.getType() == ApiConstant.TX_TYPE_COINBASE) {
                processCoinBaseTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_TRANSFER) {
                processTransferTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_ALIAS) {
                processAliasTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_REGISTER_AGENT) {
                processCreateAgentTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_JOIN_CONSENSUS) {
                processDepositTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CANCEL_DEPOSIT) {
                processCancelDepositTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_STOP_AGENT) {
                processStopAgentTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_YELLOW_PUNISH) {
                processYellowPunishTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_RED_PUNISH) {
                processRedPunishTx(chainId, tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CREATE_CONTRACT) {
                //                processCreateContract(tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CALL_CONTRACT) {
                //                processCallContract(tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_DELETE_CONTRACT) {
                //                processDeleteContract(tx);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CONTRACT_TRANSFER) {
                //                processContractTransfer(tx);
            }
        }
    }

    private void processCoinBaseTx(int chainId, TransactionInfo tx) {

    }

    private void processTransferTx(int chainId, TransactionInfo tx) {

    }
    private void processAliasTx(int chainId, TransactionInfo tx) {

    }
    private void processCreateAgentTx(int chainId, TransactionInfo tx) {

    }

    private void processDepositTx(int chainId, TransactionInfo tx) {

    }
    private void processCancelDepositTx(int chainId, TransactionInfo tx) {

    }
    private void processStopAgentTx(int chainId, TransactionInfo tx) {

    }

    private void processYellowPunishTx(int chainId, TransactionInfo tx) {

    }
    private void processRedPunishTx(int chainId, TransactionInfo tx) {

    }















    private BlockInfo queryBlock(int chainId, long blockHeight) {
        BlockHeaderInfo headerInfo = blockService.getBlockHeader(chainId, blockHeight);
        if (headerInfo == null) {
            return null;
        }
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setHeader(headerInfo);
        List<TransactionInfo> txList = new ArrayList<>();
        for (int i = 0; i < headerInfo.getTxHashList().size(); i++) {
            TransactionInfo tx = transactionService.getTx(chainId, headerInfo.getTxHashList().get(i));
            if (tx != null) {
                txList.add(tx);
            }
        }
        blockInfo.setTxList(txList);
        return blockInfo;
    }




    private AgentInfo queryAgentInfo(int chainId, String key, int type) {
        AgentInfo agentInfo;
        for (int i = 0; i < agentInfoList.size(); i++) {
            agentInfo = agentInfoList.get(i);

            if (type == 1 && agentInfo.getTxHash().equals(key)) {
                return agentInfo;
            } else if (type == 2 && agentInfo.getAgentAddress().equals(key)) {
                return agentInfo;
            } else if (type == 3 && agentInfo.getPackingAddress().equals(key)) {
                return agentInfo;
            }
        }
        if (type == 1) {
            agentInfo = agentService.getAgentByHash(chainId, key);
        } else if (type == 2) {
            agentInfo = agentService.getAgentByAgentAddress(chainId, key);
        } else {
            agentInfo = agentService.getAgentByPackingAddress(chainId, key);
        }
        if (agentInfo != null) {
            agentInfoList.add(agentInfo);
        }
        return agentInfo;
    }

    private void clear() {

    }


}
