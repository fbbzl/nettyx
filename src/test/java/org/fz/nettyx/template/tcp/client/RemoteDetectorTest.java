package org.fz.nettyx.template.tcp.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RemoteDetectorTest {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);

    private Channel serverChannel;
    private CountDownLatch received;

    @Before
    public void startServer() {
        received = new CountDownLatch(1);
        serverChannel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ReferenceCountUtil.release(msg);
                                received.countDown();
                            }
                        });
                    }
                })
                .bind("127.0.0.1", 0)
                .syncUninterruptibly()
                .channel();
    }

    @After
    public void stopServer() {
        if (serverChannel != null) serverChannel.close().syncUninterruptibly();
        workerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
        bossGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
    }

    @Test
    public void connectedChannelCanSendDetectMessage() throws Exception {
        TestDetector detector = newDetector();
        detector.setDetectRetryTimes(1);
        detector.setWaitResponseMillis(10);

        try {
            assertFalse(detector.doDetect());
            assertTrue("detect message was not received", received.await(1, TimeUnit.SECONDS));
        }
        finally {
            detector.shutdown();
        }
    }

    @Test
    public void retryCreatesDetectMessageForEveryAttempt() throws Exception {
        TestDetector detector = newDetector();
        detector.setDetectRetryTimes(3);
        detector.setWaitResponseMillis(1);

        try {
            assertFalse(detector.doDetect());
            assertEquals(3, detector.detectMessageCount());
        }
        finally {
            detector.shutdown();
        }
    }

    @Test
    public void interruptIsPropagatedAndRestored() throws Exception {
        TestDetector detector = newDetector();
        detector.setDetectRetryTimes(3);
        detector.setWaitResponseMillis(30_000);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread detectThread = new Thread(() -> {
            try {
                detector.doDetect();
            }
            catch (Throwable error) {
                failure.set(error);
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        });

        try {
            detectThread.start();
            assertTrue("detect message was not received", received.await(1, TimeUnit.SECONDS));
            detectThread.interrupt();
            detectThread.join(2_000);

            assertFalse("detect thread did not stop", detectThread.isAlive());
            assertTrue(failure.get() instanceof InterruptedException);
            assertTrue("interrupt state was not restored", interrupted.get());
        }
        finally {
            if (detectThread.isAlive()) detectThread.interrupt();
            detector.shutdown();
        }
    }

    @Test
    public void connectFailureKeepsOriginalCause() throws Exception {
        InetSocketAddress unavailableAddress;
        try (ServerSocket socket = new ServerSocket(0)) {
            unavailableAddress = new InetSocketAddress("127.0.0.1", socket.getLocalPort());
        }
        TestDetector detector = new TestDetector(unavailableAddress);

        try {
            ConnectException error = assertThrows(ConnectException.class, detector::doDetect);
            assertNotNull(error.getCause());
        }
        finally {
            detector.shutdown();
        }
    }

    private TestDetector newDetector() {
        return new TestDetector((InetSocketAddress) serverChannel.localAddress());
    }

    private static final class TestDetector extends RemoteDetector<ByteBuf> {

        private final AtomicInteger detectMessageCount = new AtomicInteger();

        private TestDetector(InetSocketAddress address) {
            super(address);
        }

        @Override
        public boolean checkResponse(ByteBuf response) {
            return false;
        }

        @Override
        public void initDetectChannel(NioSocketChannel channel) {
        }

        @Override
        public ByteBuf getDetectMessage() {
            detectMessageCount.incrementAndGet();
            return Unpooled.wrappedBuffer(new byte[]{ 1 });
        }

        private int detectMessageCount() {
            return detectMessageCount.get();
        }

        private void shutdown() {
            getEventLoopGroup().shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
        }
    }
}
