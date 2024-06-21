package io.nuls.crosschain.utils.thread.handler;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.crosschain.base.message.CtxFullSignMessage;
import io.nuls.crosschain.base.service.ResetLocalVerifierService;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.ConsensusCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.utils.CommonUtil;
import io.nuls.crosschain.utils.TxUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @description TODO
 * @date 2024/6/18 14:55
 * @COPYRIGHT nabox.io
 */
public class SaveCtxFullSignHandler implements Runnable {

    private Chain chain;

    private CtxStatusService ctxStatusService;

    private ResetLocalVerifierService resetLocalVerifierService;

    private ConvertCtxService convertCtxService;

    private NulsCoresConfig config;

    public SaveCtxFullSignHandler(
            Chain chain,
            NulsCoresConfig config,
            CtxStatusService ctxStatusService,
            ResetLocalVerifierService resetLocalVerifierService,
            ConvertCtxService convertCtxService) {
        this.ctxStatusService = ctxStatusService;
        this.resetLocalVerifierService = resetLocalVerifierService;
        this.config = config;
        this.chain = chain;
        this.convertCtxService = convertCtxService;
    }

    @Override
    public void run() {
        while (!chain.getTxFullSignMessageQueue().isEmpty()) {
            try {
                UntreatedMessage untreatedMessage = chain.getTxFullSignMessageQueue().take();
                CtxFullSignMessage message = (CtxFullSignMessage) untreatedMessage.getMessage();
                chain.getLogger().debug("Start storing cross-chain transaction signatures pushed by other nodes: localTxHash:{},sign:{}"
                        , message.getLocalTxHash().toHex()
                        , HexUtil.encode(message.getTransactionSignature())
                );

                CtxStatusPO ctxStatusPO = ctxStatusService.get(message.getLocalTxHash(), chain.getChainId());
                if (ctxStatusPO == null) {
                    chain.getLogger().warn("No corresponding cross-chain transaction was found locally:{}", message.getLocalTxHash().toHex());
                    continue;
                }

                Transaction tx = ctxStatusPO.getTx();
                List<String> packAddressList = getPackingAddressList(tx, tx.getHash(), chain);

                chain.getLogger().debug("Gets the number of validators address listed:{}",packAddressList.size());

                TransactionSignature localTxSign = new TransactionSignature();
                localTxSign.parse(tx.getTransactionSignature(),0);
                chain.getLogger().debug("Gets the number of signatures for local cross-chain transactions:{}",localTxSign.getP2PHKSignatures().size());

                TransactionSignature messageTxSign = new TransactionSignature();
                messageTxSign.parse(message.getTransactionSignature(),0);
                chain.getLogger().debug("Gets the number of signatures contained in the message:{}",messageTxSign.getP2PHKSignatures().size());

                Transaction convertCtx = ctxStatusPO.getTx();
                if (!config.isMainNet() && convertCtx.getType() == config.getCrossCtxType()) {
                    convertCtx = convertCtxService.get(message.getLocalTxHash(), chain.getChainId());
                }
                chain.getLogger().debug("Signature verification hash:{}",convertCtx.getHash().toHex());
                int signVerifyPassNumber = 0;
                for (P2PHKSignature sign: messageTxSign.getP2PHKSignatures()
                     ) {
                    if (ECKey.verify(convertCtx.getHash().getBytes(), sign.getSignData().getSignBytes(), sign.getPublicKey())) {
                        localTxSign.getP2PHKSignatures().add(sign);
                        signVerifyPassNumber++;
                    }
                }
                chain.getLogger().debug("Result of verifying the signature in the message,total:{},pass:{}",messageTxSign.getP2PHKSignatures().size(),signVerifyPassNumber);
                localTxSign.setP2PHKSignatures(CommonUtil.getMisMatchSigns(chain, localTxSign, packAddressList));
                chain.getLogger().debug("Number of transaction signatures after the merger:{}",localTxSign.getP2PHKSignatures().size());

                tx.setTransactionSignature(localTxSign.serialize());

                ctxStatusPO.setTx(tx);

                ctxStatusService.save(message.getLocalTxHash(),ctxStatusPO,chain.getChainId());
                chain.getLogger().debug("Save transaction transaction signature:{}",untreatedMessage.getCacheHash().toHex());
            } catch (Exception e) {
                chain.getLogger().error("An error occurred processing the full signature pushed by another node",e);
            }
        }
    }

    private List<String> getPackingAddressList(Transaction ctx, NulsHash realHash, Chain chain) {
        List<String> packAddressList;
        //Byzantine signature saturation increase 0To avoid floating upwards
        Float signCountOverflow = 0F;
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            String txHash = realHash.toHex();
            //This is a special initialization validator transaction, where the user resets the main network validator list stored on the parallel chain
            if (resetLocalVerifierService.isResetOtherVerifierTx(txHash)) {
                packAddressList = chain.getVerifierList();
                //1To float up to all
                signCountOverflow = 1F;
            } else {
                packAddressList = (List<String>) ConsensusCall.getSeedNodeList(chain).get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
            }
        } else {
            packAddressList = chain.getVerifierList();
        }
        return packAddressList;
    }


}
