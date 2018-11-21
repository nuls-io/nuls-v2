package io.nuls.rpc.model.message;

/**
 * @author tangyi
 */
public enum MessageType {
    /**
     * Message type
     */
    NegotiateConnection,
    NegotiateConnectionResponse,
    Request,
    Response,
    Unsubscribe,
    Ack,
    Notification,
    RegisterCompoundMethod,
    UnregisterCompoundMethod,

}
