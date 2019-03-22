package io.nuls.api.analysis;

import io.nuls.api.ApiContext;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.constant.ApiErrorCode;
import io.nuls.api.constant.CommandConstant;
import io.nuls.api.model.po.db.*;
import io.nuls.api.rpc.RpcCall;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.basic.Result;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletRpcHandler {

    public static Result<BlockInfo> getBlockInfo(int chainID, long height) {
        Map<String, Object> params = new HashMap<>(ApiConstant.INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, ApiContext.VERSION);
        params.put("chainId", chainID);
        params.put("height", height);
        try {
            String blockHex = (String) RpcCall.request(ModuleE.BL.abbr, CommandConstant.GET_BLOCK_BY_HEIGHT, params);
            if (null == blockHex) {
                return Result.getSuccess(null);
            }
            byte[] bytes = HexUtil.decode(blockHex);
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            BlockInfo blockInfo = AnalysisHandler.toBlockInfo(block, chainID);

            return Result.getSuccess(null).setData(blockInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.getFailed(ApiErrorCode.DATA_PARSE_ERROR);
    }

    public static Result<BlockInfo> getBlockInfo(int chainID, String hash) {
        Map<String, Object> params = new HashMap<>(ApiConstant.INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, ApiContext.VERSION);
        params.put("chainId", chainID);
        params.put("hash", hash);
        try {
            String blockHex = (String) RpcCall.request(ModuleE.BL.abbr, CommandConstant.GET_BLOCK_BY_HASH, params);
            if (null == blockHex) {
                return Result.getSuccess(null);
            }
            byte[] bytes = HexUtil.decode(blockHex);
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            BlockInfo blockInfo = AnalysisHandler.toBlockInfo(block, chainID);
            return Result.getSuccess(null).setData(blockInfo);
        } catch (Exception e) {
            e.printStackTrace();
            // return Result.getFailed()
        }
        return Result.getFailed(ApiErrorCode.DATA_PARSE_ERROR);
    }

    public static AccountInfo getAccountBalance(int chainId, String address, int assetChainId, int assetId) {
        Map<String, Object> params = new HashMap<>(ApiConstant.INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, ApiContext.VERSION);
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("assetChainId", assetChainId);
        params.put("assetId", assetId);
        try {
            Map map = (Map) RpcCall.request(ModuleE.LG.abbr, CommandConstant.GET_BALANCE, params);
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setTotalBalance(new BigInteger(map.get("total").toString()));
            accountInfo.setBalance(new BigInteger(map.get("available").toString()));
            accountInfo.setTimeLock(new BigInteger(map.get("timeHeightLocked").toString()));
            accountInfo.setConsensusLock(new BigInteger(map.get("permanentLocked").toString()));

            return accountInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Result<TransactionInfo> getTx(int chainId, String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, ApiContext.VERSION);
        params.put("chainId", chainId);
        params.put("txHash", hash);
        try {
            Map map = (Map) RpcCall.request(ModuleE.TX.abbr, CommandConstant.CLIENT_GETTX, params);
            String txHex = (String) map.get("txHex");
            if (null == txHex) {
                return null;
            }
            Transaction tx = Transaction.getInstance(txHex);
            TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx);
            txInfo.setHeight(Long.parseLong(map.get("height").toString()));
            return Result.getSuccess(null).setData(txInfo);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            return Result.getFailed(ApiErrorCode.DATA_PARSE_ERROR);
        }
    }

    public static Result<AgentInfo> getAgentInfo(int chainId, String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("agentHash", hash);
        try {
            Map map = (Map) RpcCall.request(ModuleE.CS.abbr, CommandConstant.GET_AGENT, params);
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.setCreditValue(Double.parseDouble(map.get("creditVal").toString()));
            agentInfo.setDepositCount((Integer) map.get("memberCount"));
            agentInfo.setDepositCount((Integer) map.get("status"));

            return Result.getSuccess(null).setData(agentInfo);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        }
    }

    public static Result<Map> getConsensusConfig(int chainId) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        try {
            Map map = (Map) RpcCall.request(ModuleE.CS.abbr, CommandConstant.GET_CONSENSUS_CONFIG, params);
            return Result.getSuccess(null).setData(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        }
    }

    public Result<ContractResultInfo> getContractResult(int chainId, String txHash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", txHash);

        try {
            Map map = (Map) RpcCall.request(ModuleE.CS.abbr, CommandConstant.GET_CONSENSUS_CONFIG, params);
            return Result.getSuccess(null).setData(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        }
    }

    public static Result<ContractInfo> getContractInfo(int chainId, String contractAddress) throws NulsException {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("contractAddress", contractAddress);
        Map map = (Map) RpcCall.request(ModuleE.SC.abbr, CommandConstant.CONTRACT_INFO, params);

        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setCreateTxHash(map.get("createTxHash").toString());
        contractInfo.setContractAddress(map.get("address").toString());
        contractInfo.setCreater(map.get("creater").toString());
        contractInfo.setCreateTime(Long.parseLong(map.get("createTime").toString()));
        contractInfo.setBlockHeight(Long.parseLong(map.get("blockHeight").toString()));
        contractInfo.setNrc20((Boolean) map.get("isNrc20"));
        contractInfo.setStatus(0);
        if (contractInfo.isNrc20()) {
            contractInfo.setTokenName(map.get("nrc20TokenName").toString());
            contractInfo.setSymbol(map.get("nrc20TokenSymbol").toString());
            contractInfo.setDecimals((Integer) map.get("decimals"));
            contractInfo.setTotalSupply(map.get("totalSupply").toString());
        }

        List<Map<String, Object>> methodMap = (List<Map<String, Object>>) map.get("method");
        List<ContractMethod> methodList = new ArrayList<>();
        List<Map<String, Object>> argsList;
        List<String> paramList;
        for (Map<String, Object> map1 : methodMap) {
            ContractMethod method = new ContractMethod();
            method.setName((String) map1.get("name"));
            method.setReturnType((String) map1.get("returnArg"));
            argsList = (List<Map<String, Object>>) map1.get("args");
            paramList = new ArrayList<>();
            for (Map<String, Object> arg : argsList) {
                paramList.add((String) arg.get("name"));
            }
            method.setParams(paramList);
            methodList.add(method);
        }
        contractInfo.setMethods(methodList);
        return Result.getSuccess(null).setData(map);

    }

    public static Result<ContractResultInfo> getContractResultInfo(int chainId, String hash) throws NulsException {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        Map map = (Map) RpcCall.request(ModuleE.SC.abbr, CommandConstant.CONTRACT_RESULT, params);
        map = (Map) map.get("data");

        ContractResultInfo resultInfo = new ContractResultInfo();
        resultInfo.setSuccess((Boolean) map.get("success"));
        resultInfo.setTxHash((String) map.get("txHash"));
        resultInfo.setContractAddress((String) map.get("contractAddress"));
        resultInfo.setErrorMessage((String) map.get("errorMessage"));
        resultInfo.setResult((String) map.get("result"));

        resultInfo.setGasUsed(map.get("gasUsed") != null ? Long.parseLong(map.get("gasUsed").toString()) : 0);
        resultInfo.setGasLimit(map.get("gasLimit") != null ? Long.parseLong(map.get("gasLimit").toString()) : 0);
        resultInfo.setPrice(map.get("price") != null ? Long.parseLong(map.get("price").toString()) : 0);
        resultInfo.setTotalFee((String) map.get("totalFee"));
        resultInfo.setTxSizeFee((String) map.get("txSizeFee"));
        resultInfo.setActualContractFee((String) map.get("actualContractFee"));
        resultInfo.setRefundFee((String) map.get("refundFee"));
        resultInfo.setValue((String) map.get("value"));
        resultInfo.setBalance((String) map.get("balance"));
        resultInfo.setRemark((String) map.get("remark"));


        return null;
    }
}
