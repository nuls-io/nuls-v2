/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.enums.BlockType;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.manager.ContractTxProcessorManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.BatchInfo;
import io.nuls.contract.model.bo.ContractTempTransaction;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.dto.ModuleCmdRegisterDto;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.storage.ContractRewardLogByConsensusStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.*;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
@NulsCoresCmd(module = ModuleE.SC)
public class ContractCmd extends BaseCmd {

    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxProcessorManager contractTxProcessorManager;
    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;
    @Autowired
    private CmdRegisterManager cmdRegisterManager;
    @Autowired
    private NulsCoresConfig contractConfig;

    @CmdAnnotation(cmd = BATCH_BEGIN, version = 1.0, description = "Execute the start notification for a batch of contracts, generate information for the current batch/batch begin")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockType", parameterType = "int", parameterDes = "Block processing mode, Packaging blocks - 0, Verify Block - 1"),
        @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "The height of the currently packaged blocks"),
        @Parameter(parameterName = "blockTime", parameterType = "long", parameterDes = "The current packaged block time"),
        @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "The current block packaging address"),
        @Parameter(parameterName = "preStateRoot", parameterType = "String", parameterDes = "PreviousstateRoot")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response batchBegin(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            Integer blockType = (Integer) params.get("blockType");
            ChainManager.chainHandle(chainId, blockType);
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Long blockTime = Long.parseLong(params.get("blockTime").toString());
            String packingAddress = (String) params.get("packingAddress");
            String preStateRoot = (String) params.get("preStateRoot");
            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                Result result = contractService.beginV8(chainId, blockHeight, blockTime, packingAddress, preStateRoot);
                return success(result.getData());
            } else {
                contractService.begin(chainId, blockHeight, blockTime, packingAddress, preStateRoot);
                return success();
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INVOKE_CONTRACT, version = 1.0, description = "After the batch notification starts, execute the contract one by one/invoke contract one by one")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockType", parameterType = "int", parameterDes = "Block processing mode, Packaging blocks - 0, Verify Block - 1"),
        @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "Serialized transactionHEXEncoding string")
    })
    @ResponseData(description = "No specific return value, successful without errors. If an error is returned, the transaction will be discarded")
    public Response invokeContractOneByOne(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            Integer blockType = (Integer) params.get("blockType");
            ChainManager.chainHandle(chainId, blockType);
            String txData = (String) params.get("tx");
            ContractTempTransaction tx = new ContractTempTransaction();
            tx.setTxHex(txData);
            tx.parse(RPCUtil.decode(txData), 0);
            String hash = tx.getHash().toHex();
            Map<String, Boolean> dealResult = new HashMap<>(2);
            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.PROTOCOL_22 ) {
                // add by peter at 2025/01/16
                Result result = contractService.invokeContractOneByOneV22(chainId, tx);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                if (result.getData() == null) {
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("success", true);
                    resultData.put("gasUsed", 0);
                    resultData.put("txList", List.of());
                    return success(resultData);
                }
                return success(result.getData());
            } else if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.PROTOCOL_14 ) {
                // add by pierre at 2022/6/2 p14
                Result result = contractService.invokeContractOneByOneV14(chainId, tx);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                if (result.getData() == null) {
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("success", true);
                    resultData.put("gasUsed", 0);
                    resultData.put("txList", List.of());
                    return success(resultData);
                }
                return success(result.getData());
            } else if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                Result result = contractService.invokeContractOneByOneV8(chainId, tx);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                if (result.getData() == null) {
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("success", true);
                    resultData.put("gasUsed", 0);
                    resultData.put("txList", List.of());
                    return success(resultData);
                }
                return success(result.getData());
            } else {
                if(!contractHelper.getChain(chainId).getBatchInfo().checkGasCostTotal(hash)) {
                    Log.warn("Exceed tx count [600] or gas limit of block [13,000,000 gas], the contract transaction [{}] revert to package queue.", hash);
                    dealResult.put(RPC_RESULT_KEY, false);
                    return success(dealResult);
                }
                Result result = contractService.invokeContractOneByOne(chainId, tx);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                dealResult.put(RPC_RESULT_KEY, true);
                return success(dealResult);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = BATCH_BEFORE_END, version = 1.0, description = "After the transaction module has packaged the transaction, before conducting unified verification, notify the contract module to stop receiving transactions and start asynchronous processing of the results of this batch/batch before end")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockType", parameterType = "int", parameterDes = "Block processing mode, Packaging blocks - 0, Verify Block - 1"),
        @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "The height of the currently packaged blocks")
    })
    @ResponseData(description = "No specific return value, success is achieved without errors. If an error is returned, the batch is discarded, and all executed contract transactions within the batch are returned to the queue for packaging transactions")
    public Response batchBeforeEnd(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            Integer blockType = (Integer) params.get("blockType");
            ChainManager.chainHandle(chainId, blockType);
            // version8Above and above, skip processing
            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                return success();
            }
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Result result = contractService.beforeEnd(chainId, blockHeight);
            Log.info("[Before End Result] contract batch, result is {}", result.toString());
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = BATCH_END, version = 1.0, description = "Notify the end of the current batch and return the result/batch end")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "The height of the currently packaged blocks")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "stateRoot", description = "currentstateRoot"),
        @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "List of newly generated transaction serialization strings for contracts(There may be a contract transfer、Contract consensus、Contract returnGAS), version8Only return the contract and aboveGAStransaction")
    }))
    public Response batchEnd(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId, BlockType.VERIFY_BLOCK.type());
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Log.info("[End Contract Batch] contract batch request start, height is {}", blockHeight);

            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                Result result = contractService.endV8(chainId, blockHeight);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                return success(result.getData());
            }
            Result result = contractService.end(chainId, blockHeight);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            List<String> pendingTxHashList = batchInfo.getPendingTxHashList();
            ContractPackageDto dto = (ContractPackageDto) result.getData();
            List<String> resultTxDataList = dto.getResultTxList();
            Map<String, Object> resultMap = MapUtil.createHashMap(2);
            resultMap.put("stateRoot", RPCUtil.encode(dto.getStateRoot()));
            resultMap.put("txList", resultTxDataList);
            resultMap.put("originTxList", dto.getResultOrginTxList());
            // Store unprocessed transactions
            resultMap.put("pendingTxHashList", pendingTxHashList);
            Log.info("[End Contract Batch] Gas total cost is [{}], packaging blockHeight is [{}], packaging StateRoot is [{}]", batchInfo.getGasCostTotal(), blockHeight, RPCUtil.encode(dto.getStateRoot()));
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = PACKAGE_BATCH_END, version = 1.0, description = "Packaging completed - Notify the end of the current batch and return the result/batch end")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
            @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "The height of the currently packaged blocks")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "stateRoot", description = "currentstateRoot"),
            @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "List of newly generated transaction serialization strings for contracts(There may be a contract transfer、Contract consensus、Contract returnGAS), version8Only return the contract and aboveGAStransaction")
    }))
    public Response packageBatchEnd(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId, BlockType.PACKAGE_BLOCK.type());
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Log.info("[End Package Contract Batch] contract batch request start, height is {}", blockHeight);

            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                Result result = contractService.packageEndV8(chainId, blockHeight);
                if (result.isFailed()) {
                    return wrapperFailed(result);
                }
                Log.info("[End Package Contract Batch] packaging blockHeight is [{}], packaging StateRoot is [{}]", blockHeight, ((Map)result.getData()).get("stateRoot"));
                return success(result.getData());
            }

            Result result = contractService.packageEnd(chainId, blockHeight);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            List<String> pendingTxHashList = batchInfo.getPendingTxHashList();
            ContractPackageDto dto = (ContractPackageDto) result.getData();
            List<String> resultTxDataList = dto.getResultTxList();
            Map<String, Object> resultMap = MapUtil.createHashMap(2);
            resultMap.put("stateRoot", RPCUtil.encode(dto.getStateRoot()));
            resultMap.put("txList", resultTxDataList);
            resultMap.put("originTxList", dto.getResultOrginTxList());
            // Store unprocessed transactions
            resultMap.put("pendingTxHashList", pendingTxHashList);
            Log.info("[End Package Contract Batch] Gas total cost is [{}], packaging blockHeight is [{}], packaging StateRoot is [{}]", batchInfo.getGasCostTotal(), blockHeight, RPCUtil.encode(dto.getStateRoot()));
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

