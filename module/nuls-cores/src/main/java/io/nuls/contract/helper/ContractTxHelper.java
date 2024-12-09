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
package io.nuls.contract.helper;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.dto.AccountAmountDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.rpc.call.AccountCall;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.NulsData;
import io.nuls.core.basic.Result;
import io.nuls.core.basic.VarInt;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ArraysTool;
import io.nuls.core.model.LongUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.nuls.contract.config.ContractContext.LOCAL_MAIN_ASSET_ID;
import static io.nuls.contract.config.ContractContext.LOCAL_CHAIN_ID;
import static io.nuls.contract.constant.ContractConstant.MAX_GASLIMIT;
import static io.nuls.contract.constant.ContractConstant.UNLOCKED_TX;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-12
 */
@Component
public class ContractTxHelper {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;

    public Result<CreateContractTransaction> makeCreateTx(int chainId, String sender, String alias, Long gasLimit, Long price,
                                                          byte[] contractCode, String[][] args,
                                                          String password, String remark) {
        try {
            Result accountResult = AccountCall.validationPassword(chainId, sender, password);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            // Generate an address as the smart contract address
            String contractAddress = AccountCall.createContractAddress(chainId);

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            byte[] senderBytes = AddressTool.getAddress(sender);

            Result validateCreate = this.validateCreate(chainId, senderBytes, contractAddressBytes, gasLimit, price, contractCode, args);
            if (validateCreate.isFailed()) {
                return validateCreate;
            }
            Result<CreateContractTransaction> result = this.newCreateTx(chainId, sender, senderBytes, contractAddressBytes, alias, gasLimit, price, contractCode, args, remark);
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    public Result<CreateContractTransaction> newCreateTx(int chainId, String sender, byte[] senderBytes, byte[] contractAddressBytes, String alias, Long gasLimit, Long price,
                                                         byte[] contractCode, String[][] args, String remark) {
        try {
            BigInteger value = BigInteger.ZERO;

            CreateContractTransaction tx = new CreateContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            // assembletxData
            CreateContractData createContractData = this.getCreateContractData(senderBytes, contractAddressBytes, alias, gasLimit, price, contractCode, args);

            // calculateCoinData
            /*
             * Smart contract calculates transaction fees for consumptionGas*PriceAs a basis, however, smart contracts are not executed when creating transactions,
             * So at this point, the transactionCoinDataIt's not fixed, more than it actually is,
             * The smart contract is executed during packaging, and the actual transaction fee has been calculated. HowevertxThe handling fee has been deducted,
             * Excess deductions will result inContractReturnGasTransactionTransaction returnSender
             */
            CoinData coinData = new CoinData();
            Result makeCoinDataResult = this.makeCoinData(chainId, sender, senderBytes, contractAddressBytes, gasLimit, price, value, tx.size(), createContractData, coinData, null, null);
            if (makeCoinDataResult.isFailed()) {
                return makeCoinDataResult;
            }

            tx.setTxDataObj(createContractData);
            tx.setCoinDataObj(coinData);
            tx.serializeData();

            return getSuccess().setData(tx);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    public Result validateCreate(int chainId, byte[] sender, byte[] contractAddress, Long gasLimit, Long price,
                                 byte[] contractCode, String[][] args) {
        try {
            BigInteger value = BigInteger.ZERO;

            if (!ContractUtil.checkPrice(price.longValue())) {
                return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
            }

            if (contractAddress == null) {
                contractAddress = AddressTool.getAddress(AccountCall.createContractAddress(chainId));
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // Current block height
            long blockHeight = blockHeader.getHeight();
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            // obtainVMActuator
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // implementVMVerify legality
            ProgramCreate programCreate = new ProgramCreate();
            programCreate.setContractAddress(contractAddress);
            programCreate.setSender(sender);
            programCreate.setValue(value);
            programCreate.setPrice(price.longValue());
            programCreate.setNumber(blockHeight);
            programCreate.setContractCode(contractCode);
            if (args != null) {
                programCreate.setArgs(args);
            }
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            // Skip when verifying contractGasvalidate
            long realGasLimit = gasLimit;
            programCreate.setGasLimit(MAX_GASLIMIT);
            ProgramResult programResult = track.create(programCreate);

            // When the execution result fails, the transaction returns an error directly, without being linked or consumedGas,
            if (!programResult.isSuccess()) {
                Log.error(programResult.getErrorMessage() + ", " + programResult.getStackTrace());
                Result result = Result.getFailed(DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                addDebugEvents(programResult.getDebugEvents(), result);
                return result;
            } else {
                // After all other legality passes, verify againGas
                if (realGasLimit != MAX_GASLIMIT) {
                    programCreate.setGasLimit(realGasLimit);
                    track = programExecutor.begin(prevStateRoot);
                    programResult = track.create(programCreate);
                    if (!programResult.isSuccess()) {
                        Log.error(programResult.getStackTrace());
                        Result result = Result.getFailed(DATA_ERROR);
                        result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                        addDebugEvents(programResult.getDebugEvents(), result);
                        return result;
                    }
                }
            }
            return getSuccess().setData(programResult);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    private int calcSize(NulsData nulsData) {
        if (nulsData == null) {
            return 0;
        }
        int size = nulsData.size();
        // calculatetx.size()WhencoinDataandtxDataWhen empty, calculated1Length, if at this timenulsDataIf it is not empty, it will be deducted1Length
        return VarInt.sizeOf(size) + size - 1;
    }

    public Result makeCoinData(int chainId, String sender, byte[] senderBytes, byte[] contractAddress, long gasLimit, long price, BigInteger value, int txSize, NulsData txData, CoinData coinData, List<ProgramMultyAssetValue> multyAssetValues, List<AccountAmountDto> nulsValueToOtherList) {
        long gasUsed = gasLimit;
        BigInteger imputedValue = BigInteger.valueOf(LongUtils.mul(gasUsed, price));
        // Total expenses
        BigInteger totalValue = imputedValue;
        int nulsAssetChainId = LOCAL_CHAIN_ID;
        int nulsAssetId = LOCAL_MAIN_ASSET_ID;
        totalValue = totalValue.add(value);
        if (value.compareTo(BigInteger.ZERO) > 0) {
            coinData.addTo(new CoinTo(contractAddress, nulsAssetChainId, nulsAssetId, value));
        }
        if (nulsValueToOtherList != null && !nulsValueToOtherList.isEmpty()) {
            for (AccountAmountDto dto : nulsValueToOtherList) {
                totalValue = totalValue.add(dto.getValue());
                coinData.addTo(new CoinTo(AddressTool.getAddress(dto.getTo()), nulsAssetChainId, nulsAssetId, dto.getValue()));
            }
        }
        ContractBalance senderBalance = contractHelper.getUnConfirmedBalanceAndNonce(chainId, nulsAssetChainId, nulsAssetId, sender);
        CoinFrom coinFrom = new CoinFrom(senderBytes, nulsAssetChainId, nulsAssetId, totalValue, RPCUtil.decode(senderBalance.getNonce()), UNLOCKED_TX);
        coinData.addFrom(coinFrom);

        if (multyAssetValues != null && !multyAssetValues.isEmpty()) {
            BigInteger _value;
            for (ProgramMultyAssetValue multyAssetValue : multyAssetValues) {
                int assetChainId = multyAssetValue.getAssetChainId();
                int assetId = multyAssetValue.getAssetId();
                _value = multyAssetValue.getValue();
                ContractBalance senderBalanceOfTransfer = contractHelper.getUnConfirmedBalanceAndNonce(chainId, assetChainId, assetId, sender);
                if (_value.compareTo(BigInteger.ZERO) > 0) {
                    if (senderBalanceOfTransfer.getBalance().compareTo(_value) < 0) {
                        Log.error("Insufficient balance, asset: {}-{}", assetChainId, assetId);
                        return Result.getFailed(INSUFFICIENT_BALANCE);
                    }
                    CoinFrom coinFromOfTransfer = new CoinFrom(senderBytes, assetChainId, assetId, _value, RPCUtil.decode(senderBalanceOfTransfer.getNonce()), UNLOCKED_TX);
                    CoinTo coinTo = new CoinTo(contractAddress, assetChainId, assetId, _value);
                    coinData.addFrom(coinFromOfTransfer);
                    coinData.addTo(coinTo);
                }
            }
        }
        Chain chain = contractHelper.getChain(chainId);
        BigInteger fee = TransactionFeeCalculator.getNormalUnsignedTxFee(txSize + calcSize(txData) + calcSize(coinData), chain.getConfig().getFeeUnit(nulsAssetChainId, nulsAssetId), chain.getConfig().getFeeCoefficient(nulsAssetChainId, nulsAssetId));
        totalValue = totalValue.add(fee);
        if (senderBalance.getBalance().compareTo(totalValue) < 0) {
            Log.error("Insufficient balance, asset: {}-{}", LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
            return Result.getFailed(INSUFFICIENT_BALANCE);
        }
        coinFrom.setAmount(totalValue);

        return getSuccess();
    }

    public CreateContractData getCreateContractData(byte[] senderBytes, byte[] contractAddressBytes, String alias, long gasLimit, long price, byte[] contractCode, String[][] args) {
        CreateContractData createContractData = new CreateContractData();
        createContractData.setSender(senderBytes);
        createContractData.setContractAddress(contractAddressBytes);
        createContractData.setAlias(alias);
        createContractData.setGasLimit(gasLimit);
        createContractData.setPrice(price);
        createContractData.setCode(contractCode);
        if (args != null) {
            createContractData.setArgsCount((short) args.length);
            createContractData.setArgs(args);
        }
        return createContractData;
    }

    public Result<CallContractTransaction> makeCallTx(int chainId, String sender, BigInteger value, Long gasLimit, Long price, String contractAddress,
                                                      String methodName, String methodDesc, String[][] args,
                                                      String password, String remark, List<ProgramMultyAssetValue> multyAssetValues, List<AccountAmountDto> nulsValueToOtherList) {

        if (value == null) {
            value = BigInteger.ZERO;
        }

        Result accountResult = AccountCall.validationPassword(chainId, sender, password);
        if (accountResult.isFailed()) {
            return accountResult;
        }

        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        byte[] senderBytes = AddressTool.getAddress(sender);

        Result validateCall = this.validateCall(chainId, senderBytes, contractAddressBytes, value, gasLimit, price, methodName, methodDesc, args, multyAssetValues);
        if (validateCall.isFailed()) {
            return validateCall;
        }

        Result<CallContractTransaction> result = this.newCallTx(chainId, sender, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, args, remark, multyAssetValues, nulsValueToOtherList);
        return result;
    }

    public Result<CallContractTransaction> newCallTx(int chainId, String sender, byte[] senderBytes, BigInteger value, Long gasLimit, Long price, byte[] contractAddressBytes,
                                                     String methodName, String methodDesc, String[][] args, String remark, List<ProgramMultyAssetValue> multyAssetValues, List<AccountAmountDto> nulsValueToOtherList) {
        try {

            CallContractTransaction tx = new CallContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            // assembletxData
            CallContractData callContractData = this.getCallContractData(senderBytes, contractAddressBytes, value, gasLimit, price, methodName, methodDesc, args);

            // calculateCoinData
            /*
             * Smart contract calculates transaction fees for consumptionGas*PriceAs a basis, however, smart contracts are not executed when creating transactions,
             * So at this point, the transactionCoinDataIt's not fixed, more than it actually is,
             * The smart contract is executed during packaging, and the actual transaction fee has been calculated. HowevertxThe handling fee has been deducted,
             * Excess deductions will result inCoinBaseTransaction returnSender
             */
            CoinData coinData = new CoinData();
            Result makeCoinDataResult = this.makeCoinData(chainId, sender, senderBytes, contractAddressBytes, gasLimit, price, value, tx.size(), callContractData, coinData, multyAssetValues, nulsValueToOtherList);
            if (makeCoinDataResult.isFailed()) {
                return makeCoinDataResult;
            }

            tx.setTxDataObj(callContractData);
            tx.setCoinDataObj(coinData);
            tx.serializeData();

            return getSuccess().setData(tx);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }


    private CallContractData getCallContractData(byte[] senderBytes, byte[] contractAddressBytes, BigInteger value, long gasLimit, long price, String methodName, String methodDesc, String[][] args) {
        CallContractData callContractData = new CallContractData();
        callContractData.setContractAddress(contractAddressBytes);
        callContractData.setSender(senderBytes);
        callContractData.setValue(value);
        callContractData.setPrice(price);
        callContractData.setGasLimit(gasLimit);
        callContractData.setMethodName(methodName);
        callContractData.setMethodDesc(methodDesc);
        if (args != null) {
            callContractData.setArgsCount((short) args.length);
            callContractData.setArgs(args);
        }
        return callContractData;
    }


    public Result validateCall(int chainId, byte[] senderBytes, byte[] contractAddressBytes, BigInteger value, Long gasLimit, Long price, String methodName, String methodDesc, String[][] args, List<ProgramMultyAssetValue> multyAssetValues) {
        try {
            if (!ContractUtil.checkPrice(price.longValue())) {
                return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // Current block height
            long blockHeight = blockHeader.getHeight();
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            // assembleVMExecution data
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddressBytes);
            programCall.setSender(senderBytes);
            programCall.setNumber(blockHeight);
            programCall.setMethodName(methodName);
            programCall.setMethodDesc(methodDesc);
            programCall.setArgs(args);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            if (method == null) {
                return Result.getFailed(CONTRACT_METHOD_NOT_EXIST);
            }
            // If the method is a contract call that is not linked, execute the contract code synchronously without changing the state root and return a value
            if (method.isView()) {
                return Result.getFailed(CONTRACT_NOT_EXECUTE_VIEW);
            }
            // Create on chain transactions, including smart contracts
            programCall.setValue(value);
            programCall.setMultyAssetValues(multyAssetValues);
            programCall.setPrice(price.longValue());

            // obtainVMActuator
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // implementVMVerify legality
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            // Skip when verifying contractGasvalidate
            long realGasLimit = gasLimit;
            programCall.setGasLimit(MAX_GASLIMIT);
            ProgramResult programResult = track.call(programCall);

            // When the execution result fails, the transaction returns an error directly, without being linked or consumedGas
            if (!programResult.isSuccess()) {
                Log.error("sender[{}], contractAddress[{}]" + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), AddressTool.getStringAddressByBytes(senderBytes), AddressTool.getStringAddressByBytes(contractAddressBytes));
                Result result = Result.getFailed(DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                addDebugEvents(programResult.getDebugEvents(), result);
                return result;
            } else {
                // After all other legality passes, verify againGas
                if (realGasLimit != MAX_GASLIMIT) {
                    programCall.setGasLimit(realGasLimit);
                    track = programExecutor.begin(prevStateRoot);
                    programResult = track.call(programCall);
                    if (!programResult.isSuccess()) {
                        Log.error(programResult.getStackTrace());
                        Result result = Result.getFailed(DATA_ERROR);
                        result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                        addDebugEvents(programResult.getDebugEvents(), result);
                        return result;
                    }
                }
            }

            return getSuccess().setData(programResult);

        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    public Result<ContractResult> previewCall(int chainId, byte[] senderBytes, byte[] contractAddressBytes, BigInteger value, Long gasLimit, Long price, String methodName, String methodDesc, String[][] args, List<ProgramMultyAssetValue> multyAssetValues) {
        try {
            if (!ContractUtil.checkPrice(price.longValue())) {
                return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // Current block height
            long blockHeight = blockHeader.getHeight();
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            // assembleVMExecution data
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddressBytes);
            programCall.setSender(senderBytes);
            programCall.setNumber(blockHeight);
            programCall.setMethodName(methodName);
            programCall.setMethodDesc(methodDesc);
            programCall.setArgs(args);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            if (method == null) {
                return Result.getFailed(CONTRACT_METHOD_NOT_EXIST);
            }
            // If the method is a contract call that is not linked, execute the contract code synchronously without changing the state root and return a value
            if (method.isView()) {
                return Result.getFailed(CONTRACT_NOT_EXECUTE_VIEW);
            }
            // Create on chain transactions, including smart contracts
            programCall.setValue(value);
            // add by pierre at 2020-10-29
            programCall.setMultyAssetValues(multyAssetValues);
            // end code by pierre
            programCall.setPrice(price.longValue());

            // obtainVMActuator
            ProgramExecutor programExecutor = contractHelper.getProgramExecutor(chainId);
            // implementVMVerify legality
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            programCall.setGasLimit(gasLimit);
            ProgramResult programResult = track.call(programCall);

            // When the execution result fails, the transaction returns an error directly, without being linked or consumedGas
            if (!programResult.isSuccess()) {
                Log.error("sender[{}], contractAddress[{}]" + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), AddressTool.getStringAddressByBytes(senderBytes), AddressTool.getStringAddressByBytes(contractAddressBytes));
                Result result = Result.getFailed(DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                result = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                addDebugEvents(programResult.getDebugEvents(), result);
                return result;
            }
            ContractResult contractResult = new ContractResult();

            contractResult.setGasUsed(programResult.getGasUsed());
            contractResult.setPrice(price);
            contractResult.setContractAddress(contractAddressBytes);
            contractResult.setSender(senderBytes);
            contractResult.setValue(value.longValue());
            contractResult.setRemark(ContractConstant.PREVIEW_CALL_REMARK);
            // Batch submission method, transactiontrackPlace it in the external method for processing contract execution results and submit it
            contractResult.setDebugEvents(programResult.getDebugEvents());

            // Return the call result、UsedGas、State root、Message Event、Contract transfer(Transfer out from contract)etc.
            contractResult.setError(false);
            contractResult.setRevert(false);
            contractResult.setResult(programResult.getResult());
            contractResult.setEvents(programResult.getEvents());
            contractResult.setTransfers(programResult.getTransfers());
            contractResult.setInvokeRegisterCmds(programResult.getInvokeRegisterCmds());
            contractResult.setContractAddressInnerCallSet(generateInnerCallSet(programResult.getInternalCalls()));
            contractResult.setAccounts(programResult.getAccounts());

            return getSuccess().setData(contractResult);

        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    public Set<String> generateInnerCallSet(List<ProgramInternalCall> internalCalls) {
        return internalCalls.stream().map(a -> AddressTool.getStringAddressByBytes(a.getContractAddress())).collect(Collectors.toSet());
    }


    public Result<DeleteContractTransaction> makeDeleteTx(int chainId, String sender, String contractAddress, String password, String remark) {
        Result accountResult = AccountCall.validationPassword(chainId, sender, password);
        if (accountResult.isFailed()) {
            return accountResult;
        }

        byte[] senderBytes = AddressTool.getAddress(sender);
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

        Result validateDelete = this.validateDelete(chainId, senderBytes, contractAddress, contractAddressBytes);
        if (validateDelete.isFailed()) {
            return validateDelete;
        }

        Result<DeleteContractTransaction> result = this.newDeleteTx(chainId, sender, senderBytes, contractAddressBytes, remark);
        return result;


    }

    public Result<DeleteContractTransaction> newDeleteTx(int chainId, String sender, byte[] senderBytes, byte[] contractAddressBytes, String remark) {
        try {
            DeleteContractTransaction tx = new DeleteContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            // assembletxData
            DeleteContractData deleteContractData = this.getDeleteContractData(contractAddressBytes, senderBytes);

            // calculateCoinData
            /*
             * absenceGasConsumption, in terminating smart contracts
             */
            CoinData coinData = new CoinData();
            Result makeCoinDataResult = this.makeCoinData(chainId, sender, senderBytes, contractAddressBytes, 0L, 0L, BigInteger.ZERO, tx.size(), deleteContractData, coinData, null, null);
            if (makeCoinDataResult.isFailed()) {
                return makeCoinDataResult;
            }

            tx.setTxDataObj(deleteContractData);
            tx.setCoinDataObj(coinData);
            tx.serializeData();

            return getSuccess().setData(tx);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_OTHER_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    private DeleteContractData getDeleteContractData(byte[] contractAddressBytes, byte[] senderBytes) {
        DeleteContractData deleteContractData = new DeleteContractData();
        deleteContractData.setContractAddress(contractAddressBytes);
        deleteContractData.setSender(senderBytes);
        return deleteContractData;
    }

    public Result validateDelete(int chainId, byte[] senderBytes, String contractAddress, byte[] contractAddressBytes) {
        try {
            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
            if (contractAddressInfoPoResult.isFailed()) {
                return contractAddressInfoPoResult;
            }
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            if (contractAddressInfoPo == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);

            // Current block state root
            byte[] stateRoot = ContractUtil.getStateRoot(blockHeader);
            // Obtain the current status of the contract
            ProgramStatus status = contractHelper.getContractStatus(chainId, stateRoot, contractAddressBytes);
            boolean isTerminatedContract = ContractUtil.isTerminatedContract(status.ordinal());
            if (isTerminatedContract) {
                return Result.getFailed(ContractErrorCode.CONTRACT_DELETED);
            }


            if (!ArraysTool.arrayEquals(senderBytes, contractAddressInfoPo.getSender())) {
                return Result.getFailed(ContractErrorCode.CONTRACT_DELETE_CREATER);
            }

            ContractBalance balance = contractHelper.getRealBalance(chainId, LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID, contractAddress);
            BigInteger totalBalance = balance.getTotal();
            if (totalBalance.compareTo(BigInteger.ZERO) != 0) {
                return Result.getFailed(ContractErrorCode.CONTRACT_DELETE_BALANCE);
            }

            return getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    public Result signAndBroadcastTx(int chainId, String sender, String password, Transaction tx) {
        try {
            // Generate signature
            AccountCall.transactionSignature(chainId, sender, password, tx);
            String txData = RPCUtil.encode(tx.serialize());

            // Send transaction to transaction module
            boolean broadcast = TransactionCall.newTx(chainId, txData);
            if (!broadcast) {
                // Sending transaction to transaction module failed, rolling back unconfirmed transactions in ledger
                return getFailed();
            }
            return getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }
}
