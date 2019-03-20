package io.nuls.test.cases.block;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 14:09
 * @Description: 功能描述
 */
@TestCase
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
        return "区块数据一致性";
    }
}
