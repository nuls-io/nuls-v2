/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.manager.ContractTxProcessorManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.ContractTempTransaction;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.ADDRESS_ERROR;
import static io.nuls.contract.constant.ContractErrorCode.DATA_ERROR;
import static io.nuls.contract.util.ContractUtil.wrapperFailed;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractCmd extends BaseCmd {

    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxProcessorManager contractTxProcessorManager;
    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;

    @CmdAnnotation(cmd = BATCH_BEGIN, version = 1.0, description = "batch begin")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    @Parameter(parameterName = "blockTime", parameterType = "long")
    @Parameter(parameterName = "packingAddress", parameterType = "String")
    @Parameter(parameterName = "preStateRoot", parameterType = "String")
    public Response batchBegin(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            Long blockTime = Long.parseLong(params.get("blockTime").toString());
            String packingAddress = (String) params.get("packingAddress");
            String preStateRoot = (String) params.get("preStateRoot");

            Result result = contractService.begin(chainId, blockHeight, blockTime, packingAddress, preStateRoot);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INVOKE_CONTRACT, version = 1.0, description = "invoke contract one by one")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response invokeContractOneByOne(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            String txData = (String) params.get("tx");
            ContractTempTransaction tx = new ContractTempTransaction();
            tx.setTxHex(txData);
            tx.parse(RPCUtil.decode(txData), 0);
            Result result = contractService.invokeContractOneByOne(chainId, tx);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = BATCH_BEFORE_END, version = 1.0, description = "batch before end")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response batchBeforeEnd(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
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

    @CmdAnnotation(cmd = BATCH_END, version = 1.0, description = "batch end")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response batchEnd(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());

            Result result = contractService.end(chainId, blockHeight);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            ContractPackageDto dto = (ContractPackageDto) result.getData();
            List<String> resultTxDataList = new ArrayList<>();
            List<Transaction> resultTxList = dto.getResultTxList();
            for (Transaction resultTx : resultTxList) {
                Log.info("Batch txType is [{}], hash is [{}]", resultTx.getType(), resultTx.getHash().toString());
                resultTxDataList.add(RPCUtil.encode(resultTx.serialize()));
            }

            Map<String, Object> resultMap = MapUtil.createHashMap(2);
            resultMap.put("stateRoot", RPCUtil.encode(dto.getStateRoot()));
            resultMap.put("txList", resultTxDataList);
            Log.info("[End Contract Batch] packaging blockHeight is [{}], packaging StateRoot is [{}]", blockHeight, RPCUtil.encode(dto.getStateRoot()));
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CREATE_VALIDATOR, version = 1.0, description = "create contract validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createValidator(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            String txData = (String) params.get("tx");
            CreateContractTransaction tx = new CreateContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != TX_TYPE_CREATE_CONTRACT) {
                return failed("non create contract tx");
            }
            Result validator = contractTxValidatorManager.createValidator(chainId, tx);
            if (validator.isFailed()) {
                return wrapperFailed(validator);
            }
            result.put("value", true);
            return success(result);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CALL_VALIDATOR, version = 1.0, description = "call contract validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response callValidator(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            String txData = (String) params.get("tx");
            CallContractTransaction tx = new CallContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != TX_TYPE_CALL_CONTRACT) {
                return failed("non call contract tx");
            }
            Result validator = contractTxValidatorManager.callValidator(chainId, tx);
            if (validator.isFailed()) {
                return wrapperFailed(validator);
            }
            result.put("value", true);
            return success(result);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = DELETE_VALIDATOR, version = 1.0, description = "delete contract validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response deleteValidator(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            String txData = (String) params.get("tx");
            DeleteContractTransaction tx = new DeleteContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != TX_TYPE_DELETE_CONTRACT) {
                return failed("non delete contract tx");
            }
            Result validator = contractTxValidatorManager.deleteValidator(chainId, tx);
            if (validator.isFailed()) {
                return wrapperFailed(validator);
            }
            result.put("value", true);
            return success(result);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INTEGRATE_VALIDATOR, version = 1.0, description = "transaction integrate validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "String")
    public Response integrateValidator(Map<String, Object> params) {
        try {
            //Integer chainId = (Integer) params.get("chainId");
            //List<String> txHexList = (List<String>) params.get("txHexList");
            /**
             *  暂无统一验证器
             */
            Map<String, Object> result = new HashMap<>(2);
            result.put("list", new ArrayList<>());
            return success(result);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = COMMIT, version = 1.0, description = "commit contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List<String>")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response commit(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            List<String> txDataList = (List<String>) params.get("txList");
            String blockHeaderData = (String) params.get("blockHeader");

            Result result = contractService.commitProcessor(chainId, txDataList, blockHeaderData);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("value", true);
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = ROLLBACK, version = 1.0, description = "commit contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List<String>")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response rollback(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            List<String> txDataList = (List<String>) params.get("txList");
            String blockHeaderData = (String) params.get("blockHeader");

            Result result = contractService.rollbackProcessor(chainId, txDataList, blockHeaderData);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INITIAL_ACCOUNT_TOKEN, version = 1.0, description = "initial account token")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    public Response initialAccountToken(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            String address = (String) params.get("address");
            if (!AddressTool.validAddress(chainId, address)) {
                return failed(ADDRESS_ERROR);
            }

            ContractTokenBalanceManager contractTokenBalanceManager = contractHelper.getChain(chainId).getContractTokenBalanceManager();
            if (contractTokenBalanceManager == null) {
                return failed(DATA_ERROR);
            }

            Result result = contractTokenBalanceManager.initAllTokensByImportAccount(address);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

}
