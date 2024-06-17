package io.nuls.test.cases.block;

import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:42
 * @Description: Function Description
 */
@Component
public class SyncGetLastBlockHeaderCase extends BaseBlockCase<BlockHeaderData,Integer> {

    @Autowired
    GetLastBlockHeaderCase getLastBlockHeaderCase;

    @Override
    public String title() {
        return "Latest blocknetwork consistency";
    }

    @Override
    public BlockHeaderData doTest(Integer testCount, int depth) throws TestFailException {
        if(testCount == null){
            testCount = 1;
        }
        BlockHeaderData blockHeader = getLastBlockHeaderCase.check(null,depth);
        Boolean res = new BlockAbstractRemoteTestCase<>() {
            @Override
            public String title() {
                return "Remote node block header data consistency";
            }
        }.check(new RemoteTestParam(GetLastBlockHeaderCase.class,blockHeader,null),depth);
        if(!res){
            if(testCount <= 3){
                return doTest(testCount + 1,depth);
            }
            throw new TestFailException(title() + "Failed, local node and remote node data are inconsistent");
        }
        return blockHeader;
    }
}
