package org.fz.nettyx.event;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChannelEventsTest {

    @Test
    public void recognizesIdleStateEventsAndStates() {
        assertTrue(ChannelEvents.isReadIdle(IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT));
        assertTrue(ChannelEvents.isWriteIdle(IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT));
        assertTrue(ChannelEvents.isAllIdle(IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT));

        assertTrue(ChannelEvents.isReadIdle(IdleState.READER_IDLE));
        assertTrue(ChannelEvents.isWriteIdle(IdleState.WRITER_IDLE));
        assertTrue(ChannelEvents.isAllIdle(IdleState.ALL_IDLE));
    }

    @Test
    public void rejectsDifferentIdleStatesAndUnrelatedObjects() {
        assertFalse(ChannelEvents.isReadIdle(IdleState.WRITER_IDLE));
        assertFalse(ChannelEvents.isWriteIdle(IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT));
        assertFalse(ChannelEvents.isAllIdle("not an idle event"));
    }
}
