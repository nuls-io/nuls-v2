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

package io.nuls.account.constant;


/**
 * @author: qinyifeng
 * @description: RPC常量 RPC constants
 */
public interface RpcConstant {

    /**
     * --------[call EventBus module RPC constants] -------
     */
    /**
     * EVENT_SEND_CMD
     */
    String EVENT_SEND_CMD = "send";
    /**
     * EVENT_SEND_VERSION
     */
    String EVENT_SEND_VERSION = "1.0";
    /**
     * EVENT_SEND_TOPIC
     */
    String EVENT_SEND_TOPIC = "topic";
    /**
     * EVENT_SEND_DATA
     */
    String EVENT_SEND_DATA = "data";
    /**
     * --------[call Transaction module RPC constants] -------
     */
    /**
     * TX_REGISTER_VERSION
     */
    String TX_REGISTER_VERSION = "1.0";
    /**
     * TX_REGISTER_CMD
     */
    String TX_REGISTER_CMD = "tx_register";
    /**
     * TX_MODULE_CODE
     */
    String TX_MODULE_CODE = "moduleCode";
    /**
     * TX_MODULE_VALIDATE_CMD
     */
    String TX_MODULE_VALIDATE_CMD = "moduleValidateCmd";
    /**
     * TX_TYPE
     */
    String TX_TYPE = "txType";
    /**
     * TX_VALIDATE_CMD
     */
    String TX_VALIDATE_CMD = "validateCmd";
    /**
     * TX_COMMIT_CMD
     */
    String TX_COMMIT_CMD = "commitCmd";
    /**
     * TX_ROLLBACK_CMD
     */
    String TX_ROLLBACK_CMD = "rollbackCmd";
    /**
     * TX_NEW_VERSION
     */
    String TX_NEW_VERSION = "1.0";
    /**
     * TX_NEW_CMD
     */
    String TX_NEW_CMD = "newTx";
    /**
     * TX_DATA_HEX
     */
    String TX_DATA_HEX = "txHex";
    /**
     * TX_CHAIN_ID
     */
    String TX_CHAIN_ID = "chainId";
    /**
     * --------[RPC response constants] -------
     */
    /**
     * value
     */
    String VALUE = "value";
    /**
     * address
     */
    String ADDRESS = "address";
    /**
     * encryptedPriKey
     */
    String ENCRYPTED_PRIKEY = "encryptedPriKey";
    /**
     * path
     */
    String PATH = "path";
    /**
     * signatureHex
     */
    String SIGNATURE_HEX = "signatureHex";

    /**
     * --------[RPC Module role constants] -------
     */
    /**
     * Role_Account
     */
    String ROLE_AC = "Role_Account";
    /**
     * Role_Event
     */
    String ROLE_EV = "Role_Event";
}
