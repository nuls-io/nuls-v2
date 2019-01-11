package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.BroadcastCrossTxHashMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.message.base.BaseMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxSignResult;
import io.nuls.transaction.model.bo.CrossTxVerifyResult;
import io.nuls.transaction.rpc.call.AccountCall;
import io.nuls.transaction.rpc.call.ConsensusCall;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.CrossChainTxService;
import io.nuls.transaction.utils.TxUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: qinyifeng
 * @date: 2018/12/19
 */
@Service
public class CrossChainTxServiceImpl implements CrossChainTxService {

    private final Lock CTX_LOCK = new ReentrantLock();

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;

    @Autowired
    private CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService;

    @Autowired
    private TxVerifiedPool txVerifiedPool;

    @Autowired
    private TxVerifiedStorageService txVerifiedStorageService;

    @Autowired
    private TransactionH2Service transactionH2Service;

    @Override
    public void newCrossTx(Chain chain, String nodeId, Transaction tx) {
        if (tx == null) {
            return;
        }
        int chainId = chain.getChainId();
        //判断是否存在
        CrossChainTx ctxExist = crossChainTxUnprocessedStorageService.getTx(chainId, tx.getHash());
        if (null != ctxExist) {
            return;
        }
        ctxExist = crossChainTxStorageService.getTx(chainId, tx.getHash());
        if (null != ctxExist) {
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
        if (null != crossChainTx) {
            chain.getLogger().error(hash.getDigestHex() + TxErrorCode.TX_NOT_EXIST.getMsg());
            return false;
        }
        crossChainTx.setState(state);
        return crossChainTxStorageService.putTx(chain.getChainId(), crossChainTx);
    }

    /**
     * 接收链内其他节点广播的跨链验证结果, 并保存.
     * 1.如果是主网 当一个交易的签名者超过共识节点总数的80%，则通过
     * 2.如果是友链 如果交易的签名者是友链最近x块的出块者
     * @param chain
     * @param nodeId
     * @param message
     * @throws NulsException
     */
    private void crossNodeResultProcess(Chain chain, String nodeId, BroadcastCrossNodeRsMessage message) throws NulsException {
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
        if (ctx == null) {
            //去要交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setRequestHash(message.getRequestHash());
            // todo
            getTxMessage.setCommand(TxCmd.NW_ASK_CROSS_TX_M_M);
            NetworkCall.sendToNode(chain.getChainId(), getTxMessage, nodeId);
            return;
        }
        /*
         * 1.结果是否已收到过
         * 2.是否共识节点
         * 3.验证签名,验证签名中的公钥和共识节点地址是否匹配
         * 4.统计结果是否满足打包条件
         */
        List<CrossTxSignResult> signRsList = ctx.getSignRsList();
        for (CrossTxSignResult crossTxSignResult : signRsList) {
            if (crossTxSignResult.getPackingAddress().equals(message.getPackingAddress())) {
                //已收到过该结果,不再处理
                return;
            }
        }
        //验证结果签名正确性
        if (!verifyNodeResult(chain, message, ctx)) {
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
        } else {
            //如果是友链,需满足签名者达到了最近N块的出块者的M%时则通过
            agentAddrs = ConsensusCall.getRecentPackagerAddress(chain, TxConstant.RECENT_PACKAGER_THRESHOLD);
        }
        boolean isPass = isNodePass(agentAddrs, signRsList);
        if (!isPass) {
            return;
        }
        Transaction tx = ctx.getTx();
        //加入待打包
        txVerifiedPool.add(chain, tx,false);
        //保存到rocksdb
        txVerifiedStorageService.putTx(chain.getChainId(),tx);
        //保存到h2数据库
        transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
        //调账本记录未确认交易
        LedgerCall.commitTxLedger(chain, tx, false);
        //广播交易hash
        BroadcastCrossTxHashMessage ctxHashMessage = new BroadcastCrossTxHashMessage();
        ctxHashMessage.setCommand(TxCmd.NW_NEW_CROSS_HASH);
        ctxHashMessage.setRequestHash(tx.getHash());
        NetworkCall.broadcast(chain.getChainId(), ctxHashMessage);
    }


    /**
     * 根据总数, 通过数, 达成通过条件百分比,验证结果是否应达成通过条件
     *
     * @param agentAddrs 总数
     * @param signRsList 收到的结果
     * @return boolean
     */
    private boolean isNodePass(List<String> agentAddrs, List<CrossTxSignResult> signRsList) {
        int signRsCount = 0;
        //从收到结果中提取出在当前的共识节点出块地址集合中的地址,加入通过率计算
        for (String agentAddr : agentAddrs) {
            for (CrossTxSignResult ctxRs : signRsList) {
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


    @Override
    public void ctxResultProcess(Chain chain, BaseMessage message, String nodeId) throws NulsException {
        CTX_LOCK.lock();
        if (message instanceof VerifyCrossResultMessage) {
            //处理跨链节点验证结果
            verifyCrossResultProcess(chain, nodeId, (VerifyCrossResultMessage) message);
        } else if (message instanceof BroadcastCrossNodeRsMessage) {
            //处理链内节点验证结果
            crossNodeResultProcess(chain, nodeId, (BroadcastCrossNodeRsMessage) message);
        }
        CTX_LOCK.unlock();
    }

    /**
     * 验证主网共识节点签名结果的正确性
     * 包括发送结果的是否是有效共识节点, 签名数据和节点地址匹配, 签名正确性
     *
     * @param chain
     * @param message
     * @return
     */
    private boolean verifyNodeResult(Chain chain, BroadcastCrossNodeRsMessage message, CrossChainTx ctx) throws NulsException {
        String agentAddress = message.getPackingAddress();
        if (chain.getChainId() == TxConstant.NULS_CHAINID && !ConsensusCall.isConsensusNode(chain, agentAddress)) {
            return false;
        }
        P2PHKSignature signature = message.getSignature();
        int addrChainId = AddressTool.getChainIdByAddress(agentAddress);
        byte[] addrbytes = AddressTool.getAddress(signature.getPublicKey(), addrChainId);
        if (!Arrays.equals(addrbytes, AddressTool.getAddress(agentAddress))) {
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

    /**
     * 处理跨链节点验证结果
     *
     * @param chain
     * @param nodeId
     * @param message
     * @return
     * @throws NulsException
     */
    private void verifyCrossResultProcess(Chain chain, String nodeId, VerifyCrossResultMessage message) throws NulsException {
        //查询处理中的跨链交易
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
        if (ctx == null) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        //获取跨链交易验证结果
        List<CrossTxVerifyResult> verifyResultList = ctx.getCtxVerifyResultList();
        if (verifyResultList == null) {
            verifyResultList = new ArrayList<>();
        }
        //添加新的跨链验证结果
        CrossTxVerifyResult verifyResult = new CrossTxVerifyResult();
        verifyResult.setChainId(chain.getChainId());
        verifyResult.setNodeId(nodeId);
        verifyResult.setHeight(message.getHeight());
        verifyResultList.add(verifyResult);
        ctx.setCtxVerifyResultList(verifyResultList);
        //TODO 获取共识节点的节点地址
        String packingAddress = ConsensusCall.cs_getNodePackingAddress(chain);
        //判断当前节点是共识节点还是普通节点
        if (StringUtils.isNotBlank(packingAddress)) {
            //共识节点
            BigDecimal rs = new BigDecimal(Integer.toString(ctx.getCtxVerifyResultList().size()));
            BigDecimal agents = new BigDecimal(Integer.toString(ctx.getVerifyNodeList().size()));
            BigDecimal passRate = new BigDecimal(TxConstant.CROSS_VERIFY_RESULT_PASS_RATE);
            //超过全部链接节点51%的节点验证通过,则节点判定交易的验证通过
            if (rs.compareTo(agents.multiply(passRate)) >= 0) {
                //使用该地址到账户模块对跨链交易atx_trans_hash签名
                P2PHKSignature signature = AccountCall.signDigest(packingAddress, null, message.getRequestHash().getDigestHex());
                BroadcastCrossNodeRsMessage rsMessage = new BroadcastCrossNodeRsMessage();
                rsMessage.setCommand(TxCmd.NW_CROSS_NODE_RS);
                rsMessage.setRequestHash(message.getRequestHash());
                rsMessage.setSignature(signature);
                rsMessage.setPackingAddress(packingAddress);
                //广播交易hash
                NetworkCall.broadcast(chain.getChainId(), rsMessage);
                ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
            }
        } else {
            //普通节点
            if (verifyResultList.size() >= 3) {
                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(), message.getRequestHash());
                ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
            }
        }

        //保存跨链交易验证结果
        crossChainTxStorageService.putTx(chain.getChainId(), ctx);
    }
}
