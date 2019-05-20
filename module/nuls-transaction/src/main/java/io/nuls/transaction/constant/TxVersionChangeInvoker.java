package io.nuls.transaction.constant;

import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.log.Log;

public class TxVersionChangeInvoker implements VersionChangeInvoker {

    @Override
    public void process(int chainId) {
        Log.info("TxVersionChangeInvoker trigger, chainId-" + chainId);
    }
    /**
     * 1.开启正在进行协议升级的标志
     *  ** 所有需要重新处理的交易都走网络交易通道重新处理
     *  。孤儿交易倒序放回未处理交易队列最前面
     *  。把待打包队里的交易全部拿出来放回未处理交易队列最前面
     *  。打包中的该标志开启直接放回未处理交易队列最前面(智能合约非系统交易除外)
     * 2.完成后关闭标志
     */

}
