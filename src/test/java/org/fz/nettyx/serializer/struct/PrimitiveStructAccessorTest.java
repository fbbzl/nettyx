package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.primitive.PrimitiveFieldHandler;
import org.fz.nettyx.beanmodel.primitive.PrimitiveStruct;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

public class PrimitiveStructAccessorTest {

    @BeforeClass
    public static void scanPrimitiveModels() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.primitive");
    }

    @Test
    public void asmAccessorBoxesAndUnboxesEveryPrimitiveType() {
        ByteBuf input = Unpooled.buffer();
        ByteBuf output = Unpooled.buffer();
        try {
            input.writeBoolean(true);
            input.writeByte(0x12);
            input.writeChar('A');
            input.writeShort(0x2345);
            input.writeInt(0x3456789a);
            input.writeLong(0x456789abcdef0123L);
            input.writeFloat(12.5f);
            input.writeDouble(34.5d);
            byte[] expected = new byte[input.readableBytes()];
            input.getBytes(input.readerIndex(), expected);

            PrimitiveStruct value = toStruct(PrimitiveStruct.class, input);
            assertTrue(value.isBooleanValue());
            assertEquals((byte) 0x12, value.getByteValue());
            assertEquals('A', value.getCharValue());
            assertEquals((short) 0x2345, value.getShortValue());
            assertEquals(0x3456789a, value.getIntValue());
            assertEquals(0x456789abcdef0123L, value.getLongValue());
            assertEquals(12.5f, value.getFloatValue(), 0.0f);
            assertEquals(34.5d, value.getDoubleValue(), 0.0d);

            toByteBuf(value, output);
            byte[] actual = new byte[output.readableBytes()];
            output.readBytes(actual);
            assertArrayEquals(expected, actual);
            assertFalse(new PrimitiveFieldHandler().isSingleton());
        }
        finally {
            input.release();
            output.release();
        }
    }
}
