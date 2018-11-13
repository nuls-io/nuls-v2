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
import io.nuls.network.model.message.body.VerackMessageBody;
import io.nuls.tools.exception.NulsException;

/**
 * VerackMessage
 * @author lan
 * @date 2018/11/01
 *
 */
public class VerackMessage extends BaseMessage<VerackMessageBody> {

    @Override
    protected VerackMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return byteBuffer.readNulsData(new VerackMessageBody());
        } catch (Exception e) {
           throw new NulsException(e);
        }
    }
    public VerackMessage() {
       super(NetworkConstant.CMD_MESSAGE_VERACK,0);
    }

    public VerackMessage(long magicNumber, String cmd, VerackMessageBody body) {
        super(cmd,magicNumber);
        this.setMsgBody(body);
    }
}
