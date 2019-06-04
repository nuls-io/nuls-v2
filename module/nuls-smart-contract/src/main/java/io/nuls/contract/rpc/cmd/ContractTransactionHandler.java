package io.nuls.contract.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.util.ContractUtil.wrapperFailed;
import static io.nuls.core.constant.TxType.*;

@Service
public class ContractTransactionHandler extends BaseCmd {

    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;

    @CmdAnnotation(cmd = CREATE_VALIDATOR, version = 1.0, description = "create contract validator")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createValidator(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String txData = (String) params.get("tx");
            CreateContractTransaction tx = new CreateContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != CREATE_CONTRACT) {
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
            ChainManager.chainHandle(chainId);
            String txData = (String) params.get("tx");
            CallContractTransaction tx = new CallContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != CALL_CONTRACT) {
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
            ChainManager.chainHandle(chainId);
            String txData = (String) params.get("tx");
            DeleteContractTransaction tx = new DeleteContractTransaction();
            tx.parse(RPCUtil.decode(txData), 0);
            Map<String, Boolean> result = new HashMap<>(2);
            if (tx.getType() != DELETE_CONTRACT) {
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

}
