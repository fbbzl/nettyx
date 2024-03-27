package client;

import codec.UserCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
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
        OpenSslConfig serverSslConfig = new OpenSslConfig();
        serverSslConfig.setCert("/usr/local/yourapp/ssl/server/xxx.cer");
        serverSslConfig.setKey("/usr/local/yourapp/ssl/server/xxx.key");
        serverSslConfig.setKeyPass("aasdf2@##$");
        serverSslConfig.setRoot("/usr/local/yourapp/ssl/root/yyy.cer");

        SslContext serverSslContext = new OpenSslContextFactory(serverSslConfig).getServerSslContext();

        channel.pipeline().addLast(
                // 如果程序 既要接收ssl又要接收非ssl, 可以使用OptionalSslHandler
                new OptionalSslHandler(serverSslContext)
                , new StartEndFlagFrameCodec(320, true, wrappedBuffer(new byte[]{(byte) 0x7e}))
                , new EscapeCodec(EscapeMap.mapHex("7e", "7d5e"))
                , new UserCodec()
                , new LoggerHandler(log, INFO));
    }
}
