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
 * Server side configuration class
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
        //webSocketThe protocol itself is based onhttpProtocol, so we also need to use it herehttpDecoder
        pipeline.addLast(new HttpServerCodec());
        //A processor written in blocks
        pipeline.addLast(new ChunkedWriteHandler());
        //nettyIt is based on segmented requests,HttpObjectAggregatorThe function of is to segment and re aggregate requests,The parameter is the maximum length of aggregated bytes
        pipeline.addLast(new HttpObjectAggregator(104 * 1024 * 1024));
        //The parameters refer tocontex_path
        pipeline.addLast(new WebSocketServerProtocolHandler(path, null, true, 104 * 1024 * 1024));

        //webSocketDefined the transmission of data6inframetype
        pipeline.addLast(new ServerHandler());
    }
}
