package template.websocket;

import cn.hutool.core.lang.Console;
import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LoggingHandler;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.MessageEchoHandler;
import org.fz.nettyx.template.websocket.server.WebSocketServerTemplate;

import static io.netty.handler.logging.ByteBufFormat.HEX_DUMP;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/11/7 14:22
 */
public class WebSocketServer extends WebSocketServerTemplate {
    protected WebSocketServer() {
        super(9510);
    }

    @Override
    protected ChannelHandler[] childChannelHandlers(Channel channel) {
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
                        Console.log("server接收到消息：{}", msg);
                    }
                }
                , new LoggingHandler(HEX_DUMP)};
    }

    public static void main(String[] args) {
        WebSocketServer webSocketServer = new WebSocketServer();
        ChannelFuture   bindFuture      = webSocketServer.bind();
        bindFuture.addListener(cf -> Console.log("binding state:" + cf.isSuccess()));
        bindFuture.channel().closeFuture().addListener(cf -> {
            Console.log("关闭了");
            webSocketServer.shutdownGracefully();
        });

    }
}
