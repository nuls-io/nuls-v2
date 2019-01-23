package io.nuls.transaction.manager;

/**
 * 管理接收的其他链创建的跨链交易, 暂存验证中的跨链交易.
 *
 * @author: Charlie
 * @date: 2018/11/26
 */
/*@Service
public class VerifyCtxManager {

    @Autowired
    private CtxService ctxService;

    public void initCrossTxVerifyingMap(Chain chain) {
        //初始化跨链交易到缓存中
        List<CrossTx> txList = ctxService.getTxList(chain);
        if (txList != null) {
            txList.forEach(tx -> chain.getCrossTxVerifyingMap().put(tx.getTx().getHash(), tx));
        }
    }

    public void putCrossChainTx(Chain chain, CrossTx crossChainTx) {
        chain.getCrossTxVerifyingMap().put(crossChainTx.getTx().getHash(), crossChainTx);
    }

    public boolean containsKey(Chain chain, NulsDigestData hash) {
        return chain.getCrossTxVerifyingMap().containsKey(hash);
    }

    public void removeCrossChainTx(Chain chain, NulsDigestData hash) {
        chain.getCrossTxVerifyingMap().remove(hash);
    }

    public Map<NulsDigestData, CrossTx> getCrossTxVerifyingMap(Chain chain) {
        return chain.getCrossTxVerifyingMap();
    }

    public boolean updateCrossChainTxState(Chain chain, NulsDigestData hash, int state) {
        boolean rs = false;
        if (chain.getCrossTxVerifyingMap().containsKey(hash)) {
            CrossTx crossChainTx = chain.getCrossTxVerifyingMap().get(hash);
            crossChainTx.setState(state);
            rs = true;
        }
        return rs;
    }
}*/
