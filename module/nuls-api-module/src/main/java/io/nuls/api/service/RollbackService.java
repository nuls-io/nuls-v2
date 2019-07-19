package io.nuls.api.service;

import io.nuls.api.analysis.AnalysisHandler;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.constant.ApiErrorCode;
import io.nuls.api.db.*;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.utils.DBUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.api.constant.ApiConstant.ENABLE;

@Component
public class RollbackService {
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
    private TokenService tokenService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private AccountLedgerService ledgerService;

    //记录每个区块打包交易涉及到的账户的余额变动
    private Map<String, AccountInfo> accountInfoMap = new HashMap<>();
    //记录每个账户的资产变动
    private Map<String, AccountLedgerInfo> accountLedgerInfoMap = new HashMap<>();
    //记录每个区块代理节点的变化
    private List<AgentInfo> agentInfoList = new ArrayList<>();
    //记录每个区块设置别名信息
    private List<AliasInfo> aliasInfoList = new ArrayList<>();
    //记录每个区块委托共识的信息
    private List<DepositInfo> depositInfoList = new ArrayList<>();
    //记录惩罚交易的hash
    private List<String> punishTxHashList = new ArrayList<>();
    //记录每个区块新创建的智能合约信息
    private Map<String, ContractInfo> contractInfoMap = new HashMap<>();
    //记录智能合约相关的交易信息
    private List<String> contractTxHashList = new ArrayList<>();
    //记录每个区块智能合约相关的账户token信息
    private Map<String, AccountTokenInfo> accountTokenMap = new HashMap<>();
    //记录合约转账信息
    private List<String> tokenTransferHashList = new ArrayList<>();
    //记录链信息
    private List<ChainInfo> chainInfoList = new ArrayList<>();
    //记录每个区块交易和账户地址的关系
    private Set<TxRelationInfo> txRelationInfoSet = new HashSet<>();

