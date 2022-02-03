import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.client.SingleChannelClient;
import org.fz.nettyx.function.ChannelFutureAction;
import org.fz.nettyx.function.ChannelHandlerContextAction;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.IndexedChannelPipeline;
import org.fz.nettyx.support.ActionableChannelFutureListener;

/**
 * @author fengbinbin
 * @since 2022-02-03 13:16
 **/
public class DemoClient extends SingleChannelClient {

    @Override
    public void connect(SocketAddress address) {
        ChannelFutureListener connectListener = new ActionableChannelFutureListener()
            .whenFailure(connectFailureAction(address))
            .whenSuccess(connectSuccess());

        newBootstrap()
            .connect(address)
            .addListeners(connectListener);
    }

    @Override
    public ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                InboundAdvice inboundAdvice = new InboundAdvice(channel)
                    .whenChannelActive(channelActiveAction())
                    .whenChannelInactive(channelInactiveAction());

                final IndexedChannelPipeline channelPipeline = new IndexedChannelPipeline(channel);

                channel.pipeline()
                    .addLast(inboundAdvice);
            }
        };
    }

    //************************************      private start      ***************************************************//

    private ChannelFutureAction connectSuccess() {
        return ctx -> this.channel = ctx.channel();
    }

    private ChannelFutureAction connectFailureAction(SocketAddress address) {
        return channelFuture -> schedule(() -> this.connect(address), 1, TimeUnit.SECONDS);
    }

    private ChannelHandlerContextAction channelActiveAction() {
        return ctx -> {
        };
    }

    private ChannelHandlerContextAction channelInactiveAction() {
        return ctx -> this.connect(ctx.channel().remoteAddress());
    }

    private ChannelHandlerContextAction readIdleAction() {
        return channel -> {
        };
    }

    //************************************      private end      ***************************************************//
}
