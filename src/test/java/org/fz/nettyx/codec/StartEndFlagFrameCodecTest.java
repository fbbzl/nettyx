package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class StartEndFlagFrameCodecTest {

    private static final byte START = 0x02; // STX
    private static final byte END   = 0x03; // ETX

    @Test
    public void testEncode() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, false, new byte[]{START}, new byte[]{END}));

        ByteBuf msg = Unpooled.wrappedBuffer(new byte[]{'H', 'i'});
        assertTrue(channel.writeOutbound(msg));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        byte[] result = new byte[encoded.readableBytes()];
        encoded.readBytes(result);
        assertArrayEquals(new byte[]{START, 'H', 'i', END}, result);
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testDecode() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, true, new byte[]{START}, new byte[]{END}));

        byte[] data = {START, 'O', 'K', END};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(new byte[]{'O', 'K'}, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testDecodeWithoutStrip() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, false, new byte[]{START}, new byte[]{END}));

        byte[] data = {START, 'O', 'K', END};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        // Flags not stripped
        assertArrayEquals(new byte[]{START, 'O', 'K', END}, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testSameStartEndFlag() {
        // Both start and end use same flag (e.g., 0x7E)
        byte flag = 0x7E;
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, true, new byte[]{flag}));

        byte[] data = {flag, 'A', 'B', flag};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(new byte[]{'A', 'B'}, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testMultipleFlags() {
        byte start = 0x02;
        byte end = 0x03;
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, true, new byte[]{start}, new byte[]{end}));

        byte[] data = {start, 'X', end, start, 'Y', 'Z', end};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf frame1 = channel.readInbound();
        assertNotNull(frame1);
        byte[] r1 = new byte[frame1.readableBytes()];
        frame1.readBytes(r1);
        assertArrayEquals(new byte[]{'X'}, r1);
        frame1.release();

        ByteBuf frame2 = channel.readInbound();
        assertNotNull(frame2);
        byte[] r2 = new byte[frame2.readableBytes()];
        frame2.readBytes(r2);
        assertArrayEquals(new byte[]{'Y', 'Z'}, r2);
        frame2.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeWithSameFlag() {
        byte flag = 0x7E;
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, false, new byte[]{flag}));

        ByteBuf msg = Unpooled.wrappedBuffer(new byte[]{'D', 'A', 'T', 'A'});
        assertTrue(channel.writeOutbound(msg));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        byte[] result = new byte[encoded.readableBytes()];
        encoded.readBytes(result);
        assertArrayEquals(new byte[]{flag, 'D', 'A', 'T', 'A', flag}, result);
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testHexConstructor() {
        // Construct with hex strings
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, true, "02", "03"));

        byte[] data = {0x02, 'H', 'E', 'X', 0x03};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(new byte[]{'H', 'E', 'X'}, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEmptyPayload() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StartEndFlagFrameCodec(1024, true, new byte[]{START}, new byte[]{END}));

        byte[] data = {START, END};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(0, decoded.readableBytes());
        decoded.release();

        assertFalse(channel.finish());
    }
}
