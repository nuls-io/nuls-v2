package io.nuls.api.rpc.controller;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.AnalysisHandler;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.*;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.entity.CallContractData;
import io.nuls.api.model.entity.CreateContractData;
import io.nuls.api.model.entity.DeleteContractData;
import io.nuls.api.model.po.*;
import io.nuls.api.model.po.mini.MiniCoinBaseInfo;
import io.nuls.api.model.po.mini.MiniTransactionInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.DBTableConstant.TX_COUNT;
import static io.nuls.core.constant.TxType.*;

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
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            hash = "" + params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is inValid");
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
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
            } else if (tx.getType() == CREATE_CONTRACT) {
//                try {
//                    ContractResultInfo resultInfo = contractService.getContractResultInfo(tx.getHash());
//                    ContractInfo contractInfo = (ContractInfo) tx.getTxData();
//                    contractInfo.setResultInfo(resultInfo);
//                } catch (Exception e) {
//                    Log.error(e);
//                }
            } else if (tx.getType() == CALL_CONTRACT) {
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
        int chainId, pageNumber, pageSize, type;
        long startTime = 0, endTime = 0;
        boolean isHidden;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            type = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            isHidden = (boolean) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[isHidden] is inValid");
        }
        try {
            startTime = Long.parseLong(params.get(5).toString());
        } catch (Exception e) {

        }
        try {
            endTime = Long.parseLong(params.get(6).toString());
        } catch (Exception e) {

        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<MiniTransactionInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        } else {
            pageInfo = txService.getTxList(chainId, pageNumber, pageSize, type, isHidden, startTime, endTime);
        }
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(pageInfo);
        return rpcResult;
    }

    @RpcMethod("getBlockTxList")
    public RpcResult getBlockTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, type;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            height = Long.valueOf(params.get(1).toString());
        } catch (Exception e) {
            return RpcResult.paramError("[height] is inValid");
        }
        try {
            type = Integer.parseInt("" + params.get(2));
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        List<MiniTransactionInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new ArrayList<>();
        } else {
            pageInfo = txService.getBlockTxList(chainId, height, type);
        }
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(pageInfo);
        return rpcResult;
    }

    @RpcMethod("getTxStatistical")
    public RpcResult getTxStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, type;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            type = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }

        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.success(new ArrayList<>());
        }
        List list = this.statisticalService.getStatisticalList(chainId, type, TX_COUNT);
        return new RpcResult().setResult(list);
    }

    @RpcMethod("validateTx")
    public RpcResult validateTx(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(txHex)) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        Result result = WalletRpcHandler.validateTx(chainId, txHex);
        if (result.isSuccess()) {
            return RpcResult.success(result.getData());
        } else {
            return RpcResult.failed(result);
        }
    }

    //private static final String QUEUE_CONTRACT = "NULSd6HgugbpQf76wayhtXyH3obWaLezkTBn5";
    //private static final String QUEUE_CONTRACT_METHOD = "depositForOwn";
    //private static final ExecutorService QUEUE_CONTRACT_SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor(new NulsThreadFactory("queue_contract"));

    @RpcMethod("broadcastTx")
    public RpcResult broadcastTx(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            int type = this.extractTxTypeFromTx(txHex);
            if(type == CROSS_CHAIN){
                return RpcResult.failed(CommonCodeConstanst.PARAMETER_ERROR,"Cross-chain tx pause support");
            }
            Result result = Result.getSuccess(null);
            CallContractData call = null;
            String contract = null, txHash = null;
            switch (type) {
                case CREATE_CONTRACT:
                    Transaction tx = new Transaction();
                    tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CreateContractData create = new CreateContractData();
                    create.parse(new NulsByteBuffer(tx.getTxData()));
                    RpcResult createArgsResult = this.validateContractArgs(create.getArgs());
                    if (createArgsResult.getError() != null) {
                        return createArgsResult;
                    }
                    result = WalletRpcHandler.validateContractCreate(chainId,
                            AddressTool.getStringAddressByBytes(create.getSender()),
                            create.getGasLimit(),
                            create.getPrice(),
                            RPCUtil.encode(create.getCode()),
                            create.getArgs());
                    break;
                case CALL_CONTRACT:
                    Transaction callTx = new Transaction();
                    callTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    txHash = callTx.getHash().toHex();
                    call = new CallContractData();
                    call.parse(new NulsByteBuffer(callTx.getTxData()));
                    contract = AddressTool.getStringAddressByBytes(call.getContractAddress());
                    RpcResult argsResult = this.validateContractArgs(call.getArgs());
                    if (argsResult.getError() != null) {
                        return argsResult;
                    }
                    result = WalletRpcHandler.validateContractCall(chainId,
                            AddressTool.getStringAddressByBytes(call.getSender()),
                            call.getValue(),
                            call.getGasLimit(),
                            call.getPrice(),
                            contract,
                            call.getMethodName(),
                            call.getMethodDesc(),
                            call.getArgs());
                    break;
                case DELETE_CONTRACT:
                    Transaction deleteTx = new Transaction();
                    deleteTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    DeleteContractData delete = new DeleteContractData();
                    delete.parse(new NulsByteBuffer(deleteTx.getTxData()));
                    result = WalletRpcHandler.validateContractDelete(chainId,
                            AddressTool.getStringAddressByBytes(delete.getSender()),
                            AddressTool.getStringAddressByBytes(delete.getContractAddress()));
                    break;
                default:
                    break;
            }
            Map contractMap = (Map) result.getData();
            if (contractMap != null && Boolean.FALSE.equals(contractMap.get("success"))) {
                result.setErrorCode(CommonCodeConstanst.DATA_ERROR);
                result.setMsg((String) contractMap.get("msg"));
                return RpcResult.failed(result);
            }

            //if(call != null) {
            //    if(QUEUE_CONTRACT.equals(contract) && QUEUE_CONTRACT_METHOD.equals(call.getMethodName())) {
            //        QUEUE_CONTRACT_SINGLE_THREAD_EXECUTOR.submit(new QueueContractRun(chainId, txHex, txService));
            //        Map<String, Object> map = new HashMap<>(4);
            //        map.put("value", true);
            //        map.put("hash", txHash);
            //        return RpcResult.success(map);
            //    }
            //}

            result = WalletRpcHandler.broadcastTx(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    private RpcResult validateContractArgs(String[][] args) {
        if (args == null || args.length == 0) {
            return RpcResult.success(null);
        }
        try {
            String[] arg;
            for (int i = 0, length = args.length; i < length; i++) {
                arg = args[i];
                if (arg == null || arg.length == 0) {
                    continue;
                }
                for (String str : arg) {
                    if (!this.checkSpaceArg(str)) {
                        return RpcResult.failed(RpcErrorCode.CONTRACT_VALIDATION_FAILED);
                    }
                }
            }
            return RpcResult.success(null);
        } catch (Exception e) {
            Log.error("parse args error.", e);
            return RpcResult.failed(RpcErrorCode.CONTRACT_VALIDATION_FAILED);
        }
    }

    private boolean checkSpaceArg(String s) {
        if (s == null) {
            return true;
        }
        return s.length() == s.trim().length();
    }

    @RpcMethod("broadcastTxWithoutAnyValidation")
    public RpcResult broadcastTxWithoutAnyValidation(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }

            Result result = WalletRpcHandler.broadcastTxWithoutAnyValidation(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    private int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }


    @RpcMethod("sendCrossTx")
    public RpcResult sendCrossTx(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        //if(true){
         //   return RpcResult.failed(CommonCodeConstanst.PARAMETER_ERROR,"Cross-chain tx pause support");
        //}
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        try {
            Result result = WalletRpcHandler.sendCrossTx(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    @RpcMethod("broadcastTxWithNoContractValidation")
    public RpcResult broadcastTxWithNoContractValidation(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String txHex;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result result = WalletRpcHandler.broadcastTx(chainId, txHex);
            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                return RpcResult.success(result.getData());
            } else {
                return RpcResult.failed(result);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.TX_PARSE_ERROR);
        }
    }

    @RpcMethod("getCrossTxList")
    public RpcResult getCrossTxList(List<Object> params) {
        if (!ApiContext.isReady) {
            return RpcResult.chainNotReady();
        }
        VerifyUtils.verifyParams(params, 5);
        int chainId, crossChainId, pageNumber, pageSize;
        long startTime = 0, endTime = 0;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            crossChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            startTime = Long.parseLong(params.get(4).toString());
        } catch (Exception e) {

        }
        try {
            endTime = Long.parseLong(params.get(5).toString());
        } catch (Exception e) {

        }
        PageInfo<CrossTxRelationInfo> pageInfo;
        pageInfo = txService.getCrossTxList(chainId, crossChainId, pageNumber, pageSize, startTime, endTime);
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(pageInfo);
        return rpcResult;
    }
}
