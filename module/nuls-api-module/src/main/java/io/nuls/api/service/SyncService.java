package io.nuls.api.service;


import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.constant.ApiErrorCode;
import io.nuls.api.db.*;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.api.constant.ApiConstant.*;

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
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private ContractService contractService;
    @Autowired
    private TokenService tokenService;

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
    //记录每个区块新创建的智能合约信息
    private Map<String, ContractInfo> contractInfoMap = new HashMap<>();
    //记录智能合约执行结果
    private List<ContractResultInfo> contractResultList = new ArrayList<>();
    //记录智能合约相关的交易信息
    private List<ContractTxInfo> contractTxInfoList = new ArrayList<>();
    //记录每个区块智能合约相关的账户token信息
    private Map<String, AccountTokenInfo> accountTokenMap = new HashMap<>();
    //记录合约转账信息
    private List<TokenTransfer> tokenTransferList = new ArrayList<>();
    //记录链信息
    private List<ChainInfo> chainInfoList = new ArrayList<>();

    public SyncInfo getSyncInfo(int chainId) {
        return chainService.getSyncInfo(chainId);
    }

    public BlockHeaderInfo getBestBlockHeader(int chainId) {
        return blockService.getBestBlockHeader(chainId);
    }


    public boolean syncNewBlock(int chainId, BlockInfo blockInfo) {
        if (blockInfo.getHeader().getHeight() > 100) {
            return false;
        }
        LoggerUtil.commonLog.info("-----height finish:" + blockInfo.getHeader().getHeight());
        clear();
        long time1, time2;
        time1 = System.currentTimeMillis();

        findAddProcessAgentOfBlock(chainId, blockInfo);
        //处理交易
        processTxs(chainId, blockInfo.getTxList());
        //处理交易
        roundManager.process(chainId, blockInfo);
        //保存数据
        save(chainId, blockInfo);
        time2 = System.currentTimeMillis();

        ApiCache apiCache = CacheManager.getCache(chainId);
        apiCache.setBestHeader(blockInfo.getHeader());
        LoggerUtil.commonLog.info("-----height finish:" + blockInfo.getHeader().getHeight() + "-----txCount:" + blockInfo.getHeader().getTxCount() + "-----use:" + (time2 - time1) + "-----");
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
     */
    private void processTxs(int chainId, List<TransactionInfo> txs) {
        for (int i = 0; i < txs.size(); i++) {
            TransactionInfo tx = txs.get(i);
            tx.setStatus(TX_CONFIRM);
            CoinDataInfo coinDataInfo = new CoinDataInfo(tx.getHash(), tx.getCoinFroms(), tx.getCoinTos());
            coinDataList.add(coinDataInfo);

            if (tx.getType() == TxType.COIN_BASE || tx.getType() == TxType.CONTRACT_RETURN_GAS) {
                processCoinBaseTx(chainId, tx);
            } else if (tx.getType() == TxType.TRANSFER || tx.getType() == TxType.CONTRACT_TRANSFER) {
                processTransferTx(chainId, tx);
            } else if (tx.getType() == TxType.ACCOUNT_ALIAS) {
                processAliasTx(chainId, tx);
            } else if (tx.getType() == TxType.REGISTER_AGENT || tx.getType() == TxType.CONTRACT_CREATE_AGENT) {
                processCreateAgentTx(chainId, tx);
            } else if (tx.getType() == TxType.DEPOSIT || tx.getType() == TxType.CONTRACT_DEPOSIT) {
                processDepositTx(chainId, tx);
            } else if (tx.getType() == TxType.CANCEL_DEPOSIT || tx.getType() == TxType.CONTRACT_CANCEL_DEPOSIT) {
                processCancelDepositTx(chainId, tx);
            } else if (tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.CONTRACT_STOP_AGENT) {
                processStopAgentTx(chainId, tx);
            } else if (tx.getType() == TxType.YELLOW_PUNISH) {
                processYellowPunishTx(chainId, tx);
            } else if (tx.getType() == TxType.RED_PUNISH) {
                processRedPunishTx(chainId, tx);
            } else if (tx.getType() == TxType.CREATE_CONTRACT) {
                processCreateContract(chainId, tx);
            } else if (tx.getType() == TxType.CALL_CONTRACT) {
                processCallContract(chainId, tx);
            } else if (tx.getType() == TxType.DELETE_CONTRACT) {
                processDeleteContract(chainId, tx);
            } else if (tx.getType() == TxType.CROSS_CHAIN) {
                processCrossTransferTx(chainId, tx);
            } else if (tx.getType() == TxType.REGISTER_CHAIN_AND_ASSET) {
                processRegChainTx(chainId, tx);
            } else if (tx.getType() == TxType.DESTROY_CHAIN_AND_ASSET) {
                processDestoryChainTx(chainId, tx);
            } else if (tx.getType() == TxType.ADD_ASSET_TO_CHAIN) {
                processAddAssetTx(chainId, tx);
            } else if (tx.getType() == TxType.REMOVE_ASSET_FROM_CHAIN) {
                processCancelAssetTx(chainId, tx);
            }
        }
    }

    private void processCoinBaseTx(int chainId, TransactionInfo tx) {
        if (tx.getCoinTos() == null || tx.getCoinTos().isEmpty()) {
            return;
        }
        Set<String> addressSet = new HashSet<>();

        ApiCache apiCache = CacheManager.getCache(chainId);
        AssetInfo assetInfo = apiCache.getChainInfo().getDefaultAsset();
        for (CoinToInfo output : tx.getCoinTos()) {
            addressSet.add(output.getAddress());
            AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getSymbol(), output.getAmount(), TRANSFER_TO_TYPE, ledgerInfo.getTotalBalance()));

            //奖励是本链主资产的时候，累计奖励金额
            if (assetInfo.getChainId() == output.getChainId() && assetInfo.getAssetId() == output.getAssetsId()) {
                AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
                accountInfo.setTotalReward(accountInfo.getTotalReward().add(output.getAmount()));
            }
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
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_FROM_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getSymbol(), output.getAmount(), TRANSFER_TO_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        }
    }

    private void processCrossTransferTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                if (chainId != AddressTool.getChainIdByAddress(input.getAddress())) {
                    continue;
                }
                addressSet.add(input.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_FROM_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                if (chainId != AddressTool.getChainIdByAddress(output.getAddress())) {
                    continue;
                }
                addressSet.add(output.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getSymbol(), output.getAmount(), TRANSFER_TO_TYPE, ledgerInfo.getTotalBalance()));
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
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_FROM_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                AccountLedgerInfo ledgerInfo = calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getSymbol(), output.getAmount(), TRANSFER_TO_TYPE, ledgerInfo.getTotalBalance()));
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
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));

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
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));

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
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), input.getAmount(), TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));

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
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "entity error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
        }
    }

    private void processStopAgentTx(int chainId, TransactionInfo tx) {
        AgentInfo agentInfo = (AgentInfo) tx.getTxData();
        agentInfo = queryAgentInfo(chainId, agentInfo.getTxHash(), 1);
        agentInfo.setDeleteHash(tx.getHash());
        agentInfo.setDeleteHeight(tx.getHeight());
        agentInfo.setStatus(ApiConstant.STOP_AGENT);
        agentInfo.setNew(false);

        ApiCache apiCache = CacheManager.getCache(chainId);
        AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
        BigInteger agentAmount = BigInteger.ZERO;
        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            //如果是代理节点需要特殊处理，因为代理节点可能包含2条output，一条是创建节点的保证金，一条是委托总额
            if (accountInfo.getAddress().equals(agentInfo.getAgentAddress())) {
                if (output.getLockTime() > 0) {
                    accountInfo.setTxCount(accountInfo.getTxCount() + 1);
                    accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(tx.getFee()));
                    if (accountInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
                        throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + accountInfo.getAddress() + "] totalBalance < 0");
                    }
                }
                agentAmount = agentAmount.add(output.getAmount());
            } else {
                accountInfo.setTxCount(accountInfo.getTxCount() + 1);
                AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, output.getAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, defaultAsset.getChainId(), defaultAsset.getAssetId(), defaultAsset.getSymbol(), output.getAmount(), TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, agentInfo.getAgentAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
        txRelationInfoSet.add(new TxRelationInfo(agentInfo.getAgentAddress(), tx, defaultAsset.getChainId(), defaultAsset.getAssetId(), defaultAsset.getSymbol(), agentAmount, TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));

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
                throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "entity error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
            }
        }
    }

    public void processYellowPunishTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();
        for (TxDataInfo txData : tx.getTxDataList()) {
            PunishLogInfo punishLog = (PunishLogInfo) txData;
            punishLogList.add(punishLog);
            addressSet.add(punishLog.getAddress());
//            AgentInfo agentInfo = queryAgentInfo(chainId, punishLog.getAddress(), 2);
        }

        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() + 1);
            txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, chainInfo.getChainId(), chainInfo.getDefaultAsset().getAssetId(), chainInfo.getDefaultAsset().getSymbol(), BigInteger.ZERO, TRANSFER_NO_TYPE, accountInfo.getTotalBalance()));
        }
    }

    public void processRedPunishTx(int chainId, TransactionInfo tx) {
        PunishLogInfo redPunish = (PunishLogInfo) tx.getTxData();
        punishLogList.add(redPunish);
        //根据红牌找到被惩罚的节点
        AgentInfo agentInfo = queryAgentInfo(chainId, redPunish.getAddress(), 2);
        agentInfo.setDeleteHash(tx.getHash());
        agentInfo.setDeleteHeight(tx.getHeight());
        agentInfo.setStatus(ApiConstant.STOP_AGENT);
        agentInfo.setNew(false);

        ApiCache apiCache = CacheManager.getCache(chainId);
        AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
        BigInteger agentAmount = BigInteger.ZERO;
        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            if (accountInfo.getAddress().equals(agentInfo.getAgentAddress())) {
                if (output.getLockTime() > 0) {
                    accountInfo.setTxCount(accountInfo.getTxCount() + 1);
                }
                agentAmount = agentAmount.add(output.getAmount());
            } else {
                accountInfo.setTxCount(accountInfo.getTxCount() + 1);
                AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, accountInfo.getAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
                txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, defaultAsset.getChainId(), defaultAsset.getAssetId(), defaultAsset.getSymbol(), output.getAmount(), TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));
            }
        }
        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, agentInfo.getAgentAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
        txRelationInfoSet.add(new TxRelationInfo(agentInfo.getAgentAddress(), tx, defaultAsset.getChainId(), defaultAsset.getAssetId(), defaultAsset.getSymbol(), agentAmount, TRANSFER_NO_TYPE, ledgerInfo.getTotalBalance()));

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
                    throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "entity error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
                }
            }
        }
    }

    private void processCreateContract(int chainId, TransactionInfo tx) {
        CoinFromInfo coinFromInfo = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, coinFromInfo.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        accountInfo.setTotalOut(accountInfo.getTotalOut().add(tx.getFee()));
        accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(tx.getFee()));
        txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, coinFromInfo.getChainId(), coinFromInfo.getAssetsId(), coinFromInfo.getSymbol(), BigInteger.ZERO, TRANSFER_NO_TYPE, accountInfo.getTotalBalance()));

        ContractInfo contractInfo = (ContractInfo) tx.getTxData();
        contractInfo.setTxCount(1);
        contractInfo.setNew(true);
        contractInfo.setRemark(tx.getRemark());
        createContractTxInfo(tx, contractInfo);
        contractInfoMap.put(contractInfo.getContractAddress(), contractInfo);

        if (contractInfo.isSuccess() && contractInfo.isNrc20()) {
            processTokenTransfers(chainId, contractInfo.getResultInfo().getTokenTransfers(), tx);
        }
    }

    private void processCallContract(int chainId, TransactionInfo tx) {
        processTransferTx(chainId, tx);
        ContractCallInfo callInfo = (ContractCallInfo) tx.getTxData();
        ContractInfo contractInfo = queryContractInfo(chainId, callInfo.getContractAddress());
        contractInfo.setTxCount(contractInfo.getTxCount() + 1);

        contractResultList.add(callInfo.getResultInfo());
        createContractTxInfo(tx, contractInfo);

        if (callInfo.getResultInfo().isSuccess() && contractInfo.isNrc20()) {
            processTokenTransfers(chainId, callInfo.getResultInfo().getTokenTransfers(), tx);
        }
    }

    private void processDeleteContract(int chainId, TransactionInfo tx) {
        CoinFromInfo coinFromInfo = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, coinFromInfo.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        accountInfo.setTotalOut(accountInfo.getTotalOut().add(tx.getFee()));
        accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(tx.getFee()));
        txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx, coinFromInfo.getChainId(), coinFromInfo.getAssetsId(), coinFromInfo.getSymbol(), BigInteger.ZERO, TRANSFER_NO_TYPE, accountInfo.getTotalBalance()));

        ContractDeleteInfo deleteInfo = (ContractDeleteInfo) tx.getTxData();
        ContractResultInfo resultInfo = deleteInfo.getResultInfo();
        ContractInfo contractInfo = queryContractInfo(chainId, resultInfo.getContractAddress());
        contractInfo.setTxCount(contractInfo.getTxCount() + 1);

        contractResultList.add(resultInfo);
        createContractTxInfo(tx, contractInfo);
        if (resultInfo.isSuccess()) {
            contractInfo.setStatus(ApiConstant.CONTRACT_STATUS_DELETE);
        }
    }

    private void processRegChainTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);

        CoinToInfo output = null;
        for (CoinToInfo to : tx.getCoinTos()) {
            if (!to.getAddress().equals(accountInfo.getAddress())) {
                output = to;
                break;
            }
        }
        AccountLedgerInfo ledgerInfo = calcBalance(chainId, accountInfo, tx.getFee().add(output.getAmount()), input);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getSymbol(), output.getAmount(), TRANSFER_FROM_TYPE, ledgerInfo.getTotalBalance()));

        AccountInfo destroyAccount = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(destroyAccount.getTxCount() + 1);
        ledgerInfo = calcBalance(chainId, output);
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getSymbol(), output.getAmount(), TRANSFER_TO_TYPE, ledgerInfo.getTotalBalance()));

        chainInfoList.add((ChainInfo) tx.getTxData());
    }

    private void processDestoryChainTx(int chainId, TransactionInfo tx) {
        ChainInfo chainInfo = CacheManager.getChainInfo(chainId);
        chainInfo.setStatus(DISABLE);
        for (AssetInfo assetInfo : chainInfo.getAssets()) {
            assetInfo.setStatus(DISABLE);
        }
        chainInfo.getDefaultAsset().setStatus(DISABLE);
        chainInfo.setNew(false);
        chainInfoList.add((ChainInfo) tx.getTxData());
    }

    private void processAddAssetTx(int chainId, TransactionInfo tx) {
        AssetInfo assetInfo = (AssetInfo) tx.getTxData();

        ChainInfo chainInfo = CacheManager.getChainInfo(chainId);
        chainInfo.getAssets().add(assetInfo);
        chainInfo.setNew(false);
        chainInfoList.add((ChainInfo) tx.getTxData());
    }

    private void processCancelAssetTx(int chainId, TransactionInfo tx) {
        AssetInfo assetInfo = (AssetInfo) tx.getTxData();

        ChainInfo chainInfo = CacheManager.getChainInfo(chainId);
        chainInfo.getAsset(assetInfo.getAssetId()).setStatus(DISABLE);
        chainInfo.setNew(false);
        chainInfoList.add((ChainInfo) tx.getTxData());
    }

    private void processTokenTransfers(int chainId, List<TokenTransfer> tokenTransfers, TransactionInfo tx) {
        if (tokenTransfers.isEmpty()) {
            return;
        }
        AccountTokenInfo tokenInfo;
        TokenTransfer tokenTransfer;
        ContractInfo contractInfo;
        for (int i = 0; i < tokenTransfers.size(); i++) {
            tokenTransfer = tokenTransfers.get(i);
            tokenTransfer.setTxHash(tx.getHash());
            tokenTransfer.setHeight(tx.getHeight());
            tokenTransfer.setTime(tx.getCreateTime());

            contractInfo = queryContractInfo(chainId, tokenTransfer.getContractAddress());
            if (!contractInfo.getOwners().contains(tokenTransfer.getToAddress())) {
                contractInfo.getOwners().add(tokenTransfer.getToAddress());
            }
            contractInfo.setTransferCount(contractInfo.getTransferCount() + 1);

            if (tokenTransfer.getFromAddress() != null) {
                tokenInfo = processAccountNrc20(chainId, contractInfo, tokenTransfer.getFromAddress(), new BigInteger(tokenTransfer.getValue()), -1);
                tokenTransfer.setFromBalance(tokenInfo.getBalance().toString());
            }
            tokenInfo = processAccountNrc20(chainId, contractInfo, tokenTransfer.getToAddress(), new BigInteger(tokenTransfer.getValue()), 1);
            tokenTransfer.setToBalance(tokenInfo.getBalance().toString());

            tokenTransferList.add(tokenTransfer);
        }
    }

    private AccountTokenInfo processAccountNrc20(int chainId, ContractInfo contractInfo, String address, BigInteger value, int type) {
        AccountTokenInfo tokenInfo = queryAccountTokenInfo(chainId, address + contractInfo.getContractAddress());
        if (tokenInfo == null) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.getTokens().add(contractInfo.getContractAddress() + "," + contractInfo.getSymbol());

            tokenInfo = new AccountTokenInfo(address, contractInfo.getContractAddress(), contractInfo.getTokenName(), contractInfo.getSymbol(), contractInfo.getDecimals());
        }

        if (type == 1) {
            tokenInfo.setBalance(tokenInfo.getBalance().add(value));
        } else {
            tokenInfo.setBalance(tokenInfo.getBalance().subtract(value));
        }

        if (tokenInfo.getBalance().compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("data error: " + address + " token[" + contractInfo.getSymbol() + "] balance < 0");
        }
        if (!accountTokenMap.containsKey(tokenInfo.getKey())) {
            accountTokenMap.put(tokenInfo.getKey(), tokenInfo);
        }

        return tokenInfo;
    }

    private void createContractTxInfo(TransactionInfo tx, ContractInfo contractInfo) {
        ContractTxInfo contractTxInfo = new ContractTxInfo();
        contractTxInfo.setTxHash(tx.getHash());
        contractTxInfo.setBlockHeight(tx.getHeight());
        contractTxInfo.setContractAddress(contractInfo.getContractAddress());
        contractTxInfo.setTime(tx.getCreateTime());
        contractTxInfo.setType(tx.getType());
        contractTxInfo.setFee(tx.getFee().toString());

        contractTxInfoList.add(contractTxInfo);
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
            accountInfo.setTotalOut(accountInfo.getTotalOut().add(input.getAmount()));
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
     * Store entity in the database after parsing the block and all transactions
     */
    public void save(int chainId, BlockInfo blockInfo) {
        long height = blockInfo.getHeader().getHeight();
        int count = blockInfo.getHeader().getTxCount();

        SyncInfo syncInfo = chainService.saveNewSyncInfo(chainId, height);
        //存储区块头信息
        blockService.saveBLockHeaderInfo(chainId, blockInfo.getHeader());
        //存储交易记录
        txService.saveTxList(chainId, blockInfo.getTxList());
        // txService.saveCoinDataList(chainId, coinDataList);
        //存储交易和地址关系记录
        txService.saveTxRelationList(chainId, txRelationInfoSet);
        //存储别名记录
        aliasService.saveAliasList(chainId, aliasInfoList);
        //存储红黄牌惩罚记录
        punishService.savePunishList(chainId, punishLogList);
        //存储委托/取消委托记录
        depositService.saveDepositList(chainId, depositInfoList);
        //存储智能合约交易关系记录
        contractService.saveContractTxInfos(chainId, contractTxInfoList);
        //存储智能合约结果记录
        contractService.saveContractResults(chainId, contractResultList);
        //存储token转账信息
        tokenService.saveTokenTransfers(chainId, tokenTransferList);
        //存储链信息
        chainService.saveChainList(chainInfoList);
        /*
            涉及到统计类的表放在最后来存储，便于回滚
         */
        //存储共识节点列表
        syncInfo.setStep(10);
        chainService.updateStep(syncInfo);
        agentService.saveAgentList(chainId, agentInfoList);
        //存储账户资产信息
        syncInfo.setStep(20);
        chainService.updateStep(syncInfo);
        ledgerService.saveLedgerList(chainId, accountLedgerInfoMap);
        //存储智能合约信息表
        syncInfo.setStep(30);
        chainService.updateStep(syncInfo);
        contractService.saveContractInfos(chainId, contractInfoMap);
        //存储账户token信息
        syncInfo.setStep(40);
        chainService.updateStep(syncInfo);
        tokenService.saveAccountTokens(chainId, accountTokenMap);
        //存储账户信息表
        syncInfo.setStep(50);
        chainService.updateStep(syncInfo);
        accountService.saveAccounts(chainId, accountInfoMap);
        //完成解析
        syncInfo.setStep(100);
        chainService.updateStep(syncInfo);
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

    private ContractInfo queryContractInfo(int chainId, String contractAddress) {
        ContractInfo contractInfo = contractInfoMap.get(contractAddress);
        if (contractInfo == null) {
            contractInfo = contractService.getContractInfo(chainId, contractAddress);
            contractInfoMap.put(contractInfo.getContractAddress(), contractInfo);
        }
        return contractInfo;
    }

    private AccountTokenInfo queryAccountTokenInfo(int chainId, String key) {
        AccountTokenInfo accountTokenInfo = accountTokenMap.get(key);
        if (accountTokenInfo == null) {
            accountTokenInfo = tokenService.getAccountTokenInfo(chainId, key);
        }
        return accountTokenInfo;
    }

    private void clear() {
        accountInfoMap.clear();
        accountLedgerInfoMap.clear();
        agentInfoList.clear();
        txRelationInfoSet.clear();
        aliasInfoList.clear();
        depositInfoList.clear();
        punishLogList.clear();
        coinDataList.clear();
        contractInfoMap.clear();
        contractResultList.clear();
        contractTxInfoList.clear();
        accountTokenMap.clear();
        tokenTransferList.clear();
        chainInfoList.clear();
    }
}
