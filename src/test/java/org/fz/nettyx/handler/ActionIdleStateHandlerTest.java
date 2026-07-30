package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleStateEvent;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ActionIdleStateHandlerTest {

    @Test
    public void testConstruction() {
        ActionIdleStateHandler handler = ActionIdleStateHandler.newReadIdleHandler(5, ctx -> {});
        assertEquals(5, handler.getReaderIdleSeconds());
        assertEquals(0, handler.getWriterIdleSeconds());
        assertEquals(0, handler.getAllIdleSeconds());
    }

    @Test
    public void testWriteIdleHandler() {
        ActionIdleStateHandler handler = ActionIdleStateHandler.newWriteIdleHandler(10, ctx -> {});
        assertEquals(0, handler.getReaderIdleSeconds());
        assertEquals(10, handler.getWriterIdleSeconds());
        assertEquals(0, handler.getAllIdleSeconds());
    }

    @Test
    public void testAllIdleHandler() {
        ActionIdleStateHandler handler = ActionIdleStateHandler.newAllIdleHandler(30, ctx -> {});
        assertEquals(0, handler.getReaderIdleSeconds());
        assertEquals(0, handler.getWriterIdleSeconds());
        assertEquals(30, handler.getAllIdleSeconds());
    }

    @Test
    public void idleCallbacksAndConstructorOverloadsAreCovered() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        ExposedIdle handler = new ExposedIdle(false, 1, 2, 3, TimeUnit.SECONDS, false);
        handler.readIdleAction(ctx -> calls.incrementAndGet())
               .writeIdleAction(ctx -> calls.incrementAndGet())
               .allIdleAction(ctx -> calls.incrementAndGet());
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        ChannelHandlerContext ctx = channel.pipeline().context(handler);

        handler.trigger(ctx, IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT);
        handler.trigger(ctx, IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT);
        handler.trigger(ctx, IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT);
        handler.trigger(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);
        assertEquals(4, calls.get());
        assertFalse(handler.fireNext());

        assertTrue(new ActionIdleStateHandler(1, 2, 3).fireNext());
        assertTrue(new ActionIdleStateHandler(1L, 2L, 3L, TimeUnit.SECONDS).fireNext());
        assertFalse(new ActionIdleStateHandler(1, 2, 3, false).fireNext());
        assertFalse(new ActionIdleStateHandler(1L, 2L, 3L, TimeUnit.SECONDS, false).fireNext());
        assertTrue(new ActionIdleStateHandler(true, 1, 2, 3, TimeUnit.SECONDS).fireNext());
        channel.finishAndReleaseAll();
    }

    @Test
    public void timeoutHandlerConstructorOverloadsRetainConfiguration() {
        ExposedReadTimeout read = new ExposedReadTimeout(1, TimeUnit.SECONDS, (ctx, error) -> {}, false);
        ExposedWriteTimeout write = new ExposedWriteTimeout(1, TimeUnit.SECONDS, (ctx, error) -> {}, false);
        assertFalse(read.fireNext());
        assertFalse(write.fireNext());
        assertTrue(new ActionReadTimeoutHandler(1, (actual, error) -> {}).fireNext());
        assertTrue(new ActionReadTimeoutHandler(1, TimeUnit.SECONDS, (actual, error) -> {}).fireNext());
        assertTrue(new ActionWriteTimeoutHandler(1, (actual, error) -> {}).fireNext());
        assertTrue(new ActionWriteTimeoutHandler(1, TimeUnit.SECONDS, (actual, error) -> {}).fireNext());
    }

    @Test
    public void everyHeartBeaterMatchesOnlyItsIdleState() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ExposedReadHeart read = new ExposedReadHeart(1, ignored -> calls.incrementAndGet());
        ExposedWriteHeart write = new ExposedWriteHeart(1, ignored -> calls.incrementAndGet());
        ExposedAllHeart all = new ExposedAllHeart(1, ignored -> calls.incrementAndGet());

        read.trigger(ctx, IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT);
        read.trigger(ctx, IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT);
        write.trigger(ctx, IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT);
        all.trigger(ctx, IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT);

        assertEquals(3, calls.get());
        assertEquals("read-idle", read.name());
        assertEquals("write-idle", write.name());
        assertEquals("all-idle", all.name());
        channel.finishAndReleaseAll();
    }

    private static class ExposedIdle extends ActionIdleStateHandler {
        ExposedIdle(boolean observeOutput, long reader, long writer, long all, TimeUnit unit, boolean fireNext) {
            super(observeOutput, reader, writer, all, unit, fireNext);
        }
        void trigger(ChannelHandlerContext ctx, IdleStateEvent event) throws Exception { channelIdle(ctx, event); }
    }

    private static class ExposedReadTimeout extends ActionReadTimeoutHandler {
        ExposedReadTimeout(long timeout, TimeUnit unit,
                           org.fz.nettyx.action.ChannelExceptionAction action, boolean fireNext) {
            super(timeout, unit, action, fireNext);
        }
    }

    private static class ExposedWriteTimeout extends ActionWriteTimeoutHandler {
        ExposedWriteTimeout(long timeout, TimeUnit unit,
                            org.fz.nettyx.action.ChannelExceptionAction action, boolean fireNext) {
            super(timeout, unit, action, fireNext);
        }
    }

    private static class ExposedReadHeart extends IdledHeartBeater.ReadIdleHeartBeater {
        ExposedReadHeart(int seconds, org.fz.nettyx.action.ChannelHandlerContextAction action) { super(seconds, action); }
        void trigger(ChannelHandlerContext ctx, IdleStateEvent event) throws Exception { channelIdle(ctx, event); }
        String name() { return stateName(); }
    }

    private static class ExposedWriteHeart extends IdledHeartBeater.WriteIdleHeartBeater {
        ExposedWriteHeart(int seconds, org.fz.nettyx.action.ChannelHandlerContextAction action) { super(seconds, action); }
        void trigger(ChannelHandlerContext ctx, IdleStateEvent event) throws Exception { channelIdle(ctx, event); }
        String name() { return stateName(); }
    }

    private static class ExposedAllHeart extends IdledHeartBeater.AllIdleHeartBeater {
        ExposedAllHeart(int seconds, org.fz.nettyx.action.ChannelHandlerContextAction action) { super(seconds, action); }
        void trigger(ChannelHandlerContext ctx, IdleStateEvent event) throws Exception { channelIdle(ctx, event); }
        String name() { return stateName(); }
    }
}