    public boolean rollbackBlock(int chainId, long blockHeight) {
        clear();
        BlockHexInfo blockHexInfo = blockService.getBlockHexInfo(chainId, blockHeight);
        if (blockHexInfo == null) {
            blockService.deleteBlockHeader(chainId, blockHeight);
            return true;
        }

        BlockInfo blockInfo;
        try {
            blockInfo = AnalysisHandler.toBlockInfo(blockHexInfo.getBlockHex(), chainId);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }

        findAddProcessAgentOfBlock(chainId, blockInfo);

        processTxs(chainId, blockInfo.getTxList());

        roundManager.rollback(chainId, blockInfo);

        save(chainId, blockInfo);

        System.out.println("-------------rollbackBlock: " + blockHeight + ", txCount:" + blockInfo.getHeader().getTxCount());
        return true;
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
                calcCommissionReward(chainId, agentInfo, blockInfo.getTxList().get(0));
            }
        }
    }

    private void calcCommissionReward(int chainId, AgentInfo agentInfo, TransactionInfo coinBaseTx) {
        List<CoinToInfo> list = coinBaseTx.getCoinTos();
        if (null == list || list.isEmpty()) {
            return;
        }

        AssetInfo assetInfo = CacheManager.getCacheChain(chainId).getDefaultAsset();
        BigInteger agentReward = BigInteger.ZERO, otherReward = BigInteger.ZERO;
        for (CoinToInfo output : list) {
            //奖励只计算本链的共识资产
            if (output.getChainId() == assetInfo.getChainId() && output.getAssetsId() == assetInfo.getAssetId()) {
                if (output.getAddress().equals(agentInfo.getRewardAddress())) {
                    agentReward = agentReward.add(output.getAmount());
                } else {
                    otherReward = otherReward.add(output.getAmount());
                }
            }
        }
        agentInfo.setTotalReward(agentInfo.getTotalReward().subtract(agentReward).subtract(otherReward));
        agentInfo.setAgentReward(agentInfo.getAgentReward().subtract(agentReward));
        agentInfo.setCommissionReward(agentInfo.getCommissionReward().subtract(otherReward));
    }

    private void processTxs(int chainId, List<TransactionInfo> txs) {
        for (int i = 0; i < txs.size(); i++) {
            TransactionInfo tx = txs.get(i);
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
                processDestroyChainTx(chainId, tx);
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

        AssetInfo assetInfo = CacheManager.getCacheChain(chainId).getDefaultAsset();
        Set<String> addressSet = new HashSet<>();
        for (CoinToInfo output : tx.getCoinTos()) {
            addressSet.add(output.getAddress());
            calcBalance(chainId, output);
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

            //奖励是本链主资产的时候，回滚奖励金额
            if (output.getChainId() == assetInfo.getChainId() && output.getAssetsId() == assetInfo.getAssetId()) {
                AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
                accountInfo.setTotalReward(accountInfo.getTotalReward().subtract(output.getAmount()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        }
    }

    private void processTransferTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                addressSet.add(input.getAddress());
                calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx.getHash()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        }
    }

    private void processCrossTransferTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                //如果地址不是本链的地址，不参与计算与存储
                if (chainId != AddressTool.getChainIdByAddress(input.getAddress())) {
                    continue;
                }
                addressSet.add(input.getAddress());
                calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx.getHash()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                //如果地址不是本链的地址，不参与计算与存储
                if (chainId != AddressTool.getChainIdByAddress(output.getAddress())) {
                    continue;
                }
                addressSet.add(output.getAddress());
                calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        }
    }

    private void processAliasTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();

        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                addressSet.add(input.getAddress());
                calcBalance(chainId, input);
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx.getHash()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                addressSet.add(output.getAddress());
                calcBalance(chainId, output);
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));
            }
        }
        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        }

        AliasInfo aliasInfo = (AliasInfo) tx.getTxData();
        if (aliasInfo != null) {
            AccountInfo accountInfo = queryAccountInfo(chainId, aliasInfo.getAddress());
            accountInfo.setAlias(null);
            aliasInfoList.add(aliasInfo);
        }
    }

    private void processCreateAgentTx(int chainId, TransactionInfo tx) {
        CoinToInfo output = tx.getCoinTos().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        //查找到代理节点，设置isNew = true，最后做存储的时候删除
        AgentInfo agentInfo = queryAgentInfo(chainId, tx.getHash(), 1);
        agentInfo.setNew(true);
        accountInfo.setConsensusLock(accountInfo.getConsensusLock().subtract(agentInfo.getDeposit()));
    }

    private void processDepositTx(int chainId, TransactionInfo tx) {
        CoinToInfo output = tx.getCoinTos().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        //查找到委托记录，设置isNew = true，最后做存储的时候删除
        DepositInfo depositInfo = (DepositInfo) tx.getTxData();
        depositInfo.setNew(true);
        depositInfoList.add(depositInfo);
        accountInfo.setConsensusLock(accountInfo.getConsensusLock().subtract(depositInfo.getAmount()));

        AgentInfo agentInfo = queryAgentInfo(chainId, depositInfo.getAgentHash(), 1);
        agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().subtract(depositInfo.getAmount()));
        agentInfo.setNew(false);
