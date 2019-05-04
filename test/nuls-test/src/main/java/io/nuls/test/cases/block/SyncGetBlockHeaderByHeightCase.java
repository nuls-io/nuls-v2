package io.nuls.test.cases.block;

import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:07
 * @Description: 功能描述
 */
@Component
public class SyncGetBlockHeaderByHeightCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Autowired GetBlockHeaderByHeightCase getBlockHeaderByHeightCase;

    @Override
    public String title() {
        return "通过高度获取区块头网络一致性";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        data.setHeight(data.getHeight()-1);
        BlockHeaderData blockHeaderData = getBlockHeaderByHeightCase.check(data,depth);
        Boolean res = new BlockAbstractRemoteTestCase<BlockHeaderData>(){

            @Override
            public String title() {
                return "远程节点通过height查询区块头一致性";
            }
        }.check(new RemoteTestParam<>(GetBlockHeaderByHeightCase.class,blockHeaderData,blockHeaderData),depth);
        if(!res){
            throw new TestFailException(title() + "失败，本地节点与远程节点数据不一致");
        }
        return blockHeaderData;
    }
}
