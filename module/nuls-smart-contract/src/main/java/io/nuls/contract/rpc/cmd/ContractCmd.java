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

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.*;
import io.nuls.contract.model.bo.ContractTempTransaction;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.dto.ModuleCmdRegisterDto;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.checkVmResultAndReturn;
import static io.nuls.contract.util.ContractUtil.wrapperFailed;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;

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
    @Autowired
    private CmdRegisterManager cmdRegisterManager;

    @CmdAnnotation(cmd = BATCH_BEGIN, version = 1.0, description = "batch begin")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    @Parameter(parameterName = "blockTime", parameterType = "long")
    @Parameter(parameterName = "packingAddress", parameterType = "String")
    @Parameter(parameterName = "preStateRoot", parameterType = "String")
    public Response batchBegin(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
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
            ChainManager.chainHandle(chainId);
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
            ChainManager.chainHandle(chainId);
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
            ChainManager.chainHandle(chainId);
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());

            Result result = contractService.end(chainId, blockHeight);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            ContractPackageDto dto = (ContractPackageDto) result.getData();
            List<String> resultTxDataList = new ArrayList<>();
            List resultTxList = dto.getResultTxList();
            Transaction tx;
            for (Object resultTx : resultTxList) {
                // 合约调用其他模块生成的交易
                if (resultTx instanceof String) {
                    resultTxDataList.add((String) resultTx);
                } else {
                    // 合约内部生成的交易
                    tx = (Transaction) resultTx;
                    if (Log.isDebugEnabled()) {
                        Log.debug("Batch new txHash is [{}]", tx.getHash().toString());
                    }
                    resultTxDataList.add(RPCUtil.encode(tx.serialize()));
                }

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

    @CmdAnnotation(cmd = INTEGRATE_VALIDATOR, version = 1.0, description = "transaction integrate validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "String")
    public Response integrateValidator(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
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
            ChainManager.chainHandle(chainId);
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
            ChainManager.chainHandle(chainId);
            List<String> txDataList = (List<String>) params.get("txList");
            String blockHeaderData = (String) params.get("blockHeader");

            Result result = contractService.rollbackProcessor(chainId, txDataList, blockHeaderData);
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

    @CmdAnnotation(cmd = INITIAL_ACCOUNT_TOKEN, version = 1.0, description = "initial account token")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    public Response initialAccountToken(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
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

    /**
     * 其他模块向合约模块注册可被合约调用的命令
     *
     * @see ModuleCmdRegisterDto#cmdRegisterList cmdRegisterList结构
     */
    @CmdAnnotation(cmd = REGISTER_CMD_FOR_CONTRACT, version = 1.0, description = "register cmd for contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "moduleCode", parameterType = "String")
    @Parameter(parameterName = "cmdRegisterList", parameterType = "List")
    public Response registerCmdForContract(Map<String, Object> params) {
        try {
            String errorMsg = PARAMETER_ERROR.getMsg();
            Integer chainId = (Integer) params.get("chainId");
            ObjectUtils.canNotEmpty(chainId, errorMsg);
            ChainManager.chainHandle(chainId);

            String moduleCode = (String) params.get("moduleCode");
            ObjectUtils.canNotEmpty(moduleCode, errorMsg);

            List cmdRegisterList = (List) params.get("cmdRegisterList");
            ObjectUtils.canNotEmpty(params.get("cmdRegisterList"), errorMsg);


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
     * 当收益
     *
     * @return 当前stateRoot
     */
    @CmdAnnotation(cmd = TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT, version = 1.0, description = "trigger payable for consensus contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "stateRoot", parameterType = "String")
    @Parameter(parameterName = "blockHeight", parameterType = "Long")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response triggerPayableForConsensusContract(Map<String, Object> params) {
        try {
            String errorMsg = PARAMETER_ERROR.getMsg();
            Integer chainId = (Integer) params.get("chainId");
            ObjectUtils.canNotEmpty(chainId, errorMsg);
            ChainManager.chainHandle(chainId);

            String stateRoot = (String) params.get("stateRoot");
            Long blockHeight = Long.parseLong(params.get("blockHeight").toString());
            String contractAddress = (String) params.get("contractAddress");
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            String txString = (String) params.get("tx");
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txString), 0);
            if(TxType.COIN_BASE != tx.getType()) {
                return failed(PARAMETER_ERROR);
            }
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            CoinData coinData = tx.getCoinDataInstance();
            List<CoinTo> toList = coinData.getTo();
            BigInteger value = BigInteger.ZERO;
            String[][] args = new String[toList.size()][];
            int i = 0;
            byte[] address;
            for(CoinTo to : toList) {
                address = to.getAddress();
                if(Arrays.equals(address, contractAddressBytes)) {
                    value = to.getAmount();
                    continue;
                }
                args[++i] = new String[]{AddressTool.getStringAddressByBytes(address), to.getAmount().toString()};
            }
            // 把合约地址的收益放在参数列表的首位
            args[0] = new String[]{contractAddress, value.toString()};

            if (value.compareTo(BigInteger.ZERO) < 0) {
                return failed(PARAMETER_ERROR);
            }

            // 验证此合约是否接受直接转账
            byte[] stateRootBytes = RPCUtil.decode(stateRoot);
            ProgramMethod methodInfo = contractHelper.getMethodInfoByContractAddress(chainId, stateRootBytes,
                    BALANCE_TRIGGER_METHOD_NAME, BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC, contractAddressBytes);
            if(methodInfo == null) {
                return failed(CONTRACT_METHOD_NOT_EXIST);
            }
            if(!methodInfo.isPayable()) {
                return failed(CONTRACT_NO_ACCEPT_DIRECT_TRANSFER);
            }
            // 组装VM执行数据
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddressBytes);
            programCall.setSender(null);
            programCall.setNumber(blockHeight);
            programCall.setValue(value);
            programCall.setPrice(CONTRACT_MINIMUM_PRICE);
            programCall.setGasLimit(MAX_GASLIMIT);
            programCall.setMethodName(BALANCE_TRIGGER_METHOD_NAME);
            programCall.setMethodDesc(BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC);
            programCall.setArgs(args);

            // 获取VM执行器
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // 执行VM
            ProgramExecutor track = programExecutor.begin(stateRootBytes);
            ProgramResult programResult = track.call(programCall);
            if(!programResult.isSuccess()) {
                Log.error("contractAddress[{}], errorMessage[{}], errorStackTrace[{}]" ,
                        AddressTool.getStringAddressByBytes(contractAddressBytes),
                        programResult.getErrorMessage() ,
                        programResult.getStackTrace());
                Result result = Result.getFailed(DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                return wrapperFailed(result);
            }
            // 限制不能token转账、不能发送事件、不能内部转账、不能内部调用合约、不能产生新交易
            List<String> events = programResult.getEvents();
            List<ProgramTransfer> transfers = programResult.getTransfers();
            List<ProgramInternalCall> internalCalls = programResult.getInternalCalls();
            List<ProgramInvokeRegisterCmd> invokeRegisterCmds = programResult.getInvokeRegisterCmds();
            int size = events.size() + transfers.size() + internalCalls.size();
            if(size > 0) {
                return failed(TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT_ERROR);
            }
            for(ProgramInvokeRegisterCmd registerCmd : invokeRegisterCmds) {
                if(CmdRegisterMode.NEW_TX.equals(registerCmd.getCmdRegisterMode())) {
                    return failed(TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT_ERROR);
                }
            }

            // 提交这次合约执行结果
            track.commit();
            byte[] newStateRootBytes = track.getRoot();
            Map result = new HashMap(2);
            result.put(RPC_RESULT_KEY, RPCUtil.encode(newStateRootBytes));
            return success(result);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

}
