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
package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 封装进入跨链过程后的跨链交易,同时保存验证过程中的状态和数据的模型
 * @author: Charlie
 * @date: 2018/11/13
 */
public class CrossTx extends BaseNulsData {

    /** 该跨链交易在本链中的验证状态 */
    private int state;

    /** 交易的来源节点, 用于经跨链验证时需要排除的节点*/
    private int senderChainId;

    /**
     * 可能为空？如果是同链节点转发？
     */
    private String senderNodeId;

    /** 跨链交易在当前链生效的高度(交易的block高度 + 阈值高度)*/
    private long height = -1L;

    /** 交易 */
    private Transaction tx;

    /**
     * 跨链验证节点列表
     */
    private List<Node> verifyNodeList = new ArrayList<>();

    /**
     * 收到的跨链节点验证结果
     */
    private List<CrossTxVerifyResult> ctxVerifyResultList = new ArrayList<>();

    /**
     * 收到的本链节点验证结果(签名)
     */
    private List<CrossTxSignResult> signRsList = new ArrayList<>();

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(state);
        stream.writeUint16(senderChainId);
        stream.writeString(senderNodeId);
        stream.writeVarInt(height);
        stream.writeNulsData(tx);
        stream.writeVarInt(verifyNodeList.size());
        for(Node node : verifyNodeList){
            stream.writeNulsData(node);
        }
        stream.writeVarInt(ctxVerifyResultList.size());
        for(CrossTxVerifyResult ctxResult : ctxVerifyResultList){
            stream.writeNulsData(ctxResult);
        }
        stream.writeVarInt(signRsList.size());
        for(CrossTxSignResult crossTxSignResult : signRsList){
            stream.writeNulsData(crossTxSignResult);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.state = byteBuffer.readUint16();
        this.senderChainId = byteBuffer.readUint16();
        this.senderNodeId = byteBuffer.readString();
        this.height = byteBuffer.readVarInt();
        this.tx = byteBuffer.readNulsData(new Transaction());
        long verifyNodeListSize = byteBuffer.readVarInt();
        for(int i=0;i<verifyNodeListSize;i++){
            verifyNodeList.add(byteBuffer.readNulsData(new Node()));
        }
        long ctxVerifyResultListSize = byteBuffer.readVarInt();
        for(int i=0;i<ctxVerifyResultListSize;i++){
            ctxVerifyResultList.add(byteBuffer.readNulsData(new CrossTxVerifyResult()));
        }
        long signRsListSize = byteBuffer.readVarInt();
        for(int i=0;i<signRsListSize;i++){
            signRsList.add(byteBuffer.readNulsData(new CrossTxSignResult()));
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(senderNodeId);
        size += SerializeUtils.sizeOfVarInt(height);
        size += SerializeUtils.sizeOfNulsData(tx);
        size += SerializeUtils.sizeOfVarInt(verifyNodeList.size());
        for(Node node : verifyNodeList){
            size += SerializeUtils.sizeOfNulsData(node);
        }
        size += SerializeUtils.sizeOfVarInt(ctxVerifyResultList.size());
        for(CrossTxVerifyResult crossTxVerifyResult : ctxVerifyResultList){
            size += SerializeUtils.sizeOfNulsData(crossTxVerifyResult);
        }
        size += SerializeUtils.sizeOfVarInt(signRsList.size());
        for(CrossTxSignResult crossTxVerifyResult : signRsList){
            size += SerializeUtils.sizeOfNulsData(crossTxVerifyResult);
        }
        return size;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSenderChainId() {
        return senderChainId;
    }

    public void setSenderChainId(int senderChainId) {
        this.senderChainId = senderChainId;
    }

    public String getSenderNodeId() {
        return senderNodeId;
    }

    public void setSenderNodeId(String senderNodeId) {
        this.senderNodeId = senderNodeId;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public List<CrossTxVerifyResult> getCtxVerifyResultList() {
        return ctxVerifyResultList;
    }

    public void setCtxVerifyResultList(List<CrossTxVerifyResult> ctxVerifyResultList) {
        this.ctxVerifyResultList = ctxVerifyResultList;
    }

    public List<Node> getVerifyNodeList() {
        return verifyNodeList;
    }

    public void setVerifyNodeList(List<Node> verifyNodeList) {
        this.verifyNodeList = verifyNodeList;
    }

    public List<CrossTxSignResult> getSignRsList() {
        return signRsList;
    }

    public void setSignRsList(List<CrossTxSignResult> signRsList) {
        this.signRsList = signRsList;
    }
}
