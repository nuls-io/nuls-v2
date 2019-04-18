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

package io.nuls.transaction.constant;

/**
 * @author: Charlie
 * @date: 2018-12-25
 */
public interface TxCmd {

    /**
     * 模块接口
     */
    String TX_GET_SYSTEM_TYPES = "tx_getSystemTypes";
    String TX_REGISTER = "tx_register";
    String TX_UNREGISTER = "tx_unregister";
    String TX_PACKABLETXS = "tx_packableTxs";
    String TX_SAVE = "tx_save";
    String TX_GENGSIS_SAVE = "tx_gengsisSave";
    String TX_ROLLBACK = "tx_rollback";
    String TX_GET_CONFIRMED_TX = "tx_getConfirmedTx";
    String TX_GETTX = "tx_getTx";
    String TX_GET_BLOCK_TXS = "tx_getBlockTxs";
    String TX_GET_BLOCK_TXS_EXTEND = "tx_getBlockTxsExtend";
    String TX_BATCHVERIFY = "tx_batchVerify";
    String TX_CREATE_CROSS_TX = "tx_createCtx";
    String CLIENT_GETTX = "tx_getTxClient";
    String CLIENT_GETTX_CONFIRMED = "tx_getConfirmedTxClient";

    String TX_BLOCK_HEIGHT = "tx_blockHeight";
    String TX_VERIFYTX = "tx_verifyTx";

    /**
     * 修改节点共识状态
     */
    String TX_CS_STATE = "tx_cs_state";


    String TX_NEWTX = "tx_newTx";

    String TX_BASE_VALIDATE = "tx_baseValidateTx";

    /**
     * 接收广播的新交易hash
     */
    String NW_NEW_HASH = "newHash";
    /**
     * 接收其他节点发送的完整交易
     */
    String NW_RECEIVE_TX = "receiveTx";
    /**
     * 索取完整交易
     */
    String NW_ASK_TX = "askTx";

}
