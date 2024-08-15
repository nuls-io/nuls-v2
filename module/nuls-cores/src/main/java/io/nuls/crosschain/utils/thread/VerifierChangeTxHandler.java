package io.nuls.crosschain.utils.thread;

import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.TxUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VerifierChangeTxHandler implements Runnable {
    private Chain chain;
    private Transaction transaction;
    private long height;

    public VerifierChangeTxHandler(Chain chain, Transaction transaction, long height) {
        this.chain = chain;
        this.transaction = transaction;
        this.height = height;
        chain.getLogger().info("TxHash: {}", transaction.getHash().toHex());
    }

    @Override
    public void run() {
        /*
         * 1.If it is at the time of submission
         *   1.1.If the submitted transaction is not the same as the transaction being processed in the cache, return
         *   1.2.Update local validator list,Save transaction broadcast height and wait for broadcast
         *   1.3.Clear processing transaction flag
         * 2.If it is a round change（The transaction has just been created）
         *   2.1.Determine if there are any validator change transactions currently being processed, and if so, merge them
         *   2.2.Determine if the current number of prominent nodes is greater than30%The number of consensus nodes, if greater than30%Split the transaction
         *   2.3.Signature Broadcast
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
            if (processTx != null) {
                VerifierChangeData processTxData = new VerifierChangeData();
                try {
                    processTxData.parse(processTx.getTxData(), 0);
                } catch (NulsException e) {
                    chain.getLogger().error(e);
                    return;
                }
                //If the number of validators currently processing transaction exits is greater than or equal to the current list of validators30%Then there is no need to merge and wait for the transaction being processed to complete
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
        //Determine whether the transaction needs to be split and processed
        verifierSplitHandle(chain, transaction, height, txData, txChanged);
    }

    /**
     * If there are currently validator change transactions being processed, it is necessary to merge two validator change transactions
     * If there is currently a verifier change transaction being processed, two verifier change transactions need to be consolidated
     */
    private VerifierChangeData mergeVerifierChangeTx(VerifierChangeData txData, VerifierChangeData processTxData) {
        VerifierChangeData mergeTxData = new VerifierChangeData();
        /*
         * 1.Merge and add validators, remove reordering
         * 2.Merge and cancel validators, remove reordering
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
     * Determine whether the current validator change transaction needs to be split. If the number of exiting nodes is greater than or equal to the current number of nodes30%Then it needs to be split
     * Judge whether the current verifier's change transaction needs to be split. If the number of exiting nodes is greater than or equal to 30% of the current number of nodes, it needs to be split
     */
    private void verifierSplitHandle(Chain chain, Transaction transaction, long height, VerifierChangeData txData, boolean txChanged) {
        boolean needSplit = false;
        int maxCount = 0;
        int cancelCount = 0;
        if (txData.getCancelAgentList() != null && !txData.getCancelAgentList().isEmpty() && txData.getCancelAgentList().size() > 1) {
            maxCount = chain.getVerifierList().size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100;
            cancelCount = txData.getCancelAgentList().size();
            if (txData.getCancelAgentList().size() > maxCount) {
                needSplit = true;
            }
        }
        if (needSplit) {
            //If the transaction has changed, it has already been sorted before,Sort the validators, then split and exit the validator list
            if (!txChanged) {
                txData.getCancelAgentList().sort(Comparator.naturalOrder());
            }
            List<String> firstCancelList = txData.getCancelAgentList().subList(0, maxCount);
            List<String> secondCancelList = txData.getCancelAgentList().subList(maxCount, cancelCount);
            try {
                //The first transaction is prioritized, with a height of the current height, and the second transaction height is after the current height2Two heights to avoid simultaneous broadcasting during broadcasting
                Transaction firstTx = TxUtil.createVerifierChangeTx(txData.getRegisterAgentList(), firstCancelList, transaction.getTime(), chain.getChainId());
                Transaction secondTx = TxUtil.createVerifierChangeTx(new ArrayList<>(), secondCancelList, transaction.getTime(), chain.getChainId());
                chain.getLogger().info("The exit node of the transaction changed by the verifier is greater than 30% of the current node, which is divided into two transactions,firstTx:{},secondTx:{}", firstTx.getHash().toHex(), secondTx.getHash().toHex());
                chain.getCrossTxThreadPool().execute(new VerifierChangeTxHandler(chain, firstTx, height));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    chain.getLogger().error(e);
                }
                chain.getCrossTxThreadPool().execute(new VerifierChangeTxHandler(chain, secondTx, height + 2));
            } catch (IOException e) {
                chain.getLogger().error(e);
            }
        } else {
            try {
                transaction.setTxData(txData.serialize());
            } catch (IOException e) {
                chain.getLogger().error(e);
                return;
            }
            TxUtil.handleNewCtx(transaction, chain, txData.getCancelAgentList());
        }
    }
}
