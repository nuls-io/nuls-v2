package io.nuls.api.analysis;

import io.nuls.api.ApiContext;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.model.entity.*;
import io.nuls.api.model.po.db.*;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalysisHandler {

    public static BlockInfo toBlockInfo(Block block, int chainId) throws Exception {
        BlockInfo blockInfo = new BlockInfo();
        BlockHeaderInfo blockHeader = toBlockHeaderInfo(block.getHeader(), chainId);
        blockInfo.setTxList(toTxs(block.getTxs(), blockHeader));
        //计算coinbase奖励
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
        info.setHash(blockHeader.getHash().getDigestHex());
        info.setHeight(blockHeader.getHeight());
        info.setPreHash(blockHeader.getPreHash().getDigestHex());
        info.setMerkleHash(blockHeader.getMerkleHash().getDigestHex());
        info.setCreateTime(blockHeader.getTime());
        info.setPackingAddress(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chainId)));
        info.setTxCount(blockHeader.getTxCount());
        info.setRoundIndex(extendsData.getRoundIndex());
        info.setSize(blockHeader.getSize());
        info.setPackingIndexOfRound(extendsData.getPackingIndexOfRound());
        info.setScriptSign(HexUtil.encode(blockHeader.getBlockSignature().serialize()));
        info.setAgentVersion(extendsData.getBlockVersion());
        info.setRoundStartTime(extendsData.getRoundStartTime());
        info.setAgentVersion(extendsData.getBlockVersion());
        //是否是种子节点打包的区块
        if (ApiContext.SEED_NODE_ADDRESS.contains(info.getPackingAddress()) || info.getHeight() == 0) {
            info.setSeedPacked(true);
        }
        return info;
    }


    public static List<TransactionInfo> toTxs(List<Transaction> txList, BlockHeaderInfo blockHeader) throws Exception {
        List<TransactionInfo> txs = new ArrayList<>();
        for (int i = 0; i < txList.size(); i++) {
            TransactionInfo txInfo = toTransaction(txList.get(i));
            if (txInfo.getType() == ApiConstant.TX_TYPE_RED_PUNISH) {
                PunishLogInfo punishLog = (PunishLogInfo) txInfo.getTxData();
                punishLog.setRoundIndex(blockHeader.getRoundIndex());
                punishLog.setIndex(blockHeader.getPackingIndexOfRound());
            } else if (txInfo.getType() == ApiConstant.TX_TYPE_YELLOW_PUNISH) {
                for (TxDataInfo txData : txInfo.getTxDataList()) {
                    PunishLogInfo punishLog = (PunishLogInfo) txData;
                    punishLog.setRoundIndex(blockHeader.getRoundIndex());
                    punishLog.setIndex(blockHeader.getPackingIndexOfRound());
                }
            }
            txs.add(txInfo);
        }
        return txs;
    }

    public static TransactionInfo toTransaction(Transaction tx) throws Exception {
        TransactionInfo info = new TransactionInfo();
        info.setHash(tx.getHash().getDigestHex());
        info.setHeight(tx.getBlockHeight());
        info.setFee(tx.getFee());
        info.setType(tx.getType());
        info.setSize(tx.getSize());
        info.setCreateTime(tx.getTime());

        if (tx.getTxData() != null) {
            info.setTxDataHex(HexUtil.encode(tx.getTxData()));
        }
        if (tx.getRemark() != null) {
            try {
                info.setRemark(new String(tx.getRemark(), ToolsConstant.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                info.setRemark(HexUtil.encode(tx.getRemark()));
            }
        }

        CoinData coinData = new CoinData();
        if (tx.getCoinData() != null) {
            coinData.parse(new NulsByteBuffer(tx.getCoinData()));
            info.setCoinFroms(toFroms(coinData));
            info.setCoinTos(toCoinToList(coinData));
        }


        if (info.getType() == ApiConstant.TX_TYPE_YELLOW_PUNISH) {
            info.setTxDataList(toYellowPunish(tx));
        } else {
            info.setTxData(toTxData(tx));
        }

        BigInteger value = BigInteger.ZERO;
        if (info.getType() == ApiConstant.TX_TYPE_COINBASE) {
            if (info.getCoinTos() != null) {
                for (CoinToInfo coinTo : info.getCoinTos()) {
                    value = value.add(coinTo.getAmount());
                }
            }
        } else if (info.getType() == ApiConstant.TX_TYPE_TRANSFER ||
                info.getType() == ApiConstant.TX_TYPE_CALL_CONTRACT ||
                info.getType() == ApiConstant.TX_TYPE_CONTRACT_TRANSFER ||
                info.getType() == ApiConstant.TX_TYPE_DATA) {
            Set<String> addressSet = new HashSet<>();
            for (CoinFromInfo coinFrom : info.getCoinFroms()) {
                addressSet.add(coinFrom.getAddress());
            }
            if (null != info.getCoinTos()) {
                for (CoinToInfo coinTo : info.getCoinTos()) {
                    if (!addressSet.contains(coinTo.getAddress())) {
                        value = value.add(coinTo.getAmount());
                    }
                }
            }
        } else if (info.getType() == ApiConstant.TX_TYPE_ALIAS) {
            value = ApiConstant.ALIAS_AMOUNT;
        }
        info.setValue(value);
        return info;
    }

    public static List<CoinFromInfo> toFroms(CoinData coinData) {
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

            toInfoList.add(coinToInfo);
        }
        return toInfoList;
    }


    public static TxDataInfo toTxData(Transaction tx) throws NulsException {
        if (tx.getType() == ApiConstant.TX_TYPE_ALIAS) {
            return toAlias(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_REGISTER_AGENT) {
            return toAgent(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_JOIN_CONSENSUS) {
            return toDeposit(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_CANCEL_DEPOSIT) {
            return toCancelDeposit(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_STOP_AGENT) {
            return toStopAgent(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_RED_PUNISH) {
            return toRedPublishLog(tx);
        } else if (tx.getType() == ApiConstant.TX_TYPE_CREATE_CONTRACT) {
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
        agentInfo.setRewardAddress(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
        agentInfo.setDeposit(agent.getDeposit());
        agentInfo.setCommissionRate(agent.getCommissionRate());
        agentInfo.setTxHash(tx.getHash().getDigestHex());
        agentInfo.setAgentId(agentInfo.getTxHash().substring(agentInfo.getTxHash().length() - 8));
        agentInfo.setBlockHeight(tx.getBlockHeight());
        return agentInfo;
    }

    public static DepositInfo toDeposit(Transaction tx) throws NulsException {
        Deposit deposit = new Deposit();
        deposit.parse(new NulsByteBuffer(tx.getTxData()));

        DepositInfo info = new DepositInfo();
        info.setTxHash(tx.getHash().getDigestHex());
        info.setAmount(deposit.getDeposit());
        info.setAgentHash(deposit.getAgentHash().getDigestHex());
        info.setAddress(AddressTool.getStringAddressByBytes(deposit.getAddress()));
        info.setTxHash(tx.getHash().getDigestHex());
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
        deposit.setTxHash(cancelDeposit.getJoinTxHash().getDigestHex());
        deposit.setFee(tx.getFee());
        deposit.setCreateTime(tx.getTime());
        deposit.setType(ApiConstant.CANCEL_CONSENSUS);
        return deposit;
    }

    public static AgentInfo toStopAgent(Transaction tx) throws NulsException {
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(new NulsByteBuffer(tx.getTxData()));

        AgentInfo agentNode = new AgentInfo();
        agentNode.setTxHash(stopAgent.getCreateTxHash().getDigestHex());
        return agentNode;
    }

    public static List<TxDataInfo> toYellowPunish(Transaction tx) throws NulsException {
        YellowPunishData data = new YellowPunishData();
        data.parse(new NulsByteBuffer(tx.getTxData()));
        List<TxDataInfo> logList = new ArrayList<>();
        for (byte[] address : data.getAddressList()) {
            PunishLogInfo log = new PunishLogInfo();
            log.setTxHash(tx.getHash().getDigestHex());
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
        punishLog.setTxHash(tx.getHash().getDigestHex());
        punishLog.setType(ApiConstant.PUTLISH_RED);
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
