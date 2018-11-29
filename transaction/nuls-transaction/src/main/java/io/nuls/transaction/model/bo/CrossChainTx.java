package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;


/**
 * 记录跨链交易验证过程中的状态和数据的模型
 * @author: Charlie
 * @date: 2018/11/13
 */
public class CrossChainTx extends BaseNulsData {


    private Transaction tx;


    /**
     * 该跨链交易在本链中的验证状态
     */
    private int state;

    //TODO
    /**
     * 1.收到的跨链节点验证结果(数量？)
     * 2.收到的本链节点验证结果(签名？)
     *
     */

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer nulsOutputStreamBuffer) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer nulsByteBuffer) throws NulsException {

    }

    @Override
    public int size() {
        return 0;
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


}
