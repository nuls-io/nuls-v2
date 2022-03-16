package io.nuls.account.tx.v13;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.helper.AccountContractCallHelper;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.AccountContractCallData;
import io.nuls.account.model.po.AccountContractCallPO;
import io.nuls.account.storage.AccountForTransferOnContractCallStorageService;
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

@Component("AccountForTransferOnContractCallProcessorV13")
public class AccountForTransferOnContractCallProcessorV13 implements TransactionProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AccountContractCallHelper accountContractCallHelper;
    @Autowired
    private AccountForTransferOnContractCallStorageService accountForTransferOnContractCallStorageService;

    @Override
    public int getType() {
        return TxType.ACCOUNT_FOR_TRANSFER_ON_CONTRACT_CALL;
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
                    Result rs =  accountContractCallHelper.validate(chain, tx);
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
            AccountContractCallData data = new AccountContractCallData();
            try {
                data.parse(new NulsByteBuffer(tx.getTxData()));
                String[] addresses = data.getAddresses();
                int type = data.getType();
                if (type == 1) {
                    // 添加白名单
                    List<AccountContractCallPO> list = Arrays.asList(addresses).stream().map(a -> new AccountContractCallPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                    result = accountForTransferOnContractCallStorageService.saveAccountList(list);
                } else {
                    // type=2, 移除白名单
                    List<byte[]> list = Arrays.asList(addresses).stream().map(a -> AddressTool.getAddress(a)).collect(Collectors.toList());
                    result = accountForTransferOnContractCallStorageService.removeAccount(list);
                }
            } catch (Exception e) {
                LoggerUtil.LOG.error("AccountForTransferOnContractCall tx commit error", e);
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("AccountForTransferOnContractCall tx commit error");
                break;
            }
            commitSucTxList.add(tx);
        }
        try {
            //如果提交失败，将已经提交成功的交易回滚
            if (!result) {
                boolean rollback = true;
                for (Transaction tx : commitSucTxList) {
                    AccountContractCallData data = new AccountContractCallData();
                    data.parse(new NulsByteBuffer(tx.getTxData()));
                    String[] addresses = data.getAddresses();
                    int type = data.getType();
                    if (type == 1) {
                        List<byte[]> list = Arrays.asList(addresses).stream().map(a -> AddressTool.getAddress(a)).collect(Collectors.toList());
                        rollback = accountForTransferOnContractCallStorageService.removeAccount(list);
                    } else {
                        List<AccountContractCallPO> list = Arrays.asList(addresses).stream().map(a -> new AccountContractCallPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                        rollback = accountForTransferOnContractCallStorageService.saveAccountList(list);
                    }

                }
                //回滚失败，抛异常
                if (!rollback) {
                    LoggerUtil.LOG.error("AccountForTransferOnContractCall tx rollback error");
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
            AccountContractCallData data = new AccountContractCallData();
            try {
                data.parse(new NulsByteBuffer(tx.getTxData()));
                String[] addresses = data.getAddresses();
                int type = data.getType();
                if (type == 1) {
                    List<byte[]> list = Arrays.asList(addresses).stream().map(a -> AddressTool.getAddress(a)).collect(Collectors.toList());
                    result = accountForTransferOnContractCallStorageService.removeAccount(list);
                } else {
                    List<AccountContractCallPO> list = Arrays.asList(addresses).stream().map(a -> new AccountContractCallPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                    result = accountForTransferOnContractCallStorageService.saveAccountList(list);
                }

            } catch (Exception e) {
                LoggerUtil.LOG.error("AccountForTransferOnContractCall tx rollback error", e);
                result = false;
            }
            if (!result) {
                LoggerUtil.LOG.warn("AccountForTransferOnContractCall tx rollback error");
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
                    AccountContractCallData data = new AccountContractCallData();
                    data.parse(new NulsByteBuffer(tx.getTxData()));
                    String[] addresses = data.getAddresses();
                    int type = data.getType();
                    if (type == 1) {
                        List<AccountContractCallPO> list = Arrays.asList(addresses).stream().map(a -> new AccountContractCallPO(AddressTool.getAddress(a))).collect(Collectors.toList());
                        commit = accountForTransferOnContractCallStorageService.saveAccountList(list);
                    } else {
                        List<byte[]> list = Arrays.asList(addresses).stream().map(a -> AddressTool.getAddress(a)).collect(Collectors.toList());
                        commit = accountForTransferOnContractCallStorageService.removeAccount(list);
                    }

                }
                //保存失败，抛异常
                if (!commit) {
                    LoggerUtil.LOG.error("AccountForTransferOnContractCall tx commit error");
                    throw new NulsException(AccountErrorCode.ALIAS_SAVE_ERROR);
                }
            }
        } catch (Exception e) {
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
