package io.nuls.crosschain.base.constant;

/**
 * Cross chain module command constant management class
 *
 * @author tag
 * 2019/04/08
 */
public interface CommandConstant {

    /**
     * Obtain cross chain transactions from other chain nodes
     */
    String GET_OTHER_CTX_MESSAGE = "getOtherCtx";

    /**
     * Initiate a link to receive verification cross chain transaction messages sent from the main network
     */
    String VERIFY_CTX_MESSAGE = "verifyCtx";

    /**
     * The processing results of initiating link collection cross chain transactions in the main chain
     */
    String CTX_STATE_MESSAGE = "recvCtxState";
    /**
     * Initiate link and receive asset statistics for the acquisition chain sent by the main network
     */
    String GET_CIRCULLAT_MESSAGE = "getCirculat";


    /**
     * Receive cross chain transactions from other chain nodes
     */
    String NEW_OTHER_CTX_MESSAGE = "recvOtherCtx";

    /**
     * Receive link and receive cross chain transaction verification results from the main network
     */
    String CTX_VERIFY_RESULT_MESSAGE = "recvVerifyRs";

    /**
     * The receiving chain receives a message from the main network to obtain cross chain transaction processing results
     */
    String GET_CTX_STATE_MESSAGE = "getCtxState";

    /**
     * Cross chain transactions for receiving links and broadcasting from the main networkHash
     */
    String BROAD_CTX_HASH_MESSAGE = "recvCtxHash";

    /**
     * Broadcast transactions from the receiving chain to nodes within the chainHashAnd signature
     */
    String BROAD_CTX_SIGN_MESSAGE = "recvCtxSign";

    /**
     * The main network received an asset issuance statistics message sent by the friend chain
     * */
    String  CIRCULATION_MESSAGE = "recvCirculat";

    /**
     * Re broadcast the Byzantine signature for the specified cross chain transaction
     */
    String CROSS_TX_REHANDLE_MESSAGE = "ctxRehandle";

}
