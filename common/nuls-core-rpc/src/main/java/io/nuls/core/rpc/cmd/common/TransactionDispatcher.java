package io.nuls.core.rpc.cmd.common;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
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

@Component
public class TransactionDispatcher extends BaseCmd {

    private List<TransactionProcessor> processors;

    public List<TransactionProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<TransactionProcessor> processors) {
        this.processors = processors;
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
    @Parameter(parameterName = "preStateRoot", parameterType = "String")
    public Response txValidator(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("blockHeader"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("preStateRoot"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        List<Transaction> finalInvalidTxs = new ArrayList<>();
        for (String txStr : txList) {
            try {
                Transaction tx = getInstanceRpcStr(txStr, Transaction.class);
                txs.add(tx);
            } catch (NulsException e) {
                return failed(e.getErrorCode());
            }

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
            List<Transaction> invalidTxs = processor.validate(chainId, map.get(processor.getType()), txs);
            finalInvalidTxs.addAll(invalidTxs);
            txs.removeAll(invalidTxs);
        }
        if (finalInvalidTxs.isEmpty()) {
            return success();
        }
        Map responseData = new HashMap<>(2);
        List<String> list = finalInvalidTxs.stream().map(e -> e.getHash().toHex()).collect(Collectors.toList());
        responseData.put("invalidHashs", list);
        return success(responseData);
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
    @Parameter(parameterName = "preStateRoot", parameterType = "String")
    public Response txCommit(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("blockHeader"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("preStateRoot"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        List<Transaction> finalInvalidTxs = new ArrayList<>();
        for (String txStr : txList) {
            try {
                Transaction tx = getInstanceRpcStr(txStr, Transaction.class);
                txs.add(tx);
            } catch (NulsException e) {
                return failed(e.getErrorCode());
            }

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
            boolean commit = processor.commit(chainId, txs);
            if (!commit) {
                return failed(CommonCodeConstanst.DATA_ERROR);
            }
        }
        return success();
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
    @Parameter(parameterName = "preStateRoot", parameterType = "String")
    public Response txRollback(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("blockHeader"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("preStateRoot"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        List<String> txList = (List<String>) params.get("txList");
        List<Transaction> txs = new ArrayList<>();
        List<Transaction> finalInvalidTxs = new ArrayList<>();
        for (String txStr : txList) {
            try {
                Transaction tx = getInstanceRpcStr(txStr, Transaction.class);
                txs.add(tx);
            } catch (NulsException e) {
                return failed(e.getErrorCode());
            }

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
            List<Transaction> invalidTxs = processor.validate(chainId, map.get(processor.getType()), txs);
            finalInvalidTxs.addAll(invalidTxs);
            txs.removeAll(invalidTxs);
        }
        for (TransactionProcessor processor : processors) {
            boolean commit = processor.rollback(chainId, txs);
            if (!commit) {
                return failed(CommonCodeConstanst.DATA_ERROR);
            }
        }
        return success();
    }

    public <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (null == bytes || bytes.length == 0) {
            throw new NulsException(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (Exception e) {
            throw new NulsException(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
    }

    /**
     * RPCUtil 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public <T> T getInstanceRpcStr(String data, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (StringUtils.isBlank(data)) {
            throw new NulsException(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
        return getInstance(RPCUtil.decode(data), clazz);
    }

}
