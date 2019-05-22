/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.base.data;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小区块，用于新区块打包完成后进行广播，小区块中包含区块头、块中交易hash列表、打包过程中产生的交易（其他节点一定没有的交易）
 * Block block, used for broadcasting after the new block is packaged,
 * and the blocks in the block are included in the block header ,tx hash list of the block
 * and the transaction generated in the packaging process (other transactions that must not be made by other nodes).
 *
 * @author tag
 * 2018/11/21
 */
public class SmallBlock extends BaseNulsData {
    /**
     * 区块头
     * block header
     */
    private BlockHeader header;

    /**
     * 交易摘要列表
     * transaction hash list
     */
    private ArrayList<NulsHash> txHashList;

    /**
     * 系统交易列表（其他节点一定没有的交易，如共识奖励交易、红牌交易、黄牌交易）
     * Consensus trading list (transactions that no other node must have)
     */
    private List<Transaction> systemTxList = new ArrayList<>();

    public SmallBlock() {
    }

    @Override
    public int size() {
        int size = header.size();
        size += SerializeUtils.sizeOfVarInt(txHashList.size());
        for (NulsHash hash : txHashList) {
            size += SerializeUtils.sizeOfNulsData(hash);
        }
        size += SerializeUtils.sizeOfVarInt(systemTxList.size());
        for (Transaction tx : systemTxList) {
            size += SerializeUtils.sizeOfNulsData(tx);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(header);
        stream.writeVarInt(txHashList.size());
        for (NulsHash hash : txHashList) {
            stream.writeNulsData(hash);
        }
        stream.writeVarInt(systemTxList.size());
        for (Transaction tx : systemTxList) {
            stream.writeNulsData(tx);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = byteBuffer.readNulsData(new BlockHeader());

        this.txHashList = new ArrayList<>();
        long hashListSize = byteBuffer.readVarInt();
        for (int i = 0; i < hashListSize; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }

        this.systemTxList = new ArrayList<>();
        long subTxListSize = byteBuffer.readVarInt();
        for (int i = 0; i < subTxListSize; i++) {
            Transaction tx = byteBuffer.readTransaction();
            tx.setBlockHeight(header.getHeight());
            this.systemTxList.add(tx);
        }
    }

    /**
     * 区块头
     * block header
     *
     * @return BlockHeader
     */
    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

//    /**
//     * 交易摘要列表
//     * transaction hash list
//     */
    public ArrayList<NulsHash> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(ArrayList<NulsHash> txHashList) {
        this.txHashList = txHashList;
    }

//    /**
//     * 共识交易列表（其他节点一定没有的交易）
//     * Consensus trading list (transactions that no other node must have)
//     */
    public List<Transaction> getSystemTxList() {
        return systemTxList;
    }

    public void addSystemTx(Transaction tx) {
        this.systemTxList.add(tx);
    }

}
