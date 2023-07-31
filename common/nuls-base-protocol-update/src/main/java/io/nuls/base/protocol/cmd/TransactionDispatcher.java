package io.nuls.base.protocol.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.CommonAdvice;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.NulsCoresCmd;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易分发器
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/24 19:02
 */
@Component
@NulsCoresCmd(module = ModuleE.NC)
public final class TransactionDispatcher extends BaseCmd {

    private List<TransactionProcessor> processors;

    public List<TransactionProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<TransactionProcessor> processors) {
        processors.forEach(e -> Log.info("register TransactionProcessor-" + e.toString()));
        processors.sort(TransactionProcessor.COMPARATOR);
        this.processors = processors;
    }

    private Map<String, CommonAdvice> commitAdviceMap = new HashMap<>();
    private Map<String, CommonAdvice> rollbackAdviceMap = new HashMap<>();

    public void register(ModuleE module, CommonAdvice commitAdvice, CommonAdvice rollbackAdvice) {
        if (module == ModuleE.SC) {
            // 跨链模块的token跨链转入交易，需要把普通跨链交易转换成调用合约交易来写入系统跨链合约
            if (commitAdvice != null) {
                commitAdviceMap.put(String.valueOf(TxType.CROSS_CHAIN), commitAdvice);
            }
            if (rollbackAdvice != null) {
                rollbackAdviceMap.put(String.valueOf(TxType.CROSS_CHAIN), rollbackAdvice);
            }
        }
        // 按实际模块注册
        if (commitAdvice != null) {
            commitAdviceMap.put(module.abbr, commitAdvice);
        }
        if (rollbackAdvice != null) {
            rollbackAdviceMap.put(module.abbr, rollbackAdvice);
        }
    }

    /**
     * 获取最新主链高度
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txValidator(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        String blockHeaderStr = (String) params.get("blockHeader");
        BlockHeader blockHeader = null;
        if (StringUtils.isNotBlank(blockHeaderStr)) {
            blockHeader = RPCUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        }
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        List<Transaction> finalInvalidTxs = new ArrayList<>();
        for (String txStr : txList) {
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            txs.add(tx);
        }
        Map<Integer, List<Transaction>> map = new HashMap<>();
        for (TransactionProcessor processor : processors) {
            for (Transaction tx : txs) {
                List<Transaction> transactions = map.computeIfAbsent(processor.getType(), k -> new ArrayList<>());
                if (tx.getType() == processor.getType()) {
                    transactions.add(tx);
                }
            }
        }
        String errorCode = "";
        for (TransactionProcessor processor : processors) {
            List<Transaction> transactions = map.get(processor.getType());
            if (transactions.isEmpty()) {
                continue;
            }
            Map<String, Object> validateMap = processor.validate(chainId, transactions, map, blockHeader);
            if(validateMap == null) {
                continue;
            }
            List<Transaction> invalidTxs = (List<Transaction>) validateMap.get("txList");
            //List<Transaction> invalidTxs = processor.validate(chainId, map.get(processor.getType()), map, blockHeader);
            if (invalidTxs != null && !invalidTxs.isEmpty()) {
                errorCode = (String) validateMap.get("errorCode");
                finalInvalidTxs.addAll(invalidTxs);
                invalidTxs.forEach(e -> map.get(e.getType()).remove(e));
            }
        }
        Map<String, Object> resultMap = new HashMap<>(2);
        List<String> list = finalInvalidTxs.stream().map(e -> e.getHash().toHex()).collect(Collectors.toList());
        resultMap.put("errorCode", errorCode);
        resultMap.put("list", list);
        return success(resultMap);
    }

    /**
     * 获取最新主链高度
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txCommit(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("blockHeader"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        String blockHeaderStr = (String) params.get("blockHeader");
        BlockHeader blockHeader = RPCUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        for (String txStr : txList) {
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            txs.add(tx);
        }
        boolean commitAdviceBegin = false;
        CommonAdvice commitAdvice = null;
        Map<Integer, List<Transaction>> map = new HashMap<>();
        for (TransactionProcessor processor : processors) {
            for (Transaction tx : txs) {
                List<Transaction> transactions = map.computeIfAbsent(processor.getType(), k -> new ArrayList<>());
                if (tx.getType() == processor.getType()) {
                    transactions.add(tx);
                }
            }
        }
        Map<String, Boolean> resultMap = new HashMap<>(2);
        List<TransactionProcessor> completedProcessors = new ArrayList<>();
        for (TransactionProcessor processor : processors) {
            List<Transaction> transactions = map.get(processor.getType());
            if (transactions.isEmpty()) {
                continue;
            }
            // 按实际模块调用
            if (!commitAdviceBegin) {
                commitAdviceBegin = true;
                String moduleCode = ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(processor.getType());
                commitAdvice = commitAdviceMap.get(moduleCode);
                if (commitAdvice == null) {
                    commitAdvice = commitAdviceMap.get(String.valueOf(processor.getType()));
                }
                if (commitAdvice != null) {
                    commitAdvice.begin(chainId, txs, blockHeader);
                }
            }

            boolean commit = processor.commit(chainId, transactions, blockHeader);
            if (!commit) {
                completedProcessors.forEach(e -> e.rollback(chainId, map.get(e.getType()), blockHeader));
                resultMap.put("value", commit);
                return success(resultMap);
            } else {
                completedProcessors.add(processor);
            }
        }
        resultMap.put("value", true);
        if (commitAdvice != null) {
            commitAdvice.end(chainId, txs, blockHeader);
        }
        return success(resultMap);
    }

    /**
     * 获取最新主链高度
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txRollback(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("blockHeader"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        String blockHeaderStr = (String) params.get("blockHeader");
        BlockHeader blockHeader = RPCUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        for (String txStr : txList) {
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            txs.add(tx);
        }
        boolean rollbackAdviceBegin = false;
        CommonAdvice rollbackAdvice = null;
        Map<Integer, List<Transaction>> map = new HashMap<>();
        for (TransactionProcessor processor : processors) {
            for (Transaction tx : txs) {
                List<Transaction> transactions = map.computeIfAbsent(processor.getType(), k -> new ArrayList<>());
                if (tx.getType() == processor.getType()) {
                    transactions.add(tx);
                }
            }
        }
        Map<String, Boolean> resultMap = new HashMap<>(2);
        List<TransactionProcessor> completedProcessors = new ArrayList<>();
        for (TransactionProcessor processor : processors) {
            List<Transaction> transactions = map.get(processor.getType());
            if (transactions.isEmpty()) {
                continue;
            }
            // 按实际模块调用
            if (!rollbackAdviceBegin) {
                rollbackAdviceBegin = true;
                String moduleCode = ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(processor.getType());
                rollbackAdvice = rollbackAdviceMap.get(moduleCode);
                if (rollbackAdvice == null) {
                    rollbackAdvice = rollbackAdviceMap.get(String.valueOf(processor.getType()));
                }
                if (rollbackAdvice != null) {
                    rollbackAdvice.begin(chainId, txs, blockHeader);
                }
            }

            boolean rollback = processor.rollback(chainId, transactions, blockHeader);
            if (!rollback) {
                completedProcessors.forEach(e -> e.commit(chainId, map.get(e.getType()), blockHeader));
                resultMap.put("value", rollback);
                return success(resultMap);
            } else {
                completedProcessors.add(processor);
            }
        }
        resultMap.put("value", true);
        if (rollbackAdvice != null) {
            rollbackAdvice.end(chainId, txs, blockHeader);
        }
        return success(resultMap);
    }

}
