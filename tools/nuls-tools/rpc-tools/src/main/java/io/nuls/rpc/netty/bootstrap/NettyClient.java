package io.nuls.rpc.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.nuls.rpc.netty.handler.ClientHandler;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * netty客服端启动实现类
 * Customer Service Start Implementation Class
 *
 * @author tag
 * 2019/2/21
 */
public class NettyClient {
    /**
     * 连接服务器，返回连接通道
     * Connect to the server and return to the connection channel
     */
    public static Channel createConnect(String uri) {
        try {
            URI webSocketURI = null;
            try {
                webSocketURI = new URI(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            final ClientHandler handler =
                    new ClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    webSocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders(), 65536 * 5));
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(1024 * 1024 * 10),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    handler);
                        }
                    });
            Channel ch = b.connect(webSocketURI.getHost(), webSocketURI.getPort()).sync().channel();
            handler.handshakeFuture().sync();
            ResponseMessageProcessor.handshake(ch);
            return ch;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
