package io.nuls.test.cases.block;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:07
 * @Description: 功能描述
 */
@Component
public class GetBlockHeaderByHashCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Override
    public String title() {
        return "通过高度获取区块头";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        Result<BlockHeaderData> blockHeader = blockService.getBlockHeaderByHash(new GetBlockHeaderByHashReq(data.getHash()));
        checkResultStatus(blockHeader);
        check(blockHeader.getData().getHash().equals(data.getHash()),"hash不一致");
        return blockHeader.getData();
    }
}
