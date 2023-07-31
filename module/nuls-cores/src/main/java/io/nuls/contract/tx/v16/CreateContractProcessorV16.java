package io.nuls.contract.tx.v16;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.BatchInfoV8;
import io.nuls.contract.model.bo.ContractCreate;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.dto.ContractResultDto;
import io.nuls.contract.model.dto.CreateContractDataDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.processor.CreateContractTxProcessor;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.Log;
import io.nuls.contract.validator.CreateContractTxValidator;
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

import static io.nuls.contract.util.ContractUtil.getSuccess;

// add by pierre at 2023/4/26
@Component("CreateContractProcessorV16")
public class CreateContractProcessorV16 implements TransactionProcessor {

    @Autowired
    private CreateContractTxProcessor createContractTxProcessor;
    @Autowired
    private CreateContractTxValidator createContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractService contractService;

    @Override
    public int getType() {
        return TxType.CREATE_CONTRACT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        result.put("txList", errorList);
        String errorCode = null;
        CreateContractTransaction createTx;
        for(Transaction tx : txs) {
            createTx = new CreateContractTransaction();
            createTx.copyTx(tx);
            try {
                Result validate = createContractTxValidator.validate(chainId, createTx);
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
                        Log.warn("empty contract result with txHash: {}", txHash);
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
            CreateContractData create;
            for (Transaction tx : txs) {
                create = new CreateContractData();
                create.parse(tx.getTxData(), 0);
                this.onRollback(chainId, new ContractWrapperTransaction(tx, create));
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    private Result onCommit(int chainId, ContractWrapperTransaction tx) throws Exception {
        BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
        long blockHeight = blockHeader.getHeight();
        tx.setBlockHeight(blockHeight);
        ContractResult contractResult = tx.getContractResult();
        contractResult.setBlockHeight(blockHeight);
        Result saveContractExecuteResult = contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        if (saveContractExecuteResult.isFailed()) {
            return saveContractExecuteResult;
        }
        // 执行失败的合约直接返回
        if (!contractResult.isSuccess()) {
            return getSuccess();
        }
        CreateContractData txData = (CreateContractData) tx.getContractData();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        String alias = txData.getAlias();
        byte[] code = txData.getCode();
        byte[] newestStateRoot = blockHeader.getStateRoot();

        ContractCreate create = new ContractCreate();
        create.setTokenType(contractResult.getTokenType());
        create.setTokenName(contractResult.getTokenName());
        create.setTokenSymbol(contractResult.getTokenSymbol());
        create.setTokenDecimals(contractResult.getTokenDecimals());
        create.setTokenTotalSupply(contractResult.getTokenTotalSupply());
        create.setAcceptDirectTransfer(contractResult.isAcceptDirectTransfer());
        Map<String, ContractAddressInfoPo> infoPoMap = new HashMap<>();
        Result result = contractHelper.onCommitForCreateV16(chainId, blockHeader, create, tx.getHash(), tx.getTime(), contractAddress, sender, code, alias, infoPoMap);
        if (result.isFailed()) {
            return result;
        }
        return result;
    }

    private Result onRollback(int chainId, ContractWrapperTransaction tx) throws Exception {
        ContractData txData = tx.getContractData();
        byte[] contractAddress = txData.getContractAddress();
        // 回滚代币转账交易
        ContractResult contractResult = tx.getContractResult();
        if (contractResult == null) {
            contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
        }
        if (contractResult == null) {
            return Result.getSuccess(null);
        }
        try {
            CreateContractData contractData = (CreateContractData) tx.getContractData();
            Log.info("rollback create tx, contract data is {}, result is {}", JSONUtils.obj2json(new CreateContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
        } catch (Exception e) {
            Log.warn("failed to trace create rollback log, error is {}", e.getMessage());
        }
        Result result = contractHelper.onRollbackForCreateV16(chainId, contractAddress, contractResult.isNrc20());
        if (result.isFailed()) {
            return result;
        }
        return contractService.deleteContractExecuteResult(chainId, tx.getHash());
    }
}
