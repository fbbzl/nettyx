import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.client.SingleChannelClient;

/**
 * @author fengbinbin
 * @since 2022-02-08 20:32
 **/
public class DemoClient extends SingleChannelClient {

    @Override
    public void connect(SocketAddress address) {
        super.newBootstrap().handler(channelInitializer()).connect(address);
    }

    @Override
    public ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ch.pipeline()
                    .addLast(new IdleStateHandler(1, 4, 0, TimeUnit.SECONDS))
                    .addLast(new StringEncoder(UTF_8))
                    .addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.err.println(ctx.channel().remoteAddress());
                            super.channelActive(ctx);
                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                            System.out.println("客户端循环心跳监测发送: ");
                            if (evt instanceof IdleStateEvent) {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    ctx.writeAndFlush("biubiu");
                                }
                            }
                        }
                    });
            }
        };
    }

    //https://blog.csdn.net/u013967175/article/details/78591810
    public static void main(String[] args) {
        DemoClient demoClient = new DemoClient();

        demoClient.connect(new InetSocketAddress(5655));

        for (int i = 0; i < 9; i++) {
            demoClient.send(1);
        }

    }
}
