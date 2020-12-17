package io.nuls.crosschain.nuls.message;

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
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CrossTxRehandleMessage;
import io.nuls.crosschain.base.utils.HashSetDuplicateProcessor;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.rpc.call.BlockCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.crosschain.nuls.utils.thread.CrossTxHandler;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020/9/11 11:19
 * @Description: 处理 跨链重发 消息。
 * 当跨链交易出现在本链已确认，但拜赞庭签名失败，切无法完成时，通过广播此消息实现在当前验证人列表的条件下重新完成拜赞庭签名。
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
        String messageHash;
        try {
            messageHash = HexUtil.encode(message.serialize());
        } catch (IOException e) {
            Log.error("解析消息CrossTxRehandleMessage消息发生异常");
            return ;
        }
        //如果没有处理过这个消息才处理
        if(processorOfTx.insertAndCheck(messageHash)){
            Chain chain = chainManager.getChainMap().get(chainId);
//            Map packerInfo = ConsensusCall.getPackerInfo(chain);
//            String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
//            if (!StringUtils.isBlank(address) && chain.getVerifierList().contains(address)) {
//                chain.getLogger().debug("不是共识节点，不处理跨链交易");
//                return ;
//            }
            String ctxHash = message.getCtxHash().toHex();
            Result<Transaction> tx = transferService.getConfirmedTxByHash(new GetConfirmedTxByHashReq(ctxHash));
            if(tx.isFailed()){
                chain.getLogger().error("处理【重新处理跨链交易拜赞庭签名】失败，ctx hash : [{}] 不是一个有效的交易hash",ctxHash);
                return ;
            }
            Transaction transaction = tx.getData();
            if(transaction.getType() != TxType.CROSS_CHAIN){
                chain.getLogger().error("处理【重新处理跨链交易拜赞庭签名】失败，ctx hash : [{}] 不是一个跨链交易",ctxHash);
                return ;
            }
            //检查本地是否已经处理完此消息，并且已经确认
            CtxStatusPO ctxStatusPO = ctxStatusService.get(message.getCtxHash(), chainId);
            if(ctxStatusPO != null){
                if(ctxStatusPO.getStatus() == TxStatusEnum.CONFIRMED.getStatus()){
                    chain.getLogger().info("该跨链转账交易之前已处理完成，将重新进行处理：{}",message.getCtxHash().toHex() );
                    ctxStatusPO.setStatus(TxStatusEnum.UNCONFIRM.getStatus());
                    ctxStatusService.save(message.getCtxHash(),ctxStatusPO, chainId);
                }
            }else{
                chain.getLogger().info("该跨链转账交易之前没有存储到待处理列表中，在ctx_status_po中存储此交易：{}",message.getCtxHash().toHex() );
                ctxStatusPO = new CtxStatusPO(transaction,TxStatusEnum.UNCONFIRM.getStatus());
                ctxStatusService.save(message.getCtxHash(),ctxStatusPO,chainId);
            }
            chain.getLogger().debug("对ctx:[{}]重新进行拜占庭签名验证", ctxHash);
            int syncStatus = BlockCall.getBlockStatus(chain);
            //发起拜占庭验证
            chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain,  tx.getData(), syncStatus));
        }
    }

}
