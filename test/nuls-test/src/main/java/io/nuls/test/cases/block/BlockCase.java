package io.nuls.test.cases.block;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:12
 * @Description:
 * Block query related instruction testing
 * 0.Get the last local block data
 * 1.Obtain the last block data of the remote node.
 * 3.Compare with the local area.
 * 4.Local Query Latest Height-1A block of high height
 * 5.Query the blocks of remote nodes by specifying a height.
 * 6.Compare with local results
 * 7.Local pass【4】MiddlehashQuery Block
 * 8.adopthashQuery the blocks of remote nodes
 * 9.Compare with local results
 */
@TestCase("block")
@Component
public class BlockCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                SyncGetLastBlockHeaderCase.class,
                SyncGetBlockHeaderByHeightCase.class,
                SyncGetBlockHeaderByHashCase.class
        };
    }

    @Override
    public String title() {
        return "Block module";
    }
}
