package io.nuls.base.api.provider.block;

import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.NulsDateUtils;

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
        return _call("getBlockHeaderPoByHash", req, this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeaderData> getBlockHeaderByHeight(GetBlockHeaderByHeightReq req) {
        return _call("getBlockHeaderPoByHeight", req, this::tranderBlockHeader);
    }

    @Override
    public Result<BlockHeaderData> getBlockHeaderByLastHeight(GetBlockHeaderByLastHeightReq req) {
        return _call("latestBlockHeaderPo", req, this::tranderBlockHeader);
    }

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> res) {
        return callRpc(ModuleE.BL.abbr, method, req, res);
    }

    private Result<BlockHeaderData> _call(String method, Object req, Function<String, Result> callback) {
        return call(method, req, callback);
    }

    private Result<BlockHeaderData> tranderBlockHeader(String hexString) {
        try {
            BlockHeaderPo header = new BlockHeaderPo();
            header.parse(new NulsByteBuffer(RPCUtil.decode(hexString)));
            BlockHeaderData res = new BlockHeaderData();
            BlockExtendsData blockExtendsData = new BlockExtendsData();
            blockExtendsData.parse(new NulsByteBuffer(header.getExtend()));
            res.setHash(header.getHash().toString());
            res.setHeight(header.getHeight());
            res.setSize(header.getBlockSize());
            res.setTime(NulsDateUtils.timeStamp2DateStr(header.getTime()));
            res.setTxCount(header.getTxCount());
            res.setMerkleHash(header.getMerkleHash().toString());
            res.setBlockSignature(header.getBlockSignature().getSignData().toString());
            res.setPreHash(header.getPreHash().toString());
            res.setPackingAddress(AddressTool.getStringAddressByBytes(header.getPackingAddress(getChainId())));
            res.setConsensusMemberCount(blockExtendsData.getConsensusMemberCount());
            res.setMainVersion(blockExtendsData.getMainVersion());
            res.setBlockVersion(blockExtendsData.getBlockVersion());
            res.setPackingIndexOfRound(blockExtendsData.getPackingIndexOfRound());
            res.setRoundIndex(blockExtendsData.getRoundIndex());
            res.setRoundStartTime(NulsDateUtils.timeStamp2DateStr(blockExtendsData.getRoundStartTime()));
            res.setStateRoot(RPCUtil.encode(blockExtendsData.getStateRoot()));
            return success(res);
        } catch (NulsException e) {
            Log.error("反序列化block header发生异常", e);
            return fail(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
    }
}
