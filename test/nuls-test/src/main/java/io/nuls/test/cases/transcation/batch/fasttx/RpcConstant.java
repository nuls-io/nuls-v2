/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.test.cases.transcation.batch.fasttx;


/**
 * @author: qinyifeng
 * @description: RPCconstant RPC constants
 */
public interface RpcConstant {

    /**
     * --------[call EventBus module RPC constants] -------
     */
    /**
     * Event sending interface
     */
    String EVENT_SEND_CMD = "send";
    /**
     * Event sending interface version number
     */
    String EVENT_SEND_VERSION = "1.0";
    /**
     * Event sending topic
     */
    String EVENT_SEND_TOPIC = "topic";
    /**
     * Event sending data
     */
    String EVENT_SEND_DATA = "data";
    /**
     * --------[call Transaction module RPC constants] -------
     */
    /**
     * tx_registerRegister transaction interface version number
     */
    String TX_REGISTER_VERSION = "1.0";
    /**
     * Module code for registering transactions
     */
    String TX_MODULE_CODE = "moduleCode";
    /**
     * Module Unified Transaction Verifier Interface
     */
    String TX_MODULE_VALIDATE_CMD = "moduleValidator";
    /**
     * Module Unified Transaction Submission Interface
     */
    String TX_MODULE_COMMIT_CMD = "commit";
    /**
     * Module Unified Transaction Rollback Interface
     */
    String TX_MODULE_ROLLBACK_CMD = "rollback";
    /**
     * Transaction type
     */
    String TX_TYPE = "txType";
    /**
     * Single transaction validator interface
     */
    String TX_VALIDATE_CMD = "validator";
    /**
     * Transaction submission interface
     */
    String TX_COMMIT_CMD = "commit";
    /**
     * Transaction rollback interface
     */
    String TX_ROLLBACK_CMD = "rollback";

    /**
     * Is it a transaction generated by the system（Generate packaging nodes for block reward settlement、Red and yellow card punishment）
     */
    String TX_IS_SYSTEM_CMD = "systemTx";
    /**
     * Is it an unlocked transaction
     */
    String TX_UNLOCK_CMD = "unlockTx";
    /**
     * Does the transaction require signature verification in the ledger
     */
    String TX_VERIFY_SIGNATURE_CMD = "verifySignature";

    /**
     * newTxInitiate new transaction interface version number
     */
    String TX_NEW_VERSION = "1.0";
    /**
     * Initiate new transaction interface
     */
    String TX_NEW_CMD = "tx_newTx";

    String TX_BASE_VALIDATE = "tx_baseValidateTx";
    /**
     * transaction dataHEXcoding
     */
    String TX_DATA = "tx";
    /**
     * Exchange ChainID
     */
    String TX_CHAIN_ID = "chainId";
    /**
     * --------[RPC response constants] -------
     */
    /**
     * Single return value default keykey
     */
    String VALUE = "value";
    /**
     * Set return value default keykey
     */
    String LIST = "list";
    /**
     * Account address
     */
    String ADDRESS = "address";
    /**
     * Encrypt private key
     */
    String ENCRYPTED_PRIKEY = "encryptedPriKey";
    /**
     * keystoreBackup address
     */
    String PATH = "path";
    /**
     * Signature data
     */
    String SIGNATURE = "signature";

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

    /**
     * TX_HASH
     */
    String TX_HASH = "txHash";
}
