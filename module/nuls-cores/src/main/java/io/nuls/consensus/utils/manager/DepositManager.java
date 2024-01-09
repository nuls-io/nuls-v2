package io.nuls.consensus.utils.manager;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.CancelDeposit;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.storage.DepositStorageService;
import io.nuls.consensus.utils.compare.DepositComparator;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 委托信息管理类，负责委托信息相关处理
 * Delegated information management category, responsible for delegated information related processing
 *
 * @author tag
 * 2018/12/5
 */
@Component
public class DepositManager {
    @Autowired
    private DepositStorageService depositStorageService;

    /**
     * 初始化委托信息
     * Initialize delegation information
     *
     * @param chain 链信息/chain info
     */
    public void loadDeposits(Chain chain) throws Exception {
        List<Deposit> allDepositList = new ArrayList<>();
        List<DepositPo> poList = depositStorageService.getList(chain.getConfig().getChainId());
        for (DepositPo po : poList) {
            Deposit deposit = poToDeposit(po);
            allDepositList.add(deposit);
        }
        Collections.sort(allDepositList, new DepositComparator());
        chain.setDepositList(allDepositList);
    }

    /**
     * 添加委托缓存
     * Add delegation cache
     *
     * @param chain   chain info
     * @param deposit deposit info
     */
    public void addDeposit(Chain chain, Deposit deposit) {
        chain.getDepositList().add(deposit);
    }

    /**
     * 修改委托缓存
     * modify delegation cache
     *
     * @param chain   chain
     * @param deposit deposit info
     */
    public void updateDeposit(Chain chain, Deposit deposit) {
        List<Deposit> depositList = chain.getDepositList();
        if (depositList.size() == 0) {
            depositList.add(deposit);
            return;
        }
        for (int index = 0; index < depositList.size(); index++) {
            if (deposit.getTxHash().equals(depositList.get(index).getTxHash())) {
                depositList.set(index, deposit);
                break;
            }
        }
    }

    /**
     * 删除指定链的委托信息
     * Delete delegate information for a specified chain
     *
     * @param chain  chain nfo
     * @param txHash 创建该委托交易的Hash/Hash to create the delegated transaction
     */
    public void removeDeposit(Chain chain, NulsHash txHash) throws Exception {
        List<Deposit> depositList = chain.getDepositList();
        if (depositList == null || depositList.size() == 0) {
            return;
        }
        for (Deposit deposit : depositList) {
            if (txHash.equals(deposit.getTxHash())) {
                depositList.remove(deposit);
                return;
            }
        }
    }

    /**
     * DepositPo to Deposit
     *
     * @param po DepositPo
     * @return Deposit
     */
    public Deposit poToDeposit(DepositPo po) {
        Deposit deposit = new Deposit();
        deposit.setDeposit(po.getDeposit());
        deposit.setAgentHash(po.getAgentHash());
        deposit.setTime(po.getTime());
        deposit.setDelHeight(po.getDelHeight());
        deposit.setBlockHeight(po.getBlockHeight());
        deposit.setAddress(po.getAddress());
        deposit.setTxHash(po.getTxHash());
        return deposit;
    }

    /**
     * DepositPo to Deposit
     *
     * @param deposit Deposit
     * @return DepositPo
     */
    public DepositPo depositToPo(Deposit deposit) {
        DepositPo po = new DepositPo();
        po.setTxHash(deposit.getTxHash());
        po.setAddress(deposit.getAddress());
        po.setAgentHash(deposit.getAgentHash());
        po.setBlockHeight(deposit.getBlockHeight());
        po.setDelHeight(deposit.getDelHeight());
        po.setDeposit(deposit.getDeposit());
        po.setTime(deposit.getTime());
        return po;
    }

    public boolean depositCommit(Transaction transaction, BlockHeader blockHeader, Chain chain) throws NulsException {
        Deposit deposit = new Deposit();
        deposit.parse(transaction.getTxData(), 0);
        deposit.setTxHash(transaction.getHash());
        deposit.setTime(transaction.getTime());
        deposit.setBlockHeight(blockHeader.getHeight());
        DepositPo depositPo = depositToPo(deposit);
        if (!depositStorageService.save(depositPo, chain.getConfig().getChainId())) {
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
        }
        addDeposit(chain, deposit);
        return true;
    }

    public boolean depositRollBack(Transaction transaction, Chain chain) throws NulsException {
        if (!depositStorageService.delete(transaction.getHash(), chain.getConfig().getChainId())) {
            throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }
        try {
            removeDeposit(chain, transaction.getHash());
        } catch (Exception e) {
            throw new NulsException(e);
        }

        return true;
    }

    public boolean cancelDepositCommit(Transaction transaction, BlockHeader header, Chain chain) throws NulsException {
        int chainId = chain.getConfig().getChainId();
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(transaction.getTxData(), 0);
        //获取该笔交易对应的加入共识委托交易
        DepositPo po = depositStorageService.get(cancelDeposit.getJoinTxHash(), chainId);
        //委托交易不存在
        if (po == null) {
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        //委托交易已退出
        if (po.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_WAS_CANCELED);
        }
        //设置退出共识高度
        po.setDelHeight(header.getHeight());
        if (!depositStorageService.save(po, chainId)) {
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
        }
        updateDeposit(chain, poToDeposit(po));
        return true;
    }

    public boolean cancelDepositRollBack(Transaction transaction, Chain chain, BlockHeader header) throws NulsException {
        int chainId = chain.getConfig().getChainId();
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(transaction.getTxData(), 0);
        //获取该笔交易对应的加入共识委托交易
        DepositPo po = depositStorageService.get(cancelDeposit.getJoinTxHash(), chainId);
        //委托交易不存在
        if (po == null) {
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        if (po.getDelHeight() != header.getHeight()) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_NEVER_CANCELED);
        }
        po.setDelHeight(-1L);
        if (!depositStorageService.save(po, chainId)) {
            throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }
        updateDeposit(chain, poToDeposit(po));
        return true;
    }
}
