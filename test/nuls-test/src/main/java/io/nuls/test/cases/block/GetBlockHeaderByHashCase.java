package io.nuls.test.cases.block;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:07
 * @Description: Function Description
 */
@Component
public class GetBlockHeaderByHashCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Override
    public String title() {
        return "Obtain block heads by height";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        Result<BlockHeaderData> blockHeader = blockService.getBlockHeaderByHash(new GetBlockHeaderByHashReq(data.getHash()));
        checkResultStatus(blockHeader);
        check(blockHeader.getData().getHash().equals(data.getHash()),"hashInconsistent");
        return blockHeader.getData();
    }
}
