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
package io.nuls.contract.rpc.resource;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.StringUtils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractResource extends BaseCmd {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxService contractTxService;

    @CmdAnnotation(cmd = CREATE, version = 1.0, description = "invoke contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response create(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractCode = (String) params.get("contractCode");
            Object[] args = (Object[]) params.get("args");
            String remark = (String) params.get("remark");

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if(StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = Hex.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractCreateTx(chainId, sender, gasLimit, price, contractCodeBytes, convertArgs, password, remark);

            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = PRE_CREATE, version = 1.0, description = "pre create contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response preCreate(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractCode = (String) params.get("contractCode");
            Object[] args = (Object[]) params.get("args");
            String remark = (String) params.get("remark");

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if(StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = Hex.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractPreCreateTx(chainId, sender, gasLimit, price, contractCodeBytes, convertArgs, password, remark);

            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_CREATE, version = 1.0, description = "validate create contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    public Response validateCreate(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractCode = (String) params.get("contractCode");
            Object[] args = (Object[]) params.get("args");

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if(StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = Hex.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.validateContractCreateTx(chainId, AddressTool.getAddress(sender), gasLimit, price, contractCodeBytes, convertArgs);

            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CALL, version = 1.0, description = "call contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "remark", parameterType = "remark")
    public Response call(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            BigInteger value = new BigInteger(params.get("value").toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            Object[] args = (Object[]) params.get("args");
            String password = (String) params.get("password");
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractCallTx(chainId, sender, value, gasLimit, price, contractAddress, methodName, methodDesc, convertArgs, password, remark);

            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_CALL, version = 1.0, description = "validate call contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    public Response validateCall(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            BigInteger value = new BigInteger(params.get("value").toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            Object[] args = (Object[]) params.get("args");

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.validateContractCallTx(chainId, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, convertArgs);

            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = DELETE, version = 1.0, description = "delete contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "remark", parameterType = "remark")
    public Response delete(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            String contractAddress = (String) params.get("contractAddress");
            String password = (String) params.get("password");
            String remark = (String) params.get("remark");
            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }
            Result result = contractTxService.contractDeleteTx(chainId, sender, contractAddress, password, remark);
            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }
            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_DELETE, version = 1.0, description = "validate delete contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    public Response validateDelete(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            String contractAddress = (String) params.get("contractAddress");
            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }
            Result result = contractTxService.validateContractDeleteTx(chainId, sender, contractAddress);
            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }



    @CmdAnnotation(cmd = TRANSFER_FEE, version = 1.0, description = "transfer fee, transfer NULS from sender to contract address")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "toAddress", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "remark", parameterType = "remark")
    public Response transferFee(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("address");
            String contractAddress = (String) params.get("toAddress");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            BigInteger value = new BigInteger(params.get("value").toString());
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if(!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }

            Result result = contractTxService.txFee(chainId, sender, value, gasLimit, price, contractAddress,
                    ContractConstant.BALANCE_TRIGGER_METHOD_NAME,
                    ContractConstant.BALANCE_TRIGGER_METHOD_DESC,
                    null, remark);
            //TODO pierre 
            if(result.isFailed()) {
                return failed(result.getErrorCode());
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
