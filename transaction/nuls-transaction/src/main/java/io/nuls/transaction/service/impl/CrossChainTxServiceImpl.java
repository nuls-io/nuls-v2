package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.data.LongUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxSignResult;
import io.nuls.transaction.rpc.call.ConsensusCall;
import io.nuls.transaction.service.CrossChainTxService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author: qinyifeng
 * @date: 2018/12/19
 */
@Service
public class CrossChainTxServiceImpl implements CrossChainTxService {

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;
    @Autowired
    private CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService;

    @Override
    public void newCrossTx(Chain chain, String nodeId, Transaction tx) {
        if (tx == null) {
            return;
        }
        int chainId = chain.getChainId();
        //判断是否存在
        CrossChainTx ctxExist = crossChainTxUnprocessedStorageService.getTx(chainId, tx.getHash());
        if(null != ctxExist){
            return;
        }
        ctxExist = crossChainTxStorageService.getTx(chainId, tx.getHash());
        if(null != ctxExist){
            return;
        }
        CrossChainTx ctx = new CrossChainTx();
        ctx.setTx(tx);
        ctx.setSenderChainId(chainId);
        ctx.setSenderNodeId(nodeId);
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        crossChainTxUnprocessedStorageService.putTx(chain.getChainId(), ctx);
    }

    @Override
    public boolean removeTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        return crossChainTxStorageService.removeTx(chain.getChainId(), hash);
    }

    @Override
    public CrossChainTx getTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return crossChainTxStorageService.getTx(chain.getChainId(), hash);
    }

    @Override
    public List<CrossChainTx> getTxList(Chain chain) {
        return crossChainTxStorageService.getTxList(chain.getChainId());
    }

    @Override
    public boolean updateCrossTxState(Chain chain, NulsDigestData hash, int state) {
        CrossChainTx crossChainTx = crossChainTxStorageService.getTx(chain.getChainId(), hash);
        if(null != crossChainTx){
            chain.getLogger().error(hash.getDigestHex() + TxErrorCode.TX_NOT_EXIST.getMsg());
            return false;
        }
        crossChainTx.setState(state);
        return crossChainTxStorageService.putTx(chain.getChainId(), crossChainTx);
    }

    @Override
    public void crossNodeResultProcess(Chain chain, String nodeId, BroadcastCrossNodeRsMessage message) throws NulsException {
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
        if (ctx == null) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        /*
         * 1.结果是否已收到过
         * 2.是否共识节点
         * 3.验证签名,验证签名中的公钥和共识节点地址是否匹配
         * 4.统计结果是否满足打包条件
         */
        List<CrossTxSignResult> signRsList = ctx.getSignRsList();
        for(CrossTxSignResult crossTxSignResult : signRsList){
            if(crossTxSignResult.getPackingAddress().equals(message.getPackingAddress())){
                //已收到过该结果,不再处理
                return;
            }
        }
        //验证结果签名正确性
        if(!verifyNodeResult(chain, message, ctx)){
            return;
        }

        CrossTxSignResult crossTxSignResult = new CrossTxSignResult();
        crossTxSignResult.setNodeId(nodeId);
        crossTxSignResult.setPackingAddress(message.getPackingAddress());
        crossTxSignResult.setSignature(message.getSignature());
        signRsList.add(crossTxSignResult);
        crossChainTxStorageService.putTx(chain.getChainId(), ctx);
        /*
            1.如果是主网 当一个交易的签名者超过共识节点总数的80%，则通过
            2.如果是友链 如果交易的签名者是友链最近x块的出块者
        */
        List<String> agentAddrs = null;
        if (chain.getChainId() == TxConstant.NULS_CHAINID) {
            //主网
            agentAddrs = ConsensusCall.getAgentAddressList(chain);
        }else{
            //如果是友链,需满足签名者达到了最近N块的出块者的M%时则通过
            agentAddrs =  ConsensusCall.getRecentPackagerAddress(chain, TxConstant.RECENT_PACKAGER_THRESHOLD);
        }
        boolean isPass = isNodePass(agentAddrs, signRsList);
        if(!isPass){
            return;
        }
        //加入待打包
    }

    /**
     * 根据总数, 通过数, 达成通过条件百分比,验证结果是否应达成通过条件
     * @param agentAddrs 总数
     * @param signRsList 收到的结果
     * @return boolean
     */
    private boolean isNodePass(List<String> agentAddrs, List<CrossTxSignResult> signRsList){
        int signRsCount = 0;
        //从收到结果中提取出在当前的共识节点出块地址集合中的地址,加入通过率计算
        for(String agentAddr : agentAddrs){
            for(CrossTxSignResult ctxRs : signRsList) {
                if (ctxRs.equals(agentAddr)) {
                    signRsCount++;
                }
            }
        }
        BigDecimal rs = new BigDecimal(Integer.toString(signRsCount));
        BigDecimal agents = new BigDecimal(Integer.toString(agentAddrs.size()));
        BigDecimal passRate = new BigDecimal(TxConstant.CHAIN_NODES_RESULT_PASS_RATE);
        if (rs.compareTo(agents.multiply(passRate)) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * 验证主网共识节点签名结果的正确性
     * 包括发送结果的是否是有效共识节点, 签名数据和节点地址匹配, 签名正确性
     * @param chain
     * @param message
     * @return
     */
    private boolean verifyNodeResult(Chain chain, BroadcastCrossNodeRsMessage message, CrossChainTx ctx){
        String agentAddress = message.getPackingAddress();
        if(chain.getChainId() == TxConstant.NULS_CHAINID && !ConsensusCall.isConsensusNode(chain, agentAddress)){
            return false;
        }
        P2PHKSignature signature = message.getSignature();
        int addrChainId = AddressTool.getChainIdByAddress(agentAddress);
        byte[] addrbytes = AddressTool.getAddress(signature.getPublicKey(), addrChainId);
        if(!Arrays.equals(addrbytes, AddressTool.getAddress(agentAddress))){
            return false;
        }
        boolean verifySignature = false;
        try {
            verifySignature = SignatureUtil.validateSignture(ctx.getTx().getHash().getDigestBytes(), signature);
        } catch (NulsException e) {
            e.printStackTrace();
            return false;
        }
        return verifySignature;

    }
}
