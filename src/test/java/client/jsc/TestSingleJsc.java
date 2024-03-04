package client.jsc;


import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.jsc.SingleJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.StopBits;
import org.fz.nettyx.endpoint.client.jsc.support.JscDeviceAddress;
import org.fz.nettyx.util.HexKit;

import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/28 14:49
 */

@Slf4j
public class TestSingleJsc extends SingleJscChannelClient {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    protected TestSingleJsc(JscDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelFutureAction whenConnectSuccess() {
        return cf -> {
            executorService.scheduleAtFixedRate(() -> {
                 this.writeAndFlush(Unpooled.wrappedBuffer(HexKit.decode("ffffffffffffffff")));
            } , 2000,200,TimeUnit.MILLISECONDS);
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
    protected ChannelFutureAction whenConnectDone() {
        return cf -> System.err.println("done");
    }

    @Override
    protected ChannelFutureAction whenConnectCancel() {
        return cf -> System.err.println("cancel");
    }

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(JscChannel channel) {
        super.doChannelConfig(channel);
    }

    @Override
    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return super.newBootstrap(remoteAddress)
                    .option(BAUD_RATE, 115200)
                    .option(DATA_BITS, 8)
                    .option(STOP_BITS, StopBits.ONE_STOP_BIT)
                    .option(PARITY_BIT, ParityBit.NO_PARITY);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleRxtx = new TestSingleJsc(new JscDeviceAddress("COM2"));
        testSingleRxtx.connect();
    }

}
