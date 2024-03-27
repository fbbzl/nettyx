package client;

import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.codec.StartEndFlagFrameCodec;
import org.fz.nettyx.handler.LoggerHandler;
import org.fz.nettyx.ssl.OpenSslContextFactory;
import org.fz.nettyx.ssl.OpenSslContextFactory.OpenSslConfig;

import javax.net.ssl.SSLException;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.fz.nettyx.handler.LoggerHandler.Sl4jLevel.INFO;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:59
 */

@Slf4j
public class TestChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    @Override
    protected void initChannel(C channel) throws SSLException {
        // 建议使用 spring的配置bean, 这里为了演示, 直接new
        OpenSslConfig clientSslConfig = new OpenSslConfig();
        clientSslConfig.setCert("/usr/local/yourapp/ssl/client/xxx.cer");
        clientSslConfig.setKey("/usr/local/yourapp/ssl/client/xxx.key");
        clientSslConfig.setKeyPass("aasdf2@##$");
        clientSslConfig.setRoot("/usr/local/yourapp/ssl/root/yyy.cer");

        // 尽量复用SslContext实例, 此处获取的是客户端的sslcontext
        SslContext clientSslContext = new OpenSslContextFactory(clientSslConfig).getClientSslContext();
        SslHandler clientSslHandler       = clientSslContext.newHandler(channel.alloc());

        channel.pipeline().addLast(
                // 如果程序 既要接收ssl又要接收非ssl, 可以使用OptionalSslHandler
                clientSslHandler
                , new StartEndFlagFrameCodec(320, true, wrappedBuffer(new byte[]{(byte) 0x7e}))
                , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                , new UserCodec()
                , new LoggerHandler(log, INFO));
    }
}
