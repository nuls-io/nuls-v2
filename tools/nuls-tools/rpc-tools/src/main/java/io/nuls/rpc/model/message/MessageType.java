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
package io.nuls.rpc.model.message;

/**
 * 消息类型
 * Message types
 *
 * @author tangyi
 */
public enum MessageType {
    /**
     * 握手 / Handshake
     */
    NegotiateConnection,

    /**
     * 握手确认 / Handshake confirm
     */
    NegotiateConnectionResponse,

    /**
     * 请求 / Request
     */
    Request,

    /**
     * 回复 / Response
     */
    Response,

    /**
     * 取消订阅 / Unsubscribe
     */
    Unsubscribe,

    /**
     * 确认收到消息 / Confirm receipt of message
     */
    Ack,

    /**
     * 通知 / Notification
     */
    Notification,

    /**
     * 批量订阅 / Batch request
     */
    RegisterCompoundMethod,

    /**
     * 取消批量订阅 / Batch unsubscribe
     */
    UnregisterCompoundMethod,

}
