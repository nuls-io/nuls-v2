package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.protocol.TransactionProcessor;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.PunishManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("RedPunishProcessorV1")
public class RedPunishProcessor implements TransactionProcessor {
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private ChainManager chainManager;

    @Override
    public int getType() {
        return TxType.RED_PUNISH;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist");
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        Set<String> addressHexSet = new HashSet<>();
        for (Transaction tx:txs) {
            try {
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.parse(tx.getTxData(), 0);
                String addressHex = HexUtil.encode(redPunishData.getAddress());
                /*
                 * 重复的红牌交易不打包
                 * */
                if (!addressHexSet.add(addressHex)) {
                    invalidTxList.add(tx);
                }
            }catch (Exception e){
                invalidTxList.add(tx);
                LoggerUtil.commonLog.error("Transaction Verification Exceptions");
                LoggerUtil.commonLog.error(e);
            }
        }
        return invalidTxList;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx:txs) {
            try {
                if(punishManager.redPunishCommit(tx,chain,blockHeader)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to red punish transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    punishManager.redPunishRollback(rollbackTx, chain,blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to red punish transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx:txs) {
            try {
                if(punishManager.redPunishRollback(tx,chain,blockHeader)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to red punish transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    punishManager.redPunishCommit(commitTx, chain, blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to red punish transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }
}
