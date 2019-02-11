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
package io.nuls.network.model.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.VersionMessageBody;
import io.nuls.tools.exception.NulsException;

/**
 * VersionMessage
 *
 * @author lan
 * @date 2018/11/01
 */
public class VersionMessage extends BaseMessage<VersionMessageBody> {

    @Override
    protected VersionMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return byteBuffer.readNulsData(new VersionMessageBody());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public VersionMessage() {
        super(NetworkConstant.CMD_MESSAGE_VERSION);
    }

    public VersionMessage(long magicNumber, String cmd, VersionMessageBody body) {
        super(cmd, magicNumber);
        this.setMsgBody(body);
    }
}
