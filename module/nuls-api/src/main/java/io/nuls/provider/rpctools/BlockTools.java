package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.RpcCall;
import io.nuls.provider.api.constant.CommandConstant;
import io.nuls.provider.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 14:06
 * @Description: 区块模块工具类
 */
@Component
public class BlockTools implements CallRpc {

    /**
     * 根据高度获取区块
     *
     * @param chainId
     * @param height
     * @return
     */
    public Result<Block> getBlockByHeight(int chainId, long height) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("height", height);
        try {
            Block block = callRpc(ModuleE.BL.name, "getBlockByHeight", param, (Function<Map, Block>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                Block _block = new Block();
                try {
                    _block.parse(new NulsByteBuffer(HexUtil.decode((String) res.get("value"))));
                    for(Transaction tx : _block.getTxs()) {
                        tx.setStatus(TxStatusEnum.CONFIRMED);
                    }
                } catch (NulsException e) {
                    Log.error(e);
                    return null;
                }
                return _block;
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 根据hash获取区块
     *
     * @param chainId
     * @param hash
     * @return
     */
    public Result<Block> getBlockByHash(int chainId, String hash) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("hash", hash);
        try {
            Block block = callRpc(ModuleE.BL.name, "getBlockByHash", param, (Function<Map, Block>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                Block _block = new Block();
                try {
                    _block.parse(new NulsByteBuffer(HexUtil.decode((String) res.get("value"))));
                    for(Transaction tx : _block.getTxs()) {
                        tx.setStatus(TxStatusEnum.CONFIRMED);
                    }
                } catch (NulsException e) {
                    Log.error(e);
                    return null;
                }
                return _block;
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取最新区块
     *
     * @param chainId
     * @return
     */
    public Result<Block> getBestBlock(int chainId) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        try {
            Block block = callRpc(ModuleE.BL.name, "latestBlock", param, (Function<Map, Block>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                Block _block = new Block();
                try {
                    _block.parse(new NulsByteBuffer(HexUtil.decode((String) res.get("value"))));
                    for(Transaction tx : _block.getTxs()) {
                        tx.setStatus(TxStatusEnum.CONFIRMED);
                    }
                } catch (NulsException e) {
                    Log.error(e);
                    return null;
                }
                return _block;
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getInfo(int chainId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        try {
            Map map = (Map) RpcCall.request(ModuleE.CS.abbr, CommandConstant.GET_CONSENSUS_CONFIG, params);
            return new Result<>(map);
        } catch (NulsException e) {
            return Result.fail(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    /**
     * 根据高度获取区块序列化字符串
     *
     * @param chainId
     * @param height
     * @return
     */
    public Result<String> getBlockSerializationByHeight(int chainId, long height) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("height", height);
        try {
            String block = callRpc(ModuleE.BL.name, "getBlockByHeight", param, (Function<Map, String>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                return (String) res.get("value");
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 根据hash获取区块序列化字符串
     *
     * @param chainId
     * @param hash
     * @return
     */
    public Result<String> getBlockSerializationByHash(int chainId, String hash) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("hash", hash);
        try {
            String block = callRpc(ModuleE.BL.name, "getBlockByHash", param, (Function<Map, String>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                return (String) res.get("value");
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<String> latestHeight(int chainId) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        try {
            Long block = callRpc(ModuleE.BL.name, "latestHeight", param, (Function<Map, Long>) res -> {
                Object value = res.get("value");
                if (value == null) {
                    return -1L;
                }
                return Long.parseLong(value.toString());
            });
            return new Result(block);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }
}
