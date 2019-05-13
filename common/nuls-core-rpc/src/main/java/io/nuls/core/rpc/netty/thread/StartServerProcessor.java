package io.nuls.core.rpc.netty.thread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.netty.initializer.ServerInitializer;

import java.net.InetSocketAddress;

/**
 * 消息处理器
 * Send message processor
 *
 * @author tag
 * 2019/2/26
 */
public class StartServerProcessor implements Runnable {

    private static final String PATH = "/ws";

    private int port;

    private String path;

    private String host;

    public StartServerProcessor(int port) {
        this.port = port;
        this.path = PATH;
        this.host = HostInfo.getLocalIP();
    }

    public StartServerProcessor(int port, String host, String path) {
        this.port = port;
        this.path = path;
        this.host = host;
    }

    @Override
    public void run() {
        /*
         * 用于处理客户端链接的线程组
         * */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        /*
         * 用来进行网络通讯读写的线程组
         * */
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ServerInitializer(path))
            ;
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(host, port)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
