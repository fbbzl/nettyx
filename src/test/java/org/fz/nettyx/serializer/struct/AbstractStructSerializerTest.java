package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

/**
 * Common base class for struct serializer round-trip tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /3/1 14:44
 */
public abstract class AbstractStructSerializerTest {

    @BeforeClass
    public static void init()
    {
        new StructSerializerContext("org.fz.nettyx.codec.model");
    }

    protected static void assertRoundTrip(TypeReference<?> typeRef, byte[] bytes)
    {
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        ByteBuf input2 = null;
        ByteBuf out1Buf = Unpooled.buffer();
        ByteBuf out2Buf = Unpooled.buffer();
        try {
            Object obj = toStruct(typeRef, input);
            assertNotNull(obj);
            assertEquals(bytes.length, input.readerIndex());

            toByteBuf(typeRef, obj, out1Buf);
            byte[] out1 = new byte[out1Buf.readableBytes()];
            out1Buf.readBytes(out1);
            assertArrayEquals(bytes, out1);

            input2 = Unpooled.wrappedBuffer(out1);
            Object obj2 = toStruct(typeRef, input2);
            assertEquals(out1.length, input2.readerIndex());
            toByteBuf(typeRef, obj2, out2Buf);
            byte[] out2 = new byte[out2Buf.readableBytes()];
            out2Buf.readBytes(out2);
            assertArrayEquals(out1, out2);
        }
        finally {
            input.release();
            if (input2 != null) input2.release();
            out1Buf.release();
            out2Buf.release();
        }
    }
}
