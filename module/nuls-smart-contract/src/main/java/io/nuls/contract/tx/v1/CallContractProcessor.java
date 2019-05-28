package io.nuls.contract.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.processor.CallContractTxProcessor;
import io.nuls.contract.util.Log;
import io.nuls.contract.validator.CallContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("CallContractProcessor")
public class CallContractProcessor implements TransactionProcessor {

    @Autowired
    private CallContractTxProcessor callContractTxProcessor;
    @Autowired
    private CallContractTxValidator callContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;

    @Override
    public int getType() {
        return TxType.CALL_CONTRACT;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        List<Transaction> errorList = new ArrayList<>();
        CallContractTransaction callTx;
        for(Transaction tx : txs) {
            callTx = new CallContractTransaction();
            callTx.copyTx(tx);
            try {
                Result validate = callContractTxValidator.validate(chainId, callTx);
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
                    callContractTxProcessor.onCommit(chainId, wrapperTx);
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
                call = new CallContractData();
                call.parse(tx.getTxData(), 0);
                callContractTxProcessor.onRollback(chainId, new ContractWrapperTransaction(tx, call));
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }
}
