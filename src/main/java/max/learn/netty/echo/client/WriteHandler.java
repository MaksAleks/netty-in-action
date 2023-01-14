package max.learn.netty.echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class WriteHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("Connected to " + remoteAddress);
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Sending " + ((ByteBuf) msg).toString(StandardCharsets.UTF_8).strip() +  " to :8080");
        ctx.writeAndFlush(msg, promise);
    }
}
