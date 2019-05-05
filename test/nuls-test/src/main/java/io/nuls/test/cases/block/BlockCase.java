package io.nuls.test.cases.block;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:12
 * @Description:
 * 区块查询相关指令测试
 * 0.获取本地最后一个区块数据
 * 1.获取远程节点最后一个区块数据。
 * 3.与本地进行比对。
 * 4.本地查询最新高度-1的高度的区块
 * 5.通过指定高度查询远程节点的区块。
 * 6.与本地结果进行比对
 * 7.本地通过【4】中的hash查询区块
 * 8.通过hash查询远程节点的区块
 * 9.与本地结果进行比对
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
        return "区块模块";
    }
}
