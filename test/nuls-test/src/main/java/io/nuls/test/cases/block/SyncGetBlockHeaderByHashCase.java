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
public class SyncGetBlockHeaderByHashCase extends BaseBlockCase<BlockHeaderData,BlockHeaderData> {

    @Autowired GetBlockHeaderByHashCase getBlockHeaderByHashCase;

    @Override
    public String title() {
        return "adopthashObtain block head network consistency";
    }

    @Override
    public BlockHeaderData doTest(BlockHeaderData data, int depth) throws TestFailException {
        Boolean res = new BlockAbstractRemoteTestCase<BlockHeaderData>(){

            @Override
            public String title() {
                return "Remote nodes pass throughhashQuery block header consistency";
            }
        }.check(new RemoteTestParam<>(GetBlockHeaderByHashCase.class,data,data),depth);
        if(!res){
            throw new TestFailException(title() + "Failed, local node and remote node data are inconsistent");
        }
        return data;
    }
}
