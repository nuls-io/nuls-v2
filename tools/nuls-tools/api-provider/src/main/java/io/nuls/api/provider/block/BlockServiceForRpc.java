package io.nuls.api.provider.block;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.block.facade.BlockHeaderData;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.DateUtils;

import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:37
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class BlockServiceForRpc extends BaseRpcService implements BlockService {

    @Override
    public Result<BlockHeaderData> getBlockHeaderByHash(GetBlockHeaderByHashReq req) {
        return _call("getBlockHeaderByHash",req,this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeaderData> getBlockHeaderByHeight(GetBlockHeaderByHeightReq req) {
        return _call("getBlockHeaderByHeight",req,this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeaderData> getBlockHeaderByLastHeight(GetBlockHeaderByLastHeightReq req) {
        return _call("latestBlockHeader",req,this::tranderBlockHeader);
    }

    @Override
    protected <T,R> Result<T> call(String method, Object req, Function<R, Result> res) {
        return callRpc(ModuleE.BL.abbr,method,req,res);
    }

    private Result<BlockHeaderData> _call(String method, Object req, Function<String, Result> callback){
        return call(method,req,callback);
    }

    private Result<BlockHeaderData> tranderBlockHeader(String hexString){
        try {
            BlockHeader header = new BlockHeader();
            header.parse(new NulsByteBuffer(HexUtil.decode(hexString)));
            BlockHeaderData res = new BlockHeaderData();
            BlockExtendsData blockExtendsData = new BlockExtendsData();
            blockExtendsData.parse(new NulsByteBuffer(header.getExtend()));
            res.setHash(header.getHash().toString());
            res.setHeight(header.getHeight());
            res.setSize(header.size());
            res.setTime(DateUtils.timeStamp2DateStr(header.getTime()));
            res.setTxCount(header.getTxCount());
            res.setMerkleHash(header.getMerkleHash().toString());
            res.setBlockSignature(header.getBlockSignature().getSignData().toString());
            res.setPreHash(header.getPreHash().toString());
            res.setPackingAddress(AddressTool.getStringAddressByBytes(header.getPackingAddress(getChainId())));
            res.setConsensusMemberCount(blockExtendsData.getConsensusMemberCount());
            res.setMainVersion(blockExtendsData.getMainVersion());
            res.setPackingIndexOfRound(blockExtendsData.getPackingIndexOfRound());
            res.setRoundIndex(blockExtendsData.getRoundIndex());
            res.setRoundStartTime(DateUtils.timeStamp2DateStr(blockExtendsData.getRoundStartTime()));
            return success(res);
        } catch (NulsException e) {
            Log.error("反序列化block header发生异常",e);
            return fail(ERROR_CODE);
        }
    }
}
