package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.fz.nettyx.serializer.schema.SchemaRegistry;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaCodecIntegrationTest
{

    private static final SchemaRegistry REGISTRY = SchemaRegistry.load(
            "configured/device.xml", "configured/geo.xml");

    private static final Map<String, Object> TRACK = Map.of(
            "count", (short) 2,
            "points", List.of(
                    Map.of("longitude", 11, "latitude", 12),
                    Map.of("longitude", 21, "latitude", 22)));

    private static final byte[] TRACK_BYTES = {
            2,
            0, 0, 0, 11,
            0, 0, 0, 12,
            0, 0, 0, 21,
            0, 0, 0, 22
    };

    @Test
    public void codecParticipatesInNettyInboundAndOutboundPipelines()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new SchemaCodec(REGISTRY, "device.Track"));

        assertTrue(channel.writeOutbound(TRACK));
        ByteBuf encoded = channel.readOutbound();
        assertArrayEquals(TRACK_BYTES, readableBytes(encoded));
        encoded.release();

        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(TRACK_BYTES)));
        assertEquals(TRACK, channel.readInbound());
        channel.finishAndReleaseAll();
    }

    private static byte[] readableBytes(ByteBuf buffer)
    {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }
}
