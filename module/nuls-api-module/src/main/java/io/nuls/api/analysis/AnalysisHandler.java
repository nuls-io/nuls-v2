package io.nuls.api.analysis;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.entity.*;
import io.nuls.api.model.po.db.*;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.RPCUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AnalysisHandler {

    public static BlockInfo toBlockInfo(Block block, int chainId) throws Exception {
        BlockInfo blockInfo = new BlockInfo();
        BlockHeaderInfo blockHeader = toBlockHeaderInfo(block.getHeader(), chainId);

        List<String> hashList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            if (tx.getType() == TxType.CREATE_CONTRACT ||
                    tx.getType() == TxType.CALL_CONTRACT ||
                    tx.getType() == TxType.DELETE_CONTRACT) {
                hashList.add(tx.getHash().toHex());
            }
        }
        if (!hashList.isEmpty()) {
            WalletRpcHandler.getContractResults(chainId, hashList);
        }

        blockInfo.setTxList(toTxs(chainId, block.getTxs(), blockHeader));
        //计算coinBase奖励
        blockHeader.setReward(calcCoinBaseReward(blockInfo.getTxList().get(0)));
        //计算总手续费
        blockHeader.setTotalFee(calcFee(blockInfo.getTxList()));
        List<String> txHashList = new ArrayList<>();
        for (int i = 0; i < block.getTxs().size(); i++) {
            txHashList.add(blockInfo.getTxList().get(i).getHash());
        }
        blockHeader.setTxHashList(txHashList);
        blockInfo.setHeader(blockHeader);
        return blockInfo;
    }

    public static BlockHeaderInfo toBlockHeaderInfo(BlockHeader blockHeader, int chainId) throws IOException {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());

        BlockHeaderInfo info = new BlockHeaderInfo();
        info.setHash(blockHeader.getHash().toHex());
        info.setHeight(blockHeader.getHeight());
        info.setPreHash(blockHeader.getPreHash().toHex());
        info.setMerkleHash(blockHeader.getMerkleHash().toHex());
        info.setCreateTime(blockHeader.getTime());
        info.setPackingAddress(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chainId)));
        info.setTxCount(blockHeader.getTxCount());
        info.setRoundIndex(extendsData.getRoundIndex());
        info.setSize(blockHeader.size());
        info.setPackingIndexOfRound(extendsData.getPackingIndexOfRound());
        info.setScriptSign(HexUtil.encode(blockHeader.getBlockSignature().serialize()));
        info.setAgentVersion(extendsData.getBlockVersion());
        info.setRoundStartTime(extendsData.getRoundStartTime());
        info.setAgentVersion(extendsData.getBlockVersion());
        //是否是种子节点打包的区块
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache.getChainInfo().getSeeds().contains(info.getPackingAddress()) || info.getHeight() == 0) {
            info.setSeedPacked(true);
        }
        return info;
    }

    public static List<TransactionInfo> toTxs(int chainId, List<Transaction> txList, BlockHeaderInfo blockHeader) throws Exception {
        List<TransactionInfo> txs = new ArrayList<>();
        for (int i = 0; i < txList.size(); i++) {
            TransactionInfo txInfo = toTransaction(chainId, txList.get(i));
            if (txInfo.getType() == TxType.RED_PUNISH) {
                PunishLogInfo punishLog = (PunishLogInfo) txInfo.getTxData();
                punishLog.setRoundIndex(blockHeader.getRoundIndex());
                punishLog.setPackageIndex(blockHeader.getPackingIndexOfRound());
            } else if (txInfo.getType() == TxType.YELLOW_PUNISH) {
                for (TxDataInfo txData : txInfo.getTxDataList()) {
                    PunishLogInfo punishLog = (PunishLogInfo) txData;
                    punishLog.setRoundIndex(blockHeader.getRoundIndex());
                    punishLog.setPackageIndex(blockHeader.getPackingIndexOfRound());
                }
            }
            txs.add(txInfo);
        }
        return txs;
    }

    public static TransactionInfo toTransaction(int chainId, Transaction tx) throws Exception {
        TransactionInfo info = new TransactionInfo();
        info.setHash(tx.getHash().toHex());
        info.setHeight(tx.getBlockHeight());
        info.setFee(tx.getFee());
        info.setType(tx.getType());
        info.setSize(tx.getSize());
        info.setCreateTime(tx.getTime());
        if (tx.getTxData() != null) {
            info.setTxDataHex(RPCUtil.encode(tx.getTxData()));
        }
        if (tx.getRemark() != null) {
            info.setRemark(new String(tx.getRemark(), StandardCharsets.UTF_8));
        }

        CoinData coinData = new CoinData();
        if (tx.getCoinData() != null) {
            coinData.parse(new NulsByteBuffer(tx.getCoinData()));
            info.setCoinFroms(toCoinFromList(coinData));
            info.setCoinTos(toCoinToList(coinData));
        }
        if (info.getType() == TxType.YELLOW_PUNISH) {
            info.setTxDataList(toYellowPunish(tx));
        } else {
            info.setTxData(toTxData(chainId, tx));
        }
        info.calcValue();
        return info;
    }

    public static List<CoinFromInfo> toCoinFromList(CoinData coinData) {
        if (coinData == null || coinData.getFrom() == null) {
            return null;
        }
        List<CoinFromInfo> fromInfoList = new ArrayList<>();
        for (CoinFrom from : coinData.getFrom()) {
            CoinFromInfo fromInfo = new CoinFromInfo();
            fromInfo.setAddress(AddressTool.getStringAddressByBytes(from.getAddress()));
            fromInfo.setAssetsId(from.getAssetsId());
            fromInfo.setChainId(from.getAssetsChainId());
            fromInfo.setLocked(from.getLocked());
            fromInfo.setAmount(from.getAmount());
            fromInfo.setNonce(HexUtil.encode(from.getNonce()));
            fromInfo.setSymbol(CacheManager.getChainInfo(fromInfo.getChainId()).getAsset(fromInfo.getAssetsId()).getSymbol());
            fromInfoList.add(fromInfo);
        }
        return fromInfoList;
    }

    public static List<CoinToInfo> toCoinToList(CoinData coinData) {
        if (coinData == null || coinData.getTo() == null) {
            return null;
        }
        List<CoinToInfo> toInfoList = new ArrayList<>();
        for (CoinTo to : coinData.getTo()) {
            CoinToInfo coinToInfo = new CoinToInfo();
            coinToInfo.setAddress(AddressTool.getStringAddressByBytes(to.getAddress()));
            coinToInfo.setAssetsId(to.getAssetsId());
            coinToInfo.setChainId(to.getAssetsChainId());
            coinToInfo.setLockTime(to.getLockTime());
            coinToInfo.setAmount(to.getAmount());
            coinToInfo.setSymbol(CacheManager.getChainInfo(coinToInfo.getChainId()).getAsset(coinToInfo.getAssetsId()).getSymbol());
            toInfoList.add(coinToInfo);
        }
        return toInfoList;
    }

    public static TxDataInfo toTxData(int chainId, Transaction tx) throws NulsException {
        if (tx.getType() == TxType.ACCOUNT_ALIAS) {
            return toAlias(tx);
        } else if (tx.getType() == TxType.REGISTER_AGENT || tx.getType() == TxType.CONTRACT_CREATE_AGENT) {
            return toAgent(tx);
        } else if (tx.getType() == TxType.DEPOSIT || tx.getType() == TxType.CONTRACT_DEPOSIT) {
            return toDeposit(tx);
        } else if (tx.getType() == TxType.CANCEL_DEPOSIT || tx.getType() == TxType.CONTRACT_CANCEL_DEPOSIT) {
            return toCancelDeposit(tx);
        } else if (tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.CONTRACT_STOP_AGENT) {
            return toStopAgent(tx);
        } else if (tx.getType() == TxType.RED_PUNISH) {
            return toRedPublishLog(tx);
        } else if (tx.getType() == TxType.CREATE_CONTRACT) {
            return toContractInfo(chainId, tx);
        } else if (tx.getType() == TxType.CALL_CONTRACT) {
            return toContractCallInfo(chainId, tx);
        } else if (tx.getType() == TxType.DELETE_CONTRACT) {
            return toContractDeleteInfo(chainId, tx);
        } else if (tx.getType() == TxType.CONTRACT_TRANSFER) {
            return toContractTransferInfo(tx);
        } else if (tx.getType() == TxType.REGISTER_CHAIN_AND_ASSET || tx.getType() == TxType.DESTROY_CHAIN_AND_ASSET) {
            return toChainInfo(tx);
        } else if (tx.getType() == TxType.ADD_ASSET_TO_CHAIN || tx.getType() == TxType.REMOVE_ASSET_FROM_CHAIN) {
            return toAssetInfo(tx);
        }
        return null;
    }

    public static AliasInfo toAlias(Transaction tx) throws NulsException {
        Alias alias = new Alias();
        alias.parse(new NulsByteBuffer(tx.getTxData()));
        AliasInfo info = new AliasInfo();
        info.setAddress(AddressTool.getStringAddressByBytes(alias.getAddress()));
        info.setAlias(alias.getAlias());
        return info;
    }

    public static AgentInfo toAgent(Transaction tx) throws NulsException {
        Agent agent = new Agent();
        agent.parse(new NulsByteBuffer(tx.getTxData()));

        AgentInfo agentInfo = new AgentInfo();
        agentInfo.init();
        agentInfo.setAgentAddress(AddressTool.getStringAddressByBytes(agent.getAgentAddress()));
        agentInfo.setPackingAddress(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
        agentInfo.setRewardAddress(AddressTool.getStringAddressByBytes(agent.getRewardAddress()));
        agentInfo.setDeposit(agent.getDeposit());
        agentInfo.setCreateTime(tx.getTime());

        agentInfo.setCommissionRate(agent.getCommissionRate());
        agentInfo.setTxHash(tx.getHash().toHex());
        agentInfo.setAgentId(agentInfo.getTxHash().substring(agentInfo.getTxHash().length() - 8));
        agentInfo.setBlockHeight(tx.getBlockHeight());
        return agentInfo;
    }

    public static DepositInfo toDeposit(Transaction tx) throws NulsException {
        Deposit deposit = new Deposit();
        deposit.parse(new NulsByteBuffer(tx.getTxData()));

        DepositInfo info = new DepositInfo();
        info.setTxHash(tx.getHash().toHex());
        info.setAmount(deposit.getDeposit());
        info.setAgentHash(deposit.getAgentHash().toHex());
        info.setAddress(AddressTool.getStringAddressByBytes(deposit.getAddress()));
        info.setTxHash(tx.getHash().toHex());
        info.setCreateTime(tx.getTime());
        info.setBlockHeight(tx.getBlockHeight());
        info.setFee(tx.getFee());
        info.setKey(info.getTxHash() + info.getAddress());
        return info;
    }

    public static DepositInfo toCancelDeposit(Transaction tx) throws NulsException {
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(new NulsByteBuffer(tx.getTxData()));
        DepositInfo deposit = new DepositInfo();
        deposit.setTxHash(cancelDeposit.getJoinTxHash().toHex());
        deposit.setFee(tx.getFee());
        deposit.setCreateTime(tx.getTime());
        deposit.setType(ApiConstant.CANCEL_CONSENSUS);
        return deposit;
    }

    public static AgentInfo toStopAgent(Transaction tx) throws NulsException {
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(new NulsByteBuffer(tx.getTxData()));

        AgentInfo agentNode = new AgentInfo();
        agentNode.setTxHash(stopAgent.getCreateTxHash().toHex());
        return agentNode;
    }

    public static List<TxDataInfo> toYellowPunish(Transaction tx) throws NulsException {
        YellowPunishData data = new YellowPunishData();
        data.parse(new NulsByteBuffer(tx.getTxData()));
        List<TxDataInfo> logList = new ArrayList<>();
        for (byte[] address : data.getAddressList()) {
            PunishLogInfo log = new PunishLogInfo();
            log.setTxHash(tx.getHash().toHex());
            log.setAddress(AddressTool.getStringAddressByBytes(address));
            log.setBlockHeight(tx.getBlockHeight());
            log.setTime(tx.getTime());
            log.setType(ApiConstant.PUBLISH_YELLOW);
            log.setReason("No packaged blocks");
            logList.add(log);
        }
        return logList;
    }

    public static PunishLogInfo toRedPublishLog(Transaction tx) throws NulsException {
        RedPunishData data = new RedPunishData();
        data.parse(new NulsByteBuffer(tx.getTxData()));

        PunishLogInfo punishLog = new PunishLogInfo();
        punishLog.setTxHash(tx.getHash().toHex());
        punishLog.setType(ApiConstant.PUBLISH_RED);
        punishLog.setAddress(AddressTool.getStringAddressByBytes(data.getAddress()));
        if (data.getReasonCode() == ApiConstant.TRY_FORK) {
            punishLog.setReason("Trying to bifurcate many times");
        } else if (data.getReasonCode() == ApiConstant.DOUBLE_SPEND) {
            punishLog.setReason("double-send tx in the block");
        } else if (data.getReasonCode() == ApiConstant.TOO_MUCH_YELLOW_PUNISH) {
            punishLog.setReason("too much yellow publish");
        }
        punishLog.setBlockHeight(tx.getBlockHeight());
        punishLog.setTime(tx.getTime());
        return punishLog;
    }

    public static ContractInfo toContractInfo(int chainId, Transaction tx) throws NulsException {
        CreateContractData data = new CreateContractData();
        data.parse(new NulsByteBuffer(tx.getTxData()));
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setCreateTxHash(tx.getHash().toHex());
        contractInfo.setContractAddress(AddressTool.getStringAddressByBytes(data.getContractAddress()));
        contractInfo.setBlockHeight(tx.getBlockHeight());
        contractInfo.setCreateTime(tx.getTime());
        Result<ContractInfo> result = WalletRpcHandler.getContractInfo(chainId, contractInfo);
        return result.getData();
    }

    public static ContractCallInfo toContractCallInfo(int chainId, Transaction tx) throws NulsException {
        CallContractData data = new CallContractData();
        data.parse(new NulsByteBuffer(tx.getTxData()));

        ContractCallInfo callInfo = new ContractCallInfo();
        callInfo.setCreater(AddressTool.getStringAddressByBytes(data.getSender()));
        callInfo.setContractAddress(AddressTool.getStringAddressByBytes(data.getContractAddress()));
        callInfo.setGasLimit(data.getGasLimit());
        callInfo.setPrice(data.getPrice());
        callInfo.setMethodName(data.getMethodName());
        callInfo.setMethodDesc(data.getMethodDesc());
        callInfo.setCreateTxHash(tx.getHash().toHex());
        String args = "";
        String[][] arrays = data.getArgs();
        if (arrays != null) {
            for (String[] arg : arrays) {
                if (arg != null) {
                    for (String s : arg) {
                        args = args + s + ",";
                    }
                }
            }
        }
        callInfo.setArgs(args);

        //查询智能合约详情之前，先查询创建智能合约的执行结果是否成功
        Result<ContractResultInfo> result = WalletRpcHandler.getContractResultInfo(chainId, callInfo.getCreateTxHash());
        callInfo.setResultInfo(result.getData());
        return callInfo;
    }

    public static ContractDeleteInfo toContractDeleteInfo(int chainId, Transaction tx) throws NulsException {
        DeleteContractData data = new DeleteContractData();
        data.parse(new NulsByteBuffer(tx.getTxData()));

        ContractDeleteInfo info = new ContractDeleteInfo();
        info.setTxHash(tx.getHash().toHex());
        info.setCreater(AddressTool.getStringAddressByBytes(data.getSender()));
        info.setContractAddress(AddressTool.getStringAddressByBytes(data.getContractAddress()));
        Result<ContractResultInfo> result = WalletRpcHandler.getContractResultInfo(chainId, info.getTxHash());
        info.setResultInfo(result.getData());
        return info;
    }

    private static ContractTransferInfo toContractTransferInfo(Transaction tx) throws NulsException {
        ContractTransferData data = new ContractTransferData();
        data.parse(new NulsByteBuffer(tx.getTxData()));

        ContractTransferInfo info = new ContractTransferInfo();
        info.setTxHash(tx.getHash().toHex());
        info.setContractAddress(AddressTool.getStringAddressByBytes(data.getContractAddress()));
        info.setOrginTxHash(data.getOrginTxHash().toHex());
        return info;
    }

    private static ChainInfo toChainInfo(Transaction tx) throws NulsException {
        TxChain txChain = new TxChain();
        txChain.parse(new NulsByteBuffer(tx.getTxData()));

        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(txChain.getChainId());

        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(txChain.getAssetId());
        assetInfo.setChainId(txChain.getChainId());
        assetInfo.setSymbol(txChain.getSymbol());
        assetInfo.setInitCoins(txChain.getInitNumber());

        chainInfo.setDefaultAsset(assetInfo);
        chainInfo.getAssets().add(assetInfo);
        chainInfo.setInflationCoins(txChain.getDepositNuls());

        return chainInfo;
    }

    private static AssetInfo toAssetInfo(Transaction tx) throws NulsException {
        TxAsset txAsset = new TxAsset();
        txAsset.parse(new NulsByteBuffer(tx.getTxData()));

        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(txAsset.getAssetId());
        assetInfo.setChainId(txAsset.getChainId());
        assetInfo.setSymbol(txAsset.getSymbol());
        assetInfo.setInitCoins(txAsset.getInitNumber());
        assetInfo.setAddress(AddressTool.getStringAddressByBytes(txAsset.getAddress()));
        return assetInfo;
    }

    public static BigInteger calcCoinBaseReward(TransactionInfo coinBaseTx) {
        BigInteger reward = BigInteger.ZERO;
        if (coinBaseTx.getCoinTos() == null) {
            return reward;
        }

        for (CoinToInfo coinTo : coinBaseTx.getCoinTos()) {
            reward = reward.add(coinTo.getAmount());
        }
        return reward;
    }

    public static BigInteger calcFee(List<TransactionInfo> txs) {
        BigInteger fee = BigInteger.ZERO;
        for (int i = 1; i < txs.size(); i++) {
            fee = fee.add(txs.get(i).getFee());
        }
        return fee;
    }
}
