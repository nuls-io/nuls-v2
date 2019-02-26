package io.nuls.rpc.netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.nuls.rpc.netty.handler.ClientHandler;

/**
 * 客户端配置类
 * Client Configuration Class
 * @author tag
 * 2019/2/21
 * */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel){
        ChannelPipeline pipeline = socketChannel.pipeline();
        //webSocket协议本身是基于http协议的，所以这边也要使用http解编码器
        pipeline.addLast(new HttpServerCodec());
        //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
        pipeline.addLast(new HttpObjectAggregator(1024*1024*1024));
        //webSocket定义了传递数据的6中frame类型
        pipeline.addLast("hookedHandler",new ClientHandler());
    }
}
