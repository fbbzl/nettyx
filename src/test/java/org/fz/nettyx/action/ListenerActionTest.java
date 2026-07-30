package org.fz.nettyx.action;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.util.ChannelState;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ListenerActionTest {

    @Test
    public void supplierAndOperatorRedoRunOnEventLoop() {
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelFuture completed = channel.newSucceededFuture();
        ActionChannelFutureListener listener = new ActionChannelFutureListener();
        AtomicInteger supplied = new AtomicInteger();
        AtomicInteger operated = new AtomicInteger();

        ListenerAction.redo(() -> {
            supplied.incrementAndGet();
            return channel.newSucceededFuture();
        }, 0, TimeUnit.NANOSECONDS).act(listener, completed);
        ListenerAction.redo(future -> {
            assertSame(completed, future);
            operated.incrementAndGet();
            return channel.newSucceededFuture();
        }, 0, TimeUnit.NANOSECONDS).act(listener, completed);
        channel.runScheduledPendingTasks();

        assertEquals(1, supplied.get());
        assertEquals(1, operated.get());
        channel.finishAndReleaseAll();
    }

    @Test
    public void retryLimitUpdatesStateAndInvokesCallback() {
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelState state = new ChannelState();
        channel.attr(ChannelState.CHANNEL_STATE_KEY).set(state);
        ChannelPromise failed = channel.newPromise();
        failed.setFailure(new IllegalStateException("test"));
        ActionChannelFutureListener listener = new ActionChannelFutureListener();
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger stopped = new AtomicInteger();
        ListenerAction action = ListenerAction.redo(() -> {
            attempts.incrementAndGet();
            return channel.newSucceededFuture();
        }, 0, TimeUnit.NANOSECONDS, 2, (actualListener, actualFuture) -> {
            assertSame(listener, actualListener);
            assertSame(failed, actualFuture);
            stopped.incrementAndGet();
        });

        action.act(listener, failed);
        channel.runScheduledPendingTasks();
        action.act(listener, failed);
        channel.runScheduledPendingTasks();
        action.act(listener, failed);
        channel.runScheduledPendingTasks();

        assertEquals(2, attempts.get());
        assertEquals(1, stopped.get());
        assertEquals(2, state.getConnectTimes());
        assertEquals(2, state.getConnectFailureTimes());
        assertEquals(2, state.getConnectDoneTimes());
        channel.finishAndReleaseAll();
    }

    @Test
    public void operatorLimitAndMissingStateBranchesAreSupported() {
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelFuture completed = channel.newSucceededFuture();
        ActionChannelFutureListener listener = new ActionChannelFutureListener();
        AtomicInteger calls = new AtomicInteger();

        ListenerAction.redo(future -> {
            calls.incrementAndGet();
            return future;
        }, 0, TimeUnit.NANOSECONDS, 1).act(listener, completed);
        channel.runScheduledPendingTasks();
        assertEquals(1, calls.get());

        ChannelState state = new ChannelState();
        state.setConnectTimes(1);
        channel.attr(ChannelState.CHANNEL_STATE_KEY).set(state);
        ListenerAction.redo(future -> {
            calls.incrementAndGet();
            return future;
        }, 0, TimeUnit.NANOSECONDS, 1).act(listener, completed);
        ListenerAction.redo(() -> {
            calls.incrementAndGet();
            return completed;
        }, 0, TimeUnit.NANOSECONDS, 1).act(listener, completed);
        channel.runScheduledPendingTasks();

        assertEquals(1, calls.get());
        channel.finishAndReleaseAll();
    }
}
