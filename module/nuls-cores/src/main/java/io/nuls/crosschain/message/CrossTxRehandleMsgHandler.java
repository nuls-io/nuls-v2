package io.nuls.crosschain.message;

import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CrossTxRehandleMessage;
import io.nuls.crosschain.base.utils.HashSetDuplicateProcessor;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.BlockCall;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.utils.manager.ChainManager;
import io.nuls.crosschain.utils.thread.CrossTxHandler;

import java.io.IOException;

/**
 * @Author: zhoulijun
 * @Time: 2020/9/11 11:19
 * @Description: handle Cross chain retransmission Message.
 * When a cross chain transaction appears on this chain that has been confirmed but failed to be signed by the Byzantine Court and cannot be completed, broadcast this message to complete the Byzantine Court signature again under the current verifier list conditions.
 */
@Component("CrossTxRehandleMsgHandlerV1")
public class CrossTxRehandleMsgHandler implements MessageProcessor {

    private static HashSetDuplicateProcessor processorOfTx = new HashSetDuplicateProcessor(1000);

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private ChainManager chainManager;

    TransferService transferService = ServiceManager.get(TransferService.class);

    @Override
    public String getCmd() {
        return CommandConstant.CROSS_TX_REHANDLE_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String messageStr) {
        CrossTxRehandleMessage message = RPCUtil.getInstanceRpcStr(messageStr, CrossTxRehandleMessage.class);
        process(chainId,message);
    }

    public void process(int chainId, CrossTxRehandleMessage message){
        String messageHash;
        try {
            messageHash = HexUtil.encode(message.serialize());
        } catch (IOException e) {
            Log.error("Parsing messagesCrossTxRehandleMessageMessage exception occurred");
            return ;
        }
        //If this message has not been processed before, it will be processed
        if(processorOfTx.insertAndCheck(messageHash)){
            Chain chain = chainManager.getChainMap().get(chainId);
//            Map packerInfo = ConsensusCall.getPackerInfo(chain);
//            String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
//            if (!StringUtils.isBlank(address) && chain.getVerifierList().contains(address)) {
//                chain.getLogger().debug("Not a consensus node, do not handle cross chain transactions");
//                return ;
//            }
            String ctxHash = message.getCtxHash().toHex();
            Result<Transaction> tx = transferService.getConfirmedTxByHash(new GetConfirmedTxByHashReq(ctxHash));
            if(tx.isFailed()){
                chain.getLogger().error("handle【Reprocess cross chain transactions with Byzantine signatures】Failed,ctx hash : [{}] Not a valid transactionhash",ctxHash);
                return ;
            }
            Transaction transaction = tx.getData();
            if(transaction.getType() != TxType.CROSS_CHAIN && transaction.getType() != TxType.CONTRACT_TOKEN_CROSS_TRANSFER){
                chain.getLogger().error("handle【Reprocess cross chain transactions with Byzantine signatures】Failed,ctx hash : [{}] Not a cross chain transaction",ctxHash);
                return ;
            }
            //Check if this message has been processed locally and confirmed
            CtxStatusPO ctxStatusPO = ctxStatusService.get(message.getCtxHash(), chainId);
            if(ctxStatusPO != null){
                if(ctxStatusPO.getStatus() == TxStatusEnum.CONFIRMED.getStatus()){
                    chain.getLogger().info("The cross chain transfer transaction has been processed before and will be reprocessed：{}",message.getCtxHash().toHex() );
                    ctxStatusPO.setStatus(TxStatusEnum.UNCONFIRM.getStatus());
                    ctxStatusService.save(message.getCtxHash(),ctxStatusPO, chainId);
                }
            }else{
                chain.getLogger().info("The cross chain transfer transaction was not stored in the pending list beforectx_status_poStore this transaction in the middle：{}",message.getCtxHash().toHex() );
                ctxStatusPO = new CtxStatusPO(transaction,TxStatusEnum.UNCONFIRM.getStatus());
                ctxStatusService.save(message.getCtxHash(),ctxStatusPO,chainId);
            }
            chain.getLogger().debug("rightctx:[{}]Perform Byzantine signature verification again", ctxHash);
            int syncStatus = BlockCall.getBlockStatus(chain);
            //Initiate Byzantine verification
            chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain,  tx.getData(), syncStatus));
        }
    }


}
