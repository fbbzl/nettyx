package org.fz.nettyx.serializer.struct.generator;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.*;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.codec.model.NestedGeneric;
import org.fz.nettyx.codec.model.You;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import static org.fz.nettyx.serializer.struct.StructContext.getStructDefinition;
import static org.junit.Assert.*;

public class StructAccessorStrategyTest {

    private static StructDefinition definition;

    @BeforeClass
    public static void scanStructs() {
        new StructContext("org.fz.nettyx.beanmodel.valid");
        new StructContext("org.fz.nettyx.codec.model");
        FixtureContext fixtureContext = new FixtureContext();
        fixtureContext.scanBasicTypes(Set.of(NoProgressBasic.class));
        fixtureContext.scanStructTypes(Set.of(NoProgressStruct.class));
        definition = getStructDefinition(AccessorBean.class);
    }

    @Test
    public void asmAccessorSupportsConstructionReadAndWrite() throws Exception {
        StructAccessor asm = accessor();

        assertAccessor(asm);
    }

    @Test
    public void factoryDoesNotReferenceRuntimeJavaCompiler() throws Exception {
        String resource = StructAccessorFactory.class.getSimpleName() + ".class";
        try (InputStream input = StructAccessorFactory.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            String classFile = new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(classFile.contains("javax/tools/JavaCompiler"));
            assertFalse(classFile.contains("javax/tools/ToolProvider"));
        }
    }

    @Test
    public void factoryDoesNotContainMethodHandleFallback() throws Exception {
        String resource = StructAccessorFactory.class.getSimpleName() + ".class";
        try (InputStream input = StructAccessorFactory.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            String classFile = new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(classFile.contains("MethodHandleStructAccessor"));
            assertFalse(classFile.contains("createMethodHandleAccessor"));
        }
    }

    @Test
    public void factoryDoesNotReferenceStructHelper() throws Exception {
        String resource = StructAccessorFactory.class.getSimpleName() + ".class";
        try (InputStream input = StructAccessorFactory.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            String classFile = new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(classFile.contains("StructHelper"));
        }
    }

    @Test
    public void concreteBasicAsmAccessorDoesNotKeepGenericFieldDispatchState() throws Exception {
        StructAccessor asm = accessor();
        assertEquals(0, asm.getClass().getDeclaredFields().length);
    }

    @Test
    public void asmAccessorHandlesByteOrdersAndBasicArrays() throws Exception {
        assertAsmRoundTrip(InheritedAccessorBean.class, new byte[]{4, 3, 2, 1});
        assertAsmRoundTrip(BasicArrayBean.class, new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        assertAsmRoundTrip(FlexibleBasicArrayBean.class, new byte[]{1, 0, 0, 0, 2, 0, 0, 0});
    }

    @Test
    public void asmAccessorHandlesHandlersAndNestedStructs() throws Exception {
        assertAsmRoundTrip(GirlFriend.class, new byte[]{'A', 0});

        byte[] bytes = new byte[122];
        Arrays.fill(bytes, (byte) 67);
        assertAsmRoundTrip(You.class, bytes);
    }

    @Test
    public void asmAccessorHandlesRuntimeGenericResolution() throws Exception {
        Type type = new TypeReference<NestedGeneric<cint, cchar>>() {}.getType();
        assertAsmRoundTrip(type, new byte[]{0, 0, 0, 1, 0, 0, 0, 2, 3});

        Type flexibleArrayType = new TypeReference<FlexibleGenericBasicArrayBean<cint>>() {}.getType();
        assertAsmRoundTrip(flexibleArrayType, new byte[]{1, 0, 0, 0, 2, 0, 0, 0});
    }

    @Test
    public void concreteFlexibleBasicArrayRejectsElementsThatMakeNoReadProgress() {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            assertThrows(SerializeException.class,
                         () -> StructSerializer.toStruct(NoProgressStruct.class, input));
        }
        finally {
            input.release();
        }
    }

    private static StructAccessor accessor() {
        return StructAccessorFactory.get(definition);
    }

    private static void assertAsmRoundTrip(Type type, byte[] bytes) throws Exception {
        StructDefinition targetDefinition = getStructDefinition(type);
        assertNotNull(targetDefinition);

        byte[] asmBytes = roundTrip(StructAccessorFactory.get(targetDefinition), type, bytes);
        assertArrayEquals(bytes, asmBytes);
    }

    private static byte[] roundTrip(StructAccessor accessor, Type type, byte[] bytes) {
        StructSerializer serializer = new StructSerializer(type);
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        ByteBuf output = Unpooled.buffer();
        try {
            Object value = accessor.read(serializer, type, type, input);
            assertEquals(bytes.length, input.readerIndex());

            accessor.write(serializer, type, type, value, output);
            byte[] serialized = new byte[output.readableBytes()];
            output.readBytes(serialized);
            return serialized;
        }
        finally {
            input.release();
            output.release();
        }
    }

    private static void assertAccessor(StructAccessor accessor) {
        assertTrue(accessor.newInstance() instanceof AccessorBean);

        StructSerializer serializer = new StructSerializer(AccessorBean.class);
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 42});
        ByteBuf output = Unpooled.buffer();
        try {
            AccessorBean decoded = (AccessorBean) accessor.read(serializer, AccessorBean.class,
                                                                AccessorBean.class, input);
            assertEquals(Integer.valueOf(42), decoded.getValue().value());
            accessor.write(serializer, AccessorBean.class, AccessorBean.class, decoded, output);

            byte[] actual = new byte[output.readableBytes()];
            output.readBytes(actual);
            assertArrayEquals(new byte[]{0, 0, 0, 42}, actual);
        }
        finally {
            input.release();
            output.release();
        }
    }

    private static final class FixtureContext extends StructContext {
        private FixtureContext() {
            super("package.that.does.not.exist");
        }

        private void scanBasicTypes(Set<Class<?>> types) {
            super.scanBasic(types);
        }

        private void scanStructTypes(Set<Class<?>> types) {
            super.scanStruct(types);
        }
    }

    public static final class NoProgressBasic extends Basic<Integer> {
        public NoProgressBasic(ByteBuf input, ByteOrder order) {
            super(input, order);
            input.writerIndex(input.readerIndex());
        }

        @Override public int size() { return 1; }
        @Override public boolean hasSigned() { return true; }
        @Override public void write(ByteBuf writingBuf, ByteOrder byteOrder) { }
        @Override protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) { return 0; }
    }

    @Struct(endian = Struct.Endian.BE)
    public static final class NoProgressStruct {
        @ToArray(flexible = true)
        private NoProgressBasic[] values;

        public NoProgressBasic[] getValues() {
            return values;
        }

        public void setValues(NoProgressBasic[] values) {
            this.values = values;
        }
    }
}
