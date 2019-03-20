package io.nuls.test.cases.block;

import io.nuls.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.data.BlockHeader;
import io.nuls.test.cases.AbstractRemoteTestCase;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:42
 * @Description: 功能描述
 */
@Component
public class LastBlockSyncCase implements TestCaseIntf<Boolean,Void> {

    @Autowired
    GetLastBlockHeaderCase getLastBlockHeaderCase;

    @Override
    public String title() {
        return "最新区块网络一致性";
    }

    @Override
    public Boolean doTest(Void param, int depth) throws TestFailException {
        BlockHeaderData blockHeader = getLastBlockHeaderCase.check(null,depth);
        Boolean res = new AbstractRemoteTestCase(){
            @Override
            public String title() {
                return "获取远程节点区块头";
            }
        }.check(new RemoteTestParam(GetLastBlockHeaderCase.class,blockHeader,null),depth);
        if(!res){
            throw new TestFailException(title() + "失败，本地节点与远程节点数据不一致");
        }
        return res;
    }
}
