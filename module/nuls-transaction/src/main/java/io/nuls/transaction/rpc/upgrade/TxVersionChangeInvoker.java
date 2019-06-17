package io.nuls.transaction.rpc.upgrade;

import io.nuls.base.data.Transaction;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.service.TxService;

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
            chain.getLogger().error(e);
        }

        //处理待打包队列
        PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
        boolean hasNext = true;
        while (hasNext) {
            //从队尾开始取
            Transaction tx = packablePool.pollLast(chain);
            if(null != tx) {
                addBack(chain, tx);
            }else{
                hasNext = false;
            }
        }

        LedgerCall.clearUnconfirmTxs(chain);
        //处理完成重置标志
        chain.getProtocolUpgrade().set(false);
        chain.getLogger().info("Version Change process, chainId:[{}]", chainId);
    }

    private void addBack(Chain chain, Transaction tx){
        addBack(chain, new TransactionNetPO(tx));
    }

    /**
     * 加回到新交易队列
     * @param chain
     * @param txNet
     */
    private void addBack(Chain chain, TransactionNetPO txNet){
        try {
            Transaction tx = txNet.getTx();
            //执行交易基础验证
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            TxService txService =  SpringLiteContext.getBean(TxService.class);
            txService.baseValidateTx(chain,tx, txRegister);
            chain.getUnverifiedQueue().addLast(txNet);
        } catch (NulsException e) {
            chain.getLogger().warn("TxVersionChangeInvoker verify failed", e);
        }
    }

}
