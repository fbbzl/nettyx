package org.fz.nettyx.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author fengbinbin
 * @since 2024
 */
public class ChannelStateTest {

    @Test
    public void testInitialState() {
        ChannelState state = new ChannelState();
        assertEquals(0, state.getConnectTimes());
        assertEquals(0, state.getConnectSuccessTimes());
        assertEquals(0, state.getConnectFailureTimes());
        assertEquals(0, state.getConnectDoneTimes());
        assertEquals(0, state.getConnectCancelledTimes());
    }

    @Test
    public void testIncreaseSuccess() {
        ChannelState state = new ChannelState();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelFuture cf = channel.newSucceededFuture();

        state.increase(cf);

        assertEquals(1, state.getConnectTimes());
        assertEquals(1, state.getConnectSuccessTimes());
        assertEquals(0, state.getConnectFailureTimes());
        assertEquals(1, state.getConnectDoneTimes());
        assertEquals(0, state.getConnectCancelledTimes());

        channel.close();
    }

    @Test
    public void testIncreaseFailure() {
        ChannelState state = new ChannelState();
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelFuture cf = new DefaultChannelPromise(channel).setFailure(new RuntimeException("test"));

        state.increase(cf);

        assertEquals(1, state.getConnectTimes());
        assertEquals(0, state.getConnectSuccessTimes());
        assertEquals(1, state.getConnectFailureTimes());
        assertEquals(1, state.getConnectDoneTimes());
        assertEquals(0, state.getConnectCancelledTimes());

        channel.close();
    }

    @Test
    public void testMultipleIncreases() {
        ChannelState state = new ChannelState();
        EmbeddedChannel channel = new EmbeddedChannel();

        state.increase(channel.newSucceededFuture());
        state.increase(channel.newSucceededFuture());
        state.increase(new DefaultChannelPromise(channel).setFailure(new RuntimeException("fail")));

        assertEquals(3, state.getConnectTimes());
        assertEquals(2, state.getConnectSuccessTimes());
        assertEquals(1, state.getConnectFailureTimes());
        assertEquals(3, state.getConnectDoneTimes());

        channel.close();
    }

    @Test
    public void testReset() {
        ChannelState state = new ChannelState();
        EmbeddedChannel channel = new EmbeddedChannel();

        state.increase(channel.newSucceededFuture());
        state.increase(channel.newSucceededFuture());

        assertEquals(2, state.getConnectTimes());

        state.reset();

        assertEquals(0, state.getConnectTimes());
        assertEquals(0, state.getConnectSuccessTimes());
        assertEquals(0, state.getConnectFailureTimes());
        assertEquals(0, state.getConnectDoneTimes());
        assertEquals(0, state.getConnectCancelledTimes());

        channel.close();
    }

    @Test
    public void testGetterSetter() {
        ChannelState state = new ChannelState();
        state.setConnectTimes(10);
        state.setConnectSuccessTimes(8);
        state.setConnectFailureTimes(2);
        state.setConnectDoneTimes(10);
        state.setConnectCancelledTimes(1);

        assertEquals(10, state.getConnectTimes());
        assertEquals(8, state.getConnectSuccessTimes());
        assertEquals(2, state.getConnectFailureTimes());
        assertEquals(10, state.getConnectDoneTimes());
        assertEquals(1, state.getConnectCancelledTimes());
    }
}