//        if (agentInfo.getTotalDeposit().compareTo(BigInteger.ZERO) < 0) {
//            throw new RuntimeException("data error: agent[" + agentInfo.getTxHash() + "] totalDeposit < 0");
//        }
    }

    private void processCancelDepositTx(int chainId, TransactionInfo tx) {
        CoinToInfo output = tx.getCoinTos().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        //查询取消委托记录，再根据deleteHash反向查到委托记录
        DepositInfo cancelInfo = (DepositInfo) tx.getTxData();
        DepositInfo depositInfo = depositService.getDepositInfoByKey(chainId, cancelInfo.getDeleteKey());
        accountInfo.setConsensusLock(accountInfo.getConsensusLock().add(depositInfo.getAmount()));

        depositInfo.setDeleteKey(null);
        depositInfo.setDeleteHeight(0);
        cancelInfo.setNew(true);
        depositInfoList.add(depositInfo);
        depositInfoList.add(cancelInfo);

        AgentInfo agentInfo = queryAgentInfo(chainId, depositInfo.getAgentHash(), 1);
        agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().add(depositInfo.getAmount()));
        agentInfo.setNew(false);
    }

    private void processStopAgentTx(int chainId, TransactionInfo tx) {
        AgentInfo agentInfo = queryAgentInfo(chainId, tx.getHash(), 4);
        agentInfo.setDeleteHash(null);
        agentInfo.setDeleteHeight(0);
        agentInfo.setStatus(1);
        agentInfo.setNew(false);

        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            if (accountInfo.getAddress().equals(agentInfo.getAgentAddress())) {
                if (output.getLockTime() > 0) {
                    accountInfo.setTxCount(accountInfo.getTxCount() - 1);
                    accountInfo.setConsensusLock(accountInfo.getConsensusLock().add(agentInfo.getDeposit()));
                    calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
                }
            } else {
                accountInfo.setTxCount(accountInfo.getTxCount() - 1);
            }
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));
        }

        //根据交易hash查询所有取消委托的记录
        List<DepositInfo> depositInfos = depositService.getDepositListByHash(chainId, tx.getHash());
        if (!depositInfos.isEmpty()) {
            for (DepositInfo cancelDeposit : depositInfos) {
                //需要删除的数据
                cancelDeposit.setNew(true);

                DepositInfo depositInfo = depositService.getDepositInfoByKey(chainId, cancelDeposit.getDeleteKey());
                depositInfo.setDeleteHeight(0);
                depositInfo.setDeleteKey(null);

                depositInfoList.add(cancelDeposit);
                depositInfoList.add(depositInfo);

                AccountInfo accountInfo = queryAccountInfo(chainId, depositInfo.getAddress());
                accountInfo.setConsensusLock(accountInfo.getConsensusLock().add(depositInfo.getAmount()));
                agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().add(depositInfo.getAmount()));
            }
        }
    }

    private void processYellowPunishTx(int chainId, TransactionInfo tx) {
        Set<String> addressSet = new HashSet<>();
        for (TxDataInfo txData : tx.getTxDataList()) {
            PunishLogInfo punishLog = (PunishLogInfo) txData;
            addressSet.add(punishLog.getAddress());
        }

        for (String address : addressSet) {
            AccountInfo accountInfo = queryAccountInfo(chainId, address);
            accountInfo.setTxCount(accountInfo.getTxCount() - 1);
            txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx.getHash()));
        }
        punishTxHashList.add(tx.getHash());
    }

    private void processRedPunishTx(int chainId, TransactionInfo tx) {
        PunishLogInfo redPunish = (PunishLogInfo) tx.getTxData();
        punishTxHashList.add(tx.getHash());
        //根据红牌找到被惩罚的节点，恢复节点状态
        AgentInfo agentInfo = queryAgentInfo(chainId, redPunish.getAddress(), 2);
        agentInfo.setDeleteHash(null);
        agentInfo.setDeleteHeight(0);
        agentInfo.setStatus(1);
        agentInfo.setNew(false);

        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            if (accountInfo.getAddress().equals(agentInfo.getAgentAddress())) {
                if (output.getLockTime() > 0) {
                    accountInfo.setTxCount(accountInfo.getTxCount() - 1);
                    accountInfo.setConsensusLock(accountInfo.getConsensusLock().add(agentInfo.getDeposit()));
                }
            } else {
                accountInfo.setTxCount(accountInfo.getTxCount() - 1);
            }
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));
        }

        //根据交易hash查询所有取消委托的记录
        List<DepositInfo> depositInfos = depositService.getDepositListByHash(chainId, tx.getHash());
        if (!depositInfos.isEmpty()) {
            for (DepositInfo cancelDeposit : depositInfos) {
                cancelDeposit.setNew(true);

                DepositInfo depositInfo = depositService.getDepositInfoByKey(chainId, cancelDeposit.getDeleteKey());
                depositInfo.setDeleteHeight(0);
                depositInfo.setDeleteKey(null);

                depositInfoList.add(cancelDeposit);
                depositInfoList.add(depositInfo);

                AccountInfo accountInfo = queryAccountInfo(chainId, depositInfo.getAddress());
                accountInfo.setConsensusLock(accountInfo.getConsensusLock().subtract(depositInfo.getAmount()));
                agentInfo.setTotalDeposit(agentInfo.getTotalDeposit().add(depositInfo.getAmount()));
            }
        }
    }

    private void processCreateContract(int chainId, TransactionInfo tx) {
        CoinFromInfo coinFromInfo = tx.getCoinFroms().get(0);

        AccountInfo accountInfo = queryAccountInfo(chainId, coinFromInfo.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        calcBalance(chainId, coinFromInfo);
        txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx.getHash()));

        ContractInfo contractInfo = (ContractInfo) tx.getTxData();
        contractInfo.setNew(true);
        contractTxHashList.add(tx.getHash());
        ContractResultInfo resultInfo = contractInfo.getResultInfo();
        if (resultInfo.isSuccess()) {
            processTokenTransfers(chainId, resultInfo.getTokenTransfers(), tx);
        }
    }

    private void processCallContract(int chainId, TransactionInfo tx) {
        processTransferTx(chainId, tx);
        contractTxHashList.add(tx.getHash());
        ContractCallInfo callInfo = (ContractCallInfo) tx.getTxData();
        ContractResultInfo resultInfo = callInfo.getResultInfo();
        ContractInfo contractInfo = queryContractInfo(chainId, resultInfo.getContractAddress());
        contractInfo.setTxCount(contractInfo.getTxCount() - 1);

        if (resultInfo.isSuccess()) {
            processTokenTransfers(chainId, resultInfo.getTokenTransfers(), tx);
        }
    }

    private void processDeleteContract(int chainId, TransactionInfo tx) {
        CoinFromInfo coinFromInfo = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, coinFromInfo.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        calcBalance(chainId, coinFromInfo);
        txRelationInfoSet.add(new TxRelationInfo(accountInfo.getAddress(), tx.getHash()));
        //首先查询合约交易执行结果
        ContractDeleteInfo deleteInfo = (ContractDeleteInfo) tx.getTxData();
        ContractResultInfo resultInfo = deleteInfo.getResultInfo();
        //再查询智能合约
        ContractInfo contractInfo = queryContractInfo(chainId, deleteInfo.getContractAddress());
        contractInfo.setTxCount(contractInfo.getTxCount() - 1);

        contractTxHashList.add(tx.getHash());
        if (resultInfo.isSuccess()) {
            contractInfo.setStatus(ApiConstant.CONTRACT_STATUS_NORMAL);
        }
    }

    private void processRegChainTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx.getHash()));

        CoinToInfo output = null;
        for (CoinToInfo to : tx.getCoinTos()) {
            if (!to.getAddress().equals(accountInfo.getAddress())) {
                output = to;
                break;
            }
        }
        calcBalance(chainId, input.getChainId(), input.getAssetsId(), accountInfo, output.getAmount().add(tx.getFee().getValue()));

        AccountInfo destroyAccount = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(destroyAccount.getTxCount() - 1);
        calcBalance(chainId, output);
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        chainInfoList.add((ChainInfo) tx.getTxData());
    }

    private void processDestroyChainTx(int chainId, TransactionInfo tx) {
        CoinToInfo output = tx.getCoinTos().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() + 1);
        calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        chainInfo.setStatus(ENABLE);
        for (AssetInfo assetInfo : chainInfo.getAssets()) {
            assetInfo.setStatus(ENABLE);
        }
        chainInfo.getDefaultAsset().setStatus(ENABLE);
        chainInfo.setNew(false);
        chainInfoList.add(chainInfo);
    }

    private void processAddAssetTx(int chainId, TransactionInfo tx) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx.getHash()));

        CoinToInfo output = null;
        for (CoinToInfo to : tx.getCoinTos()) {
            if (!to.getAddress().equals(accountInfo.getAddress())) {
                output = to;
                break;
            }
        }
        calcBalance(chainId, input.getChainId(), input.getAssetsId(), accountInfo, output.getAmount().add(tx.getFee().getValue()));
        AccountInfo destroyAccount = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(destroyAccount.getTxCount() - 1);
        calcBalance(chainId, output);
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        AssetInfo assetInfo = (AssetInfo) tx.getTxData();
        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        chainInfo.removeAsset(assetInfo.getAssetId());
        chainInfoList.add(chainInfo);
    }

    private void processCancelAssetTx(int chainId, TransactionInfo tx) {
        CoinToInfo output = tx.getCoinTos().get(0);
        AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
        accountInfo.setTxCount(accountInfo.getTxCount() - 1);
        calcBalance(chainId, output.getChainId(), output.getAssetsId(), accountInfo, tx.getFee().getValue());
        txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx.getHash()));

        AssetInfo assetInfo = (AssetInfo) tx.getTxData();
        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        chainInfo.getAsset(assetInfo.getAssetId()).setStatus(ENABLE);
        chainInfo.setNew(false);
        chainInfoList.add(chainInfo);
    }

    private void processTokenTransfers(int chainId, List<TokenTransfer> tokenTransfers, TransactionInfo tx) {
        if (tokenTransfers.isEmpty()) {
            return;
        }
        tokenTransferHashList.add(tx.getHash());

        TokenTransfer tokenTransfer;
        ContractInfo contractInfo;
        for (int i = 0; i < tokenTransfers.size(); i++) {
            tokenTransfer = tokenTransfers.get(i);
            contractInfo = queryContractInfo(chainId, tokenTransfer.getContractAddress());
            contractInfo.setTransferCount(contractInfo.getTransferCount() - 1);

            if (tokenTransfer.getFromAddress() != null) {
                processAccountNrc20(chainId, contractInfo, tokenTransfer.getFromAddress(), new BigInteger(tokenTransfer.getValue()), 1);
            }
            processAccountNrc20(chainId, contractInfo, tokenTransfer.getToAddress(), new BigInteger(tokenTransfer.getValue()), -1);
        }
    }

    private AccountTokenInfo processAccountNrc20(int chainId, ContractInfo contractInfo, String address, BigInteger value, int type) {
        AccountTokenInfo tokenInfo = queryAccountTokenInfo(chainId, address + contractInfo.getContractAddress());

        if (type == 1) {
            tokenInfo.setBalance(tokenInfo.getBalance().add(value));
        } else {
            tokenInfo.setBalance(tokenInfo.getBalance().subtract(value));
        }

//        if (tokenInfo.getBalance().compareTo(BigInteger.ZERO) < 0) {
//            throw new RuntimeException("data error: " + address + " token[" + contractInfo.getSymbol() + "] balance < 0");
//        }
        if (!accountTokenMap.containsKey(tokenInfo.getKey())) {
            accountTokenMap.put(tokenInfo.getKey(), tokenInfo);
        }
        return tokenInfo;
    }


    private AccountLedgerInfo calcBalance(int chainId, CoinToInfo output) {
        ChainInfo chainInfo = CacheManager.getCacheChain(chainId);
        if (output.getChainId() == chainInfo.getChainId() && output.getAssetsId() == chainInfo.getDefaultAsset().getAssetId()) {
            AccountInfo accountInfo = queryAccountInfo(chainId, output.getAddress());
            accountInfo.setTotalIn(accountInfo.getTotalIn().subtract(output.getAmount()));
            accountInfo.setTotalBalance(accountInfo.getTotalBalance().subtract(output.getAmount()));
//            if (accountInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
//                throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + accountInfo.getAddress() + "] totalBalance < 0");
//            }
        }

        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().subtract(output.getAmount()));
