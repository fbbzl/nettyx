package org.fz.nettyx.serializer.struct.accessor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.support.BigEndianLeaf;
import org.fz.nettyx.beanmodel.support.ConcreteStructArrayBean;
import org.fz.nettyx.beanmodel.support.ConcreteStructHolder;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.beanmodel.valid.BasicArrayBean;
import org.fz.nettyx.beanmodel.valid.FlexibleBasicArrayBean;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.fz.nettyx.serializer.struct.StructContext.getStructDefinition;
import static org.junit.Assert.*;

/**
 * Verifies direct basic and struct field access, array access, and delegation to field handlers.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class StructFieldAccessorCoverageTest {

    @BeforeClass
    public static void scanModels() {
        new StructContext("org.fz.nettyx.beanmodel.support", "org.fz.nettyx.beanmodel.valid");
    }

    @Test
    public void fieldAccessorIsNestedInStructAccessor() {
        assertTrue(Arrays.stream(StructAccessor.class.getDeclaredClasses())
                         .anyMatch(type -> type.getSimpleName().equals("FieldAccessor")));
        assertThrows(ClassNotFoundException.class,
                     () -> Class.forName("org.fz.nettyx.serializer.struct.accessor.StructAccessorSupport"));
    }

    @Test
    public void fieldAccessorReadsAndWritesDirectBasicAndStructFields() {
        StructField basicField = field(AccessorBean.class, "value");
        StructSerializer basicSerializer = new StructSerializer(AccessorBean.class);
        AccessorBean basicBean = new AccessorBean();
        ByteBuf basicInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 42});
        ByteBuf basicOutput = Unpooled.buffer();
        try {
            Object basic = StructAccessor.FieldAccessor.readField(basicSerializer, AccessorBean.class, basicBean,
                                                            AccessorBean.class, basicField, basicField.handler(), basicInput);
            assertEquals(Integer.valueOf(42), ((cint) basic).value());
            StructAccessor.FieldAccessor.writeField(basicSerializer, AccessorBean.class, basicBean, AccessorBean.class,
                                               basicField, basicField.handler(), basic, basicOutput);
            StructAccessor.FieldAccessor.writeField(basicSerializer, AccessorBean.class, basicBean, AccessorBean.class,
                                               basicField, basicField.handler(), null, basicOutput);
            assertEquals(8, basicOutput.readableBytes());
        }
        finally {
            basicInput.release();
            basicOutput.release();
        }

        StructField structField = field(ConcreteStructHolder.class, "leaf");
        StructSerializer structSerializer = new StructSerializer(ConcreteStructHolder.class);
        ConcreteStructHolder holder = new ConcreteStructHolder();
        ByteBuf structInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 7});
        ByteBuf structOutput = Unpooled.buffer();
        try {
            BigEndianLeaf leaf = (BigEndianLeaf) StructAccessor.FieldAccessor.readField(
                    structSerializer, ConcreteStructHolder.class, holder, ConcreteStructHolder.class,
                    structField, structField.handler(), structInput);
            assertEquals(Integer.valueOf(7), leaf.getValue().value());
            StructAccessor.FieldAccessor.writeField(structSerializer, ConcreteStructHolder.class, holder,
                                               ConcreteStructHolder.class, structField, structField.handler(),
                                               leaf, structOutput);
            StructAccessor.FieldAccessor.writeField(structSerializer, ConcreteStructHolder.class, holder,
                                               ConcreteStructHolder.class, structField, structField.handler(),
                                               null, structOutput);
            assertEquals(8, structOutput.readableBytes());
        }
        finally {
            structInput.release();
            structOutput.release();
        }
    }

    @Test
    public void fieldAccessorDelegatesNonArrayFieldsToHandler() {
        StructField field = field(AccessorBean.class, "value");
        StructSerializer serializer = new StructSerializer(AccessorBean.class);
        Object expected = new Object();
        Object[] written = new Object[1];
        StructFieldHandler<Annotation> handler = new StructFieldHandler<>() {
            @Override
            public Object doRead(
                    StructSerializer serializer,
                    Type root,
                    Object earlyStruct,
                    StructField field,
                    Type fieldType,
                    ByteBuf reading,
                    Annotation annotation) {
                return expected;
            }

            @Override
            public void doWrite(
                    StructSerializer serializer,
                    Type root,
                    Object struct,
                    StructField field,
                    Type fieldType,
                    Object fieldVal,
                    ByteBuf writing,
                    Annotation annotation) {
                written[0] = fieldVal;
            }
        };

        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1});
        ByteBuf output = Unpooled.buffer();
        try {
            assertSame(expected, StructAccessor.FieldAccessor.readField(
                    serializer, AccessorBean.class, new AccessorBean(), AccessorBean.class,
                    field, handler, input));
            StructAccessor.FieldAccessor.writeField(
                    serializer, AccessorBean.class, new AccessorBean(), AccessorBean.class,
                    field, handler, expected, output);
            assertSame(expected, written[0]);
            assertEquals(0, output.readableBytes());
        }
        finally {
            input.release();
            output.release();
        }
    }

    @Test
    public void fieldAccessorReadsAndWritesConcreteBasicArrays() {
        assertConcreteArrayRoundTrip(BasicArrayBean.class, "values",
                                     new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        assertConcreteArrayRoundTrip(FlexibleBasicArrayBean.class, "values",
                                     new byte[]{1, 0, 0, 0, 2, 0, 0, 0});
    }

    @Test
    public void fieldAccessorReadsAndWritesConcreteStructArrays() {
        StructDefinition definition = getStructDefinition(ConcreteStructArrayBean.class);
        StructSerializer serializer = new StructSerializer(ConcreteStructArrayBean.class);
        ConcreteStructArrayBean bean = new ConcreteStructArrayBean();
        StructField fixed = field(definition, "fixed");
        StructField flexible = field(definition, "flexible");
        ByteBuf fixedInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        ByteBuf flexibleInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 3});
        ByteBuf output = Unpooled.buffer();
        try {
            BigEndianLeaf[] fixedValues = (BigEndianLeaf[]) StructAccessor.FieldAccessor.readField(
                    serializer, ConcreteStructArrayBean.class, bean, ConcreteStructArrayBean.class,
                    fixed, fixed.handler(), fixedInput);
            BigEndianLeaf[] flexibleValues = (BigEndianLeaf[]) StructAccessor.FieldAccessor.readField(
                    serializer, ConcreteStructArrayBean.class, bean, ConcreteStructArrayBean.class,
                    flexible, flexible.handler(), flexibleInput);
            assertEquals(2, fixedValues.length);
            assertEquals(1, flexibleValues.length);
            StructAccessor.FieldAccessor.writeField(serializer, ConcreteStructArrayBean.class, bean,
                                               ConcreteStructArrayBean.class, fixed, fixed.handler(),
                                               fixedValues, output);
            StructAccessor.FieldAccessor.writeField(serializer, ConcreteStructArrayBean.class, bean,
                                               ConcreteStructArrayBean.class, flexible, flexible.handler(),
                                               flexibleValues, output);
            assertEquals(12, output.readableBytes());
        }
        finally {
            fixedInput.release();
            flexibleInput.release();
            output.release();
        }
    }

    @Test
    public void fieldAccessorDoesNotUseMethodHandles() throws Exception {
        String resource = StructAccessor.class.getSimpleName() + "$" + StructAccessor.FieldAccessor.class.getSimpleName() + ".class";
        try (InputStream input = StructAccessor.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            String classFile = new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(classFile.contains("java/lang/invoke/MethodHandle"));
        }
    }

    private static void assertConcreteArrayRoundTrip(Class<?> beanType, String fieldName, byte[] bytes) {
        StructField field = field(beanType, fieldName);
        StructSerializer serializer = new StructSerializer(beanType);
        Object bean;
        try {
            bean = beanType.getConstructor().newInstance();
        }
        catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        ByteBuf output = Unpooled.buffer();
        try {
            Object values = StructAccessor.FieldAccessor.readField(serializer, beanType, bean, beanType,
                                                             field, field.handler(), input);
            StructAccessor.FieldAccessor.writeField(serializer, beanType, bean, beanType,
                                               field, field.handler(), values, output);
            byte[] actual = new byte[output.readableBytes()];
            output.readBytes(actual);
            assertArrayEquals(bytes, actual);
        }
        finally {
            input.release();
            output.release();
        }
    }

    private static StructField field(Class<?> type, String name) {
        return field(getStructDefinition(type), name);
    }

    private static StructField field(StructDefinition definition, String name) {
        for (StructField field : definition.fields()) {
            if (field.wrapped().getName().equals(name)) return field;
        }
        throw new AssertionError("field not found: " + name);
    }

}
