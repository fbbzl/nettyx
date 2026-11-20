package org.fz.nettyx.template.bluetooth.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelFuture;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.bluetooth.server.BtServerChannel;
import org.junit.Test;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Checks Bluetooth server registration and notifier and event loop cleanup.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-13
 */
public class BtServerTemplateTest {

    @Test(timeout = 10_000)
    public void serverChannelRegistersWithItsOioEventLoop() throws Exception {
        CountingNotifier notifier = new CountingNotifier();
        BtServerChannel channel = new BtServerChannel(notifier);
        OioEventLoopGroup group = new OioEventLoopGroup(1);
        channel.config().setAutoRead(false);
        try {
            ChannelFuture registration = group.register(channel).sync();
            assertTrue(registration.isSuccess());
            channel.close().sync();
            assertEquals(1, notifier.closes);
        } finally {
            if (channel.isOpen()) channel.unsafe().closeForcibly();
            group.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
        }
    }

    @Test(timeout = 10_000)
    public void shutdownBeforeBindClosesNotifierOnce() throws Exception {
        CountingNotifier notifier = new CountingNotifier();
        TestTemplate template = new TestTemplate(notifier);
        try {
            template.shutdownGracefully();
            template.syncShutdownGracefully();
            assertEquals(1, notifier.closes);
            assertThrows(IllegalStateException.class,
                         () -> template.getServerBootstrap().config().channelFactory().newChannel());
        } finally {
            template.syncShutdownGracefully();
        }
    }

    @Test(timeout = 10_000)
    public void initializationFailureClosesNotifierAndCreatedGroups() throws Exception {
        CountingNotifier notifier = new CountingNotifier();
        EventLoopGroup child = new DefaultEventLoopGroup(1), parent = new DefaultEventLoopGroup(1);
        try {
            assertThrows(IllegalStateException.class, () -> new TestTemplate(notifier) {
                @Override protected EventLoopGroup childEventLoopGroup() { return child; }
                @Override protected EventLoopGroup parentEventLoopGroup() { return parent; }
                @Override protected ChannelInitializer<Channel> childChannelInitializer() {
                    throw new IllegalStateException("initializer failed");
                }
            });
            assertEquals(1, notifier.closes);
            assertTrue(child.isShuttingDown());
            assertTrue(parent.isShuttingDown());
        } finally {
            child.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
            parent.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
        }
    }

    @Test(timeout = 10_000)
    public void channelClosesNotifierAfterOwnershipTransfer() throws Exception {
        CountingNotifier notifier = new CountingNotifier();
        TestTemplate template = new TestTemplate(notifier);
        Channel channel = template.getServerBootstrap().config().channelFactory().newChannel();
        try {
            template.shutdownGracefully();
            assertEquals(0, notifier.closes);
            assertThrows(IllegalStateException.class,
                         () -> template.getServerBootstrap().config().channelFactory().newChannel());
            channel.unsafe().closeForcibly();
            assertEquals(1, notifier.closes);
        } finally {
            if (channel.isOpen()) channel.unsafe().closeForcibly();
            template.syncShutdownGracefully();
        }
    }

    /**
     * Supplies a child channel initializer for Bluetooth server template tests.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-13
     */
    private static class TestTemplate extends BtServerTemplate {
        private TestTemplate(StreamConnectionNotifier notifier) { super(notifier); }
        @Override protected EventLoopGroup childEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected EventLoopGroup parentEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected ChannelInitializer<Channel> childChannelInitializer() {
            return new ChannelInitializer<>() {
                @Override protected void initChannel(Channel channel) { }
            };
        }
    }

    /**
     * Records how often the Bluetooth stream notifier is closed.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-13
     */
    private static final class CountingNotifier implements StreamConnectionNotifier {
        private int closes;
        @Override public StreamConnection acceptAndOpen() { throw new AssertionError("hardware access"); }
        @Override public void close() { closes++; }
    }
}
