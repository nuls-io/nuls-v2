package io.nuls.core.rpc.netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Client Configuration Class
 * Client Configuration Class
 *
 * @author tag
 * 2019/2/21
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //webSocketThe protocol itself is based onhttpProtocol, so we also need to use it herehttpDecoder
        pipeline.addLast(new HttpServerCodec());
        //nettyIt is based on segmented requests,HttpObjectAggregatorThe function of is to segment and re aggregate requests,The parameter is the maximum length of aggregated bytes
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 1024));
        //webSocketDefined the transmission of data6inframetype
        //pipeline.addLast("hookedHandler",new ClientHandler());
    }
}
