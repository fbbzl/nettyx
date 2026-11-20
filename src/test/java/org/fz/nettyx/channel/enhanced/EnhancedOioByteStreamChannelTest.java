package org.fz.nettyx.channel.enhanced;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelConfig;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.oio.OioEventLoopGroup;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Verifies a read failure closes and deregisters a stream channel.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-14
 */
@SuppressWarnings("deprecation")
public class EnhancedOioByteStreamChannelTest
{
    @Test(timeout = 10_000)
    public void readFailureCompletesCloseAndDeregistration() throws Exception
    {
        OioEventLoopGroup group = new OioEventLoopGroup(1);
        FailingReadChannel channel = new FailingReadChannel();
        AtomicInteger inactiveEvents = new AtomicInteger();
        AtomicInteger unregisteredEvents = new AtomicInteger();
        CountDownLatch unregistered = new CountDownLatch(1);
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception
            {
                inactiveEvents.incrementAndGet();
                super.channelInactive(ctx);
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
            {
                unregisteredEvents.incrementAndGet();
                unregistered.countDown();
                super.channelUnregistered(ctx);
            }
        });

        try {
            group.register(channel).sync();
            channel.read();
            assertTrue(channel.readAttempt.await(5, TimeUnit.SECONDS));
            assertTrue(unregistered.await(5, TimeUnit.SECONDS));

            assertFalse(channel.isOpen());
            assertTrue(channel.closeFuture().isDone());
            assertFalse(channel.isRegistered());
            assertEquals(1, inactiveEvents.get());
            assertEquals(1, unregisteredEvents.get());
        }
        finally {
            channel.close().await(5, TimeUnit.SECONDS);
            group.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
        }
    }

    /**
     * Simulates a stream read failure and signals when the read is attempted.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    private static final class FailingReadChannel extends EnhancedOioByteStreamChannel
    {
        private final ChannelConfig config = new DefaultChannelConfig(this).setAutoRead(false);
        private final CountDownLatch readAttempt = new CountDownLatch(1);
        private volatile boolean open = true;

        private FailingReadChannel()
        {
            activate(new InputStream() {
                @Override public int available() { return 1; }
                @Override public int read() throws IOException {
                    readAttempt.countDown();
                    throw new IOException("probe read failure");
                }
                @Override public int read(byte[] bytes, int offset, int length) throws IOException {
                    readAttempt.countDown();
                    throw new IOException("probe read failure");
                }
            }, new ByteArrayOutputStream());
        }

        @Override public ChannelConfig config() { return config; }
        @Override public boolean isOpen() { return open; }
        @Override protected boolean isInputShutdown() { return !open; }
        @Override protected void doBind(SocketAddress local) { }
        @Override protected void doConnect(SocketAddress remote, SocketAddress local) { }
        @Override protected SocketAddress localAddress0() { return new InetSocketAddress(0); }
        @Override protected SocketAddress remoteAddress0() { return new InetSocketAddress(1); }

        @Override
        protected void doClose() throws Exception
        {
            open = false;
            super.doClose();
        }
    }
}
