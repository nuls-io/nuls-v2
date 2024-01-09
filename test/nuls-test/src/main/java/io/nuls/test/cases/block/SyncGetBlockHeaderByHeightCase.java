package io.nuls.test.cases.block;

import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:07
 * @Description: Function Description
 */
@Component
public class SyncGetBlockHeaderByHeightCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Autowired GetBlockHeaderByHeightCase getBlockHeaderByHeightCase;

    @Override
    public String title() {
        return "Obtain block head network consistency through height";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        data.setHeight(data.getHeight()-1);
        BlockHeaderData blockHeaderData = getBlockHeaderByHeightCase.check(data,depth);
        Boolean res = new BlockAbstractRemoteTestCase<BlockHeaderData>(){

            @Override
            public String title() {
                return "Remote nodes pass throughheightQuery block header consistency";
            }
        }.check(new RemoteTestParam<>(GetBlockHeaderByHeightCase.class,blockHeaderData,blockHeaderData),depth);
        if(!res){
            throw new TestFailException(title() + "Failed, local node and remote node data are inconsistent");
        }
        return blockHeaderData;
    }
}
