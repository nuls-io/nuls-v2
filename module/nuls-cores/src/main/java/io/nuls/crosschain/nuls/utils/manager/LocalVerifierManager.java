package io.nuls.crosschain.nuls.utils.manager;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.model.po.LocalVerifierPO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import io.nuls.crosschain.nuls.srorage.CtxStateService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.LocalVerifierService;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

@Component
public class LocalVerifierManager {
    @Autowired
    private static LocalVerifierService localVerifierService;
    @Autowired
    private static SendHeightService sendHeightService;
    @Autowired
    private static CtxStatusService ctxStatusService;

    /**
     * 加载本地验证人列表
     * Load local verifier list
     * */
    public static void loadLocalVerifier(Chain chain){
        LocalVerifierPO localVerifierPO = localVerifierService.get(chain.getChainId());
        if(localVerifierPO == null || localVerifierPO.getVerifierList() == null || localVerifierPO.getVerifierList().isEmpty()){
            chain.getLogger().info("Local verifier has not been initialized" );
            return;
        }
        chain.setVerifierList(localVerifierPO.getVerifierList());
    }

    /**
     * 初始化本地验证人
     * Initialize local verifier list
     * */
    public static boolean initLocalVerifier(Chain chain, List<String> verifierList){
        if(verifierList == null || verifierList.isEmpty()){
            chain.getLogger().error("Local verifier is empty, data is abnormal" );
            return false;
        }
        boolean saveResult =  localVerifierService.save(new LocalVerifierPO(verifierList), chain.getChainId());
        if(!saveResult){
            chain.getLogger().error("Failed to save local initialization verifier list");
            return false;
        }
        chain.setVerifierList(verifierList);
        chain.getLogger().info("Local verifier initialization complete,verifierList:{}",verifierList );
        return true;
    }

    /**
     * 验证人变更提交
     * Change of verifier
     * */
    public static boolean localVerifierChangeCommit(Chain chain, Transaction ctx, List<String> reduceList, List<String> appendList, long height, NulsHash txHash, int syncStatus){
        LocalVerifierPO localVerifierPO = localVerifierService.get(chain.getChainId());
        if(localVerifierPO == null || localVerifierPO.getVerifierList() == null || localVerifierPO.getVerifierList().isEmpty()){
            chain.getLogger().error("Local verifier is empty, data is abnormal" );
            return false;
        }
        chain.getSwitchVerifierLock().writeLock().lock();
        try {
            if(reduceList != null && !reduceList.isEmpty()){
                localVerifierPO.getVerifierList().removeAll(reduceList);
            }
            if(appendList != null && !appendList.isEmpty()){
                localVerifierPO.getVerifierList().addAll(appendList);
            }
            boolean saveResult = localVerifierService.save(localVerifierPO, chain.getChainId());
            if(!saveResult){
                chain.getLogger().error("Failed to update local initialization verifier list");
                return false;
            }
            //跨链交易状态不用回滚
            CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.CONFIRMED.getStatus());
            saveResult = ctxStatusService.save(txHash, ctxStatusPO, chain.getChainId());
            if(!saveResult){
                chain.getLogger().error("Transaction processing status save error");
                return false;
            }
            if(syncStatus == 1){
                SendCtxHashPO sendCtxHashPO = sendHeightService.get(height, chain.getChainId());
                if(sendCtxHashPO == null){
                    ArrayList<NulsHash> arrayList = new ArrayList<>();
                    arrayList.add(txHash);
                    sendCtxHashPO = new SendCtxHashPO(arrayList);
                    sendHeightService.save(height, sendCtxHashPO, chain.getChainId());
                }
            }
            chain.setVerifierList(localVerifierPO.getVerifierList());
            chain.setLastChangeHeight(height);
            chain.setVerifierChangeTx(null);
            return true;
        }finally {
            chain.getSwitchVerifierLock().writeLock().unlock();
        }
    }

    /**
     * 验证人变更提交回滚
     * Change of verifier
     * */
    public static boolean localVerifierChangeRollback(Chain chain, List<String> reduceList, List<String> appendList,long height, NulsHash txHash){
        LocalVerifierPO localVerifierPO = localVerifierService.get(chain.getChainId());
        if(localVerifierPO == null || localVerifierPO.getVerifierList() == null || localVerifierPO.getVerifierList().isEmpty()){
            chain.getLogger().error("Local verifier is empty, data is abnormal" );
            return false;
        }
        chain.getSwitchVerifierLock().writeLock().lock();
        try {
            if(reduceList != null && !reduceList.isEmpty()){
                localVerifierPO.getVerifierList().addAll(reduceList);
            }
            if(appendList != null && !appendList.isEmpty()){
                localVerifierPO.getVerifierList().removeAll(appendList);
            }
            boolean saveResult = localVerifierService.save(localVerifierPO, chain.getChainId());
            if(!saveResult){
                chain.getLogger().error("Failed to update local initialization verifier list");
                return false;
            }
            SendCtxHashPO sendCtxHashPO = sendHeightService.get(height, chain.getChainId());
            if(sendCtxHashPO != null){
                sendCtxHashPO.getHashList().remove(txHash);
                if(sendCtxHashPO.getHashList().isEmpty()){
                    sendHeightService.delete(height, chain.getChainId());
                }else{
                    sendHeightService.save(height, sendCtxHashPO, chain.getChainId());
                }
            }
            chain.setVerifierList(localVerifierPO.getVerifierList());
            chain.setLastChangeHeight(height - 1);
            return true;
        }finally {
            chain.getSwitchVerifierLock().writeLock().unlock();
        }
    }
}
