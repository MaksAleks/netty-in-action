package max.learn.netty.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;

/**
 * Bootstrapping the server comprises two main steps:
 *  - Bind to the port on which server will be listening for incoming connections
 *  - Configure {@link io.netty.channel.Channel} by adding {@link io.netty.channel.ChannelHandler}s
 *    to Channel's pipeline, so that channel can notify the handlers about events.
 *
 * The following steps are required in bootstrapping our echo server:
 * - Create a {@link io.netty.bootstrap.ServerBootstrap instance to bootstrap and bind the server.
 * - Create and assign an {@link io.netty.channel.nio.NioEventLoopGroup} instance to handle event processing,
 * such as accepting new connections and reading/writing data.
 * - Specify the local {@link java.net.InetSocketAddress} to which the server binds.
 * - Initialize each new {@link io.netty.channel.Channel} with an EchoServerHandler instance
 * - Call {@link io.netty.bootstrap.ServerBootstrap#bind()} to bind the server.
 */
public class EchoServer {

    public static void main(String[] args) throws InterruptedException {
        EchoServer server = new EchoServer();
        server.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(8080))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync(); // binds the server - it starts listening on the specified port
            future.channel().closeFuture().sync(); // the application will wait until the server’s Channel closes
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
