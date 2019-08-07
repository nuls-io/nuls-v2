package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.model.dto.TransactionDto;
import io.nuls.provider.rpctools.vo.TxRegisterDetail;
import io.nuls.provider.utils.ResultUtil;

import java.util.*;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:57
 * @Description: 功能描述
 */
@Component
public class TransactionTools implements CallRpc {


    /**
     * 验证新交易
     */
    public Result validateTx(int chainId, String txStr) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("chainId", chainId);
        params.put("tx", txStr);
        try {
            return callRpc(ModuleE.TX.abbr, "tx_verifyTx", params, (Function<Map<String, Object>, Result>) res -> new Result(res));
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 广播新交易
     */
    public Result newTx(int chainId, String txStr) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("chainId", chainId);
        params.put("tx", txStr);
        try {
            return callRpc(ModuleE.TX.abbr, "tx_newTx", params, (Function<Map<String, Object>, Result>) res -> new Result(res));
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    public boolean registerTx(int chainId, String moduleName, int... txTyps) {
        try {
            List<TxRegisterDetail> txRegisterDetailList = new ArrayList<>();
            Arrays.stream(txTyps).forEach(txType -> {
                TxRegisterDetail detail = new TxRegisterDetail();
                detail.setSystemTx(false);
                detail.setTxType(txType);
                detail.setUnlockTx(false);
                detail.setVerifySignature(true);
                detail.setVerifyFee(true);
                txRegisterDetailList.add(detail);
            });
            //向交易管理模块注册交易
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("moduleCode", moduleName);
            params.put("list", txRegisterDetailList);
            params.put("delList", List.of());
            return callRpc(ModuleE.TX.abbr, "tx_register", params, (Function<Map<String, Object>, Boolean>) res -> (Boolean) res.get("value"));
        } catch (Exception e) {
            Log.error("", e);
        }
        return true;
    }

    public Result<TransactionDto> getTx(int chainId, String txHash) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", txHash);
        try {
            return callRpc(ModuleE.TX.abbr, "tx_getTxClient", params, (Function<Map<String, Object>, Result<TransactionDto>>) res -> {
                if (res == null || res.get("tx") == null) {
                    return Result.fail(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), CommonCodeConstanst.DATA_NOT_FOUND.getMsg());
                }
                String txStr = (String) res.get("tx");
                Long height = Long.parseLong(res.get("height").toString());
                Integer status = (Integer) res.get("status");
                Transaction tx = new Transaction();
                try {
                    tx.parse(new NulsByteBuffer(HexUtil.decode(txStr)));
                    TransactionDto txDto = new TransactionDto(tx);
                    txDto.setBlockHeight(height);
                    txDto.setStatus(status);
                    return new Result(txDto);
                } catch (NulsException e) {
                    return ResultUtil.getNulsExceptionResult(e);
                }
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<TransactionDto> getConfirmedTx(int chainId, String txHash) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", txHash);
        try {
            return callRpc(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params, (Function<Map<String, Object>, Result<TransactionDto>>) res -> {
                if (res == null || res.get("tx") == null) {
                    return Result.fail(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), CommonCodeConstanst.DATA_NOT_FOUND.getMsg());
                }
                String txStr = (String) res.get("tx");
                Long height = Long.parseLong(res.get("height").toString());
                Integer status = (Integer) res.get("status");
                Transaction tx = new Transaction();
                try {
                    tx.parse(new NulsByteBuffer(HexUtil.decode(txStr)));
                    TransactionDto txDto = new TransactionDto(tx);
                    txDto.setBlockHeight(height);
                    txDto.setStatus(status);
                    return new Result(txDto);
                } catch (NulsException e) {
                    return ResultUtil.getNulsExceptionResult(e);
                }
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
