package io.nuls.core.rpc.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SerializeUtil {

    public static ByteBuf getBuffer(byte[] bytes) {
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

}
