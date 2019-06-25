package io.nuls.transaction.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TxRegisterDetail;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxPackage;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TxService txService;
    @Autowired
    private ConfirmedTxService confirmedTxService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private PackablePool packablePool;

    @CmdAnnotation(cmd = TxCmd.TX_REGISTER, version = 1.0, description = "注册模块交易/Register module transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "moduleCode", parameterType = "String", parameterDes = "注册交易的模块code"),
            @Parameter(parameterName = "list", parameterType = "List", parameterDes = "待注册交易的数据"),
            @Parameter(parameterName = "delList", parameterType = "List", parameterDes = "待移除已注册交易数据", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否注册成功")
    }))
    public Response register(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("moduleCode"), TxErrorCode.PARAMETER_ERROR.getMsg());

            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ModuleTxRegisterDTO moduleTxRegisterDto = JSONUtils.map2pojo(params,ModuleTxRegisterDTO.class);

            chain = chainManager.getChain(moduleTxRegisterDto.getChainId());
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<TxRegisterDetail> txRegisterList = moduleTxRegisterDto.getList();
            if (moduleTxRegisterDto == null || txRegisterList == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            result = txService.register(chain, moduleTxRegisterDto);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        map.put("value", result);
        return success(map);
    }

    @CmdAnnotation(cmd = TxCmd.TX_NEWTX, version = 1.0, description = "接收本地新交易/receive a new transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易序列化数据字符串")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response newTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txStr = (String) params.get("tx");
            //将txStr转换为Transaction对象
            Transaction transaction = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            //将交易放入待验证本地交易队列中
            txService.newTx(chain, transaction);
            Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            map.put("value", true);
            return success(map);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

