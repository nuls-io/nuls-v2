/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;

import java.nio.ByteOrder;
import java.util.List;

import static io.nuls.network.constant.NetworkConstant.MAX_FRAME_LENGTH;
import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/7
 */
public class NulsMessageDecoder extends ByteToMessageDecoder {
    ConnectionManager connectionManager = ConnectionManager.getInstance();
    private NulsLengthFieldBasedFrameDecoder newDecoder = new NulsLengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH, 4, 4, 16, 0, true);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        long readMagicNumber = in.getUnsignedIntLE(0);
        if (NodeGroupManager.getInstance().validMagicNumber(readMagicNumber)) {
            Object decoded = newDecoder.decode(ctx, in);
            if (decoded != null) {
                out.add(decoded);
            }
        }else{
            in.clear();
            //如果一个连接有多条链，此时通道需要保留，不该关闭连接
            if(connectionManager.isPeerSingleGroup(ctx.channel())){
                ctx.close();
            }else{
                Log.error("illegal message REC");
            }
        }
    }
}
