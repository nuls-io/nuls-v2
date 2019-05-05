package io.nuls.test.cases.block;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:40
 * @Description: 功能描述
 */
@Component
public class GetLastBlockHeaderCase extends BaseBlockCase<BlockHeaderData,Void> {

    BlockService blockService = ServiceManager.get(BlockService.class);

    @Override
    public String title() {
        return "获取最新区块头";
    }

    @Override
    public BlockHeaderData doTest(Void param, int depth) throws TestFailException {
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(new GetBlockHeaderByLastHeightReq());
        checkResultStatus(result);
        return result.getData();
    }

}
