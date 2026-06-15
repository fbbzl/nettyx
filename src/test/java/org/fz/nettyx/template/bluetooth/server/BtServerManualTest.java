package org.fz.nettyx.template.bluetooth.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.template.TestChannelInitializer;

import java.util.concurrent.CountDownLatch;

public class BtServerManualTest extends BtServerTemplate {

    public static final String serverName = "Bluetooth Test";
    public static final String serverUUID = "1000110100001000800000805F9B34FB";

    public BtServerManualTest() {
        super(serverUUID, serverName);
    }

    @Override
    protected ChannelInitializer<? extends Channel> childChannelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) throws InterruptedException {
        BtServerManualTest server   = new BtServerManualTest();
        CountDownLatch     shutdown = new CountDownLatch(1);

        server.bind().addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("BtServer started, waiting for connection...");
            } else {
                f.cause().printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownGracefully();
            System.out.println("BtServer shutdown");
            shutdown.countDown();
        }));

        shutdown.await();
    }

}
