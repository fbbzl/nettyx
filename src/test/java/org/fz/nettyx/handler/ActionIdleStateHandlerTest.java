package org.fz.nettyx.handler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
