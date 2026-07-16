package org.fz.nettyx.listener;

import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ActionChannelFutureListenerTest {

    @Test
    public void dispatchesDoneAndSuccessActions() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelPromise promise = channel.newPromise();
        AtomicInteger done = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();
        ActionChannelFutureListener listener = new ActionChannelFutureListener()
                .whenDone((actual, future) -> {
                    assertSame(promise, future);
                    done.incrementAndGet();
                })
                .whenSuccess((actual, future) -> success.incrementAndGet());

        promise.setSuccess();
        listener.operationComplete(promise);

        assertEquals(1, done.get());
        assertEquals(1, success.get());
        channel.finishAndReleaseAll();
    }

    @Test
    public void dispatchesFailureAndCancellationActions() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        AtomicInteger failures = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        ActionChannelFutureListener listener = new ActionChannelFutureListener()
                .whenFailure((actual, future) -> failures.incrementAndGet())
                .whenCancelled((actual, future) -> cancellations.incrementAndGet());

        ChannelPromise failed = channel.newPromise();
        failed.setFailure(new IllegalStateException("test"));
        listener.operationComplete(failed);

        ChannelPromise cancelled = channel.newPromise();
        cancelled.cancel(false);
        listener.operationComplete(cancelled);

        assertEquals(1, failures.get());
        assertEquals(1, cancellations.get());
        channel.finishAndReleaseAll();
    }
}
