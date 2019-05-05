package io.nuls.base.api.provider.block;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;

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
