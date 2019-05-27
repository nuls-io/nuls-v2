package io.nuls.account.tx.v1;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.account.util.LoggerUtil.LOG;

@Component("AliasProcessorV1")
public class AliasProcessor implements TransactionProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AliasService aliasService;

    @Override
    public int getType() {
        return TxType.ACCOUNT_ALIAS;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChain(chainId);
        List<Transaction> result = new ArrayList<>();
        Map<String, Transaction> aliasNamesMap = new HashMap<>(AccountConstant.INIT_CAPACITY_16);
        Map<String, Transaction> accountAddressMap = new HashMap<>(AccountConstant.INIT_CAPACITY_16);
        for (Transaction tx : txs) {
            Alias alias = new Alias();
            try {
                if (!aliasService.aliasTxValidate(chain.getChainId(), tx)) {
                    result.add(tx);
                    continue;
                }
                alias.parse(new NulsByteBuffer(tx.getTxData()));
            } catch (Exception e) {
                chain.getLogger().error(e);
                result.add(tx);
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(alias.getAddress());
            //check alias, 当有两笔交易冲突时,只需要把后一笔交易作为冲突者返回去
            Transaction tmp = aliasNamesMap.get(alias.getAlias());
            // the alias is already exist
            if (tmp != null) {
                result.add(tx);
                chain.getLogger().error("the alias is already exist,alias: " + alias.getAlias() + ",address: " + alias.getAddress());
                continue;
            } else {
                aliasNamesMap.put(alias.getAlias(), tx);
            }
            //check address
            tmp = accountAddressMap.get(address);
            // the address is already exist
            if (tmp != null) {
                result.add(tx);
            } else {
                accountAddressMap.put(address, tx);
            }
        }
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        boolean result = true;
        Chain chain = chainManager.getChain(chainId);
        List<Transaction> commitSucTxList = new ArrayList<>();
        for (Transaction tx : txs) {
            Alias alias = new Alias();
            try {
                alias.parse(new NulsByteBuffer(tx.getTxData()));
                result = aliasService.aliasTxCommit(chainId, alias);
            } catch (NulsException e) {
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("ac_commitTx alias tx commit error");
                break;
            }
            commitSucTxList.add(tx);
        }
        try {
            //如果提交失败，将已经提交成功的交易回滚
            if (!result) {
                boolean rollback = true;
                for (Transaction tx : commitSucTxList) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    rollback = aliasService.rollbackAlias(chainId, alias);
                }
                //回滚失败，抛异常
                if (!rollback) {
                    LoggerUtil.LOG.error("ac_commitTx alias tx rollback error");
                    throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
                }
            }
        } catch (Exception e) {
            errorLogProcess(chain, e);
            result = false;
        }

        return result;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        boolean result = true;
        Chain chain = chainManager.getChain(chainId);
        List<Transaction> rollbackSucTxList = new ArrayList<>();
        for (Transaction tx : txs) {
            Alias alias = new Alias();
            try {
                alias.parse(new NulsByteBuffer(tx.getTxData()));
                result = aliasService.rollbackAlias(chainId, alias);
            } catch (NulsException e) {
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("ac_rollbackTx alias tx rollback error");
                break;
            }
            rollbackSucTxList.add(tx);
        }
        //交易提交
        try {
            //如果回滚失败，将已经回滚成功的交易重新保存
            if (!result) {
                boolean commit = true;
                for (Transaction tx : rollbackSucTxList) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    commit = aliasService.aliasTxCommit(chainId, alias);
                }
                //保存失败，抛异常
                if (!commit) {
                    LoggerUtil.LOG.error("ac_rollbackTx alias tx commit error");
                    throw new NulsException(AccountErrorCode.ALIAS_SAVE_ERROR);
                }
            }
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            result = false;
        }
        return result;
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
