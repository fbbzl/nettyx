package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.primitive.PrimitiveFieldHandler;
import org.fz.nettyx.beanmodel.primitive.PrimitiveStruct;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

/**
 * Verifies primitive boxing and unboxing in generated accessors and creation of stateful field handlers.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class PrimitiveStructAccessorTest {

    @BeforeClass
    public static void scanPrimitiveModels() {
        new StructContext("org.fz.nettyx.beanmodel.primitive");
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

    @Test
    public void nonSingletonHandlerIsCreatedForEachSerializationOperation() {
        StructScannerTestSupport.invoke("scanHandler", Set.class, Set.of(StatefulHandler.class));
        StructScannerTestSupport.invoke("scanStruct", Set.class, Set.of(StatefulHandlerBean.class));
        StatefulHandler.readOperations = 0;
        StatefulHandler.writeOperations = 0;

        ByteBuf firstInput = Unpooled.wrappedBuffer(new byte[]{7});
        ByteBuf secondInput = Unpooled.wrappedBuffer(new byte[]{7});
        try {
            StatefulHandlerBean first = toStruct(StatefulHandlerBean.class, firstInput);
            StatefulHandlerBean second = toStruct(StatefulHandlerBean.class, secondInput);

            assertEquals(8, first.getValue());
            assertEquals(8, second.getValue());
            assertEquals(2, StatefulHandler.readOperations);

            ByteBuf firstOutput = Unpooled.buffer();
            ByteBuf secondOutput = Unpooled.buffer();
            try {
                StatefulHandlerBean value = new StatefulHandlerBean();
                value.setValue(8);
                toByteBuf(value, firstOutput);
                toByteBuf(value, secondOutput);
                assertEquals(7, firstOutput.readUnsignedByte());
                assertEquals(7, secondOutput.readUnsignedByte());
                assertEquals(2, StatefulHandler.writeOperations);
            }
            finally {
                firstOutput.release();
                secondOutput.release();
            }
        }
        finally {
            firstInput.release();
            secondInput.release();
        }
    }

    /**
     * Marks a field that uses a fresh handler for each serialization operation.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface StatefulField {
    }

    /**
     * Uses per-instance counters to adjust serialized integer values and tracks read and write calls across instances.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    public static class StatefulHandler implements StructFieldHandler<StatefulField> {
        private static int readOperations;
        private static int writeOperations;
        private int readCount;
        private int writeCount;

        @Override
        public Object doRead(StructSerializer serializer, Type root, Object earlyStruct, StructField field,
                             Type fieldType, ByteBuf reading, StatefulField annotation) {
            readOperations++;
            readCount++;
            return reading.readByte() + readCount;
        }

        @Override
        public void doWrite(StructSerializer serializer, Type root, Object struct, StructField field,
                            Type fieldType, Object fieldValue, ByteBuf writing, StatefulField annotation) {
            writeOperations++;
            writing.writeByte((int) fieldValue - ++writeCount);
        }
    }

    /**
     * Provides an integer property bound to a stateful struct field handler.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    @Struct(endian = Struct.Endian.BE)
    public static class StatefulHandlerBean {
        @StatefulField
        private int value;

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
