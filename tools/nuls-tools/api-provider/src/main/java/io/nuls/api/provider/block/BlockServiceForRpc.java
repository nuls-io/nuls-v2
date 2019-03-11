package io.nuls.api.provider.block;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:37
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
@Slf4j
public class BlockServiceForRpc extends BaseRpcService implements BlockService {

    @Override
    public Result<BlockHeader> getBlockHeaderByHash(GetBlockHeaderByHashReq req) {
        return _call("getBlockHeaderByHash",req,this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeader> getBlockHeaderByHeight(GetBlockHeaderByHeightReq req) {
        return _call("getBlockHeaderByHeight",req,this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeader> getBlockHeaderByLastHeight(GetBlockHeaderByLastHeightReq req) {
        return _call("latestBlockHeader",req,this::tranderBlockHeader);
    }

    @Override
    protected <T,R> Result<T> call(String method, Object req, Function<R, Result> res) {
        return callRpc(ModuleE.BL.abbr,method,req,res);
    }

    private Result<BlockHeader> _call(String method, Object req, Function<String, Result> callback){
        return call(method,req,callback);
    }

    private Result<BlockHeader> tranderBlockHeader(String hexString){
        try {
            BlockHeader header = new BlockHeader();
            header.parse(new NulsByteBuffer(HexUtil.decode(hexString)));
            return success(header);
        } catch (NulsException e) {
            log.error("反序列化block header发生异常",e);
            return fail(ERROR_CODE);
        }
    }
}
