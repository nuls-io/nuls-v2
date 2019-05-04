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
package io.nuls.core.rpc.model.message;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通知
 * Notification
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class Notification {
    /**
     * (Default: 0): This is a boolean value.
     * 0: The Micro server that made the notification does not expect any kind of message in return.
     * 1: The Micro server that made the notification expects exactly one Ack message.
     */
    @JsonProperty
    private String NotificationAck;

    /**
     * The category of the notification, each service may define its own types so it is not required that the target service processes this field.
     */
    @JsonProperty
    private String NotificationType;

    /**
     * A string comment that provides more information about the reason of the notification
     */
    @JsonProperty
    private String NotificationComment;

    /**
     * Data relevant to the notification, it is not required the target service to process this field
     */
    @JsonProperty
    private String NotificationData;

    @JsonIgnore
    public String getNotificationAck() {
        return NotificationAck;
    }

    @JsonIgnore
    public void setNotificationAck(String NotificationAck) {
        this.NotificationAck = NotificationAck;
    }

    @JsonIgnore
    public String getNotificationType() {
        return NotificationType;
    }

    @JsonIgnore
    public void setNotificationType(String NotificationType) {
        this.NotificationType = NotificationType;
    }

    @JsonIgnore
    public String getNotificationComment() {
        return NotificationComment;
    }

    @JsonIgnore
    public void setNotificationComment(String NotificationComment) {
        this.NotificationComment = NotificationComment;
    }

    @JsonIgnore
    public String getNotificationData() {
        return NotificationData;
    }

    @JsonIgnore
    public void setNotificationData(String NotificationData) {
        this.NotificationData = NotificationData;
    }
}
