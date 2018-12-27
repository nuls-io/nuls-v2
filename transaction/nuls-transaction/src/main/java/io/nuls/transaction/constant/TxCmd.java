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
    String TX_PACKABLETXS = "tx_packableTxs";
    String TX_SAVE = "tx_save";
    String TX_ROLLBACK = "tx_rollback";
    String TX_GETTX = "tx_getTx";
    String TX_GETTXS = "tx_getTxs";
//    String TX_DELETE = "tx_delete";
    String TX_VERIFY = "tx_verify";
    String TX_GETTXSINFO = "tx_getTxsInfo";
    String TX_GETTXPROCESSORS = "tx_getTxProcessors";
    String TX_RUNCHAIN = "tx_runChain";
    String TX_STOPCHAIN = "tx_stopChain";

    String TX_NEWTX = "tx_newTx";

    //网络消息对应的RPC命令
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
    /**
     * 根据原始交易和跨链交易hash向友链节点验证该交易是否被确认
     */
    String NW_VERIFY_FC = "verifyFc";
    /**
     * 根据跨链交易hash向主网验证该交易是否被确认
     */
    String NW_VERIFY_MN = "verifyMn";

    /**
     * 接收新的跨链交易hash
     */
    String NW_NEW_CROSS_HASH = "newCrossHash";
    /**
     * 索取完整跨链交易
     */
    String NW_ASK_CROSS_TX = "askCrossTx";
    /**
     * 接收主网新的完整跨链交易
     */
    String NW_NEW_MN_TX = "newMnTx";
    /**
     * 接收跨链验证结果
     */
    String NW_VERIFYR_ESULT = "verifyResult";
    /**
     * 接收跨链验证结果
     */
    String NW_CROSS_NODE_RS = "crossNodeRs";
    /**
     * 接收本地新的主网协议的跨链交易 ??????
     * todo
     */
    String NW_NEW_CROSS_TX = "newCrossTx";

}
