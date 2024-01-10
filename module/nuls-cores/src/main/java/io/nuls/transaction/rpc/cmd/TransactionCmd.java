package io.nuls.transaction.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.TxRegisterDetail;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxContext;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxPackage;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxUtil;

import java.util.*;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Component
@NulsCoresCmd(module = ModuleE.TX)
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TxService txService;
    @Autowired
    private ConfirmedTxService confirmedTxService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private PackablePool packablePool;

    @CmdAnnotation(cmd = TxCmd.TX_REGISTER, version = 1.0, description = "Registration module transaction/Register module transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "moduleCode", parameterType = "String", parameterDes = "Module for registering transactionscode"),
            @Parameter(parameterName = "list", requestType = @TypeDescriptor(value = List.class, collectionElement = TxRegisterDetail.class), parameterDes = "Data to be registered for transactions"),
            @Parameter(parameterName = "delList", requestType = @TypeDescriptor(value = List.class, collectionElement = Integer.class), parameterDes = "Pending removal of registered transaction data", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Is registration successful")
    }))
    public Response register(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("moduleCode"), TxErrorCode.PARAMETER_ERROR.getMsg());

            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ModuleTxRegisterDTO moduleTxRegisterDto = JSONUtils.map2pojo(params, ModuleTxRegisterDTO.class);

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

    @CmdAnnotation(cmd = TxCmd.TX_BROADCAST, version = 1.0, description = "Directly broadcast new transactions/broadcast a new transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "Transaction serialization data string")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful"),
            @Key(name = "hash", description = "transactionhash")
    }))
    public Response broadcastTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txStr = (String) params.get("tx");
            //taketxStrConvert toTransactionobject
            Transaction transaction = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            //todo fro 2.18.0 version
            CoinData cd = transaction.getCoinDataInstance();
            for (CoinFrom from : cd.getFrom()) {
                if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(from.getAddress()) == 2) {
                    throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                }
            }
            for (CoinTo to : cd.getTo()) {
                if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(to.getAddress()) == 2) {
                    throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                }
            }
            //Put the transaction into the local transaction queue to be verified
            boolean rs = NetworkCall.broadcastTx(chain, transaction);
            Map<String, Object> map = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            map.put("value", rs);
            map.put("hash", transaction.getHash().toHex());
            return success(map);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_NEWTX, version = 1.0, description = "Receive local new transactions/receive a new transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "Transaction serialization data string")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful"),
            @Key(name = "hash", description = "transactionhash")
    }))
    public Response newTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txStr = (String) params.get("tx");
            //taketxStrConvert toTransactionobject
            Transaction transaction = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            //Put the transaction into the local transaction queue to be verified
            txService.newTx(chain, transaction);
            Map<String, Object> map = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            map.put("value", true);
            map.put("hash", transaction.getHash().toHex());
            return success(map);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.TX_PACKABLETXS, version = 1.0, description = "Obtain a packable transaction set/returns a list of packaged transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "endTimestamp", requestType = @TypeDescriptor(value = long.class), parameterDes = "Deadline"),
            @Parameter(parameterName = "maxTxDataSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "Maximum capacity of transaction set"),
            @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "Time of block production this time"),
            @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "Current block address"),
            @Parameter(parameterName = "preStateRoot", parameterType = "String", parameterDes = "The state root of the previous block")
    })
    @ResponseData(name = "Return value", description = "Return aMap, including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = String.class, description = "Packable transaction set"),
            @Key(name = "stateRoot", description = "The current state root of the block"),
            @Key(name = "packageHeight", valueType = long.class, description = "The height of the packaged blocks this time")
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
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            //End packaging time
            long endTimestamp = Long.parseLong(params.get("endTimestamp").toString());
            //Maximum capacity value of transaction data
            int maxTxDataSize = (int) params.get("maxTxDataSize");

            long blockTime = Long.parseLong(params.get("blockTime").toString());
            String packingAddress = (String) params.get("packingAddress");
            String preStateRoot = (String) params.get("preStateRoot");

            TxPackage txPackage;
            if(ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                txPackage = txService.getPackableTxsV8(chain, endTimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
            } else {
                txPackage = txService.getPackableTxs(chain, endTimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
            }

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

    @CmdAnnotation(cmd = TxCmd.TX_BACKPACKABLETXS, version = 1.0, description = "The consensus module returns transactions that cannot be packaged and adds them back to the list to be packaged/back packaged transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction serialization data string collection")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
    }))
    public Response backPackableTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txStrList = (List<String>) params.get("txList");
            int count = txStrList.size() - 1;
            for (int i = count; i >= 0; i--) {
                Transaction tx = TxUtil.getInstanceRpcStr(txStrList.get(i), Transaction.class);
                packablePool.offerFirstOnlyHash(chain, tx);
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
     * Save transactions for new blocks
     *
     * @param params Map
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_SAVE, priority = CmdPriority.HIGH, version = 1.0, description = "Save transactions for new blocks/Save the confirmed transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction set to be saved"),
            @Parameter(parameterName = "contractList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Smart contract trading"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Block head")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
    }))
    public Response txSave(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());

            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txStrList = (List<String>) params.get("txList");
            if (null == txStrList) {
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

    @CmdAnnotation(cmd = TxCmd.TX_GENGSIS_SAVE, version = 1.0, description = "Save transactions for Genesis blocks/Save the transactions of the Genesis block ")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction set to be saved"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Block head")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
    }))
    public Response txGengsisSave(Map params) {
        Map<String, Boolean> map = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = false;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());

            chain = chainManager.getChain((Integer) params.get("chainId"));
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

    @CmdAnnotation(cmd = TxCmd.TX_ROLLBACK, priority = CmdPriority.HIGH, version = 1.0, description = "Rollback block transactions/transaction rollback")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Collection of transactions to be rolled back"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Block head")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
    }))
    public Response txRollback(Map params) {
        boolean result;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashStrList = (List<String>) params.get("txHashList");
            List<NulsHash> txHashList = new ArrayList<>();
            //Transfer the transactionhashHexDecode as transactionhashByte array
            for (String hashStr : txHashStrList) {
                txHashList.add(NulsHash.fromHex(hashStr));
            }
            //Batch rollback of confirmed transactions
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

    @CmdAnnotation(cmd = TxCmd.TX_GET_SYSTEM_TYPES, version = 1.0, description = "Get all system transaction types/Get system transaction types")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = Integer.class, description = "System transaction type collection")
    }))
    public Response getSystemTypes(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
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

    @CmdAnnotation(cmd = TxCmd.TX_GETTX, version = 1.0, description = "according tohashObtain transactions, First check for unconfirmed information, Can't find it, check again. Confirmed/Get transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Transaction to be queriedhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "The string of serialized data obtained from the transaction")
    }))
    public Response getTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
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
//                LOG.debug("getTx - from all, fail! tx is null, txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
//                LOG.debug("getTx - from all, success txHash : " + tx.getTx().getHash().toHex());
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

    @CmdAnnotation(cmd = TxCmd.TX_GET_CONFIRMED_TX, version = 1.0, description = "according tohashObtain confirmed transactions(Only check confirmed)/Get confirmed transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Transaction to be queriedhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "The string of serialized data obtained from the transaction")
    }))
    public Response getConfirmedTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
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
//                LOG.debug("getConfirmedTransaction fail, tx is null. txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
//                LOG.debug("getConfirmedTransaction success. txHash:{}", txHash);
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


    @CmdAnnotation(cmd = TxCmd.TX_ISCONFIRMED, version = 1.0, description = "according tohashObtain whether the transaction has been confirmed(Only check confirmed)/Check tx is confirmed by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Transaction to be queriedhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "true: confirmed; false:unconfirmed")
    }))
    public Response isConfirmed(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO txPO = confirmedTxService.getConfirmedTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, Boolean> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("value", false);
            if (txPO != null) {
                Transaction tx = txPO.getTx();
                if (null != tx && txHash.equals(tx.getHash().toHex())) {
                    resultMap.put("value", true);
                }
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
            description = "Obtain the complete transaction of the block. If no query is found, or if the query does not include the complete transaction data of the block, return an empty set/Get block transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction to be queriedhashaggregate")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "Returns a collection of transaction serialization data strings")
    }))
    public Response getBlockTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashList = (List<String>) params.get("txHashList");
            List<String> txList = confirmedTxService.getTxList(chain, txHashList);
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

    @CmdAnnotation(cmd = TxCmd.TX_GET_BLOCK_TXS_EXTEND, version = 1.0, description = "according tohashList, obtain transactions, check for unconfirmed first, then check for confirmed/Get transactions by hashs")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction to be queriedhashaggregate"),
            @Parameter(parameterName = "allHits", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "true：All data must be found before returning, otherwise return emptylist； false：How many were found and how many were returned")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "Returns a collection of transaction serialization data strings")
    }))
    public Response getBlockTxsExtend(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("allHits"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
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


    @CmdAnnotation(cmd = TxCmd.TX_GET_NONEXISTENT_UNCONFIRMED_HASHS, version = 1.0, description = "Query incoming transactionshashin,Transactions that are not in the unconfirmed databasehash/Get nonexistent unconfirmed transaction hashs")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Transaction to be queriedhashaggregate")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "Returns a collection of transaction serialization data strings")
    }))
    public Response getNonexistentUnconfirmedHashs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHashList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txHashList = (List<String>) params.get("txHashList");
            List<String> hashList = confirmedTxService.getNonexistentUnconfirmedHashList(chain, txHashList);
            Map<String, List<String>> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("txHashList", hashList);
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = TxCmd.TX_BATCHVERIFY, priority = CmdPriority.HIGH, version = 1.0, description = "Verify all transactions in the block/Verify all transactions in the block")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Collection of serialized data strings for transactions to be verified"),
            @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Corresponding block header"),
            @Parameter(parameterName = "preStateRoot", parameterType = "String", parameterDes = "Previous block state root")
    })
    @ResponseData(name = "Return value", description = "Return aMap, including twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Verified successfully"),
            @Key(name = "contractList", valueType = List.class, valueElement = String.class, description = "New transactions generated by smart contracts")
    }))
    public Response batchVerify(Map params) {
        VerifyLedgerResult verifyLedgerResult = null;
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("blockHeader"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("preStateRoot"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<String> txList = (List<String>) params.get("txList");

            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = TxUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);

            String preStateRoot = (String) params.get("preStateRoot");

            Map<String, Object> resultMap;
            if(ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                resultMap = txService.batchVerifyV8(chain, txList, blockHeader, blockHeaderStr, preStateRoot);
            } else {
                resultMap = txService.batchVerify(chain, txList, blockHeader, blockHeaderStr, preStateRoot);
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

    @CmdAnnotation(cmd = TxCmd.TX_SETCONTRACTGENERATETXTYPES, priority = CmdPriority.HIGH, version = 1.0, description = "Set the system transaction type generated by the smart contract module（Including consensus, cross chain, etc；ExcludinggasReturn transaction）")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txTypeList", requestType = @TypeDescriptor(value = List.class, collectionElement = Integer.class), parameterDes = "System transaction types generated by the smart contract module（Including consensus, cross chain, etc；ExcludinggasReturn transaction）"),
    })
    @ResponseData(description = "No specific return value, set successfully without errors")
    public Response setContractGenerateTxTypes(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txTypeList"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            List<Integer> txTypeList = (List<Integer>) params.get("txTypeList");
            if (txTypeList == null) {
                txTypeList = new ArrayList<>();
            }
            chain.setContractGenerateTxTypes(new HashSet<>(txTypeList));
            chain.getLogger().info("Set smart contract generation transaction type: {}", Arrays.toString(txTypeList.toArray()));
            return success();
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @CmdAnnotation(cmd = TxCmd.TX_CS_STATE, version = 1.0, description = "Set node packaging status(Set by consensus module)/Set the node packaging state")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "packaging", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "Is packaging in progress")
    })
    @ResponseData(description = "No specific return value, set successfully without errors")
    public Response packaging(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Boolean packaging = null == params.get("packaging") ? null : (Boolean) params.get("packaging");
            if (null == packaging) {
                throw new NulsException(TxErrorCode.PARAMETER_ERROR);
            }
            chain.getPackaging().set(packaging);
            chain.getLogger().debug("Task-Packaging Is the node a packaging node,Status changed to: {}", chain.getPackaging().get());
            return success();
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = TxCmd.TX_BL_STATE, version = 1.0, description = "Set node block synchronization status(Set by block module)/Set the node block state")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "status", requestType = @TypeDescriptor(value = int.class), parameterDes = "Is waiting entered, Do not process transactions")
    })
    @ResponseData(description = "No specific return value, set successfully without errors")
    public Response blockNotice(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Integer status = (Integer) params.get("status");
            if (null == status) {
                throw new NulsException(TxErrorCode.PARAMETER_ERROR);
            }
            if (1 == status) {
                chain.getProcessTxStatus().set(true);
                chain.getLogger().info("Node block synchronization status changed to: true");
            } else {
                chain.getProcessTxStatus().set(false);
                chain.getLogger().info("Node block synchronization status changed to: false");
            }
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
     * Latest block height
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.TX_BLOCK_HEIGHT, priority = CmdPriority.HIGH, version = 1.0, description = "Receive the latest block height/Receive the latest block height")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
    }))
    public Response height(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Long height = Long.parseLong(params.get("height").toString());
            chain.setBestBlockHeight(height);
            chain.getLogger().debug("The latest confirmed block height update is: [{}]" + TxUtil.nextLine() + TxUtil.nextLine(), height);
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


    @CmdAnnotation(cmd = "tx_getTxSigners", version = 1.0, description = "Obtain a list of legitimate signatories for the transaction/Gets the list of signers of the transaction's legal signature")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHex", parameterType = "String", parameterDes = "Transaction String")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = List.class, valueElement = String.class, description = "Legal signature account for transactions"),

    }))
    public Object getTxSigners(Map params){
        Chain chain = null;
        try {
            // check parameters
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHex"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHex = (String)params.get("txHex");
            Transaction tx = TxUtil.getInstance(txHex, Transaction.class);
            TransactionSignature transactionSignature = null;
            if (tx.isMultiSignTx()) {
                transactionSignature = TxUtil.getInstance(tx.getTransactionSignature(), MultiSignTxSignature.class);
            } else {
                transactionSignature = TxUtil.getInstance(tx.getTransactionSignature(), TransactionSignature.class);
            }
            List<P2PHKSignature> p2PHKSignatureList = transactionSignature.getP2PHKSignatures();
            Set<String> signers = new HashSet<>();
            if(null != p2PHKSignatureList && !p2PHKSignatureList.isEmpty()){
                for (P2PHKSignature signature : p2PHKSignatureList) {
                    if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                        throw new NulsException(new Exception("Transaction signature error !"));
                    }else{
                        signers.add(AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chain.getChainId())));
                    }
                }
            }
            Map<String, Object> map = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            map.put("list", signers);
            return success(map);
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
