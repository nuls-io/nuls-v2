package io.nuls.rpc.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.nuls.rpc.netty.handler.ClientHandler;
import io.nuls.tools.log.Log;

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
    public static SocketChannel createConnect(String uri){
        try {
            EventLoopGroup group=new NioEventLoopGroup();
            Bootstrap boot=new Bootstrap();
            boot.option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_BACKLOG,1024*1024*10)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new ChannelHandler[]{new HttpClientCodec(),
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
            return (SocketChannel) channel;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
