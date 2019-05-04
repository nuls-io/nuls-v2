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
import io.nuls.network.model.dto.Dto;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;

/**
 * @author lan
 * @description
 * @date 2019/01/29
 **/
public class GroupNodesPo extends BasePo {
    /**
     * self net-自有网络的节点
     */
    private NodesContainerPo selfNodeContainer = new NodesContainerPo();

    /**
     * 跨链-跨链连接的节点
     */
    private NodesContainerPo crossNodeContainer = new NodesContainerPo();


    public NodesContainerPo getSelfNodeContainer() {
        return selfNodeContainer;
    }

    public void setSelfNodeContainer(NodesContainerPo selfNodeContainer) {
        this.selfNodeContainer = selfNodeContainer;
    }

    public NodesContainerPo getCrossNodeContainer() {
        return crossNodeContainer;
    }

    public void setCrossNodeContainer(NodesContainerPo crossNodeContainer) {
        this.crossNodeContainer = crossNodeContainer;
    }

    @Override
    public Dto parseDto() {
        return null;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        this.getSelfNodeContainer().serializeToStream(stream);
        this.getCrossNodeContainer().serializeToStream(stream);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.getSelfNodeContainer().parse(byteBuffer);
        this.getCrossNodeContainer().parse(byteBuffer);
    }

    @Override
    public int size() {
        int size = 0;
        size += this.getSelfNodeContainer().size();
        size += this.getCrossNodeContainer().size();
        return size;
    }
}
