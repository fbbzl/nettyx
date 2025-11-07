package template.websocket;

import cn.hutool.core.lang.Console;
import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LoggingHandler;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.MessageEchoHandler;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.websocket.client.WebSocketClientTemplate;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static codec.UserCodec.TEST_USER;
import static io.netty.handler.logging.ByteBufFormat.HEX_DUMP;
import static org.fz.nettyx.action.ListenerAction.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/11/7 14:28
 */
public class WebSocketClient extends WebSocketClientTemplate {
    public WebSocketClient() {
        super(URI.create("ws://127.0.0.1:9510"));
    }

    @Override
    protected ChannelHandler[] channelHandlers(Channel channel) {
        EscapeMap escapeMap = new EscapeMap(){{
            putHex("7e", "7d5e");
        }};
        return new ChannelHandler[] {
                new StartEndFlagFrameCodec(1024 * 1024, true, "7e")
                , new EscapeCodec(escapeMap)
                , new UserCodec()
                , new MessageEchoHandler(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                super.channelRead(ctx, msg);
                Console.log("client接收到消息：{}", msg);
            }
        }
                , new LoggingHandler(HEX_DUMP)};
    }

    public static void main(String[] args) {
        WebSocketClient testClient = new WebSocketClient();

        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((ls, cf) -> {

                    testClient.writeAndFlush(TEST_USER);

                    Console.log(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((ls, cf) -> Console.log("cancel"))
                .whenFailure(redo(testClient::connect, 10, TimeUnit.SECONDS, 3, (l, c) -> System.err.println("最后次失败后执行")))
                .whenDone((ls, cf) -> Console.log("done"));

        testClient.connect().addListener(listener);
    }
}
