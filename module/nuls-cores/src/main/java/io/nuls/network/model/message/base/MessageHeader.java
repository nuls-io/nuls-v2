/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.network.model.message.base;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * Network message header, which contains magic parameters、Message body size、Instruction type、Verify parameters
 * Network message header, the message header contains
 * magic parameters, message body size, command, checksum.
 *
 * @author lanjinsheng
 */
public class MessageHeader extends BaseNulsData {
    /**
     * Magic parameters used to isolate network segments
     * Magic parameters used in the isolation section.
     */

    private long magicNumber;

    /**
     * Message body size
     * the length of the msgBody
     */

    private long payloadLength;

    /**
     * Message instruction
     * ASCII string identifying the packet content, NULL padded
     */

    private byte[] command = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * Verify Fields
     * First 4 bytes of  sha256(sha256(payload))
     */
    private long checksum;

    /**
     * Instruction string
     * String for command
     */
    private transient String commandStr = null;

    public MessageHeader() {

    }

    public MessageHeader(String command, long magicNumber) {
        byte[] commandBytes = command.getBytes();
        System.arraycopy(commandBytes, 0, this.command, 0, commandBytes.length);
        this.magicNumber = magicNumber;
    }

    public MessageHeader(String command, long magicNumber, long checksum, long payloadLength) {
        byte[] commandBytes = command.getBytes();
        System.arraycopy(commandBytes, 0, this.command, 0, commandBytes.length);
        this.magicNumber = magicNumber;
        this.payloadLength = payloadLength;
        this.checksum = checksum;
    }

    public MessageHeader(String command) {
        byte[] commandBytes = command.getBytes();
        System.arraycopy(commandBytes, 0, this.command, 0, commandBytes.length);
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(magicNumber);
        stream.writeUint32(payloadLength);
        stream.write(command);
        stream.writeUint32(checksum);
    }

    public String getCommandStr() {
        if (null != commandStr) {
            return commandStr;
        }
        int effectiveCmdLen = 0;
        for (int i = 0; i < 12; i++) {
            if (command[i] != (byte) 0xFF) {
                effectiveCmdLen++;
            } else {
                break;
            }
        }
        byte[] effectiveCmd = new byte[effectiveCmdLen];
        System.arraycopy(command, 0, effectiveCmd, 0, effectiveCmdLen);
        commandStr = new String(effectiveCmd);
        return commandStr;
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        try {
            magicNumber = buffer.readUint32();
            payloadLength = buffer.readUint32();
            command = buffer.readBytes(12);
            checksum = buffer.readUint32();
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32();
        s += SerializeUtils.sizeOfUint32();
        s += command.length;
        s += SerializeUtils.sizeOfUint32();
        return s;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }
}
