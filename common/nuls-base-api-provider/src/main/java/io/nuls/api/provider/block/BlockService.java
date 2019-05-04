package io.nuls.api.provider.block;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.block.facade.BlockHeaderData;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.data.BlockHeader;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:30
 * @Description: block service
 */
public interface BlockService {

    Result<BlockHeaderData> getBlockHeaderByHash(GetBlockHeaderByHashReq req);

    Result<BlockHeaderData> getBlockHeaderByHeight(GetBlockHeaderByHeightReq req);

    Result<BlockHeaderData> getBlockHeaderByLastHeight(GetBlockHeaderByLastHeightReq req);


}
