package io.nuls.transaction.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Page;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.dto.TxRegisterDTO;
import io.nuls.transaction.model.po.TransactionPO;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.TransactionService;
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
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionManager transactionManager;
    @Autowired
    private TransactionH2Service transactionH2Service;


    /**
     * Register module transactions, validators, processors(commit, rollback), etc.
     * 注册模块交易
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_REGISTER, version = 1.0, description = "module transaction registration")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "moduleCode", parameterType = "String")
    @Parameter(parameterName = "moduleValidator", parameterType = "String")
    @Parameter(parameterName = "list", parameterType = "List")
    public Response register(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY);
        boolean result = false;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("moduleCode"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("moduleValidator"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("list"), TxErrorCode.PARAMETER_ERROR.getMsg());

            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ModuleTxRegisterDTO moduleTxRegisterDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), ModuleTxRegisterDTO.class);

            Chain chain = chainManager.getChain(moduleTxRegisterDto.getChainId());
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
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

                result = transactionService.register(chain, txRegister);
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
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_NEWTX, version = 1.0, description = "receive a new transaction")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response newTx(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY);
        boolean result = false;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHex"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHex = (String)params.get("txHex");
            //将txHex转换为Transaction对象
            Transaction transaction = TxUtil.getTransaction(txHex);
            //将交易放入待验证本地交易队列中
            result = transactionService.newTx(chain, transaction);
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
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_PACKABLETXS, version = 1.0, description = "returns a list of packaged transactions")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "endTimestamp", parameterType = "long")
    @Parameter(parameterName = "maxTxDataSize", parameterType = "int")
    public Response packableTxs(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("endTimestamp"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("maxTxDataSize"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            //结束打包的时间
            long endTimestamp = (long) params.get("endTimestamp");
            //交易数据最大容量值
            int maxTxDataSize = (int) params.get("maxTxDataSize");

            List<String> txHexlist = transactionService.getPackableTxs(chain, endTimestamp, maxTxDataSize);
            Map<String, List<String>> map = new HashMap<>(TxConstant.INIT_CAPACITY);
            map.put("list", txHexlist);
            return success(map);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
    }

    /**
     * Save the transaction in the new block that was verified to the database
     * 保存新区块的交易
     *
     * @param params Map
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_SAVE, version = 1.0, description = "transaction save")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHashList", parameterType = "List")
    @Parameter(parameterName = "secondaryDataHex", parameterType = "String")
    public Response txSave(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY);
        boolean result = false;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("secondaryDataHex"), TxErrorCode.PARAMETER_ERROR.getMsg());

            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashHexList = (List<String>) params.get("txHashList");
            List<NulsDigestData> txHashList = new ArrayList<>();
            //将交易hashHex解码为交易hash字节数组
            for (String hashHex : txHashHexList) {
                txHashList.add(NulsDigestData.fromDigestHex(hashHex));
            }
            //批量保存已确认交易
            BlockHeaderDigest blockHeaderDigest = TxUtil.getInstance((String)params.get("secondaryDataHex"), BlockHeaderDigest.class);
            result = confirmedTransactionService.saveTxList(chain, txHashList, blockHeaderDigest);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY);
        resultMap.put("value", result);
        return success(result);
    }

    /**
     * rollback the transaction in the new block that was verified to the database
     * 回滚新区块的交易
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_ROLLBACK, version = 1.0, description = "transaction rollback")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHashList", parameterType = "List")
    @Parameter(parameterName = "secondaryDataHex", parameterType = "String")
    public Response txRollback(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY);
        boolean result = false;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("secondaryDataHex"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashHexList = (List<String>) params.get("txHashList");
            List<NulsDigestData> txHashList = new ArrayList<>();
            //将交易hashHex解码为交易hash字节数组
            for (String hashHex : txHashHexList) {
                txHashList.add(NulsDigestData.fromDigestHex(hashHex));
            }
            //批量回滚已确认交易
            BlockHeaderDigest blockHeaderDigest = TxUtil.getInstance((String)params.get("secondaryDataHex"), BlockHeaderDigest.class);
            result = confirmedTransactionService.rollbackTxList(chain, txHashList, blockHeaderDigest);

        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY);
        resultMap.put("value", result);
        return success(result);
    }

    /**
     * Get system transaction types
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_GET_SYSTEM_TYPES, version = 1.0, description = "Get system transaction types")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getSystemTypes(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<Integer> list = transactionManager.getSysTypes(chain);
            return success(list);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
    }

    /**
     * 根据hash获取已确认交易
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_GETTX, version = 1.0, description = "Get transaction ")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHash", parameterType = "String")
    public Response getTx(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsDigestData.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            Transaction tx = confirmedTransactionService.getTransaction(chain, NulsDigestData.fromDigestHex(txHash));
            if(tx == null){
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            return success(tx);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
    }

    /**
     * 分页查询交易记录
     * Query the transaction list based on conditions such as account, chain, asset, and paging information.
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_GETTXS, version = 1.0, description = "Get transaction record")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getTxs(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Integer assetChainId = null == params.get("assetChainId") ? null : Integer.parseInt(params.get("assetChainId").toString());
            Integer assetId = null == params.get("assetId") ? null : Integer.parseInt(params.get("assetId").toString());
            Integer type = null == params.get("type") ? null : Integer.parseInt(params.get("type").toString());
            Integer pageSize = null == params.get("pageSize") ? TxConstant.PAGESIZE : Integer.parseInt(params.get("pageSize").toString());
            Integer pageNumber = null == params.get("pageNumber") ? 1 : Integer.parseInt(params.get("pageNumber").toString());
            String address = (String) params.get("address");

            Page<TransactionPO> list = transactionH2Service.getTxs(address, assetChainId, assetId, type, pageNumber, pageSize);

            return success(list);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
    }

    /**
     * The transaction is verified locally before the block is saved,
     * including calling the validator, verifying the coinData, etc.
     * If it is a cross-chain transaction and not initiated by the current chain,
     * the result of the cross-chain verification is checked.
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.TX_VERIFY, version = 1.0, description = "")
    public Response batchVerify(Map params){
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY);
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
            result = transactionService.batchVerify(chainManager.getChain(chainId), txHexList);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY);
        resultMap.put("value", result);
        return success(result);
    }


}
