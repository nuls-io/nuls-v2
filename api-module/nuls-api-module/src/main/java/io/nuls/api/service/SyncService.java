package io.nuls.api.service;


import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.constant.ApiErrorCode;
import io.nuls.api.db.*;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;

import java.math.BigInteger;
import java.util.*;

@Component
public class SyncService {

    @Autowired
    private ChainService chainService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountLedgerService ledgerService;
    @Autowired
    private TransactionService txService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private PunishService punishService;

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


    public SyncInfo getSyncInfo(int chainId) {
        return chainService.getSyncInfo(chainId);
    }

    public BlockHeaderInfo getBestBlockHeader(int chainId) {
        return blockService.getBestBlockHeader(chainId);
    }

    public boolean syncNewBlock(int chainId, BlockInfo blockInfo) throws Exception {
        clear();
        findAddProcessAgentOfBlock(chainId, blockInfo);
        //处理交易
        processTxs(chainId, blockInfo.getTxList());
        //保存数据
        save(chainId, blockInfo);

        ApiCache apiCache = CacheManager.getCache(chainId);
        apiCache.setBestHeader(blockInfo.getHeader());
        return true;
    }


    /**
     * 查找当前出块节点并处理相关信息
     * Find the current outbound node and process related information
     *
     * @return
     */
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
            agentInfo.setTotalPackingCount(agentInfo.getTotalPackingCount() + 1);
            agentInfo.setLastRewardHeight(headerInfo.getHeight());
            agentInfo.setVersion(headerInfo.getAgentVersion());
            headerInfo.setByAgentInfo(agentInfo);

