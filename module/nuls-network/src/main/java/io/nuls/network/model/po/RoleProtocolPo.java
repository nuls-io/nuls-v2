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
package io.nuls.network.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.network.model.dto.Dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2018/12/20
 **/
public class RoleProtocolPo extends BasePo {
    private String role;
    private List<String> protocolCmds = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getProtocolCmds() {
        return protocolCmds;
    }

    public void setProtocolCmds(List<String> protocolCmds) {
        this.protocolCmds = protocolCmds;
    }

    @Override
    public Dto parseDto() {
        return null;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(role);
        int protocolHandlerSize = (protocolCmds == null ? 0 : protocolCmds.size());
        stream.writeVarInt(protocolHandlerSize);
        if (null != protocolCmds) {
            for (String protocolCmd : protocolCmds) {
                stream.writeString(protocolCmd);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.role = byteBuffer.readString();
        int protocolCmdsSize = (int) byteBuffer.readVarInt();
        if (0 < protocolCmdsSize) {
            for (int i = 0; i < protocolCmdsSize; i++) {
                protocolCmds.add(byteBuffer.readString());
            }
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(role);
        size += SerializeUtils.sizeOfVarInt(protocolCmds == null ? 0 : protocolCmds.size());
        if (null != protocolCmds) {
            for (String protocolCmd : protocolCmds) {
                size += SerializeUtils.sizeOfString(protocolCmd);
            }
        }
        return size;
    }

    @Override
    public String toString() {
        return "RoleProtocolPo{" +
                "role='" + role + '\'' +
                ", protocolHandlerPos=" + protocolCmds +
                '}';
    }
}