//    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "transaction integrate validator")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    @Parameter(parameterName = "txList", parameterType = "String")
//    public Response integrateValidator(Map<String, Object> params) {
//        try {
//            Integer chainId = (Integer) params.get("chainId");
//            ChainManager.chainHandle(chainId);
//            //List<String> txHexList = (List<String>) params.get("txHexList");
//            /**
//             *  There is currently no unified validator available
//             */
//            Map<String, Object> result = new HashMap<>(2);
//            result.put(RPC_COLLECTION_RESULT_KEY, new ArrayList<>());
//            return success(result);
//        } catch (Exception e) {
//            Log.error(e);
//            return failed(e.getMessage());
//        }
//    }
//
//    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "commit contract")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    @Parameter(parameterName = "txList", parameterType = "List<String>")
//    @Parameter(parameterName = "blockHeader", parameterType = "String")
//    public Response commit(Map<String, Object> params) {
//        try {
//            Integer chainId = (Integer) params.get("chainId");
//            ChainManager.chainHandle(chainId);
//            List<String> txDataList = (List<String>) params.get("txList");
//            String blockHeaderData = (String) params.get("blockHeader");
//            Result result = contractService.commitProcessor(chainId, txDataList, blockHeaderData);
//            if (result.isFailed()) {
//                return wrapperFailed(result);
//            }
//
//            Map<String, Object> resultMap = new HashMap<>(2);
//            resultMap.put(RPC_RESULT_KEY, true);
//            return success(resultMap);
//        } catch (Exception e) {
//            Log.error(e);
//            return failed(e.getMessage());
//        }
//    }
//
//    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "commit contract")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    @Parameter(parameterName = "txList", parameterType = "List<String>")
//    @Parameter(parameterName = "blockHeader", parameterType = "String")
//    public Response rollback(Map<String, Object> params) {
//        try {
//            Integer chainId = (Integer) params.get("chainId");
//            ChainManager.chainHandle(chainId);
//            List<String> txDataList = (List<String>) params.get("txList");
//            String blockHeaderData = (String) params.get("blockHeader");
//
//            Result result = contractService.rollbackProcessor(chainId, txDataList, blockHeaderData);
//            if (result.isFailed()) {
//                return wrapperFailed(result);
//            }
//
//            Map<String, Object> resultMap = new HashMap<>(2);
//            resultMap.put("value", true);
//            return success(resultMap);
//        } catch (Exception e) {
//            Log.error(e);
//            return failed(e.getMessage());
//        }
//    }

    @CmdAnnotation(cmd = CONTRACT_OFFLINE_TX_HASH_LIST, version = 1.0, description = "Return the contract generated transaction in the specified block（Contract returnGASExcluding transactions）ofhashlist（Newly generated transactions in the contract except for contract returnsGASExcept for transactions, they are not saved to the block. The contract module saves the relationship between these transactions and the specified block）/contract offline tx hash list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockHash", parameterType = "String", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "list", valueType = List.class, valueElement = String.class, description = "Contract transaction serialization string list(There may be a contract transfer、Contract consensus)")
    }))
    public Response contractOfflineTxHashList(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String blockHash = (String) params.get("blockHash");

            Result<ContractOfflineTxHashPo> result = contractService.getContractOfflineTxHashList(chainId, blockHash);
            if (result.isFailed()) {
                if(result.getErrorCode().equals(DATA_NOT_FOUND)) {
                    Map<String, Object> resultMap = new HashMap<>(2);
                    resultMap.put(RPC_COLLECTION_RESULT_KEY, Collections.emptyList());
                    return success(resultMap);
                }
                return wrapperFailed(result);
            }

            Map<String, Object> resultMap = new HashMap<>(2);
            List<byte[]> hashList = result.getData().getHashList();
            List<String> resultList = new ArrayList<>(hashList.size());
            for (byte[] hash : hashList) {
                resultList.add(RPCUtil.encode(hash));
            }
            resultMap.put(RPC_COLLECTION_RESULT_KEY, resultList);
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CONTRACT_OFFLINE_TX_LIST, version = 1.0, description = "Return the contract generated transaction in the specified block（Contract returnGASExcluding transactions）List of（Newly generated transactions in the contract except for contract returnsGASExcept for transactions, they are not saved to the block. The contract module saves the relationship between these transactions and the specified block）/contract offline tx hash list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "blockHash", parameterType = "String", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txList", valueType = List.class, valueElement = String.class, description = "Returns a collection of transaction serialization data strings")
    }))
    public Response contractOfflineTxList(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String blockHash = (String) params.get("blockHash");
            Result<ContractOfflineTxHashPo> result = contractService.getContractOfflineTxHashList(chainId, blockHash);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            List<byte[]> hashList = result.getData().getHashList();
            List<String> resultList = new ArrayList<>(hashList.size());
            for (byte[] hash : hashList) {
                resultList.add(RPCUtil.encode(hash));
            }
            return success(TransactionCall.getTxList(chainId, resultList));
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * Other modules register commands that can be called by the contract with the contract module
     *
     * @see ModuleCmdRegisterDto#cmdRegisterList cmdRegisterListstructure
     */
    @CmdAnnotation(cmd = REGISTER_CMD_FOR_CONTRACT, version = 1.0, description = "Other modules register commands that can be called by the contract with the contract module, and after registration, the registered commands can be called within the contract code/register cmd for contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
        @Parameter(parameterName = "moduleCode", parameterType = "String", parameterDes = "Module code"),
        @Parameter(parameterName = "cmdRegisterList", parameterType = "List", parameterDes = "Registration Information List")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response registerCmdForContract(Map<String, Object> params) {
        try {
            String errorMsg = PARAMETER_ERROR.getMsg();
            Integer chainId = (Integer) params.get("chainId");
            ObjectUtils.canNotEmpty(chainId, errorMsg);
            ChainManager.chainHandle(chainId);

            String moduleCode = (String) params.get("moduleCode");
            ObjectUtils.canNotEmpty(moduleCode, errorMsg);

            List cmdRegisterList = (List) params.get("cmdRegisterList");
            ObjectUtils.canNotEmpty(cmdRegisterList, errorMsg);


            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            ModuleCmdRegisterDto moduleCmdRegisterDto = JSONUtils.map2pojo(params, ModuleCmdRegisterDto.class);

            Result result = cmdRegisterManager.registerCmd(moduleCmdRegisterDto);
            if (result.isFailed()) {
                return failed(result.getErrorCode());
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * When the consensus reward return address is the contract address, it will trigger the contract_payable(String[][] args)Method, parameter is node revenue address details
     * args[0] = new String[]{address, amount}
     * ...
     *
     * @return After changesstateRoot
     */
    @CmdAnnotation(cmd = TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT, version = 1.0, description = "When the consensus reward return address is the contract address, it will trigger the contract_payable(String[][] args)Method, parameter is node revenue address details<br>args[0] = new String[]{address, amount}<br>...<br>/trigger payable for consensus contract")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "chainid"),
            @Parameter(parameterName = "stateRoot", parameterType = "String", parameterDes = "CurrentstateRoot"),
            @Parameter(parameterName = "blockHeight", parameterType = "Long", parameterDes = "The current latest block height"),
            @Parameter(parameterName = "contractAddress", parameterType = "String", parameterDes = "Contract address"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "The current packaging block contains CoinBase Transaction serialization string")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "After changes stateRoot"),
    }))
    public Response triggerPayableForConsensusContract(Map<String, Object> params) {
        Integer chainId = (Integer) params.get("chainId");
        ObjectUtils.canNotEmpty(chainId, PARAMETER_ERROR.getMsg());
        if (ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.PROTOCOL_21 ) {
            return this._triggerPayableForConsensusContractAfterP21(params);
        } else {
            return this._triggerPayableForConsensusContract(params);
        }
    }

    private Response _triggerPayableForConsensusContractAfterP21(Map<String, Object> params) {
        try {
            String errorMsg = PARAMETER_ERROR.getMsg();
            Integer chainId = (Integer) params.get("chainId");
            ObjectUtils.canNotEmpty(chainId, errorMsg);
            ChainManager.chainHandle(chainId);

            String stateRoot = (String) params.get("stateRoot");
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Long packageHeight = blockHeight + 1;
            if (Log.isDebugEnabled()) {
                Log.debug("contract trigger payable for consensus rewarding, blockHeight is {}, preStateRoot is {}",
                        packageHeight, stateRoot);
            }
            String contractAddress = (String) params.get("contractAddress");
            boolean hasAgentContract = StringUtils.isNotBlank(contractAddress);
            if (hasAgentContract && !AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            String txString = (String) params.get("tx");
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txString), 0);
            if(TxType.COIN_BASE != tx.getType()) {
                return failed(PARAMETER_ERROR);
            }
            CoinData coinData = tx.getCoinDataInstance();
            List<CoinTo> toList = coinData.getTo();
            int toListSize = toList.size();
            if(toListSize == 0) {
                Map rpcResult = new HashMap(2);
                rpcResult.put(RPC_RESULT_KEY, stateRoot);
                return success(rpcResult);
            }

            byte[] stateRootBytes = RPCUtil.decode(stateRoot);
            // obtainVMActuator
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // implementVM
            ProgramExecutor batchExecutor = programExecutor.begin(stateRootBytes);

            BigInteger agentValue = BigInteger.ZERO;
            BigInteger value;

            byte[] contractAddressBytes = null;
            if(hasAgentContract) {
                contractAddressBytes = AddressTool.getAddress(contractAddress);
            }

            List<String[]> agentArgList = new ArrayList<>();
            String[][] depositArgs = new String[1][];
            byte[] address;
            String[] element;
            Result result;
            List<CoinTo> assetRewardList = new ArrayList<>();
            for(CoinTo to : toList) {
                address = to.getAddress();
                value = to.getAmount();
                if (value.compareTo(BigInteger.ZERO) < 0) {
                    Log.error("address [{}] - error amount [{}]", AddressTool.getStringAddressByBytes(address), value.toString());
                    return failed(PARAMETER_ERROR);
                }
                if (to.getAssetsChainId() != ContractContext.LOCAL_CHAIN_ID || to.getAssetsId() != ContractContext.LOCAL_MAIN_ASSET_ID) {
                    if(AddressTool.validContractAddress(address, chainId)) {
                        assetRewardList.add(to);
                    }
                    continue;
                }
                if(hasAgentContract && Arrays.equals(address, contractAddressBytes)) {
                    agentValue = to.getAmount();
                    assetRewardList.add(to);
                    continue;
                }
                element = new String[]{AddressTool.getStringAddressByBytes(address), value.toString()};
                // WhenCoinBaseWhen the contract address of the entrusted node appears in the transaction, it triggers the contract_payable(String[][] args)Method, parameter is the revenue amount of this contract address eg. [[address, amount]]
                if(AddressTool.validContractAddress(address, chainId)) {
                    assetRewardList.add(to);
                    depositArgs[0] = element;
                    result = this.callDepositContract(chainId, address, value, blockHeight, depositArgs, batchExecutor, stateRootBytes);
                    if(result.isFailed()) {
                        Log.error("deposit contract address [{}] trigger payable error [{}], blockHeight is {}", AddressTool.getStringAddressByBytes(address), extractMsg(result), packageHeight);
                    }
                }
                agentArgList.add(element);
            }
            // When the profit address of the packaging node in this block is the contract address, the contract is triggered_payable(String[][] args)Method, parameter is a detailed list of all revenue addresses for this block eg. [[address, amount], [address, amount], ...]
            if(hasAgentContract) {
                agentArgList.add(0, new String[]{contractAddress, agentValue.toString()});
                // Place the revenue of the contract address at the top of the parameter list
                String[][] agentArgs = new String[agentArgList.size()][];
                agentArgList.toArray(agentArgs);
                result = this.callAgentContract(chainId, contractAddressBytes, agentValue, blockHeight, agentArgs, batchExecutor, stateRootBytes);
                if(result.isFailed()) {
                    Log.error("agent contract address [{}] trigger payable error [{}], blockHeight is {}", AddressTool.getStringAddressByBytes(contractAddressBytes), extractMsg(result), packageHeight);
                }
            }
            //assetRewardList.forEach(a -> Log.info("[Contract CS] height: {}, CoinTo detail: {}", blockHeight, a.toString()));
            // record reward from consensus after P21
            contractHelper.saveContractRewardLogByConsensus(chainId, assetRewardList);

            batchExecutor.commit();
            byte[] newStateRootBytes = batchExecutor.getRoot();
            if(Log.isDebugEnabled()) {
                Log.debug("contract trigger payable for consensus rewarding, blockHeight is {}, preStateRoot is {}, currentStateRoot is {}", packageHeight, stateRoot, HexUtil.encode(newStateRootBytes));
            }
            Map rpcResult = new HashMap(2);
            rpcResult.put(RPC_RESULT_KEY, RPCUtil.encode(newStateRootBytes));
            return success(rpcResult);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private Response _triggerPayableForConsensusContract(Map<String, Object> params) {
        try {
            String errorMsg = PARAMETER_ERROR.getMsg();
            Integer chainId = (Integer) params.get("chainId");
            ObjectUtils.canNotEmpty(chainId, errorMsg);
            ChainManager.chainHandle(chainId);

            String stateRoot = (String) params.get("stateRoot");
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Long packageHeight = blockHeight + 1;
            if (Log.isDebugEnabled()) {
                Log.debug("contract trigger payable for consensus rewarding, blockHeight is {}, preStateRoot is {}",
                        packageHeight, stateRoot);
            }
            String contractAddress = (String) params.get("contractAddress");
            boolean hasAgentContract = StringUtils.isNotBlank(contractAddress);
            if (hasAgentContract && !AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            String txString = (String) params.get("tx");
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txString), 0);
            if(TxType.COIN_BASE != tx.getType()) {
                return failed(PARAMETER_ERROR);
            }
            CoinData coinData = tx.getCoinDataInstance();
            List<CoinTo> toList = coinData.getTo();
            int toListSize = toList.size();
            if(toListSize == 0) {
                Map rpcResult = new HashMap(2);
                rpcResult.put(RPC_RESULT_KEY, stateRoot);
                return success(rpcResult);
            }

            byte[] stateRootBytes = RPCUtil.decode(stateRoot);
            // obtainVMActuator
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // implementVM
            ProgramExecutor batchExecutor = programExecutor.begin(stateRootBytes);

            BigInteger agentValue = BigInteger.ZERO;
            BigInteger value;

            byte[] contractAddressBytes = null;
            if(hasAgentContract) {
                contractAddressBytes = AddressTool.getAddress(contractAddress);
            }

            String[][] agentArgs = new String[toListSize][];
            String[][] depositArgs = new String[1][];
            int i = 0;
            if (hasAgentContract) {
                i++;
            }
            byte[] address;
            String[] element;
            Result result;
            for(CoinTo to : toList) {
                address = to.getAddress();
                value = to.getAmount();
                if (value.compareTo(BigInteger.ZERO) < 0) {
                    Log.error("address [{}] - error amount [{}]", AddressTool.getStringAddressByBytes(address), value.toString());
                    return failed(PARAMETER_ERROR);
                }

                if(hasAgentContract && Arrays.equals(address, contractAddressBytes)) {
                    agentValue = to.getAmount();
                    continue;
                }
                element = new String[]{AddressTool.getStringAddressByBytes(address), value.toString()};
                // WhenCoinBaseWhen the contract address of the entrusted node appears in the transaction, it triggers the contract_payable(String[][] args)Method, parameter is the revenue amount of this contract address eg. [[address, amount]]
                if(AddressTool.validContractAddress(address, chainId)) {
                    depositArgs[0] = element;
                    result = this.callDepositContract(chainId, address, value, blockHeight, depositArgs, batchExecutor, stateRootBytes);
                    if(result.isFailed()) {
                        Log.error("deposit contract address [{}] trigger payable error [{}], blockHeight is {}", AddressTool.getStringAddressByBytes(address), extractMsg(result), packageHeight);
                    }
                }
                agentArgs[i++] = element;
            }
            // When the profit address of the packaging node in this block is the contract address, the contract is triggered_payable(String[][] args)Method, parameter is a detailed list of all revenue addresses for this block eg. [[address, amount], [address, amount], ...]
            if(hasAgentContract) {
                // Place the revenue of the contract address at the top of the parameter list
                agentArgs[0] = new String[]{contractAddress, agentValue.toString()};
                result = this.callAgentContract(chainId, contractAddressBytes, agentValue, blockHeight, agentArgs, batchExecutor, stateRootBytes);
                if(result.isFailed()) {
                    Log.error("agent contract address [{}] trigger payable error [{}], blockHeight is {}", AddressTool.getStringAddressByBytes(contractAddressBytes), extractMsg(result), packageHeight);
                }
            }

            batchExecutor.commit();
            byte[] newStateRootBytes = batchExecutor.getRoot();
            if(Log.isDebugEnabled()) {
                Log.debug("contract trigger payable for consensus rewarding, blockHeight is {}, preStateRoot is {}, currentStateRoot is {}", packageHeight, stateRoot, HexUtil.encode(newStateRootBytes));
            }
            Map rpcResult = new HashMap(2);
            rpcResult.put(RPC_RESULT_KEY, RPCUtil.encode(newStateRootBytes));
            return success(rpcResult);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = GET_CROSS_TOKEN_SYSTEM_CONTRACT, version = 1.0, description = "get cross token system contract")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Token Cross Chain System Contract Address")
    }))
    public Response getCrossTokenSystemContract(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            Map result = new HashMap();
            result.put(RPC_RESULT_KEY, contractConfig.getCrossTokenSystemContract());
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private String extractMsg(Result result) {
        if(result == null) {
            return EMPTY;
        }
        String msg = result.getMsg();
        return msg != null ? msg : result.getErrorCode().getMsg();
    }

    private Result callAgentContract(int chainId, byte[] contractAddressBytes, BigInteger value, Long blockHeight, String[][] args, ProgramExecutor batchExecutor, byte[] stateRootBytes) {
        if (Log.isDebugEnabled()) {
            Log.debug("agent contract trigger payable for consensus rewarding, blockHeight is {}, contractAddress is {}, reward detail is {}",
                    blockHeight + 1, AddressTool.getStringAddressByBytes(contractAddressBytes), ContractUtil.toString(args));
        }
        return this.callConsensusContract(chainId, contractAddressBytes, value, blockHeight, args, batchExecutor, stateRootBytes, true);
    }

    private Result callDepositContract(int chainId, byte[] contractAddressBytes, BigInteger value, Long blockHeight, String[][] args, ProgramExecutor batchExecutor, byte[] stateRootBytes) {
        if (Log.isDebugEnabled()) {
            Log.debug("deposit contract trigger payable for consensus rewarding, blockHeight is {}, contractAddress is {}, reward is {}",
                    blockHeight + 1, AddressTool.getStringAddressByBytes(contractAddressBytes), value.toString());
        }
        return this.callConsensusContract(chainId, contractAddressBytes, value, blockHeight, args, batchExecutor, stateRootBytes, false);
    }

    private Result callConsensusContract(int chainId, byte[] contractAddressBytes, BigInteger value, Long blockHeight, String[][] args,
                                         ProgramExecutor batchExecutor, byte[] stateRootBytes, boolean isAgentContract) {
        // Verify if this contract accepts direct transfer
        ProgramMethod methodInfo = contractHelper.getMethodInfoByContractAddress(chainId, stateRootBytes,
                BALANCE_TRIGGER_METHOD_NAME, BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC, contractAddressBytes);
        if(methodInfo == null) {
            Log.error("chainId: {}, contractAddress: {}, stateRoot: {}", chainId,
                    AddressTool.getStringAddressByBytes(contractAddressBytes),
                    HexUtil.encode(stateRootBytes));
            return Result.getFailed(CONTRACT_METHOD_NOT_EXIST);
        }
        if(!methodInfo.isPayable()) {
            return Result.getFailed(CONTRACT_NO_ACCEPT_DIRECT_TRANSFER);
        }
        // assembleVMExecution data
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddressBytes);
        programCall.setSender(null);
        programCall.setNumber(blockHeight);
        programCall.setValue(value);
        programCall.setPrice(CONTRACT_MINIMUM_PRICE);
        if(isAgentContract) {
            programCall.setGasLimit(AGENT_CONTRACT_CONSTANT_GASLIMIT);
        } else {
            programCall.setGasLimit(DEPOSIT_CONTRACT_CONSTANT_GASLIMIT);
        }
        programCall.setMethodName(BALANCE_TRIGGER_METHOD_NAME);
        programCall.setMethodDesc(BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC);
        programCall.setArgs(args);

        ProgramExecutor track = batchExecutor.startTracking();
        ProgramResult programResult = track.call(programCall);
        if(!programResult.isSuccess()) {
            Log.error("contractAddress[{}], errorMessage[{}], errorStackTrace[{}]" , AddressTool.getStringAddressByBytes(contractAddressBytes),
                    programResult.getErrorMessage() , programResult.getStackTrace());
            Result result = Result.getFailed(DATA_ERROR);
            result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
            result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
            return result;
        }
        // Restriction cannottokenTransfer、Unable to send event、Internal transfer not allowed、Cannot call contract internally、Cannot generate new transactions
        List<String> events = programResult.getEvents();
        List<ProgramTransfer> transfers = programResult.getTransfers();
        List<ProgramInternalCall> internalCalls = programResult.getInternalCalls();
        List<ProgramInvokeRegisterCmd> invokeRegisterCmds = programResult.getInvokeRegisterCmds();
        int size = events.size() + transfers.size() + internalCalls.size();
        if(size > 0) {
            return Result.getFailed(TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT_ERROR);
        }
        for(ProgramInvokeRegisterCmd registerCmd : invokeRegisterCmds) {
            if(CmdRegisterMode.NEW_TX.equals(registerCmd.getCmdRegisterMode())) {
                return Result.getFailed(TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT_ERROR);
            }
        }

        track.commit();
        return getSuccess();
    }

}