            if (blockInfo.getTxList() != null && !blockInfo.getTxList().isEmpty()) {
                calcCommissionReward(agentInfo, blockInfo.getTxList().get(0));
            }
        }
    }

    /**
     * 分别记录当前块，代理节点自己的和委托人的奖励
     * Record the current block, the agent node's own and the principal's reward
     *
     * @param agentInfo
     * @param coinBaseTx
     */
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
        agentInfo.setTotalReward(agentInfo.getTotalReward().add(agentReward).add(otherReward));
        agentInfo.setAgentReward(agentInfo.getAgentReward().add(agentReward));
        agentInfo.setCommissionReward(agentInfo.getCommissionReward().add(otherReward));
    }

    /**
     * 处理各种交易
     *
     * @param txs
     * @throws Exception
     */
    private void processTxs(int chainId, List<TransactionInfo> txs) throws Exception {
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
        if (tx.getCoinTos() == null || tx.getCoinTos().isEmpty()) {
            return;
        }
        Set<String> addressSet = new HashSet<>();

        for (CoinToInfo output : tx.getCoinTos()) {
            addressSet.add(output.getAddress());
            AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), ledgerInfo.getTotalBalance()));
        }

        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        }
    }

    private void processTransferTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                addressSet.add(input.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), ledgerInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), ledgerInfo.getTotalBalance()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        }
    }

    private void processAliasTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                addressSet.add(input.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), ledgerInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), ledgerInfo.getTotalBalance()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        }

        AliasInfo aliasInfo = (AliasInfo) tx.getTxData();
        AccountInfo accountInfo = queryAccountInfo(chainId, aliasInfo.getAddress());
        accountInfo.setAlias(aliasInfo.getAlias());
        aliasInfoList.add(aliasInfo);
    }

    private void processCreateAgentTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        AccountLedgerInfo ledgerInfo = calcBalance(chainId, accountInfo, tx.getFee(), input);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), ledgerInfo.getTotalBalance()));

        AgentInfo agentInfo = (AgentInfo) tx.getTxData();
        agentInfo.setNew(true);
        //查询agent节点是否设置过别名
        AliasInfo aliasInfo = aliasService.getAliasByAddress(chainId, agentInfo.getAgentAddress());
        if (aliasInfo != null) {
            agentInfo.setAgentAlias(aliasInfo.getAlias());
        }
        agentInfoList.add(agentInfo);
    }

    private void processDepositTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        AccountLedgerInfo ledgerInfo = calcBalance(chainId, accountInfo, tx.getFee(), input);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), ledgerInfo.getTotalBalance()));

        DepositInfo depositInfo = (DepositInfo) tx.getTxData();
        depositInfo.setNew(true);
        depositInfoList.add(depositInfo);

        AgentInfo agentInfo = queryAgentInfo(chainId, depositInfo.getAgentHash(), 1);
        agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().add(depositInfo.getAmount()));
        agentInfo.setNew(false);
    }

    private void processCancelDepositTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        AccountLedgerInfo ledgerInfo = calcBalance(chainId, accountInfo, tx.getFee(), input);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), ledgerInfo.getTotalBalance()));

        //查询委托记录，生成对应的取消委托信息
        DepositInfo cancelInfo = (DepositInfo) tx.getTxData();
        DepositInfo depositInfo = depositService.getDepositInfoByKey(chainId, cancelInfo.getTxHash() + accountInfo.getAddress());

        cancelInfo.copyInfoWithDeposit(depositInfo);
        cancelInfo.setTxHash(tx.getHash());
        cancelInfo.setKey(tx.getHash() + depositInfo.getKey());
        cancelInfo.setBlockHeight(tx.getHeight());
        cancelInfo.setDeleteKey(depositInfo.getKey());
        cancelInfo.setNew(true);

        depositInfo.setDeleteKey(cancelInfo.getKey());
        depositInfo.setDeleteHeight(tx.getHeight());
        depositInfoList.add(depositInfo);
        depositInfoList.add(cancelInfo);

        AgentInfo agentInfo = queryAgentInfo(chainId, depositInfo.getAgentHash(), 1);
        agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().subtract(depositInfo.getAmount()));
        agentInfo.setNew(false);
        if (agentInfo.getTotalDeposit().compareTo(BigInteger.ZERO) < 0) {
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "data error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
        }
    }

    private void processStopAgentTx(int chainId, TransactionInfo tx) {
        AgentInfo agentInfo = (AgentInfo) tx.getTxData();
        agentInfo = queryAgentInfo(chainId, agentInfo.getTxHash(), 1);
        agentInfo.setDeleteHash(tx.getHash());
        agentInfo.setDeleteHeight(tx.getHeight());
        agentInfo.setStatus(ApiConstant.STOP_AGENT);
        agentInfo.setNew(false);

        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
            if (accountInfo.getAddress().equals(agentInfo.getAgentAddress())) {
                accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(tx.getFee()));
                if (accountInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
                    throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + accountInfo.getAddress() + "] totalBalance < 0");
                }
            }
            AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), ledgerInfo.getTotalBalance()));
        }
        //查询所有当前节点下的委托，生成取消委托记录
        List<DepositInfo> depositInfos = depositService.getDepositListByAgentHash(chainId, agentInfo.getTxHash());
        for (DepositInfo depositInfo : depositInfos) {
            DepositInfo cancelDeposit = new DepositInfo();
            cancelDeposit.setNew(true);
            cancelDeposit.setType(ApiConstant.CANCEL_CONSENSUS);
            cancelDeposit.copyInfoWithDeposit(depositInfo);
            cancelDeposit.setKey(tx.getHash() + depositInfo.getKey());
            cancelDeposit.setTxHash(tx.getHash());
            cancelDeposit.setBlockHeight(tx.getHeight());
            cancelDeposit.setDeleteKey(depositInfo.getKey());
            cancelDeposit.setFee(BigInteger.ZERO);
            cancelDeposit.setCreateTime(tx.getCreateTime());

            depositInfo.setDeleteKey(cancelDeposit.getKey());
            depositInfo.setDeleteHeight(tx.getHeight());
            depositInfoList.add(depositInfo);
            depositInfoList.add(cancelDeposit);
            agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().subtract(depositInfo.getAmount()));
            if (agentInfo.getTotalDeposit().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "data error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
            }
        }
    }

    public void processYellowPunishTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();
        for (TxDataInfo txData : tx.getTxDataList()) {
            PunishLogInfo punishLog = (PunishLogInfo) txData;
            punishLogList.add(punishLog);
            addressSet.add(punishLog.getAddress());
        }

        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
            txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, chainInfo.getChainId(), chainInfo.getDefaultAsset().getAssetId(), BigInteger.ZERO, accountInfo.getTotalBalance()));
        }
    }

    public void processRedPunishTx(int chainId, TransactionInfo tx) {
        PunishLogInfo redPunish = (PunishLogInfo) tx.getTxData();
        punishLogList.add(redPunish);

        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
            txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, chainInfo.getChainId(), chainInfo.getDefaultAsset().getAssetId(), output.getAmount(), accountInfo.getTotalBalance()));
        }

        //根据红牌找到被惩罚的节点
        AgentInfo agentInfo = queryAgentInfo(chainId, redPunish.getAddress(), 2);
        agentInfo.setDeleteHash(tx.getHash());
        agentInfo.setDeleteHeight(tx.getHeight());
        agentInfo.setStatus(ApiConstant.STOP_AGENT);
        agentInfo.setNew(false);

        //根据节点找到委托列表
        List<DepositInfo> depositInfos = depositService.getDepositListByAgentHash(chainId, agentInfo.getTxHash());
        if (!depositInfos.isEmpty()) {
            for (DepositInfo depositInfo : depositInfos) {
                DepositInfo cancelDeposit = new DepositInfo();
                cancelDeposit.setNew(true);
                cancelDeposit.setType(ApiConstant.CANCEL_CONSENSUS);
                cancelDeposit.copyInfoWithDeposit(depositInfo);
                cancelDeposit.setKey(tx.getHash() + depositInfo.getKey());
                cancelDeposit.setTxHash(tx.getHash());
                cancelDeposit.setBlockHeight(tx.getHeight());
                cancelDeposit.setDeleteKey(depositInfo.getKey());
                cancelDeposit.setFee(BigInteger.ZERO);
                cancelDeposit.setCreateTime(tx.getCreateTime());

                depositInfo.setDeleteKey(cancelDeposit.getKey());
                depositInfo.setDeleteHeight(tx.getHeight());
                depositInfoList.add(depositInfo);
                depositInfoList.add(cancelDeposit);

                agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().subtract(depositInfo.getAmount()));
                if (agentInfo.getTotalDeposit().compareTo(BigInteger.ZERO) < 0) {
                    throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "data error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
                }
            }
        }
    }

    private AccountLedgerInfo calcBalance(int chainId, CoinToInfo output) {
        ChainInfo chainInfo = CacheManager.getChainInfo(chainId);
        if (output.getChainId() == chainInfo.getChainId() && output.getAssetsId() == chainInfo.getDefaultAsset().getAssetId()) {
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            accountInfo.setTotalIn(accountInfo.getTotalIn().add(output.getAmount()));
            accountInfo.setTotalBalance(accountInfo.getTotalBalance().add(output.getAmount()));
        }

        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().add(output.getAmount()));
        return ledgerInfo;
    }

    private AccountLedgerInfo calcBalance(int chainId, CoinFromInfo input) {
        ChainInfo chainInfo = CacheManager.getChainInfo(chainId);
        if (input.getChainId() == chainInfo.getChainId() && input.getAssetsId() == chainInfo.getDefaultAsset().getAssetId()) {
            AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
            accountInfo.setTotalIn(accountInfo.getTotalIn().subtract(input.getAmount()));
            accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(input.getAmount()));
            if (accountInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + accountInfo.getAddress() + "] totalBalance < 0");
            }
        }
        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().subtract(input.getAmount()));
        if (ledgerInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "accountLedger[" + ledgerInfo.getAddress() + "_" + ledgerInfo.getChainId() + "_" + ledgerInfo.getAssetId() + "] totalBalance < 0");
        }
        return ledgerInfo;
    }

    private AccountLedgerInfo calcBalance(int chainId, AccountInfo accountInfo, BigInteger fee, CoinFromInfo input) {
        accountInfo.setTotalOut(accountInfo.getTotalOut().add(fee));
        accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(fee));
        if (accountInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + accountInfo.getAddress() + "] totalBalance < 0");
        }

        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().subtract(fee));
        if (ledgerInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "accountLedger[" + ledgerInfo.getAddress() + "_" + ledgerInfo.getChainId() + "_" + ledgerInfo.getAssetId() + "] totalBalance < 0");
        }
        return ledgerInfo;
    }


    /**
     * 解析区块和所有交易后，将数据存储到数据库中
     * Store data in the database after parsing the block and all transactions
     */
    public void save(int chainId, BlockInfo blockInfo) throws Exception {
        long height = blockInfo.getHeader().getHeight();
        chainService.saveNewSyncInfo(chainId, height);

        //存储区块头信息
        blockService.saveBLockHeaderInfo(chainId, blockInfo.getHeader());
        //存储交易记录
        txService.saveTxList(chainId, blockInfo.getTxList());
        //存储交易和地址关系记录
        txService.saveTxRelationList(chainId, txRelationInfoSet);
        //存储别名记录
        aliasService.saveAliasList(chainId, aliasInfoList);
        //存储红黄牌惩罚记录
        punishService.savePunishList(chainId, punishLogList);
        //存储委托/取消委托记录
        depositService.saveDepositList(chainId, depositInfoList);
        chainService.updateStep(chainId, height, 10);
        /*
            涉及到统计类的表放在最后来存储，便于回滚
         */
        //存储共识节点列表
        agentService.saveAgentList(chainId, agentInfoList);
        chainService.updateStep(chainId, height, 20);

        //存储账户资产信息
        ledgerService.saveLedgerList(chainId, accountLedgerInfoMap);
        chainService.updateStep(chainId, height, 30);

        //修改账户信息表
        accountService.saveAccounts(chainId, accountInfoMap);
        //完成解析
        chainService.syncComplete(chainId, height, 100);
    }


    private AccountInfo queryAccountInfo(int chainId, String address) {
        AccountInfo accountInfo = accountInfoMap.get(address);
        if (accountInfo == null) {
            accountInfo = accountService.getAccountInfo(chainId, address);
            if (accountInfo == null) {
                accountInfo = new AccountInfo(address);
            }
            accountInfoMap.put(address, accountInfo);
        }
        return accountInfo;
    }

    private AccountLedgerInfo queryLedgerInfo(int defaultChainId, String address, int chainId, int assetId) {
        String key = address + chainId + assetId;
        AccountLedgerInfo ledgerInfo = accountLedgerInfoMap.get(key);
        if (ledgerInfo == null) {
            ledgerInfo = ledgerService.getAccountLedgerInfo(defaultChainId, key);
            if (ledgerInfo == null) {
                ledgerInfo = new AccountLedgerInfo(address, chainId, assetId);
            }
            accountLedgerInfoMap.put(key, ledgerInfo);
        }
        return ledgerInfo;
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
            agentInfo = agentService.getAgentByAgentHash(chainId, key);
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
        accountInfoMap.clear();
        accountLedgerInfoMap.clear();
        agentInfoList.clear();
        txRelationInfoSet.clear();
        aliasInfoList.clear();
        depositInfoList.clear();
        punishLogList.clear();
    }
}
