/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.poc.model.bo.tx;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.utils.ConsensusConstant;
import io.nuls.tools.exception.NulsException;

/**
 * @author tag
 */
public class StopAgentTransaction extends Transaction<StopAgent> {

    public StopAgentTransaction() {
        super(ConsensusConstant.TX_TYPE_STOP_AGENT);
    }

    public StopAgentTransaction(CoinData coinData) throws NulsException {
        super(ConsensusConstant.TX_TYPE_STOP_AGENT);
    }

    @Override
    protected StopAgent parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new StopAgent());
    }

    @Override
    public String getInfo(byte[] address) {
        /*if (null != this.txData) {
            LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
            CreateAgentTransaction tx = (CreateAgentTransaction) ledgerService.getTx(this.txData.getCreateTxHash());
            if (null != tx) {
                return "unlock " + tx.getTxData().getDeposit().toCoinString();
            }
        }*/
        return "--";
    }

    @Override
    public boolean isUnlockTx() {
        return true;
    }

    @Override
    public boolean needVerifySignature() {
        return false;
    }
}
