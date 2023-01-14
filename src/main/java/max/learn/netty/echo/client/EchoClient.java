package max.learn.netty.echo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoClient implements AutoCloseable {

    private EventLoopGroup loopGroup;

    private Bootstrap bootstrap;

    private int poolSize = 10;

    private WriteHandler writeHandler = new WriteHandler();

    //    private BlockingQueue<ChannelFuture> connectionPool = new ArrayBlockingQueue<>(poolSize);

    public static void main(String[] args) throws InterruptedException {
        try (EchoClient client = new EchoClient()) {
            CountDownLatch latch = new CountDownLatch(100);
            AtomicInteger cnt = new AtomicInteger(100);
            for (int i = 0; i < 100; i++) {
                int num = i;
                String msg = Thread.currentThread().getName() + ": msg-" + (num + 1);
                client.send(msg).thenAccept(answer -> {
                    System.out.println("[" + Thread.currentThread().getName() + "] Got answer: " + answer + ". Left " + cnt.decrementAndGet());
                    latch.countDown();
                });
            }
            latch.await();
        }
        System.out.println("Finish");
    }

    public EchoClient() {
        start();
    }

    private ChannelFuture connect() {
        try {
//            for (int i = 0; i < poolSize; i++) {
//                connectionPool.offer(

//                );
//            }
            return bootstrap.connect()
                    .sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            close();
            throw new RuntimeException("Error: " + ex);
        }
    }

    public CompletableFuture<String> send(String msg) {
        if (!msg.endsWith("\r\n")) {
            msg = msg + "\r\n";
        }
        String msgId = UUID.randomUUID().toString();
        CompletableFuture<String> cf = new CompletableFuture<>();
        connect().channel().pipeline()
                .addLast(msgId, new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg1) throws Exception {
                        cf.complete(msg1.toString(StandardCharsets.UTF_8));
                        ctx.pipeline().remove(msgId);
                    }
                })
                .writeAndFlush(Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8)))
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().pipeline().read();
                        } else {
                            System.out.println("Error writing to channel: " + future.cause().getMessage());
                        }
                    }
                });
        return cf;
    }

    private void start() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap()
                .group(group)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(writeHandler);
                    }
                })
                .remoteAddress(new InetSocketAddress("localhost", 8080))
                .channel(NioSocketChannel.class);
        this.loopGroup = group;
    }

    @Override
    public void close() {
        invokeQuietly(() -> {
//            for (ChannelFuture cf : connectionPool) {
//                cf.channel().close().sync();
//            }
            loopGroup.shutdownGracefully().sync();
        });
    }

    private void invokeQuietly(Closer closer) {
        try {
            closer.close();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface Closer {
        void close() throws InterruptedException;
    }
}
