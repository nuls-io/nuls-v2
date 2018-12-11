/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.model.bo;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.*;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.model.po.TransactionPO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018-12-06
 */
public class TxWrapper extends BaseNulsData {

    private int chainId;

    private Transaction tx;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeNulsData(tx);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        chainId = byteBuffer.readUint16();
        tx = byteBuffer.readNulsData(tx);
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfNulsData(tx);
        return size;
    }

    public TxWrapper() {
    }

    public TxWrapper(int chainId, Transaction tx) {
        this.chainId = chainId;
        this.tx = tx;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }


    public List<TransactionPO> tx2PO() throws NulsException{
        List<TransactionPO> list = new ArrayList<>();
        if(null == tx.getCoinData()){
            return list;
        }
        CoinData coinData = tx.getCoinDataInstance();
        if(coinData.getFrom() != null){
            TransactionPO transactionPO = null;
            for(CoinFrom coinFrom : coinData.getFrom()){
                transactionPO = new TransactionPO();
                transactionPO.setAddress(AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                transactionPO.setHash(tx.getHash().getDigestHex());
                transactionPO.setType(tx.getType());
                transactionPO.setAssetChainId(coinFrom.getAssetsChainId());
                transactionPO.setAssetId(coinFrom.getAssetsId());
                transactionPO.setAmount(coinFrom.getAmount());
                // 0普通交易，-1解锁金额交易（退出共识，退出委托）
                byte locked = coinFrom.getLocked();
                int state = 0;
                if(locked == -1){
                    state = 3;
                }
                transactionPO.setState(state);
                list.add(transactionPO);
            }
        }
        if(coinData.getTo() != null){
            TransactionPO transactionPO = null;
            for(CoinTo coinTo : coinData.getTo()){
                transactionPO = new TransactionPO();
                transactionPO.setAddress(AddressTool.getStringAddressByBytes(coinTo.getAddress()));
                transactionPO.setAssetChainId(coinTo.getAssetsChainId());
                transactionPO.setAssetId(coinTo.getAssetsId());
                transactionPO.setAmount(coinTo.getAmount());
                transactionPO.setHash(tx.getHash().getDigestHex());
                transactionPO.setType(tx.getType());
                // 解锁高度或解锁时间，-1为永久锁定
                Long lockTime = coinTo.getLockTime();
                int state = 1;
                if(lockTime != 0){
                    state = 2;
                }
                transactionPO.setState(state);
                list.add(transactionPO);
            }
        }
        return list;
    }
}
