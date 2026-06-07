package org.fz.nettyx.template.bluetooth.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import org.fz.nettyx.channel.bluetooth.server.BtServerChannel;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import java.net.InetSocketAddress;

@Getter
@SuppressWarnings("deprecation")
public abstract class BtServerTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BtServerTemplate.class);
    private final        EventLoopGroup parentEventLoopGroup, childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;

    protected BtServerTemplate(String serverUUID) {
        this(serverUUID, null, false, false, false);
    }

    protected BtServerTemplate(String serverUUID, String serverName) {
        this(serverUUID, serverName, false, false, false);
    }

    protected BtServerTemplate(String serverUUID, String serverName, boolean authenticate, boolean encrypt, boolean master) {
        try {
            LocalDevice local = LocalDevice.getLocalDevice();
            if (!local.setDiscoverable(DiscoveryAgent.GIAC)) {
                log.warn("Failed to set bluetooth device discoverable");
            }

            StringBuilder url = new StringBuilder("btspp://localhost:").append(serverUUID);
            if (serverName != null) url.append(";name=").append(serverName);
            if (authenticate) url.append(";authenticate=true");
            if (encrypt) url.append(";encrypt=true");
            if (master) url.append(";master=true");

            StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(url.toString());
            this.childEventLoopGroup = childEventLoopGroup();
            this.parentEventLoopGroup = parentEventLoopGroup();
            this.serverBootstrap = newServerBootstrap(notifier);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Bluetooth server", e);
        }
    }

    protected EventLoopGroup parentEventLoopGroup() {
        return new OioEventLoopGroup();
    }

    protected EventLoopGroup childEventLoopGroup() {
        return new OioEventLoopGroup();
    }

    public ChannelFuture bind() {
        ChannelFuture bindFuture = this.getServerBootstrap().clone().bind();
        log.debug("already finish bind, bind future is [{}]", bindFuture);
        return bindFuture;
    }

    protected ServerBootstrap newServerBootstrap(StreamConnectionNotifier notifier) {
        return new ServerBootstrap()
                .group(parentEventLoopGroup, childEventLoopGroup)
                .localAddress(new InetSocketAddress(0))
                .channelFactory(() -> new BtServerChannel(notifier))
                .childHandler(childChannelInitializer());
    }

    protected abstract ChannelInitializer<? extends Channel> childChannelInitializer();

    protected void shutdownGracefully() {
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException {
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
        log.debug("has already successfully shutdown");
    }

}
