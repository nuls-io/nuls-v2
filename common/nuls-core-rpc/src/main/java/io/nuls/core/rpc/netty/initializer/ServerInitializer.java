package io.nuls.core.rpc.netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.nuls.core.rpc.netty.handler.ServerHandler;

/**
 * 服务器端配置类
 * Server Configuration Class
 *
 * @author tag
 * 2019/2/21
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private String path;

    public ServerInitializer(String path) {
        this.path = path;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //webSocket协议本身是基于http协议的，所以这边也要使用http解编码器
        pipeline.addLast(new HttpServerCodec());
        //以块的方式来写的处理器
        pipeline.addLast(new ChunkedWriteHandler());
        //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
        pipeline.addLast(new HttpObjectAggregator(104 * 1024 * 1024));
        //参数指的是contex_path
        pipeline.addLast(new WebSocketServerProtocolHandler(path, null, true, 104 * 1024 * 1024));

        //webSocket定义了传递数据的6中frame类型
        pipeline.addLast(new ServerHandler());
    }
}
