package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

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
        Object obj = toStruct(typeRef, Unpooled.wrappedBuffer(bytes));
        assertNotNull(obj);

        ByteBuf out1Buf = Unpooled.buffer();
        toByteBuf(typeRef, obj, out1Buf);
        byte[]  out1    = new byte[out1Buf.readableBytes()];
        out1Buf.readBytes(out1);

        Object obj2 = toStruct(typeRef, Unpooled.wrappedBuffer(out1));

        ByteBuf out2Buf = Unpooled.buffer();
        toByteBuf(typeRef, obj2, out2Buf);
        byte[]  out2    = new byte[out2Buf.readableBytes()];
        out2Buf.readBytes(out2);

        assertArrayEquals(out1, out2);
    }
}
