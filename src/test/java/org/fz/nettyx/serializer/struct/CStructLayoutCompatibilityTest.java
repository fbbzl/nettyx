package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.unsigned.cuint;
import org.fz.nettyx.serializer.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.cmodel.NativePaddedPacket;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteOrder;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CStructLayoutCompatibilityTest {

    @BeforeClass
    public static void init() {
        new StructContext("org.fz.nettyx.serializer.cmodel");
    }

    @Test
    public void decodesNativeCStructBytesWithExplicitAlignmentPadding() {
        byte[] bytes = expectedPacketBytes();
        ByteBuf reading = Unpooled.wrappedBuffer(bytes);
        try {
            NativePaddedPacket packet = toStruct(NativePaddedPacket.class, reading);

            assertEquals(0x7A, packet.getKind().value().intValue());
            assertArrayEquals(new byte[3], packet.getAlignment());
            assertEquals(0x12345678L, packet.getMessageId().value().longValue());
            assertEquals(0x1357, packet.getSamples()[0].value().intValue());
            assertEquals(0x2468, packet.getSamples()[1].value().intValue());
            assertEquals(bytes.length, reading.readerIndex());
        }
        finally {
            reading.release();
        }
    }

    @Test
    public void encodesNativeCStructFieldsAtExpectedOffsets() {
        NativePaddedPacket packet = new NativePaddedPacket();
        packet.setKind(new cuchar(0x7A));
        packet.setAlignment(new byte[3]);
        packet.setMessageId(new cuint(0x12345678L));
        packet.setSamples(new cushort[]{new cushort(0x1357), new cushort(0x2468)});

        ByteBuf writing = Unpooled.buffer();
        try {
            toByteBuf(packet, writing);
            byte[] actual = new byte[writing.readableBytes()];
            writing.readBytes(actual);
            assertArrayEquals(expectedPacketBytes(), actual);
        }
        finally {
            writing.release();
        }
    }

    private static byte[] expectedPacketBytes() {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            return new byte[]{0x7A, 0, 0, 0, 0x78, 0x56, 0x34, 0x12, 0x57, 0x13, 0x68, 0x24};
        }
        return new byte[]{0x7A, 0, 0, 0, 0x12, 0x34, 0x56, 0x78, 0x13, 0x57, 0x24, 0x68};
    }
}
