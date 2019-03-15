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
 * @author tag
 * 2019/2/21
 * */
public class NettyClient {
    /**
     * 连接服务器，返回连接通道
     * Connect to the server and return to the connection channel
     * */
    public static Channel createConnect(String uri){
        try {
            /*EventLoopGroup group=new NioEventLoopGroup();
            Bootstrap boot=new Bootstrap();
            boot.option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_BACKLOG,1024*1024*10)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new ChannelHandler[]{
                                    new LoggingHandler(LogLevel.TRACE),
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(1024*1024*10)});
                            p.addLast("hookedHandler", new ClientHandler());
                        }
                    });
            URI webSocketURI = null;
            try {
                webSocketURI = new URI(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            //进行握手
            WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory.newHandshaker(webSocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders, 65536*5);
            final Channel channel=boot.connect(webSocketURI.getHost(),webSocketURI.getPort()).sync().channel();
            ClientHandler handler = (ClientHandler)channel.pipeline().get("hookedHandler");
            handler.setHandshaker(handShaker);
            handShaker.handshake(channel);
            //阻塞等待是否握手成功
            handler.handshakeFuture().sync();
            Log.info("与服务器："+webSocketURI.toString()+"握手成功");
            //netty握手成功之后，发送业务握手信息
            Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
            message.setMessageData(MessageUtil.defaultNegotiateConnection());
            channel.writeAndFlush(new TextWebSocketFrame(JSONUtils.obj2json(message)));
            return channel;*/

            URI webSocketURI = null;
            try {
                webSocketURI = new URI(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            final ClientHandler handler =
                    new ClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    webSocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
            EventLoopGroup group=new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(1024*1024*10),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    handler);
                        }
                    });
            Channel ch = b.connect(webSocketURI.getHost(),webSocketURI.getPort()).sync().channel();
            handler.handshakeFuture().sync();
            ResponseMessageProcessor.handshake(ch);
            return ch;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
