package io.nuls.block;

import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.nuls.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;

public class Spammer implements Runnable {

    public static Transaction tx;
    public static Address address;
    public static ECKey key;

    static {
        key = new ECKey();
        address = new Address(2, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
        Transaction tx = new Transaction();
        tx.setTime(1);
        CoinData coinData = new CoinData();
        List<CoinFrom> froms = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            CoinFrom from = new CoinFrom();
            from.setAssetsId(1);
            from.setAssetsChainId(1);
            from.setAmount(BigInteger.TEN);
            from.setLocked((byte) 0);
            from.setAddress(address.getAddressBytes());
            from.setNonce(getInitNonceByte());
            froms.add(from);
        }
        List<CoinTo> tos = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            CoinTo to = new CoinTo();
            to.setLockTime(0);
            to.setAssetsId(1);
            to.setAssetsChainId(1);
            to.setAmount(BigInteger.TEN);
            to.setAddress(address.getAddressBytes());
            tos.add(to);
        }
        coinData.setFrom(froms);
        coinData.setTo(tos);
        tx.setType(TxType.COIN_BASE);
        tx.setBlockHeight(1);
        try {
            tx.setCoinData(coinData.serialize());
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            byte[] signBytes = SignatureUtil.signDigest(tx.getHash().getBytes(), key).serialize();
            P2PHKSignature signature = new P2PHKSignature(signBytes, key.getPubKey());
            p2PHKSignatures.add(signature);
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getInitNonceByte() {
        return new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    }

    @Override
    public void run() {
        ChainContext context = ContextManager.getContext(2);
        while (true) {
            if (context.getStatus().equals(StatusEnum.RUNNING)) {
                SmallBlockMessage message = new SmallBlockMessage();
                try {
                    message.setSmallBlock(generate());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                NetworkUtil.broadcast(2, message, SMALL_BLOCK_MESSAGE);
                break;
            }
        }
    }

    private SmallBlock generate() throws IOException {
        ChainContext context = ContextManager.getContext(2);
        BlockHeader bestBlock = context.getLatestBlock().getHeader();
        long packageHeight = bestBlock.getHeight() + 1;
        BlockHeader newHeader = new BlockHeader();
        newHeader.setHeight(packageHeight);
        newHeader.setPreHash(bestBlock.getHash());
        newHeader.setTime(1);
        newHeader.setTxCount(1);
        ArrayList<NulsHash> txHashList = new ArrayList<>();
        txHashList.add(tx.getHash());
        newHeader.setMerkleHash(NulsHash.calcMerkleHash(txHashList));
//        newHeader.setStateRoot();
        BlockExtendsData extendsData = new BlockExtendsData();
        extendsData.setRoundIndex(1);
        extendsData.setConsensusMemberCount(1);
        extendsData.setPackingIndexOfRound(1);
        extendsData.setRoundStartTime(1);
        extendsData.setMainVersion((short) 1);
        extendsData.setBlockVersion((short) 1);
        extendsData.setEffectiveRatio((byte) 80);
        extendsData.setContinuousIntervalCount((short) 100);
        newHeader.setExtend(extendsData.serialize());
        SmallBlock newBlock = new SmallBlock();
        newBlock.addSystemTx(tx);
        newBlock.setHeader(newHeader);
        newBlock.setTxHashList(txHashList);
        return newBlock;
    }
}
