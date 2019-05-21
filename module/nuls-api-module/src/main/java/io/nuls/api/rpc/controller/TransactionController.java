package io.nuls.api.rpc.controller;

import io.nuls.api.analysis.AnalysisHandler;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.*;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.po.db.mini.MiniCoinBaseInfo;
import io.nuls.api.model.po.db.mini.MiniTransactionInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.RPCUtil;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.DBTableConstant.TX_COUNT;

@Controller
public class TransactionController {
    @Autowired
    private TransactionService txService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private PunishService punishService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private StatisticalService statisticalService;

    @RpcMethod("getTx")
    public RpcResult getTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
            hash = "" + params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }
        Result<TransactionInfo> result = WalletRpcHandler.getTx(chainId, hash);
        if (result == null) {
            return RpcResult.dataNotFound();
        }
        if (result.isFailed()) {
            throw new JsonRpcException(result.getErrorCode());
        }
        TransactionInfo tx = result.getData();
        if (tx == null) {
            return RpcResult.dataNotFound();
        }
        try {
            RpcResult rpcResult = new RpcResult();
            if (tx.getType() == TxType.COIN_BASE) {
                BlockHeaderInfo headerInfo = blockService.getBlockHeader(chainId, tx.getHeight());
                MiniCoinBaseInfo coinBaseInfo = new MiniCoinBaseInfo(headerInfo.getRoundIndex(), headerInfo.getPackingIndexOfRound(), tx.getHash());
                tx.setTxData(coinBaseInfo);
            } else if (tx.getType() == TxType.DEPOSIT || tx.getType() == TxType.CONTRACT_DEPOSIT) {
                DepositInfo depositInfo = (DepositInfo) tx.getTxData();
                AgentInfo agentInfo = agentService.getAgentByHash(chainId, depositInfo.getAgentHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == TxType.CANCEL_DEPOSIT || tx.getType() == TxType.CONTRACT_CANCEL_DEPOSIT) {
                DepositInfo depositInfo = (DepositInfo) tx.getTxData();
                depositInfo = depositService.getDepositInfoByHash(chainId, depositInfo.getTxHash());
                AgentInfo agentInfo = agentService.getAgentByHash(chainId, depositInfo.getAgentHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.CONTRACT_STOP_AGENT) {
                AgentInfo agentInfo = (AgentInfo) tx.getTxData();
                agentInfo = agentService.getAgentByHash(chainId, agentInfo.getTxHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == TxType.YELLOW_PUNISH) {
                List<TxDataInfo> punishLogs = punishService.getYellowPunishLog(chainId, tx.getHash());
                tx.setTxDataList(punishLogs);
            } else if (tx.getType() == TxType.RED_PUNISH) {
                PunishLogInfo punishLog = punishService.getRedPunishLog(chainId, tx.getHash());
                tx.setTxData(punishLog);
            } else if (tx.getType() == TxType.CREATE_CONTRACT) {
//                try {
//                    ContractResultInfo resultInfo = contractService.getContractResultInfo(tx.getHash());
//                    ContractInfo contractInfo = (ContractInfo) tx.getTxData();
//                    contractInfo.setResultInfo(resultInfo);
//                } catch (Exception e) {
//                    Log.error(e);
//                }
            } else if (tx.getType() == TxType.CALL_CONTRACT) {
//                try {
//                    ContractResultInfo resultInfo = contractService.getContractResultInfo(tx.getHash());
//                    ContractCallInfo contractCallInfo = (ContractCallInfo) tx.getTxData();
//                    contractCallInfo.setResultInfo(resultInfo);
//                } catch (Exception e) {
//                    Log.error(e);
//                }
            }
            rpcResult.setResult(tx);
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    @RpcMethod("getTxList")
    public RpcResult getTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageIndex, pageSize, type;
        boolean isHidden;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            type = (int) params.get(3);
            isHidden = (boolean) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }
        try {
            PageInfo<MiniTransactionInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            } else {
                pageInfo = txService.getTxList(chainId, pageIndex, pageSize, type, isHidden);
            }
            RpcResult rpcResult = new RpcResult();
            rpcResult.setResult(pageInfo);
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getBlockTxList")
    public RpcResult getBlockTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize, type;
        long height;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            height = Long.valueOf(params.get(3).toString());
            type = Integer.parseInt("" + params.get(4));
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<TransactionInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            } else {
                pageInfo = txService.getBlockTxList(chainId, pageIndex, pageSize, height, type);
            }
            RpcResult rpcResult = new RpcResult();
            rpcResult.setResult(pageInfo);
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getTxStatistical")
    public RpcResult getTxStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, type;
        try {
            chainId = (int) params.get(0);
            type = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.success(new ArrayList<>());
            }
            List list = this.statisticalService.getStatisticalList(chainId, type, TX_COUNT);
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("validateTx")
    public RpcResult validateTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result result = WalletRpcHandler.validateTx(chainId, txHex);
            if (result.isSuccess()) {
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("broadcastTx")
    public RpcResult broadcastTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result result = WalletRpcHandler.broadcastTx(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx);
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }
}
