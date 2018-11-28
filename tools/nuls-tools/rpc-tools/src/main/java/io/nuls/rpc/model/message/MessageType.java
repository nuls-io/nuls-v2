package io.nuls.rpc.model.message;

/**
 * @author tangyi
 */
public enum MessageType {
    /**
     * 消息类型
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
