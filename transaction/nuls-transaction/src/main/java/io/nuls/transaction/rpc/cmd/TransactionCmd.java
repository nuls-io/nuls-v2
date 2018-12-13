package io.nuls.transaction.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.dto.TxRegisterDTO;
import io.nuls.transaction.rpc.call.TransactionCmdCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.TransactionService;
import io.nuls.transaction.utils.TransactionManager;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConfirmedTransactionService confirmedTransactionService;

    /**
     * Register module transactions, validators, processors(commit, rollback), etc.
     * 注册模块交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "tx_register", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "module transaction registration")
    public Response register(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        // check parameters
        try {
            if (params == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ModuleTxRegisterDTO moduleTxRegisterDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), ModuleTxRegisterDTO.class);
            List<TxRegisterDTO> txRegisterList = moduleTxRegisterDto.getList();
            if (moduleTxRegisterDto == null || txRegisterList == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            //循环注册多种交易
            for (TxRegisterDTO txRegisterDto : txRegisterList) {
                TxRegister txRegister = new TxRegister();
                txRegister.setModuleCode(moduleTxRegisterDto.getModuleCode());
                txRegister.setModuleValidator(moduleTxRegisterDto.getModuleValidator());
                txRegister.setTxType(txRegisterDto.getTxType());
                txRegister.setValidator(txRegisterDto.getValidator());
                txRegister.setCommit(txRegisterDto.getCommit());
                txRegister.setRollback(txRegisterDto.getRollback());
                txRegister.setSystemTx(txRegisterDto.isSystemTx());
                txRegister.setUnlockTx(txRegisterDto.isUnlockTx());
                txRegister.setVerifySignature(txRegisterDto.isVerifySignature());
                //注册交易
                result = transactionService.register(txRegister);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return failed(e.getMessage());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }

        map.put("value", result);
        return success(map);
    }

    /**
     * Receive a new transaction serialization data
     * 接收本地新交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "newTx", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "receive a new transaction")
    public Response newTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Object chainIdObj = params == null ? null : params.get("chainId");
            Object txHexObj = params == null ? null : params.get("txHex");
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            int chainId = (Integer) chainIdObj;
            String txHex = (String) txHexObj;
            //将txHex转换为Transaction对象
            Transaction transaction = TxUtil.getTransaction(txHex);
            //将交易放入待验证本地交易队列中
            result = transactionService.newTx(chainId, transaction);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * Extract a packaged transaction list based on the packaging end time and transactions total size
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "tx_packableTxs", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "returns a list of packaged transactions")
    public Response packableTxs(Map params) {
        List<Transaction> packableTxsList = new ArrayList<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get("chainId");
            Object endtimestampObj = params == null ? null : params.get("endtimestamp");
            Object maxTxDataSizeObj = params == null ? null : params.get("maxTxDataSize");
            if (params == null || chainIdObj == null || endtimestampObj == null || maxTxDataSizeObj == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //结束打包的时间
            String endtimestamp = (String) endtimestampObj;
            //交易数据最大容量值
            String maxTxDataSize = (String) maxTxDataSizeObj;

            //TODO 查询可打包交易列表

        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        return success(packableTxsList);
    }

    /**
     * Execute the transaction processor commit
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "tx_commit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "transaction commit")
    public Response commit(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Object chainIdObj = params == null ? null : params.get("chainId");
            Object txHexObj = params == null ? null : params.get("txHex");
            Object secondaryDataHexObj = params == null ? null : params.get("secondaryDataHex");
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null || secondaryDataHexObj == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            int chainId = (Integer) chainIdObj;
            String txHex = (String) txHexObj;
            //将txHex转换为Transaction对象
            Transaction transaction = TxUtil.getTransaction(txHex);
            TxRegister txRegister = TransactionManager.getInstance().getTxRegister(transaction.getType());
            HashMap response = TransactionCmdCall.request(txRegister.getCommit(), txRegister.getModuleCode(), params);
            result = (Boolean) response.get("value");
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(result);
    }

    /**
     * Execute transaction processor rollback
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "tx_rollback", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "transaction rollback")
    public Response rollback(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Object chainIdObj = params == null ? null : params.get("chainId");
            Object txHexObj = params == null ? null : params.get("txHex");
            Object secondaryDataHexObj = params == null ? null : params.get("secondaryDataHex");
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null || secondaryDataHexObj == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            int chainId = (Integer) chainIdObj;
            String txHex = (String) txHexObj;
            //将txHex转换为Transaction对象
            Transaction transaction = TxUtil.getTransaction(txHex);
            TxRegister txRegister = TransactionManager.getInstance().getTxRegister(transaction.getType());
            HashMap response = TransactionCmdCall.request(txRegister.getRollback(), txRegister.getModuleCode(), params);
            result = (Boolean) response.get("value");
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(result);
    }

    /**
     * Save the transaction in the new block that was verified to the database
     * 保存新区块的交易
     *
     * @param params
     * @return
     */
    public Response save(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Object chainIdObj = params == null ? null : params.get("chainId");
            Object txHexListObj = params == null ? null : params.get("txList");
            // check parameters
            if (params == null || chainIdObj == null || txHexListObj == null) {
                throw new NulsException(TxErrorCode.NULL_PARAMETER);
            }
            int chainId = (Integer) chainIdObj;
            List<String> txHexList = (List<String>) txHexListObj;
            List<Transaction> txList = new ArrayList<>();
            for (String txHex : txHexList) {
                //将txHex转换为Transaction对象
                Transaction tx = TxUtil.getTransaction(txHex);
                txList.add(tx);
            }
            result = confirmedTransactionService.saveTxList(chainId, txList);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(result);
    }

    /**
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return
     */
    public Response getTx(List params) {

        return success("success");
    }

    /**
     * Delete transactions that have been packaged into blocks from the database, block rollback, etc.
     *
     * @param params
     * @return
     */
    public Response delete(List params) {

        return success("success");
    }

    /**
     * Local validation of transactions (including cross-chain transactions),
     * including calling the validator, verifying the coinData.
     * Cross-chain verification of cross-chain transactions is not included.
     *
     * @param params
     * @return
     */
    public Response verify(List params) {

        return success("success");
    }

    /**
     * Returns the relationship list of the transaction and its corresponding commit processor and rollback processor
     *
     * @param params
     * @return
     */
    public Response getTxProcessors(List params) {

        return success("success");
    }

    /**
     * Query the transaction list based on conditions such as account, chain, asset, and paging information.
     *
     * @param params
     * @return
     */
    public Response getTxs(List params) {

        return success("success");
    }


}
