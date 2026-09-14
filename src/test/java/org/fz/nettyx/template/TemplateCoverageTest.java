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
import io.netty.bootstrap.Bootstrap;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.net.SocketAddress;

import static org.junit.Assert.*;

public class TemplateCoverageTest
{
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

    private static final class ProbeSingleTemplate extends AbstractSingleChannelTemplate<SocketAddress, EmbeddedChannel, ChannelConfig>
    {
        ProbeSingleTemplate() { super(null); }
        @Override protected EventLoopGroup newEventLoopGroup() { return new DefaultEventLoopGroup(1); }
        @Override protected ChannelInitializer<EmbeddedChannel> channelInitializer() { return new ChannelInitializer<>() { @Override protected void initChannel(EmbeddedChannel ch) { } }; }
        @Override protected Bootstrap newBootstrap(SocketAddress ignored) { return new Bootstrap(); }
        void put(EmbeddedChannel c) { storeChannel(c); }
        EmbeddedChannel current() { return channel; }
        void closeDirect() { closeChannelDirectly(); }
        void closeGracefully() { closeChannelGracefully(); }
        void closeGracefully(ChannelPromise p) { closeChannelGracefully(p); }
        public ChannelPromise write(Object m) { return super.write(m); }
        public ChannelPromise writeAndFlush(Object m) { return super.writeAndFlush(m); }
        void shutdown() { shutdownGracefully(); }
    }
}
