package io.nuls.contract.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.processor.DeleteContractTxProcessor;
import io.nuls.contract.util.Log;
import io.nuls.contract.validator.DeleteContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("DeleteContractProcessor")
public class DeleteContractProcessor implements TransactionProcessor {

    @Autowired
    private DeleteContractTxProcessor deleteContractTxProcessor;
    @Autowired
    private DeleteContractTxValidator deleteContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;

    @Override
    public int getType() {
        return TxType.DELETE_CONTRACT;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> errorList = new ArrayList<>();
        DeleteContractTransaction deleteTx;
        for(Transaction tx : txs) {
            deleteTx = new DeleteContractTransaction();
            deleteTx.copyTx(tx);
            try {
                Result validate = deleteContractTxValidator.validate(chainId, deleteTx);
                if(validate.isFailed()) {
                    errorList.add(tx);
                }
            } catch (NulsException e) {
                Log.error(e);
                errorList.add(tx);
            }
        }
        return errorList;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        try {
            ContractPackageDto contractPackageDto = contractHelper.getChain(chainId).getBatchInfo().getContractPackageDto();
            if (contractPackageDto != null) {
                Map<String, ContractResult> contractResultMap = contractPackageDto.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                if (Log.isDebugEnabled()) {
                    Log.debug("contract execute txDataSize is {}, commit txDataSize is {}", contractResultMap.keySet().size(), txs.size());
                }
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
                    deleteContractTxProcessor.onCommit(chainId, wrapperTx);
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
            DeleteContractData delete;
            for (Transaction tx : txs) {
                delete = new DeleteContractData();
                delete.parse(tx.getTxData(), 0);
                deleteContractTxProcessor.onRollback(chainId, new ContractWrapperTransaction(tx, delete));
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
}
