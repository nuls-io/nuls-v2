package io.nuls.crosschain.nuls.utils.thread;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VerifierChangeTxHandler implements Runnable {
    private Chain chain;
    private Transaction transaction;
    private long height;

    public VerifierChangeTxHandler(Chain chain, Transaction transaction,long height) {
        this.chain = chain;
        this.transaction = transaction;
        this.height = height;
    }

    @Override
    public void run() {
        /*
         * 1.如果为提交时
         *   1.1.如果提交交易与缓存中正在处理交易不是同一笔交易，则返回
         *   1.2.更新本地验证人列表,保存交易广播高度，等待广播
         *   1.3.清除正在处理交易标志
         * 2.如果为轮次变更时（交易刚创建）
         *   2.1.判断是否有正在处理的验证人变更交易，如果有则合并
         *   2.2.判断当前当前突出的节点数量是否大于30%的共识节点数量，如果大于30%则拆分交易
         *   2.3.签名广播
         * */
        TxUtil.verifierChangeWait(chain, height);
        boolean result = true;
        VerifierChangeData txData = new VerifierChangeData();
        boolean txChanged = false;
        try {
            txData.parse(transaction.getTxData(), 0);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return;
        }
        do {
            Transaction processTx = chain.isExistVerifierChangeTx(transaction);
            if(processTx != null){
                VerifierChangeData processTxData = new VerifierChangeData();
                try {
                    processTxData.parse(processTx.getTxData(), 0);
                } catch (NulsException e) {
                    chain.getLogger().error(e);
                    return;
                }
                //如果当前正在处理交易退出的验证人大于等于当前验证人列表的30%则无需合并，等待正在处理的交易处理完成
                if (processTxData.getCancelAgentList() != null && (processTxData.getCancelAgentList().size() + 1) > chain.getVerifierList().size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        chain.getLogger().error(e);
                    }
                    chain.getLogger().info("The number of exit nodes of the currently processing verifier change transaction is greater than or equal to 30%, and it needs to wait for the completion of the transaction processing");
                    result = false;
                } else {
                    txData = mergeVerifierChangeTx(txData, processTxData);
                    txChanged = true;
                }
            }
        } while (!result);
        //判断交易是否需要拆分及处理
        verifierSplitHandle(chain, transaction, height, txData, txChanged);
    }

    /**
     * 如果当前有正在处理的验证人变更交易，则需要合并两个验证人变更交易
     * If there is currently a verifier change transaction being processed, two verifier change transactions need to be consolidated
     */
    private VerifierChangeData mergeVerifierChangeTx(VerifierChangeData txData, VerifierChangeData processTxData) {
        VerifierChangeData mergeTxData = new VerifierChangeData();
        /*
         * 1.合并新增验证人，去重排序
         * 2.合并注销的验证人，去重排序
         * */
        Set<String> appendSet = null;
        if (txData.getRegisterAgentList() != null && !txData.getRegisterAgentList().isEmpty()) {
            appendSet = new HashSet<>(txData.getRegisterAgentList());
            chain.getLogger().info("New witness list for current transaction：{}", txData.getRegisterAgentList());
        }
        if (processTxData.getRegisterAgentList() != null && !processTxData.getRegisterAgentList().isEmpty()) {
            if (appendSet != null) {
                appendSet.addAll(processTxData.getRegisterAgentList());
            } else {
                appendSet = new HashSet<>(processTxData.getRegisterAgentList());
            }
            chain.getLogger().info("List of new examiners in processing transactions:{}", processTxData.getRegisterAgentList());
        }
        if (appendSet != null && !appendSet.isEmpty()) {
            mergeTxData.setRegisterAgentList(new ArrayList<>(appendSet));
            mergeTxData.getRegisterAgentList().sort(Comparator.naturalOrder());
            chain.getLogger().info("New witness list for transactions after merger:{}", mergeTxData.getRegisterAgentList());
        }

        Set<String> reduceSet = null;
        if (txData.getCancelAgentList() != null && !txData.getCancelAgentList().isEmpty()) {
            reduceSet = new HashSet<>(txData.getCancelAgentList());
            chain.getLogger().info("The list of validators for the reduction of transactions in process is:{}", txData.getCancelAgentList());
        }
        if (processTxData.getCancelAgentList() != null && !processTxData.getCancelAgentList().isEmpty()) {
            if (reduceSet != null) {
                reduceSet.addAll(processTxData.getCancelAgentList());
            } else {
                reduceSet = new HashSet<>(processTxData.getCancelAgentList());
            }
            chain.getLogger().info("The list of verifiers for current transaction decrease is:{}", processTxData.getCancelAgentList());
        }
        if (reduceSet != null && !reduceSet.isEmpty()) {
            mergeTxData.setCancelAgentList(new ArrayList<>(reduceSet));
            mergeTxData.getCancelAgentList().sort(Comparator.naturalOrder());
            chain.getLogger().info("Reduced verifier list of transactions after consolidation:{}", mergeTxData.getCancelAgentList());
        }
        return mergeTxData;
    }

    /**
     * 判断当前验证人变更交易是否需要拆分，如果退出的节点数大于等于当前节点数的30%则需要拆分
     * Judge whether the current verifier's change transaction needs to be split. If the number of exiting nodes is greater than or equal to 30% of the current number of nodes, it needs to be split
     */
    private void verifierSplitHandle(Chain chain, Transaction transaction,long height, VerifierChangeData txData, boolean txChanged){
        boolean needSplit = false;
        int maxCount = 0;
        int cancelCount = 0;
        if(txData.getCancelAgentList() != null && !txData.getCancelAgentList().isEmpty() && txData.getCancelAgentList().size() > 1){
            maxCount = chain.getVerifierList().size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100;
            cancelCount = txData.getCancelAgentList().size();
            if(txData.getCancelAgentList().size() > maxCount){
                needSplit = true;
            }
        }
        if(needSplit){
            //如果交易已变更则前面已排过序,验证人排序，然后拆分退出验证人列表
            if(!txChanged){
                txData.getCancelAgentList().sort(Comparator.naturalOrder());
            }
            List<String> firstCancelList = txData.getCancelAgentList().subList(0, maxCount);
            List<String> secondCancelList = txData.getCancelAgentList().subList(maxCount, cancelCount);
            try {
                //第一笔交易优先处理，高度为当前高度，第二笔交易高度为当前高度之后2两个高度，避免广播时同时广播的情况
                Transaction firstTx = TxUtil.createVerifierChangeTx(txData.getRegisterAgentList(), firstCancelList, transaction.getTime(), chain.getChainId());
                Transaction secondTx = TxUtil.createVerifierChangeTx(new ArrayList<>(), secondCancelList, transaction.getTime(), chain.getChainId());
                chain.getLogger().info("The exit node of the transaction changed by the verifier is greater than 30% of the current node, which is divided into two transactions,firstTx:{},secondTx:{}",firstTx.getHash().toHex(),secondTx.getHash().toHex());
                chain.getCrossTxThreadPool().execute(new VerifierChangeTxHandler(chain, firstTx, height));
                try {
                    TimeUnit.SECONDS.sleep(1);
                }catch (InterruptedException e){
                    chain.getLogger().error(e);
                }
                chain.getCrossTxThreadPool().execute(new VerifierChangeTxHandler(chain, secondTx, height + 2));
            }catch (IOException e){
                chain.getLogger().error(e);
            }
        }else{
            try {
                transaction.setTxData(txData.serialize());
            }catch (IOException e){
                chain.getLogger().error(e);
                return;
            }
            TxUtil.handleNewCtx(transaction, chain, txData.getCancelAgentList());
        }
    }
}