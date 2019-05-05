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
import com.google.common.base.Objects;

/**
 * 所有消息都应该用该对象进行传输
 * All messages should be transmitted with this object
 *
 * @author tangyi
 * @date 2018/11/15
 * @description
 */

public class Message {

    /**
     * 消息号 / Message ID
     */
    @JsonProperty
    private String MessageID;

    /**
     * 消息发送时间 / Message sending time
     */
    @JsonProperty
    private String Timestamp;

    /**
     * 消息发送时区 / Message sending Timezone
     */
    @JsonProperty
    private String TimeZone;

    /**
     * 消息类型，共9种 / Message type, 9 types
     */
    @JsonProperty
    private String MessageType;

    /**
     * 消息体，根据MessageType有不同的结构
     */
    @JsonProperty
    private Object MessageData;

    @JsonIgnore
    public String getMessageID() {
        return MessageID;
    }

    @JsonIgnore
    public void setMessageID(String MessageId) {
        this.MessageID = MessageId;
    }

    @JsonIgnore
    public String getTimestamp() {
        return Timestamp;
    }

    @JsonIgnore
    public void setTimestamp(String Timestamp) {
        this.Timestamp = Timestamp;
    }

    @JsonIgnore
    public String getTimeZone() {
        return TimeZone;
    }

    @JsonIgnore
    public void setTimeZone(String Timezone) {
        this.TimeZone = Timezone;
    }

    @JsonIgnore
    public String getMessageType() {
        return MessageType;
    }

    @JsonIgnore
    public void setMessageType(String MessageType) {
        this.MessageType = MessageType;
    }

    @JsonIgnore
    public Object getMessageData() {
        return MessageData;
    }

    @JsonIgnore
    public void setMessageData(Object MessageData) {
        this.MessageData = MessageData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return Objects.equal(MessageID, message.MessageID) &&
                Objects.equal(Timestamp, message.Timestamp) &&
                Objects.equal(TimeZone, message.TimeZone) &&
                Objects.equal(MessageType, message.MessageType) &&
                Objects.equal(MessageData, message.MessageData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(MessageID, Timestamp, TimeZone, MessageType, MessageData);
    }
}