//    /**
//     * 新交易基础验证
//     * @param params
//     * @return Response
//     */
//    @CmdAnnotation(cmd = TxCmd.TX_BASE_VALIDATE, version = 1.0, description = "新交易基础验证/Transaction base validate")
//    @Parameters(value = {
//            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
//            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易序列化数据字符串")
//    })
//    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
//            @Key(name = "value", valueType = boolean.class, description = "是否验证通过")
//    }))
//    public Response baseValidateTx(Map params) {
//        Chain chain = null;
//        try {
//            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
//            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
//            chain = chainManager.getChain((int) params.get("chainId"));
//            if (null == chain) {
//                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
//            }
//            String txStr = (String) params.get("tx");
//            //将txStr转换为Transaction对象
//            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
//            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
//            if(null == txRegister){
//                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
//            }
//            //将交易放入待验证本地交易队列中
//            txService.baseValidateTx(chain, tx, txRegister);
//            Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
//            map.put("value", true);
//            return success(map);
//        } catch (NulsException e) {
//            errorLogProcess(chain, e);
//            return failed(e.getErrorCode());
//        } catch (Exception e) {
//            errorLogProcess(chain, e);
//            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
//        }
//    }

    @CmdAnnotation(cmd = TxCmd.TX_PACKABLETXS, version = 1.0, description = "获取可打包的交易集/returns a list of packaged transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "endTimestamp", parameterType = "long", parameterDes = "截止时间"),
            @Parameter(parameterName = "maxTxDataSize", parameterType = "int", parameterDes = "交易集最大容量"),
            @Parameter(parameterName = "blockTime", parameterType = "long", parameterDes = "本次出块区块时间"),
            @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "当前出块地址"),
            @Parameter(parameterName = "preStateRoot", parameterType = "String", parameterDes = "前一个区块的状态根")
    })
    @ResponseData(name = "返回值", description = "返回一个Map，包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = String.class, description = "可打包交易集"),
            @Key(name = "stateRoot", description = "当前出块的状态根"),
            @Key(name = "packageHeight", valueType = long.class, description = "本次打包区块的高度")
    }))
    public Response packableTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("endTimestamp"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("maxTxDataSize"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockTime"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("packingAddress"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("preStateRoot"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            //结束打包的时间
            long endTimestamp =  Long.parseLong(params.get("endTimestamp").toString());
            //交易数据最大容量值
            int maxTxDataSize = (int) params.get("maxTxDataSize");

            long blockHeight = chain.getBestBlockHeight() + 1;
            long blockTime = Long.parseLong(params.get("blockTime").toString());
            String packingAddress = (String) params.get("packingAddress");
            String preStateRoot = (String) params.get("preStateRoot");

            TxPackage txPackage = txService.getPackableTxs(chain, endTimestamp, maxTxDataSize, blockHeight, blockTime, packingAddress, preStateRoot);
            Map<String, Object> map = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            map.put("list", txPackage.getList());
            map.put("stateRoot", txPackage.getStateRoot());
            map.put("packageHeight", txPackage.getPackageHeight());
            return success(map);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_BACKPACKABLETXS, version = 1.0, description = "共识模块把不能打包的交易还回来，重新加入待打包列表/back packaged transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txList", parameterType = "list", parameterDes = "交易序列化数据字符串集合")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response backPackableTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txStrList = (List<String>) params.get("txList");
            int count = txStrList.size()-1;
            for(int i = count; i >= 0; i--) {
                Transaction tx = TxUtil.getInstanceRpcStr(txStrList.get(i), Transaction.class);
                packablePool.offerFirst(chain, tx);
            }
            Map<String, Object> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            map.put("value", true);
            return success(map);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }




    /**
     * Save the transaction in the new block that was verified to the database
     * 保存新区块的交易
     *
     * @param params Map
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_SAVE, version = 1.0, description = "保存新区块的交易/Save the confirmed transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "待保存的交易集合"),
            @Parameter(parameterName = "contractList", parameterType = "List", parameterDes = "智能合约交易"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "区块头")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response txSave(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());

            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txStrList = (List<String>) params.get("txList");
            if(null == txStrList){
                throw new NulsException(TxErrorCode.PARAMETER_ERROR);
            }
            List<String> contractList = (List<String>) params.get("contractList");
            result = confirmedTxService.saveTxList(chain, txStrList, contractList, (String) params.get("blockHeader"));
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
        resultMap.put("value", result);
        return success(resultMap);
    }

    @CmdAnnotation(cmd = TxCmd.TX_GENGSIS_SAVE, version = 1.0, description = "保存创世块的交易/Save the transactions of the Genesis block ")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "待保存的交易集合"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "区块头")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response txGengsisSave(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());

            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txStrList = (List<String>) params.get("txList");
            result = confirmedTxService.saveGengsisTxList(chain, txStrList, (String) params.get("blockHeader"));
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
        resultMap.put("value", result);
        return success(resultMap);
    }

    @CmdAnnotation(cmd = TxCmd.TX_ROLLBACK, version = 1.0, description = "回滚区块的交易/transaction rollback")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHashList", parameterType = "List", parameterDes = "待回滚交易集合"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "区块头")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response txRollback(Map params) {
        boolean result;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashStrList = (List<String>) params.get("txHashList");
            List<NulsHash> txHashList = new ArrayList<>();
            //将交易hashHex解码为交易hash字节数组
            for (String hashStr : txHashStrList) {
                txHashList.add(NulsHash.fromHex(hashStr));
            }
            //批量回滚已确认交易
            result = confirmedTxService.rollbackTxList(chain, txHashList, (String) params.get("blockHeader"));
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
        resultMap.put("value", result);
        return success(resultMap);
    }

    @CmdAnnotation(cmd = TxCmd.TX_GET_SYSTEM_TYPES, version = 1.0, description = "获取所有系统交易类型/Get system transaction types")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = Integer.class, description = "系统交易类型集合")
    }))
    public Response getSystemTypes(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<Integer> list = TxManager.getSysTypes(chain);
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("list", list);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_GETTX, version = 1.0, description = "根据hash获取交易, 先查未确认, 查不到再查已确认/Get transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "待查询交易hash")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "获取到的交易的序列化数据的字符串")
    }))
    public Response getTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = txService.getTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, String> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            if (tx == null) {
                LOG.debug("getTx - from all, fail! tx is null, txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                LOG.debug("getTx - from all, success txHash : " + tx.getTx().getHash().toHex());
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX_CONFIRMED, version = 1.0, description = "根据hash获取已确认交易(只查已确认)/Get confirmed transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "待查询交易hash")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "获取到的交易的序列化数据的字符串")
    }))
    public Response getConfirmedTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = confirmedTxService.getConfirmedTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, String> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            if (tx == null) {
                LOG.debug("getConfirmedTransaction fail, tx is null. txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                LOG.debug("getConfirmedTransaction success. txHash:{}", txHash);
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_GET_BLOCK_TXS, version = 1.0,
            description = "获取区块的完整交易，如果没有查询到，或者查询到的不是区块完整的交易数据，则返回空集合/Get block transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHashList", parameterType = "list", parameterDes = "待查询交易hash集合")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "返回交易序列化数据字符串集合")
    }))
    public Response getBlockTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashList = (List<String>) params.get("txHashList");
            List<String> txList = confirmedTxService.getTxList(chain,txHashList);
            Map<String, List<String>> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("txList", txList);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_GET_BLOCK_TXS_EXTEND, version = 1.0, description = "根据hash列表，获取交易，先查未确认，再查已确认/Get transactions by hashs")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHashList", parameterType = "list", parameterDes = "待查询交易hash集合"),
            @Parameter(parameterName = "allHits", parameterType = "boolean", parameterDes = "true：必须全部查到才返回数据，否则返回空list； false：查到几个返回几个")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "返回交易序列化数据字符串集合")
    }))
    public Response getBlockTxsExtend(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("allHits"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashList = (List<String>) params.get("txHashList");
            boolean allHits = (boolean) params.get("allHits");
            List<String> txList = confirmedTxService.getTxListExtend(chain, txHashList, allHits);
            Map<String, List<String>> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("txList", txList);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_BATCHVERIFY, version = 1.0, description = "验证区块所有交易/Verify all transactions in the block")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "待验证交易序列化数据字符串集合"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "对应的区块头"),
            @Parameter(parameterName = "preStateRoot", parameterType = "String", parameterDes = "前一个区块状态根")
    })
    @ResponseData(name = "返回值", description = "返回一个Map，包含两个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class,  description = "是否验证成功"),
            @Key(name = "contractList", valueType = List.class, valueElement = String.class, description = "智能合约新产生的交易")
    }))
    public Response batchVerify(Map params) {
        VerifyLedgerResult verifyLedgerResult = null;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("preStateRoot"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txList = (List<String>)  params.get("txList");

            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = TxUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);

            String preStateRoot = (String) params.get("preStateRoot");

            Map<String, Object> resultMap = txService.batchVerify(chain, txList, blockHeader, blockHeaderStr, preStateRoot);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @CmdAnnotation(cmd = TxCmd.TX_CS_STATE, version = 1.0, description = "设置节点打包状态/Set the node packaging state")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "packaging", parameterType = "Boolean", parameterDes = "是否正在打包")
    })
    @ResponseData(description = "无特定返回值，没有错误即设置成功")
    public Response packaging(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Boolean packaging = null == params.get("packaging") ? null : (Boolean) params.get("packaging");
            if (null == packaging) {
                throw new NulsException(TxErrorCode.PARAMETER_ERROR);
            }
            chain.getPackaging().set(packaging);
            chain.getLogger().debug("Task-Packaging 节点是否是打包节点,状态变更为: {}", chain.getPackaging().get());
            return success();
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 最新区块高度
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.TX_BLOCK_HEIGHT, version = 1.0, description = "接收最新区块高度/Receive the latest block height")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "height", parameterType = "long", parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response height(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Long height =  Long.parseLong(params.get("height").toString());
            chain.setBestBlockHeight(height);
            chain.getLogger().debug("最新已确认区块高度更新为: [{}]", height);
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("value", true);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }


}
