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

import lombok.Data;
import lombok.ToString;

/**
 * 通知
 * Notification
 *
 * @author tangyi
 * @date 2018/11/15
 */
@Data
@ToString
public class Notification {
    /**
     * (Default: 0): This is a boolean value.
     * 0: The Micro server that made the notification does not expect any kind of message in return.
     * 1: The Micro server that made the notification expects exactly one Ack message.
     */
    private String notificationAck;

    /**
     * The category of the notification, each service may define its own types so it is not required that the target service processes this field.
     */
    private String notificationType;

    /**
     * A string comment that provides more information about the reason of the notification
     */
    private String notificationComment;

    /**
     * Data relevant to the notification, it is not required the target service to process this field
     */
    private String notificationData;
}
