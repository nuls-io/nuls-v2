package io.nuls.transaction.rpc.upgrade;

import io.nuls.base.data.Transaction;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.threadpool.NetTxProcessJob;
import io.nuls.transaction.threadpool.NetTxThreadPoolExecutor;

/**
 * @author: Charlie
 * @date: 2019/05/20
 */
public class TxVersionChangeInvoker implements VersionChangeInvoker {

    @Override
    public void process(int chainId) {
        /**
         * 1.开启正在进行协议升级的标志
         *  ** 所有需要重新处理的交易都走网络交易通道重新处理
         *  。孤儿交易倒序放回未处理交易队列最前面
         *  。把待打包队里的交易全部拿出来放回未处理交易队列最前面
         *  。打包中的该标志开启直接放回未处理交易队列最前面(智能合约非系统交易除外)
         * 2.完成后关闭标志
         */
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        Chain chain = chainManager.getChain(chainId);
        if (null == chain) {
            Log.error(TxErrorCode.CHAIN_NOT_FOUND.getCode());
            return;
        }
        //设置升级的标志,暂停打包交易(出空块)暂停新交易处理
        chain.getProtocolUpgrade().set(true);
        try {
            //等待正在处理的交易处理结束(打包过程中的交易、新交易)
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
        }

        //处理孤儿
//        LinkedList<TransactionNetPO> chainOrphan = chain.getOrphanList();
//        if (!chainOrphan.isEmpty()) {
//            boolean ide = true;
//           while (ide){
//               TransactionNetPO txNet = chainOrphan.pollLast();
//               if(null == txNet){
//                   ide = false;
//               }
//               addJob(chain, txNet);
//           }
//        }

        //处理待打包队列
        PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
        boolean hasNext = true;
        while (hasNext) {
            //从队尾开始取
            Transaction tx = packablePool.pollLast(chain);
            if(null != tx) {
                addJob(chain, tx);
            }else{
                hasNext = false;
            }
        }

        //处理完成重置标志
        chain.getProtocolUpgrade().set(false);
        chain.getLoggerMap().get(TxConstant.LOG_TX).info("Version Change process success!, chainId:[{}]", chainId);
    }

    private void addJob(Chain chain, Transaction tx){
        addJob(chain, new TransactionNetPO(tx));
    }

    private void addJob(Chain chain, TransactionNetPO txNet){
        NetTxProcessJob netTxProcessJob = new NetTxProcessJob(chain, txNet);
        NetTxThreadPoolExecutor threadPool = chain.getNetTxThreadPoolExecutor();
        threadPool.addFirst(netTxProcessJob);
    }

}
