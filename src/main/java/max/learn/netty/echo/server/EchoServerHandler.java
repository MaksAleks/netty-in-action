package max.learn.netty.echo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * In Netty applications, all data-processing logic is contained in implementations of ChannelHandler interfaces.
 * There are different types of predefined implementations for different event types;
 * For example:
 * - {@link io.netty.channel.ChannelInboundHandlerAdapter}
 * - {@link io.netty.channel.ChannelOutboundHandlerAdapter}
 * ...
 *
 * Because your Echo server will respond to incoming messages,
 * it will need to implement interface {@link io.netty.channel.ChannelInboundHandler},
 * which defines methods for acting on inbound events.
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * called for incomming message
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf received = (ByteBuf) msg;
        System.out.println("Received: " + received.toString(StandardCharsets.UTF_8));
        /*
         * This method will not request to actual flush, and the data will be in pending state.
         * To flush all pending data you have to call ctx.flush() or use ctx.writeAndFlush() to flush right after
         * the write operation.
         */
        ctx.write(msg);
    }

    /**
     * Invoked when the last message read by the current read operation has been consumed by
     * {@link #channelRead(ChannelHandlerContext, Object)}
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ChannelFuture channelFuture = ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
        /*
         * A ChannelFutureListener that closes the Channel which is
         * associated with the specified ChannelFuture.
         */
//        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel " + ctx.channel().remoteAddress() + " was closed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.writeAndFlush(Unpooled.wrappedBuffer(cause.getLocalizedMessage().getBytes(StandardCharsets.UTF_8)))
                .addListener(ChannelFutureListener.CLOSE);
    }
}
