package io.nuls.core.rpc.cmd.common;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.protocol.TransactionProcessor;
import io.nuls.core.rpc.util.RPCUtil;

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
public class TransactionDispatcher extends BaseCmd {

    private List<TransactionProcessor> processors;

    public List<TransactionProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<TransactionProcessor> processors) {
        processors.sort(TransactionProcessor.COMPARATOR);
        this.processors = processors;
    }

    @Autowired("EmptyCommonAdvice")
    private CommonAdvice commitAdvice;
    @Autowired("EmptyCommonAdvice")
    private CommonAdvice rollbackAdvice;

    public void register(CommonAdvice commitAdvice, CommonAdvice rollbackAdvice) {
        if (commitAdvice != null) {
            this.commitAdvice = commitAdvice;
        }
        if (rollbackAdvice != null) {
            this.rollbackAdvice = rollbackAdvice;
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
        for (TransactionProcessor processor : processors) {
            List<Transaction> invalidTxs = processor.validate(chainId, map.get(processor.getType()), map, blockHeader);
            if (invalidTxs != null && !invalidTxs.isEmpty()) {
                finalInvalidTxs.addAll(invalidTxs);
                invalidTxs.forEach(e -> map.get(e.getType()).remove(e));
            }
        }
        Map<String, List<String>> resultMap = new HashMap<>(2);
        List<String> list = finalInvalidTxs.stream().map(e -> e.getHash().toHex()).collect(Collectors.toList());
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
        commitAdvice.begin(chainId, txs, blockHeader);
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
            boolean commit = processor.commit(chainId, map.get(processor.getType()), blockHeader);
            if (!commit) {
                completedProcessors.forEach(e -> e.rollback(chainId, map.get(e.getType()), blockHeader));
                resultMap.put("value", commit);
                return success(resultMap);
            } else {
                completedProcessors.add(processor);
            }
        }
        resultMap.put("value", true);
        commitAdvice.end(chainId, txs, blockHeader);
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
        rollbackAdvice.begin(chainId, txs, blockHeader);
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
            boolean rollback = processor.rollback(chainId, map.get(processor.getType()), blockHeader);
            if (!rollback) {
                completedProcessors.forEach(e -> e.commit(chainId, map.get(e.getType()), blockHeader));
                resultMap.put("value", rollback);
                return success(resultMap);
            } else {
                completedProcessors.add(processor);
            }
        }
        resultMap.put("value", true);
        rollbackAdvice.end(chainId, txs, blockHeader);
        return success(resultMap);
    }

}
