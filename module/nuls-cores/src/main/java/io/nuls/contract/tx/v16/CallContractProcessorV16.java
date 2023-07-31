package io.nuls.contract.tx.v16;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.BatchInfoV8;
import io.nuls.contract.model.bo.ContractInternalCreate;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.dto.CallContractDataDto;
import io.nuls.contract.model.dto.ContractResultDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.processor.CallContractTxProcessor;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.validator.CallContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.getFailed;

// add by pierre at 2023/4/26
@Component("CallContractProcessorV16")
public class CallContractProcessorV16 implements TransactionProcessor {

    @Autowired
    private CallContractTxProcessor callContractTxProcessor;
    @Autowired
    private CallContractTxValidator callContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ContractService contractService;

    @Override
    public int getType() {
        return TxType.CALL_CONTRACT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        result.put("txList", errorList);
        String errorCode = null;
        CallContractTransaction callTx;
        for(Transaction tx : txs) {
            callTx = new CallContractTransaction();
            callTx.copyTx(tx);
            try {
                Result validate = callContractTxValidator.validateV14(chainId, callTx);
                if(validate.isFailed()) {
                    errorCode = validate.getErrorCode().getCode();
                    errorList.add(tx);
                }
            } catch (NulsException e) {
                Log.error(e);
                errorCode = e.getErrorCode().getCode();
                errorList.add(tx);
            }
        }
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        try {
            BatchInfoV8 batchInfo = contractHelper.getChain(chainId).getBatchInfoV8();
            if (batchInfo != null) {
                Map<String, ContractResult> contractResultMap = batchInfo.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                String txHash;
                for (Transaction tx : txs) {
                    txHash = tx.getHash().toString();
                    contractResult = contractResultMap.get(txHash);
                    if (contractResult == null) {
                        Log.warn("empty contract result with txHash: {}, txType: {}", txHash, tx.getType());
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    this.onCommit(chainId, wrapperTx);
                }
            }

            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            ChainManager.chainHandle(chainId);
            CallContractData call;
            for (Transaction tx : txs) {
                if (tx.getType() == TxType.CROSS_CHAIN) {
                    call = ContractUtil.parseCrossChainTx(tx, chainManager);
                    if (call == null) {
                        continue;
                    }
                } else {
                    call = new CallContractData();
                    call.parse(tx.getTxData(), 0);
                }
                this.onRollback(chainId, new ContractWrapperTransaction(tx, call));
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }

    private Result onCommit(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
            byte[] stateRoot = blockHeader.getStateRoot();
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();
            String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);

            Map<String, ContractAddressInfoPo> infoPoMap = new HashMap<>();
            infoPoMap.put(contractAddressStr, contractAddressInfoPo);
            // 处理内部创建合约
            List<ContractInternalCreate> internalCreates = contractResult.getInternalCreates();
            if (internalCreates != null && !internalCreates.isEmpty()) {
                for (ContractInternalCreate internalCreate : internalCreates) {
                    Result result = contractHelper.onCommitForCreateV16(chainId, blockHeader, internalCreate, tx.getHash(), tx.getTime(), internalCreate.getContractAddress(),
                            internalCreate.getSender(), contractHelper.getContractCode(chainId, stateRoot, internalCreate.getCodeCopyBy()), "internal_create", infoPoMap);
                    if (result.isFailed()) {
                        return result;
                    }
                }
            }
            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    private Result onRollback(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            // 处理内部创建合约
            List<ContractInternalCreate> internalCreates = contractResult.getInternalCreates();
            if (internalCreates != null && !internalCreates.isEmpty()) {
                for (ContractInternalCreate internalCreate : internalCreates) {
                    Result result = contractHelper.onRollbackForCreateV16(chainId, internalCreate.getContractAddress(), internalCreate.getTokenType() == TokenTypeStatus.NRC20.status());
                    if (result.isFailed()) {
                        return result;
                    }
                }
            }
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }
}