//        if (ledgerInfo.getTotalBalance().compareTo(BigInteger.ZERO) < 0) {
//            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR, "account[" + ledgerInfo.getAddress() + "] totalBalance < 0");
//        }
        return ledgerInfo;
    }

    private AccountLedgerInfo calcBalance(int chainId, CoinFromInfo input) {
        ChainInfo chainInfo = CacheManager.getCacheChain(chainId);
        if (input.getChainId() == chainInfo.getChainId() && input.getAssetsId() == chainInfo.getDefaultAsset().getAssetId()) {
            AccountInfo accountInfo = queryAccountInfo(chainId, input.getAddress());
            accountInfo.setTotalOut(accountInfo.getTotalOut().subtract(input.getAmount()));
            accountInfo.setTotalBalance(accountInfo.getTotalBalance().add(input.getAmount()));
        }
        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().add(input.getAmount()));
        return ledgerInfo;
    }

    private AccountLedgerInfo calcBalance(int chainId, int assetChainId, int assetId, AccountInfo accountInfo, BigInteger fee) {
        if (chainId == assetChainId) {
            accountInfo.setTotalOut(accountInfo.getTotalOut().subtract(fee));
            accountInfo.setTotalBalance(accountInfo.getTotalBalance().add(fee));
        }
        AccountLedgerInfo ledgerInfo = queryLedgerInfo(chainId, accountInfo.getAddress(), assetChainId, assetId);
        ledgerInfo.setTotalBalance(ledgerInfo.getTotalBalance().add(fee));
        return ledgerInfo;
    }

    private void save(int chainId, BlockInfo blockInfo) {
        SyncInfo syncInfo = chainService.getSyncInfo(chainId);
        if (blockInfo.getHeader().getHeight() != syncInfo.getBestHeight()) {
            throw new NulsRuntimeException(ApiErrorCode.DATA_ERROR);
        }
        if (syncInfo.isFinish()) {
            accountService.saveAccounts(chainId, accountInfoMap);
            syncInfo.setStep(50);
            chainService.updateStep(syncInfo);
        }

        if (syncInfo.getStep() == 50) {
            tokenService.saveAccountTokens(chainId, accountTokenMap);
            syncInfo.setStep(40);
            chainService.updateStep(syncInfo);
        }

        if (syncInfo.getStep() == 40) {
            contractService.rollbackContractInfos(chainId, contractInfoMap);
            syncInfo.setStep(30);
            chainService.updateStep(syncInfo);
        }

        if (syncInfo.getStep() == 30) {
            ledgerService.saveLedgerList(chainId, accountLedgerInfoMap);
            syncInfo.setStep(20);
            chainService.updateStep(syncInfo);
        }

        if (syncInfo.getStep() == 20) {
            agentService.rollbackAgentList(chainId, agentInfoList);
            syncInfo.setStep(10);
            chainService.updateStep(syncInfo);
        }
        //回滚chain信息
        chainService.rollbackChainList(chainInfoList);
        //回滾token转账信息
        tokenService.rollbackTokenTransfers(chainId, tokenTransferHashList, blockInfo.getHeader().getHeight());
        //回滾智能合約交易
        contractService.rollbackContractResults(chainId, contractTxHashList);
        contractService.rollbackContractTxInfos(chainId, contractTxHashList);
        depositService.rollbackDeposit(chainId, depositInfoList);
        punishService.rollbackPunishLog(chainId, punishTxHashList, blockInfo.getHeader().getHeight());
        aliasService.rollbackAliasList(chainId, aliasInfoList);
        transactionService.rollbackTxRelationList(chainId, txRelationInfoSet);
        transactionService.rollbackTx(chainId, blockInfo.getHeader().getTxHashList());
        blockService.deleteBlockHeader(chainId, blockInfo.getHeader().getHeight());

        syncInfo.setStep(100);
        syncInfo.setBestHeight(blockInfo.getHeader().getHeight() - 1);
        chainService.updateStep(syncInfo);
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

    private AccountLedgerInfo queryLedgerInfo(int chainId, String address, int assetChainId, int assetId) {
        String key = DBUtil.getAccountAssetKey(address, assetChainId, assetId);
        AccountLedgerInfo ledgerInfo = accountLedgerInfoMap.get(key);
        if (ledgerInfo == null) {
            ledgerInfo = ledgerService.getAccountLedgerInfo(chainId, key);
            if (ledgerInfo == null) {
                ledgerInfo = new AccountLedgerInfo(address, assetChainId, assetId);
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
        aliasInfoList.clear();
        depositInfoList.clear();
        punishTxHashList.clear();
        contractInfoMap.clear();
        contractTxHashList.clear();
        accountTokenMap.clear();
        tokenTransferHashList.clear();
        chainInfoList.clear();
        txRelationInfoSet.clear();
    }
}
