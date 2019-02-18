/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.model.message.base;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 所有网络上传输的消息的基类，定义了网络消息的基本格式
 * The base class for all messages transmitted over the network defines the basic format of the network message.
 *
 * @author Niels
 */
public abstract class BaseMessage<T extends BaseNulsData> extends BaseNulsData {

    private transient NulsDigestData hash;

    private MessageHeader header;

    private T msgBody;

    public BaseMessage() {

    }

    /**
     * 初始化基础消息的消息头
     */
    public BaseMessage(String command, long magicNumber) {
        this.header = new MessageHeader(command, magicNumber);
    }

    public BaseMessage(String command) {
        this.header = new MessageHeader(command);
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        long checksum = getCheckSum();
        header.setChecksum(checksum);
        stream.write(header.serialize());
        stream.write(msgBody.serialize());
    }


    public long getCheckSum() throws IOException {
        byte[] data = null;
        if (null == msgBody || msgBody.size() == 0) {
            data = ToolsConstant.PLACE_HOLDER;
        } else {
            data = msgBody.serialize();
        }
//        Log.info("=================getCheckSum:"+data.length);
        byte[] bodyHash = Sha256Hash.hashTwice(data);
        byte[] get4Byte = ByteUtils.subBytes(bodyHash, 0, 4);
        long checksum = ByteUtils.bytesToBigInteger(get4Byte).longValue();
        return checksum;
    }

    public boolean isCheckSumValid() {
        try {
            return getCheckSum() == this.getHeader().getChecksum();
        } catch (IOException e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;

        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        MessageHeader header = new MessageHeader();
        header.parse(byteBuffer);
        this.header = header;
        this.msgBody = parseMessageBody(byteBuffer);
    }

    protected abstract T parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException;


    @Override
    public int size() {
        int s = 0;
        s += header.size();
        s += msgBody.size();
        return s;
    }


    public T getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(T msgBody) {
        this.msgBody = msgBody;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

}
