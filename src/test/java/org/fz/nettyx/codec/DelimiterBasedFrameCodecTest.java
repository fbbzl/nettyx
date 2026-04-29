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
public class DelimiterBasedFrameCodecTest {

    private static final byte DELIMITER = 0x0A; // '\n'

    @Test
    public void testSingleFrame() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        byte[] data = {'H', 'e', 'l', 'l', 'o', DELIMITER};
        ByteBuf input = Unpooled.wrappedBuffer(data);
        assertTrue(channel.writeInbound(input));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeWithDelimiter() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        ByteBuf msg = Unpooled.wrappedBuffer(new byte[]{'H', 'i'});
        assertTrue(channel.writeOutbound(msg));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        byte[] result = new byte[encoded.readableBytes()];
        encoded.readBytes(result);
        // Should be "Hi" + delimiter
        assertArrayEquals(new byte[]{'H', 'i', DELIMITER}, result);
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testTwoFrames() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        byte[] data = {'A', 'B', DELIMITER, 'C', 'D', DELIMITER};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf frame1 = channel.readInbound();
        assertNotNull(frame1);
        byte[] r1 = new byte[frame1.readableBytes()];
        frame1.readBytes(r1);
        assertArrayEquals(new byte[]{'A', 'B'}, r1);
        frame1.release();

        ByteBuf frame2 = channel.readInbound();
        assertNotNull(frame2);
        byte[] r2 = new byte[frame2.readableBytes()];
        frame2.readBytes(r2);
        assertArrayEquals(new byte[]{'C', 'D'}, r2);
        frame2.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEmptyFrame() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, true, delimiter));

        byte[] data = {DELIMITER};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(0, decoded.readableBytes());
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testNoDelimiter() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        // Data without delimiter - decoder needs more data
        byte[] data = {'N', 'O', '_', 'D', 'E', 'L', 'I', 'M'};
        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(data)));
        assertNull(channel.readInbound());

        // Feed the delimiter to complete the frame
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{DELIMITER})));
        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(data, result);
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testMultiByteDelimiter() {
        byte[] delim = {0x0D, 0x0A}; // \r\n
        ByteBuf delimiter = Unpooled.wrappedBuffer(delim);
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        byte[] data = {'O', 'K', 0x0D, 0x0A, 'N', 'X', 0x0D, 0x0A};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        ByteBuf frame1 = channel.readInbound();
        assertNotNull(frame1);
        byte[] r1 = new byte[frame1.readableBytes()];
        frame1.readBytes(r1);
        assertArrayEquals(new byte[]{'O', 'K'}, r1);
        frame1.release();

        ByteBuf frame2 = channel.readInbound();
        assertNotNull(frame2);
        byte[] r2 = new byte[frame2.readableBytes()];
        frame2.readBytes(r2);
        assertArrayEquals(new byte[]{'N', 'X'}, r2);
        frame2.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeMultipleMessages() {
        ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[]{DELIMITER});
        EmbeddedChannel channel = new EmbeddedChannel(
                new DelimiterBasedFrameCodec(1024, delimiter));

        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(new byte[]{'A'})));
        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(new byte[]{'B', 'C'})));

        ByteBuf msg1 = channel.readOutbound();
        assertNotNull(msg1);
        byte[] r1 = new byte[msg1.readableBytes()];
        msg1.readBytes(r1);
        assertArrayEquals(new byte[]{'A', DELIMITER}, r1);
        msg1.release();

        ByteBuf msg2 = channel.readOutbound();
        assertNotNull(msg2);
        byte[] r2 = new byte[msg2.readableBytes()];
        msg2.readBytes(r2);
        assertArrayEquals(new byte[]{'B', 'C', DELIMITER}, r2);
        msg2.release();

        assertFalse(channel.finish());
    }
}
