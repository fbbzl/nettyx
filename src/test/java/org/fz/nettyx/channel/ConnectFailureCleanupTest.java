package org.fz.nettyx.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;

/**
 * Verifies failed serial and Bluetooth connects release channel resources.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-18
 */
@SuppressWarnings("deprecation")
public class ConnectFailureCleanupTest
{
    @Test(timeout = 10_000)
    public void serialConnectFailureClosesOpenedPort() throws Exception
    {
        IOException failure = new IOException("simulated serial setup failure");
        FailingSerialChannel channel = new FailingSerialChannel(failure);

        assertConnectFailureCloses(channel, new SerialCommChannel.SerialCommAddress("COM-test"), failure);
        assertFalse(channel.resourceOpen);
        assertEquals(1, channel.closeCount.get());
    }

    @Test(timeout = 10_000)
    public void bluetoothConnectFailureClosesAllocatedConnection() throws Exception
    {
        IOException failure = new IOException("simulated Bluetooth setup failure");
        FailingBluetoothChannel channel = new FailingBluetoothChannel(failure);

        assertConnectFailureCloses(channel, new BtDeviceAddress("bt-test"), failure);
        assertFalse(channel.resourceOpen);
        assertEquals(1, channel.closeCount.get());
    }

    @Test(timeout = 10_000)
    public void connectFailureListenerCannotReconnectSerialChannel() throws Exception
    {
        IOException failure = new IOException("simulated serial setup failure");
        FailingSerialChannel channel = new FailingSerialChannel(failure);

        assertFailureListenerCannotReconnect(channel, new SerialCommChannel.SerialCommAddress("COM-test"), failure,
                                             channel.connectAttempts, channel.closeCount, () -> channel.resourceOpen);
    }

    @Test(timeout = 10_000)
    public void connectFailureListenerCannotReconnectBluetoothChannel() throws Exception
    {
        IOException failure = new IOException("simulated Bluetooth setup failure");
        FailingBluetoothChannel channel = new FailingBluetoothChannel(failure);

        assertFailureListenerCannotReconnect(channel, new BtDeviceAddress("bt-test"), failure,
                                             channel.connectAttempts, channel.closeCount, () -> channel.resourceOpen);
    }

    private static void assertConnectFailureCloses(Channel channel, SocketAddress remoteAddress, Throwable failure)
            throws Exception
    {
        OioEventLoopGroup group = new OioEventLoopGroup(1);
        CountDownLatch unregistered = observeUnregistration(channel);
        try {
            group.register(channel).sync();

            ChannelFuture connect = channel.connect(remoteAddress);
            assertTrue(connect.await(5, TimeUnit.SECONDS));
            assertSame(failure, connect.cause());
            assertTrue(channel.closeFuture().await(5, TimeUnit.SECONDS));
            assertTrue(unregistered.await(5, TimeUnit.SECONDS));
            assertFalse(channel.isOpen());
            assertFalse(channel.isRegistered());
        }
        finally {
            channel.close().await(5, TimeUnit.SECONDS);
            assertTrue(group.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).await(5, TimeUnit.SECONDS));
        }
    }

    private static void assertFailureListenerCannotReconnect(
            Channel channel,
            SocketAddress remoteAddress,
            Throwable failure,
            AtomicInteger connectAttempts,
            AtomicInteger closeCount,
            BooleanSupplier resourceOpen) throws Exception
    {
        OioEventLoopGroup group = new OioEventLoopGroup(1);
        AtomicReference<ChannelFuture> retry = new AtomicReference<>();
        CountDownLatch unregistered = observeUnregistration(channel);
        try {
            group.register(channel).sync();

            ChannelPromise connect = channel.newPromise();
            connect.addListener(ignored -> retry.set(channel.connect(remoteAddress)));
            channel.eventLoop().submit(() -> channel.unsafe().connect(remoteAddress, null, connect)).sync();

            assertSame(failure, connect.cause());
            assertNotNull(retry.get());
            assertTrue(retry.get().cause() instanceof ClosedChannelException);
            assertTrue(channel.closeFuture().await(5, TimeUnit.SECONDS));
            assertTrue(unregistered.await(5, TimeUnit.SECONDS));
            assertFalse(channel.isOpen());
            assertFalse(channel.isRegistered());
            assertFalse(resourceOpen.getAsBoolean());
            assertEquals(1, connectAttempts.get());
            assertEquals(1, closeCount.get());
        }
        finally {
            channel.close().await(5, TimeUnit.SECONDS);
            assertTrue(group.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).await(5, TimeUnit.SECONDS));
        }
    }

    private static CountDownLatch observeUnregistration(Channel channel)
    {
        CountDownLatch unregistered = new CountDownLatch(1);
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
            {
                unregistered.countDown();
                super.channelUnregistered(ctx);
            }
        });
        return unregistered;
    }

    private static final class FailingSerialChannel extends SerialCommChannel
    {
        private final IOException failure;
        private final ChannelConfig config = new DefaultChannelConfig(this);
        private volatile boolean resourceOpen;
        private final AtomicInteger connectAttempts = new AtomicInteger();
        private final AtomicInteger closeCount = new AtomicInteger();

        private FailingSerialChannel(IOException failure)
        {
            this.failure = failure;
        }

        @Override public ChannelConfig config() { return config; }

        @Override
        protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception
        {
            if (connectAttempts.incrementAndGet() == 1) {
                resourceOpen = true;
                throw failure;
            }
            resourceOpen = true;
        }

        @Override protected InputStream getInputStream() { return InputStream.nullInputStream(); }
        @Override protected OutputStream getOutputStream() { return OutputStream.nullOutputStream(); }

        @Override
        protected void doClose() throws Exception
        {
            closeCount.incrementAndGet();
            resourceOpen = false;
            super.doClose();
        }
    }

    private static final class FailingBluetoothChannel extends BtChannel
    {
        private final IOException failure;
        private volatile boolean resourceOpen;
        private final AtomicInteger connectAttempts = new AtomicInteger();
        private final AtomicInteger closeCount = new AtomicInteger();

        private FailingBluetoothChannel(IOException failure)
        {
            this.failure = failure;
        }

        @Override
        protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception
        {
            if (connectAttempts.incrementAndGet() == 1) {
                resourceOpen = true;
                throw failure;
            }
            resourceOpen = true;
        }

        @Override
        protected void doClose() throws Exception
        {
            closeCount.incrementAndGet();
            resourceOpen = false;
            super.doClose();
        }
    }
}
