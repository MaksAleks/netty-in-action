package max.learn.netty.echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import max.learn.netty.echo.server.EchoServerHandler;

import java.nio.charset.StandardCharsets;

/**
 * {@link SimpleChannelInboundHandler} vs. {@link ChannelInboundHandler}
 *
 * You may be wondering why we used {@link SimpleChannelInboundHandler} in the client
 * instead of the {@link ChannelInboundHandlerAdapter} used in the EchoServerHandler.
 * This has to do with the interaction of two factors:
 *  - how the business logic processes messages
 *  - how Netty manages resources.
 *
 * In the client, when {@link SimpleChannelInboundHandler#channelRead0()} completes, you have the incoming message
 * and you're done with it.
 *
 * When the method returns, {@link SimpleChannelInboundHandler} takes care of releasing
 * the memory reference to the ByteBuf that holds the message.
 *
 * In {@link EchoServerHandler} you still have to echo the incoming message to the sender,
 * and the write() operation, which is asynchronous, may not complete
 * until after channelRead() returns.
 *
 * For this reason {@link EchoServerHandler} extends {@link ChannelInboundHandlerAdapter},
 * which doesn't release the message at this point.
 *
 * The message is released in channelReadComplete() in the EchoServerHandler when writeAndFlush() is called
 */
@Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Called when a message is received from the server
     *
     * This method is called whenever data is received.
     *
     * Note that the message sent by the server may be received in chunks.
     * That is, if the server sends 5 bytes, there’s no guarantee that all 5 bytes will be received at once.
     * Even for such a small amount of data, the channelRead0() method could be called twice:
     *  - first with a ByteBuf (Netty’s byte container) holding 3 bytes
     *  - and second with a ByteBuf holding 2 bytes.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        System.out.println("Received from server: " + msg.toString(StandardCharsets.UTF_8));
        ctx.close();
    }

    /**
     * Called after the connection to the server is established
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, server!".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Got an error!");
        cause.printStackTrace();
//        if (ctx.channel().isOpen()) {
//            System.out.println("Sending error message to the server..");
//            ctx.writeAndFlush(Unpooled.copiedBuffer(cause.getLocalizedMessage().getBytes(StandardCharsets.UTF_8)))
//                    .addListener(ChannelFutureListener.CLOSE);
//        }
    }
}
