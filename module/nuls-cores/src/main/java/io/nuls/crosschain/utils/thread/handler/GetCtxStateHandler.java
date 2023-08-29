package io.nuls.crosschain.utils.thread.handler;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.utils.TxUtil;

/**
 * 跨链查询跨链交易处理状态
 *
 * @author tag
 * 2019/6/25
 */
public class GetCtxStateHandler implements Runnable {
    private Chain chain;

    public GetCtxStateHandler(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        while (chain.getGetCtxStateQueue() != null) {
            try {
                UntreatedMessage untreatedMessage = chain.getGetCtxStateQueue().take();
                TxUtil.getCtxState(chain, untreatedMessage.getCacheHash());
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }
}
