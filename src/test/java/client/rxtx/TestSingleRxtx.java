package client.rxtx;


import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxDeviceAddress;
import org.fz.nettyx.util.HexKit;

import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.rxtx.RxtxChannelOption.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelClient {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    protected TestSingleRxtx(XRxtxDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelFutureAction whenConnectSuccess() {
        return cf -> {
            this.writeAndFlush(Unpooled.wrappedBuffer(HexKit.decode("ffffffffffffffff")));
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
    protected void doChannelConfig(XRxtxChannel channel) {
        RxtxChannelConfig config = channel.config();
    }

    @Override
    protected ChannelInitializer<XRxtxChannel> channelInitializer() {
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
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx(new XRxtxDeviceAddress("COM3"));
        testSingleRxtx.connect();
    }
}
