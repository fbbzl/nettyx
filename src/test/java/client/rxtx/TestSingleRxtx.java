package client.rxtx;


import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.rxtx.RxtxChannelOption.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelClient {

    protected TestSingleRxtx(RxtxDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelFutureAction whenConnectSuccess() {
        return cf -> {

            System.err.println(cf.channel().localAddress() + ": ok");
        };
    }

    @Override
    protected ChannelFutureAction whenConnectFailure() {
        return cf -> {
            System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
            cf.channel().eventLoop().schedule(this::connect, 2, TimeUnit.SECONDS);
        };
    }

    @Override
    protected void doChannelConfig(RxtxChannel channel) {
        RxtxChannelConfig config = channel.config();
    }

    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return super.newBootstrap(remoteAddress)
                    .option(BAUD_RATE, 115200)
                    .option(DATA_BITS, Databits.DATABITS_8)
                    .option(STOP_BITS, Stopbits.STOPBITS_1)
                    .option(PARITY_BIT, Paritybit.NONE)
                    .option(DTR, false)
                    .option(RTS, false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx(new RxtxDeviceAddress("COM2"));
        testSingleRxtx.connect();
    }
}
