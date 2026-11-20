package org.fz.nettyx.template;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.bootstrap.Bootstrap;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.net.SocketAddress;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.BrokenBarrierException;

import static org.junit.Assert.*;

/**
 * Checks generic channel resolution, channel storage, and cleanup in single and multiple channel templates.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-14
 */
public class TemplateCoverageTest
{
    @Test
    public void channelClassResolvesThroughReorderedSuperclassParameters()
    {
        InheritedTemplate template = new InheritedTemplate();
        try {
            assertEquals(EmbeddedChannel.class, template.getChannelClass());
        }
        finally {
            template.shutdownGracefully();
        }
    }

    @Test
    public void templateReportsChannelStatesAndFailurePromises()
    {
        ProbeTemplate template = new ProbeTemplate();
        EmbeddedChannel active = new EmbeddedChannel();
        try {
            assertEquals(EmbeddedChannel.class, template.getChannelClass());
            assertTrue(template.isRegistered(active));
            assertTrue(template.isOpen(active));
            assertTrue(template.isActive(active));
            assertTrue(template.isWritable(active));
            assertFalse(template.notRegistered(active));
            assertFalse(template.notOpen(active));
            assertFalse(template.notActive(active));
            assertFalse(template.notWritable(active));
            ChannelPromise promise = template.failurePromise(active, "failure");
            assertTrue(promise.isDone());
            assertFalse(promise.isSuccess());
            assertThrows(io.netty.channel.ChannelException.class, () -> template.failurePromise(null));
        }
        finally {
            active.finishAndReleaseAll();
            template.shutdown();
        }
        EmbeddedChannel graceful = new EmbeddedChannel();
        assertTrue(Template.gracefullyCloseable(graceful));
        graceful.finishAndReleaseAll();
        assertFalse(Template.gracefullyCloseable(null));
    }

    @Test
    public void channelStorageQueriesAndFiltersAllEntries()
    {
        EmbeddedChannel active = new EmbeddedChannel();
        EmbeddedChannel closed = new EmbeddedChannel();
        closed.close();
        try {
            AbstractMultiChannelTemplate.ChannelStorage<String> storage =
                    new AbstractMultiChannelTemplate.ChannelStorage<>(Map.of("active", active, "closed", closed));
            assertEquals(2, storage.size());
            assertFalse(storage.isAllActive());
            assertFalse(storage.isAllOpen());
            assertFalse(storage.isAllWritable());
            assertFalse(storage.isAllRegistered());
            assertEquals(List.of(active), storage.findAllActive());
            assertEquals(List.of(active), storage.findAllOpen());
            assertEquals(List.of(active), storage.findAllWritable());
            assertEquals(List.of(active), storage.findAllRegistered());
            assertEquals(List.of(active), storage.findAll(Channel::isActive));
            assertFalse(storage.isAll(Channel::isActive));
            assertTrue(new AbstractMultiChannelTemplate.ChannelStorage<String>().isAllActive());
            assertTrue(new AbstractMultiChannelTemplate.ChannelStorage<String>(4).isEmpty());
            assertTrue(new AbstractMultiChannelTemplate.ChannelStorage<String>(4, 0.75f).isEmpty());
            assertTrue(new AbstractMultiChannelTemplate.ChannelStorage<String>(4, 0.75f, 1).isEmpty());
            String storageText = storage.toString();
            assertNotNull(storageText);
            assertTrue(storageText.contains("active=" + active));
            assertTrue(storageText.contains("closed=" + closed));
        }
        finally {
            active.finishAndReleaseAll();
            closed.finishAndReleaseAll();
        }
    }

