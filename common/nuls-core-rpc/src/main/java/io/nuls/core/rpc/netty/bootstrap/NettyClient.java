package io.nuls.core.rpc.netty.bootstrap;

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
import io.nuls.core.log.Log;
import io.nuls.core.rpc.netty.handler.ClientHandler;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * nettyCustomer service startup implementation class
 * Customer Service Start Implementation Class
 *
 * @author tag
 * 2019/2/21
 */
public class NettyClient {
    /**
     * Connect to the server and return the connection channel
     * Connect to the server and return to the connection channel
     */
    public static Channel createConnect(String uri) {
        return createConnect(uri, 0);
    }

    public static Channel createConnect(String uri, int tryCount) {
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
                                    webSocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders(), 104 * 1024 * 1024));
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
                                    new HttpObjectAggregator(104 * 1024 * 1024),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    handler);
                        }
                    });
            Channel ch = b.connect(webSocketURI.getHost(), webSocketURI.getPort()).sync().channel();
            handler.handshakeFuture().sync();
            ResponseMessageProcessor.handshake(ch);
            return ch;
        } catch (Exception e) {
            if (tryCount < 5) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e1) {
                    Log.error("retrywsWhen connecting, the hibernation process encountered an exception");
                }
                Log.info("establishws:{}Failed, No{}retry", uri, tryCount + 1);
                return createConnect(uri, tryCount + 1);
            } else {
                Log.error("establishwsconnection failed：{}", uri, e);
                return null;
            }

        }
    }
}
