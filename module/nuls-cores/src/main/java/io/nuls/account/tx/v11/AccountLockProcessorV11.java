package io.nuls.account.tx.v11;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.helper.AccountBlockHelper;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.AccountBlockData;
import io.nuls.account.model.po.AccountBlockPO;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountBlockStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.account.util.LoggerUtil.LOG;

@Component("AccountLockProcessorV11")
public class AccountLockProcessorV11 implements TransactionProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountBlockHelper accountBlockHelper;
    @Autowired
    private AccountBlockStorageService accountBlockStorageService;

    @Override
    public int getType() {
        return TxType.BLOCK_ACCOUNT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Map<String, Object> result = null;
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            result = new HashMap<>(AccountConstant.INIT_CAPACITY_4);
            String errorCode = null;
            if (chain == null) {
                errorCode = AccountErrorCode.CHAIN_NOT_EXIST.getCode();
                chain.getLogger().error("chain is not exist, -chainId:{}", chainId);
                result.put("txList", txs);
                result.put("errorCode", errorCode);
                return result;
            }
            List<Transaction> txList = new ArrayList<>();
            for (Transaction tx : txs) {
                try {
                    Result rs =  accountBlockHelper.blockAccountTxValidate(chain, tx);
                    if (rs.isFailed()) {
                        errorCode = rs.getErrorCode().getCode();
                        txList.add(tx);
                    }
                } catch (Exception e) {
                    chain.getLogger().error(e);
                    if (e instanceof NulsException) {
                        errorCode = ((NulsException)e).getErrorCode().getCode();
                    } else {
                        errorCode = AccountErrorCode.DATA_ERROR.getCode();
                    }
                    txList.add(tx);
                }
            }
            result.put("txList", txList);
            result.put("errorCode", errorCode);
        } catch (Exception e) {
            errorLogProcess(chain, e);
            result.put("txList", txs);
            result.put("errorCode", AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        boolean result = true;
        Chain chain = chainManager.getChain(chainId);
        List<Transaction> commitSucTxList = new ArrayList<>();
        for (Transaction tx : txs) {
            AccountBlockData data = new AccountBlockData();
            try {
                data.parse(new NulsByteBuffer(tx.getTxData()));
                String[] addresses = data.getAddresses();
                List<AccountBlockPO> poList = Arrays.asList(addresses).stream().map(a -> new AccountBlockPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                result = accountBlockStorageService.saveAccountList(poList);
            } catch (NulsException e) {
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("ac_commitTx block_account tx commit error");
                break;
            }
            commitSucTxList.add(tx);
        }
        try {
            //如果提交失败，将已经提交成功的交易回滚
            if (!result) {
                boolean rollback = true;
                for (Transaction tx : commitSucTxList) {
                    AccountBlockData data = new AccountBlockData();
                    data.parse(new NulsByteBuffer(tx.getTxData()));
                    String[] addresses = data.getAddresses();
                    rollback = accountBlockStorageService.removeAccountList(Arrays.asList(addresses));
                }
                //回滚失败，抛异常
                if (!rollback) {
                    LoggerUtil.LOG.error("ac_commitTx block_account tx rollback error");
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
            AccountBlockData data = new AccountBlockData();
            try {
                data.parse(new NulsByteBuffer(tx.getTxData()));
                String[] addresses = data.getAddresses();
                result = accountBlockStorageService.removeAccountList(Arrays.asList(addresses));
            } catch (NulsException e) {
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("ac_rollbackTx block_account tx rollback error");
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
                    AccountBlockData data = new AccountBlockData();
                    data.parse(new NulsByteBuffer(tx.getTxData()));
                    String[] addresses = data.getAddresses();
                    List<AccountBlockPO> poList = Arrays.asList(addresses).stream().map(a -> new AccountBlockPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                    commit = accountBlockStorageService.saveAccountList(poList);
                }
                //保存失败，抛异常
                if (!commit) {
                    LoggerUtil.LOG.error("ac_rollbackTx block_account tx commit error");
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