    @Test
    public void channelStorageDoesNotExposeMapMutationMethodsOrMutableViews() throws Exception
    {
        EmbeddedChannel channel = new EmbeddedChannel();
        AbstractMultiChannelTemplate.ChannelStorage<String> storage =
                new AbstractMultiChannelTemplate.ChannelStorage<>(Map.of("one", channel));
        try {
            assertSame(channel, storage.get("one"));
            assertThrows(NoSuchMethodException.class,
                         () -> storage.getClass().getMethod("put", Object.class, Object.class));
            assertThrows(NoSuchMethodException.class, () -> storage.getClass().getMethod("clear"));
            assertThrows(UnsupportedOperationException.class, () -> storage.values().clear());
            assertThrows(UnsupportedOperationException.class,
                         () -> storage.entrySet().iterator().next().setValue(channel));
        }
        finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void failedSingleAndMultiChannelConfigurationClosesCreatedChannels()
    {
        FailingSingleTemplate single = new FailingSingleTemplate();
        FailingMultiTemplate multi = new FailingMultiTemplate();
        try {
            assertThrows(IllegalStateException.class,
                         () -> single.getBootstrap().config().channelFactory().newChannel());
            assertFalse(ObservedSocketChannel.latest.isOpen());

            assertThrows(IllegalStateException.class,
                         () -> multi.getBootstrapMap().get("one").config().channelFactory().newChannel());
            assertFalse(ObservedSocketChannel.latest.isOpen());
        }
        finally {
            single.shutdown();
            multi.shutdown();
        }
    }

    @Test
    public void multiTemplateStoresWritesAndResolvesKeys()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel first = new EmbeddedChannel();
        EmbeddedChannel replacement = new EmbeddedChannel();
        try {
            template.put("one", first);
            assertSame(first, template.channel("one"));
            template.put("one", replacement);
            assertSame(replacement, template.channel("one"));
            assertNull(ProbeMultiTemplate.keyOf(first));
            ((io.netty.util.Attribute) first.attr(AbstractMultiChannelTemplate.MULTI_CHANNEL_KEY)).set("one");
            assertEquals("one", ProbeMultiTemplate.keyOf(first));
            first.pipeline().addLast("probe", new io.netty.channel.ChannelInboundHandlerAdapter());
            assertEquals("one", ProbeMultiTemplate.keyOf(first.pipeline().context("probe")));
            ((io.netty.util.Attribute) replacement.attr(AbstractMultiChannelTemplate.MULTI_CHANNEL_KEY)).set("one");
            assertSame(replacement, template.channelFromFuture((ChannelFuture) replacement.newSucceededFuture()));
            ChannelPromise writePromise = template.write("one", new Object());
            assertNotNull(writePromise);
            replacement.close();
            ChannelPromise discarded = template.write("one", new Object());
            assertFalse(discarded.isSuccess());
            template.close("one");
            template.close("one", replacement.newPromise());
            template.clearPublic();
            assertTrue(template.storage().isEmpty());
        }
        finally {
            first.finishAndReleaseAll();
            replacement.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void multiTemplateClearClosesStoredChannelsIncludingInactiveOnes()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel active = new EmbeddedChannel();
        NioEventLoopGroup inactiveGroup = new NioEventLoopGroup(1);
        NioSocketChannel unregistered = new NioSocketChannel();
        NioSocketChannel inactive = new NioSocketChannel();
        inactiveGroup.next().register(inactive).syncUninterruptibly();
        try {
            assertTrue(active.isOpen());
            assertTrue(active.isActive());
            assertTrue(unregistered.isOpen());
            assertFalse(unregistered.isRegistered());
            assertTrue(inactive.isOpen());
            assertFalse(inactive.isActive());
            assertTrue(inactive.isRegistered());
            template.put("active", active);
            template.put("unregistered", unregistered);
            template.put("inactive", inactive);

            template.clearPublic();
            assertTrue(inactive.closeFuture().awaitUninterruptibly(5, TimeUnit.SECONDS));

            assertFalse(active.isOpen());
            assertFalse(unregistered.isOpen());
            assertFalse(inactive.isOpen());
            assertTrue(template.storage().isEmpty());
        }
        finally {
            active.finishAndReleaseAll();
            if (unregistered.isOpen()) unregistered.unsafe().closeForcibly();
            inactive.close().syncUninterruptibly();
            inactiveGroup.shutdownGracefully().syncUninterruptibly();
            template.shutdown();
        }
    }

    @Test
    public void multiTemplateClearContinuesAfterChannelCloseThrows()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel first = new EmbeddedChannel();
        EmbeddedChannel second = new EmbeddedChannel();
        EmbeddedChannel closeFailsDelegate = new EmbeddedChannel();
        Channel closeFails = (Channel) Proxy.newProxyInstance(Channel.class.getClassLoader(), new Class<?>[]{Channel.class},
                                                              (proxy, method, arguments) -> {
                                                                  if (method.getName().equals("close") && method.getParameterCount() == 0) {
                                                                      throw new IllegalStateException("close failed");
                                                                  }
                                                                  try {
                                                                      return method.invoke(closeFailsDelegate, arguments);
                                                                  } catch (InvocationTargetException exception) {
                                                                      throw exception.getCause();
                                                                  }
                                                              });
        try {
            template.put("a", first);
            template.put("b", second);
            String firstKey = template.storage().keySet().iterator().next();
            template.put(firstKey, closeFails);
            assertSame(closeFails, template.storage().values().iterator().next());

            template.clearPublic();

            assertFalse(first.isOpen());
            assertFalse(second.isOpen());
            assertFalse(closeFailsDelegate.isOpen());
            assertTrue(template.storage().isEmpty());
        }
        finally {
            first.finishAndReleaseAll();
            second.finishAndReleaseAll();
            closeFailsDelegate.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void multiTemplateClearWaitsForConcurrentStoreAndClosesIt() throws Exception
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel stored = new EmbeddedChannel();
        CountDownLatch computeStarted = new CountDownLatch(1);
        CountDownLatch clearStarted = new CountDownLatch(1);
        CountDownLatch finishCompute = new CountDownLatch(1);
        ExecutorService callers = Executors.newFixedThreadPool(2);
        try {
            Future<?> store = callers.submit(() -> template.storage().compute("late", (key, previous) -> {
                computeStarted.countDown();
                try {
                    finishCompute.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return stored;
            }));
            assertTrue(computeStarted.await(5, TimeUnit.SECONDS));

            Future<?> clear = callers.submit(() -> {
                clearStarted.countDown();
                template.clearPublic();
            });
            assertTrue(clearStarted.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> clear.get(100, TimeUnit.MILLISECONDS));
            finishCompute.countDown();
            store.get(5, TimeUnit.SECONDS);
            clear.get(5, TimeUnit.SECONDS);

            assertFalse(stored.isOpen());
            assertTrue(template.storage().isEmpty());
        }
        finally {
            finishCompute.countDown();
            callers.shutdownNow();
            stored.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void channelStorageComputeAndStoreChannelCloseRemovedOrReplacedChannels()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel removed = new EmbeddedChannel();
        NioSocketChannel inactive = new NioSocketChannel();
        EmbeddedChannel replacement = new EmbeddedChannel();
        NioSocketChannel inactiveStored = new NioSocketChannel();
        EmbeddedChannel storedReplacement = new EmbeddedChannel();
        try {
            template.put("removed", removed);
            assertNull(template.getChannelStorage().compute("removed", (key, old) -> null));
            assertFalse(removed.isOpen());
            assertFalse(template.getChannelStorage().containsKey("removed"));

            template.put("inactive", inactive);
            assertTrue(inactive.isOpen());
            assertFalse(inactive.isActive());
            assertSame(replacement, template.getChannelStorage().compute("inactive", (key, old) -> replacement));
            assertFalse(inactive.isOpen());
            assertSame(replacement, template.channel("inactive"));

            template.put("inactive-store", inactiveStored);
            assertTrue(inactiveStored.isOpen());
            assertFalse(inactiveStored.isActive());
            template.put("inactive-store", storedReplacement);
            assertFalse(inactiveStored.isOpen());
            assertSame(storedReplacement, template.channel("inactive-store"));
        }
        finally {
            removed.finishAndReleaseAll();
            if (inactive.isOpen()) inactive.unsafe().closeForcibly();
            replacement.finishAndReleaseAll();
            if (inactiveStored.isOpen()) inactiveStored.unsafe().closeForcibly();
            storedReplacement.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void publicChannelStorageComputeKeepsOldChannelWhenUnchangedOrFailed()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel original = new EmbeddedChannel();
        try {
            template.put("one", original);

            assertSame(original, template.getChannelStorage().compute("one", (key, old) -> old));
            assertTrue(original.isOpen());
            assertThrows(IllegalStateException.class,
                         () -> template.getChannelStorage().compute("one", (key, old) -> {
                             throw new IllegalStateException("mapping failed");
                         }));

            assertSame(original, template.channel("one"));
            assertTrue(original.isOpen());
        }
        finally {
            original.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void publicChannelStorageComputeClosesAliasedChannelOnlyAfterLastKeyIsRemoved()
    {
        ProbeMultiTemplate template = new ProbeMultiTemplate(Map.of());
        EmbeddedChannel shared = new EmbeddedChannel();
        try {
            template.getChannelStorage().compute("first", (key, old) -> shared);
            template.getChannelStorage().compute("second", (key, old) -> shared);

            template.getChannelStorage().compute("first", (key, old) -> null);

            assertTrue(shared.isOpen());
            assertSame(shared, template.channel("second"));

            template.getChannelStorage().compute("second", (key, old) -> null);

            assertFalse(shared.isOpen());
            assertFalse(template.getChannelStorage().containsKey("second"));
        }
        finally {
            shared.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void singleTemplateWritesAndCloses()
    {
        ProbeSingleTemplate template = new ProbeSingleTemplate();
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            template.put(channel);
            assertSame(channel, template.current());
            assertNotNull(template.write("x"));
            assertNotNull(template.writeAndFlush("y"));
            template.closeDirect();
            assertFalse(template.write("discard").isSuccess());
            template.closeGracefully();
            template.closeGracefully(channel.newPromise());
        }
        finally {
            channel.finishAndReleaseAll();
            template.shutdown();
        }
    }

    @Test
    public void concurrentSingleChannelReplacementClosesEveryUntrackedChannel() throws Exception
    {
        ProbeSingleTemplate template = new ProbeSingleTemplate(new CyclicBarrier(2));
        EmbeddedChannel first = new EmbeddedChannel();
        EmbeddedChannel second = new EmbeddedChannel();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService callers = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstStore = callers.submit(() -> {
                start.await();
                template.put(first);
                return null;
            });
            Future<?> secondStore = callers.submit(() -> {
                start.await();
                template.put(second);
                return null;
            });
            start.countDown();
            firstStore.get();
            secondStore.get();

            EmbeddedChannel tracked = template.current();
            assertTrue(tracked == first || tracked == second);
            assertTrue(tracked.isActive());
            assertFalse((tracked == first ? second : first).isActive());
        }
        finally {
            callers.shutdownNow();
            first.finishAndReleaseAll();
            second.finishAndReleaseAll();
            template.shutdown();
        }
    }

    /**
     * Places the channel type parameter second to exercise generic type resolution.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private abstract static class ReorderedTemplate<M, C extends Channel> extends Template<C>
    {
        @Override protected EventLoopGroup newEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected ChannelInitializer<C> channelInitializer() {
            return new ChannelInitializer<>() { @Override protected void initChannel(C channel) {} };
        }
    }

    /**
     * Binds the second parameter of the intermediate template to an embedded channel.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private static final class InheritedTemplate extends ReorderedTemplate<String, EmbeddedChannel> {}

    /**
     * Creates embedded channels for testing channel metadata, state, and failed promises.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-14
     */
    private static final class ProbeTemplate extends Template<EmbeddedChannel>
    {
        @Override
        protected EventLoopGroup newEventLoopGroup()
        {
            return new DefaultEventLoopGroup(1);
        }

        @Override
        protected ChannelInitializer<EmbeddedChannel> channelInitializer()
        {
            return new ChannelInitializer<>()
            {
                @Override
                protected void initChannel(EmbeddedChannel channel)
                {
                }
            };
        }

        void shutdown()
        {
            shutdownGracefully();
        }
    }

    /**
     * Exposes keyed channel storage and write operations for embedded-channel tests.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-14
     */
    private static final class ProbeMultiTemplate extends AbstractMultiChannelTemplate<String, SocketAddress, EmbeddedChannel, ChannelConfig>
    {
        ProbeMultiTemplate(Map<String, SocketAddress> addresses) { super(addresses); }
        @Override protected EventLoopGroup newEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected ChannelInitializer<EmbeddedChannel> channelInitializer() { return new ChannelInitializer<>() { @Override protected void initChannel(EmbeddedChannel ch) { } }; }
        void put(String key, Channel ch) { storeChannel(key, ch); }
        Channel channel(String key) { return getChannel(key); }
        Channel channelFromFuture(ChannelFuture f) { storeChannel(f); return getChannel(channelKey(f)); }
        ChannelStorage<String> storage() { return channelStorage; }
        void clearPublic() { clear(); }
        void close(String key) { closeChannelGracefully(key); }
        void close(String key, ChannelPromise p) { closeChannelGracefully(key, p); }
        public ChannelPromise write(String key, Object msg) { return super.write(key, msg); }
        static <T> T keyOf(Channel c) { return channelKey(c); }
        static <T> T keyOf(ChannelHandlerContext c) { return channelKey(c); }
        void shutdown() { shutdownGracefully(); }
    }

    /**
     * Exposes embedded-channel replacement and write operations for single-channel tests.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-14
     */
    private static final class ProbeSingleTemplate extends AbstractSingleChannelTemplate<SocketAddress, EmbeddedChannel, ChannelConfig>
    {
        private final CyclicBarrier concurrentNullReads;

        ProbeSingleTemplate() { this(null); }
        ProbeSingleTemplate(CyclicBarrier concurrentNullReads) {
            super(null);
            this.concurrentNullReads = concurrentNullReads;
        }
        @Override protected EventLoopGroup newEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected ChannelInitializer<EmbeddedChannel> channelInitializer() { return new ChannelInitializer<>() { @Override protected void initChannel(EmbeddedChannel ch) { } }; }
        @Override protected Bootstrap newBootstrap(SocketAddress ignored) { return new Bootstrap(); }
        @Override protected boolean isActive(Channel channel) {
            if (channel == null && concurrentNullReads != null) {
                try {
                    concurrentNullReads.await(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                } catch (BrokenBarrierException | TimeoutException ignored) {
                }
            }
            return super.isActive(channel);
        }
        void put(EmbeddedChannel c) { storeChannel(c); }
        EmbeddedChannel current() { return channel; }
        void closeDirect() { closeChannelDirectly(); }
        void closeGracefully() { closeChannelGracefully(); }
        void closeGracefully(ChannelPromise p) { closeChannelGracefully(p); }
        public ChannelPromise write(Object m) { return super.write(m); }
        public ChannelPromise writeAndFlush(Object m) { return super.writeAndFlush(m); }
        void shutdown() { shutdownGracefully(); }
    }

    /**
     * Records socket channel creation so tests can inspect cleanup after configuration failures.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    public static final class ObservedSocketChannel extends NioSocketChannel
    {
        private static volatile ObservedSocketChannel latest;

        public ObservedSocketChannel()
        {
            latest = this;
        }
    }

    /**
     * Throws during configuration of a newly created single socket channel.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    private static final class FailingSingleTemplate extends AbstractSingleChannelTemplate<SocketAddress, ObservedSocketChannel, ChannelConfig>
    {
        private FailingSingleTemplate()
        {
            super(new java.net.InetSocketAddress("127.0.0.1", 1));
        }

        @Override protected EventLoopGroup newEventLoopGroup() { return new NioEventLoopGroup(1); }
        @Override protected ChannelInitializer<ObservedSocketChannel> channelInitializer() {
            return new ChannelInitializer<>() { @Override protected void initChannel(ObservedSocketChannel channel) { } };
        }
        @Override protected void doChannelConfig(ChannelConfig config) { throw new IllegalStateException("config failed"); }
        private void shutdown() { shutdownGracefully(); }
    }

    /**
     * Throws during configuration of a newly created keyed socket channel.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    private static final class FailingMultiTemplate extends AbstractMultiChannelTemplate<String, SocketAddress, ObservedSocketChannel, ChannelConfig>
    {
        private FailingMultiTemplate()
        {
            super(Map.of("one", new java.net.InetSocketAddress("127.0.0.1", 1)));
        }

        @Override protected EventLoopGroup newEventLoopGroup() { return new NioEventLoopGroup(1); }
        @Override protected ChannelInitializer<ObservedSocketChannel> channelInitializer() {
            return new ChannelInitializer<>() { @Override protected void initChannel(ObservedSocketChannel channel) { } };
        }
        @Override protected void doChannelConfig(String key, ChannelConfig config) { throw new IllegalStateException("config failed"); }
        private void shutdown() { shutdownGracefully(); }
    }
}
