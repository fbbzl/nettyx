package org.fz.nettyx.serializer.type.generator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.support.BigEndianLeaf;
import org.fz.nettyx.beanmodel.support.ConcreteStructArrayBean;
import org.fz.nettyx.beanmodel.support.ConcreteStructHolder;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.beanmodel.valid.BasicArrayBean;
import org.fz.nettyx.beanmodel.valid.FlexibleBasicArrayBean;
import org.fz.nettyx.serializer.type.StructFieldHandler;
import org.fz.nettyx.serializer.type.StructSerializer;
import org.fz.nettyx.serializer.type.StructSerializerContext;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.fz.nettyx.serializer.type.StructSerializerContext.getStructDefinition;
import static org.junit.Assert.*;

public class StructAccessorSupportCoverageTest {

    @BeforeClass
    public static void scanModels() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.support", "org.fz.nettyx.beanmodel.valid");
    }

    @Test
    public void supportIsNestedInStructAccessor() {
        assertTrue(Arrays.stream(StructAccessor.class.getDeclaredClasses())
                         .anyMatch(type -> type.getSimpleName().equals("Support")));
        assertThrows(ClassNotFoundException.class,
                     () -> Class.forName("org.fz.nettyx.serializer.type.generator.StructAccessorSupport"));
    }

    @Test
    public void supportReadsAndWritesDirectBasicAndStructFields() {
        StructField basicField = field(AccessorBean.class, "value");
        StructSerializer basicSerializer = new StructSerializer(AccessorBean.class);
        AccessorBean basicBean = new AccessorBean();
        ByteBuf basicInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 42});
        ByteBuf basicOutput = Unpooled.buffer();
        try {
            Object basic = StructAccessor.Support.readField(basicSerializer, AccessorBean.class, basicBean,
                                                            AccessorBean.class, basicField, basicField.handler(), basicInput);
            assertEquals(Integer.valueOf(42), ((cint) basic).value());
            StructAccessor.Support.writeField(basicSerializer, AccessorBean.class, basicBean, AccessorBean.class,
                                               basicField, basicField.handler(), basic, basicOutput);
            StructAccessor.Support.writeField(basicSerializer, AccessorBean.class, basicBean, AccessorBean.class,
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
            BigEndianLeaf leaf = (BigEndianLeaf) StructAccessor.Support.readField(
                    structSerializer, ConcreteStructHolder.class, holder, ConcreteStructHolder.class,
                    structField, structField.handler(), structInput);
            assertEquals(Integer.valueOf(7), leaf.getValue().value());
            StructAccessor.Support.writeField(structSerializer, ConcreteStructHolder.class, holder,
                                               ConcreteStructHolder.class, structField, structField.handler(),
                                               leaf, structOutput);
            StructAccessor.Support.writeField(structSerializer, ConcreteStructHolder.class, holder,
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
    public void supportDelegatesNonArrayFieldsToHandler() {
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
            assertSame(expected, StructAccessor.Support.readField(
                    serializer, AccessorBean.class, new AccessorBean(), AccessorBean.class,
                    field, handler, input));
            StructAccessor.Support.writeField(
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
    public void supportReadsAndWritesConcreteBasicArrays() {
        assertConcreteArrayRoundTrip(BasicArrayBean.class, "values",
                                     new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        assertConcreteArrayRoundTrip(FlexibleBasicArrayBean.class, "values",
                                     new byte[]{1, 0, 0, 0, 2, 0, 0, 0});
    }

    @Test
    public void supportReadsAndWritesConcreteStructArrays() {
        StructDefinition definition = getStructDefinition(ConcreteStructArrayBean.class);
        StructSerializer serializer = new StructSerializer(ConcreteStructArrayBean.class);
        ConcreteStructArrayBean bean = new ConcreteStructArrayBean();
        StructField fixed = field(definition, "fixed");
        StructField flexible = field(definition, "flexible");
        ByteBuf fixedInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        ByteBuf flexibleInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 3});
        ByteBuf output = Unpooled.buffer();
        try {
            BigEndianLeaf[] fixedValues = (BigEndianLeaf[]) StructAccessor.Support.readField(
                    serializer, ConcreteStructArrayBean.class, bean, ConcreteStructArrayBean.class,
                    fixed, fixed.handler(), fixedInput);
            BigEndianLeaf[] flexibleValues = (BigEndianLeaf[]) StructAccessor.Support.readField(
                    serializer, ConcreteStructArrayBean.class, bean, ConcreteStructArrayBean.class,
                    flexible, flexible.handler(), flexibleInput);
            assertEquals(2, fixedValues.length);
            assertEquals(1, flexibleValues.length);
            StructAccessor.Support.writeField(serializer, ConcreteStructArrayBean.class, bean,
                                               ConcreteStructArrayBean.class, fixed, fixed.handler(),
                                               fixedValues, output);
            StructAccessor.Support.writeField(serializer, ConcreteStructArrayBean.class, bean,
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
    public void supportDoesNotUseMethodHandles() throws Exception {
        String resource = StructAccessor.class.getSimpleName() + "$" + StructAccessor.Support.class.getSimpleName() + ".class";
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
            Object values = StructAccessor.Support.readField(serializer, beanType, bean, beanType,
                                                             field, field.handler(), input);
            StructAccessor.Support.writeField(serializer, beanType, bean, beanType,
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
