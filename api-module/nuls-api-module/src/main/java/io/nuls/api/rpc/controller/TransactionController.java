package io.nuls.api.rpc.controller;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.db.mongo.*;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.MongoTableConstant.TX_COUNT;

@Controller
public class TransactionController {
    @Autowired
    private MongoTransactionServiceImpl txService;
    @Autowired
    private MongoAgentServiceImpl mongoAgentServiceImpl;
    @Autowired
    private MongoDepositServiceImpl mongoDepositServiceImpl;
    @Autowired
    private MongoPunishServiceImpl mongoPunishServiceImpl;
    @Autowired
    private MongoStatisticalServiceImpl mongoStatisticalServiceImpl;

    @RpcMethod("getTx")
    public RpcResult getTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId = (int) params.get(0);
        String hash = "" + params.get(1);
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }
        Result<TransactionInfo> result = WalletRpcHandler.getTx(chainId, hash);
        if (result.isFailed()) {
            throw new JsonRpcException(result.getErrorCode());
        }
        TransactionInfo tx = result.getData();
        if (tx == null) {
            return RpcResult.dataNotFound();
        }
        try {
            RpcResult rpcResult = new RpcResult();
            if (tx.getType() == ApiConstant.TX_TYPE_JOIN_CONSENSUS) {
                DepositInfo depositInfo = (DepositInfo) tx.getTxData();
                AgentInfo agentInfo = mongoAgentServiceImpl.getAgentByHash(chainId, depositInfo.getAgentHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CANCEL_DEPOSIT) {
                DepositInfo depositInfo = (DepositInfo) tx.getTxData();
                depositInfo = mongoDepositServiceImpl.getDepositInfoByHash(chainId, depositInfo.getTxHash());
                AgentInfo agentInfo = mongoAgentServiceImpl.getAgentByHash(chainId, depositInfo.getAgentHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == ApiConstant.TX_TYPE_STOP_AGENT) {
                AgentInfo agentInfo = (AgentInfo) tx.getTxData();
                agentInfo = mongoAgentServiceImpl.getAgentByHash(chainId, agentInfo.getTxHash());
                tx.setTxData(agentInfo);
            } else if (tx.getType() == ApiConstant.TX_TYPE_YELLOW_PUNISH) {
                List<TxDataInfo> punishLogs = mongoPunishServiceImpl.getYellowPunishLog(chainId, tx.getHash());
                tx.setTxDataList(punishLogs);
            } else if (tx.getType() == ApiConstant.TX_TYPE_RED_PUNISH) {
                PunishLogInfo punishLog = mongoPunishServiceImpl.getRedPunishLog(chainId, tx.getHash());
                tx.setTxData(punishLog);
            } else if (tx.getType() == ApiConstant.TX_TYPE_CREATE_CONTRACT) {
//                try {
//                    ContractResultInfo resultInfo = contractService.getContractResultInfo(tx.getHash());
//                    ContractInfo contractInfo = (ContractInfo) tx.getTxData();
//                    contractInfo.setResultInfo(resultInfo);
//                } catch (Exception e) {
//                    Log.error(e);
//                }
            } else if (tx.getType() == ApiConstant.TX_TYPE_CALL_CONTRACT) {
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
            Log.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    @RpcMethod("getTxList")
    public RpcResult getTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        int type = (int) params.get(3);
        boolean isHidden = (boolean) params.get(4);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<TransactionInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
        } else {
            pageInfo = txService.getTxList(chainId, pageIndex, pageSize, type, isHidden);
        }
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(pageInfo);
        return rpcResult;
    }

    @RpcMethod("getBlockTxList")
    public RpcResult getBlockTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        long height = Long.valueOf(params.get(3).toString());
        int type = Integer.parseInt("" + params.get(4));
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<TransactionInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
        } else {
            pageInfo = txService.getBlockTxList(chainId, pageIndex, pageSize, height, type);
        }

        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(pageInfo);
        return rpcResult;
    }

    @RpcMethod("getTxStatistical")
    public RpcResult getTxStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId = (int) params.get(0);
        int type = (int) params.get(1);
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.success(new ArrayList<>());
        }
        List list = this.mongoStatisticalServiceImpl.getStatisticalList(chainId, type, TX_COUNT);
        return new RpcResult().setResult(list);
    }


//    public RpcResult

}
