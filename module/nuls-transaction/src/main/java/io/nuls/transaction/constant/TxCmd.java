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
public final class TxCmd {

    /**
     * 模块接口
     */
    public static final String TX_GET_SYSTEM_TYPES = "tx_getSystemTypes";
    public static final String TX_REGISTER = "tx_register";
    public static final String TX_PACKABLETXS = "tx_packableTxs";
    public static final String TX_BACKPACKABLETXS = "tx_backPackableTxs";
    public static final String TX_SAVE = "tx_save";
    public static final String TX_GENGSIS_SAVE = "tx_gengsisSave";
    public static final String TX_ROLLBACK = "tx_rollback";
    public static final String TX_GET_CONFIRMED_TX = "tx_getConfirmedTx";
    public static final String TX_GETTX = "tx_getTx";
    public static final String TX_GET_BLOCK_TXS = "tx_getBlockTxs";
    public static final String TX_GET_BLOCK_TXS_EXTEND = "tx_getBlockTxsExtend";
    public static final String TX_GET_NONEXISTENT_UNCONFIRMED_HASHS = "tx_getNonexistentUnconfirmedHashs";
    public static final String TX_BATCHVERIFY = "tx_batchVerify";
    public static final String CLIENT_GETTX = "tx_getTxClient";
    public static final String CLIENT_GETTX_CONFIRMED = "tx_getConfirmedTxClient";

    public static final String TX_BLOCK_HEIGHT = "tx_blockHeight";
    public static final String TX_VERIFYTX = "tx_verifyTx";

    /**
     * 修改节点共识状态
     */
    public static final String TX_CS_STATE = "tx_cs_state";

    /**
     * 修改节点处理交易状态
     */
    public static final String TX_BL_STATE = "tx_bl_state";


    public static final String TX_NEWTX = "tx_newTx";

    public static final String TX_BASE_VALIDATE = "tx_baseValidateTx";

    /**
     * 接收广播的新交易hash
     */
    public static final String NW_NEW_HASH = "newHash";
    /**
     * 接收其他节点发送的完整交易
     */
    public static final String NW_RECEIVE_TX = "receiveTx";
    /**
     * 索取完整交易
     */
    public static final String NW_ASK_TX = "askTx";

}
