package io.nuls.test.cases.block;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:07
 * @Description: Function Description
 */
@Component
public class GetBlockHeaderByHeightCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Override
    public String title() {
        return "Obtain block heads by height";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        Result<BlockHeaderData> blockHeader = blockService.getBlockHeaderByHeight(new GetBlockHeaderByHeightReq(data.getHeight()));
        checkResultStatus(blockHeader);
        check(blockHeader.getData().getHeight() == data.getHeight(),"Height inconsistency");
        return blockHeader.getData();
    }
}
