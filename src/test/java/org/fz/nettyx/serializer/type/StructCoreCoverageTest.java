package org.fz.nettyx.serializer.type;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.support.BigEndianLeaf;
import org.fz.nettyx.beanmodel.support.ConcreteStructHolder;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.coveragebasic.MissingConstructorBasic;
import org.fz.nettyx.coveragemodel.AbstractCoverageStruct;
import org.fz.nettyx.coveragemodel.InvalidNestedStructs;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.type.annotation.Ignore;
import org.fz.nettyx.serializer.type.annotation.ToCharSequence;
import org.fz.nettyx.serializer.type.basic.Basic;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static org.fz.nettyx.serializer.type.StructFieldHandler.DEFAULT_STRUCT_FIELD_HANDLER;
import static org.fz.nettyx.serializer.type.StructSerializerContext.getStructDefinition;
import static org.junit.Assert.*;

public class StructCoreCoverageTest {

    @BeforeClass
    public static void scanModels() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.support",
                                    "org.fz.nettyx.beanmodel.valid",
                                    "org.fz.nettyx.codec.model");
    }

    @Test
    public void publicEntryPointsSupportClassAndParameterizedRoots() {
        TypeReference<GenericBox<cint>> reference = new TypeReference<>() {};
        StructSerializer parameterized = new StructSerializer(reference);
        assertEquals(reference.getType(), parameterized.getType());
        assertEquals(AccessorBean.class, new StructSerializer(AccessorBean.class).getType());

        ByteBuf classOutput = Unpooled.buffer();
        ByteBuf genericOutput = Unpooled.buffer();
        byte[] nativeSeven = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.nativeOrder()).putInt(7).array();
        ByteBuf genericInput = Unpooled.wrappedBuffer(nativeSeven);
        try {
            AccessorBean bean = new AccessorBean();
            bean.setValue(new cint(42));
            StructSerializer.toByteBuf(bean, classOutput);
            assertArrayEquals(new byte[]{0, 0, 0, 42}, bytes(classOutput));

            GenericBox<cint> box = new GenericBox<>();
            box.setValue(new cint(7));
            StructSerializer.toByteBuf(reference, box, genericOutput);
            assertArrayEquals(nativeSeven, bytes(genericOutput));
            assertEquals(Integer.valueOf(7), StructSerializer.<GenericBox<cint>>toStruct(reference, genericInput).getValue().value());

            assertThrows(SerializeException.class,
                         () -> StructSerializer.toByteBuf(AccessorBean.class, null, classOutput));
            assertThrows(TypeJudgmentException.class,
                         () -> StructSerializer.toByteBuf(unknownType(), bean, classOutput));
        }
        finally {
            classOutput.release();
            genericOutput.release();
            genericInput.release();
        }
    }

    @Test
    public void serializerDispatchesBasicStructAndArrayBranches() {
        StructSerializer serializer = new StructSerializer(AccessorBean.class);
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        ByteBuf structInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 3});
        ByteBuf output = Unpooled.buffer();
        try {
            cint[] basics = serializer.readArray(cint.class, ByteOrder.BIG_ENDIAN, input, 2, false);
            assertEquals(Integer.valueOf(1), basics[0].value());
            assertEquals(Integer.valueOf(2), basics[1].value());

            BigEndianLeaf[] structs = serializer.readArray(BigEndianLeaf.class, ByteOrder.LITTLE_ENDIAN,
                                                           structInput, 1, false);
            assertEquals(Integer.valueOf(3), structs[0].getValue().value());
            assertThrows(TypeJudgmentException.class,
                         () -> serializer.readArray(String.class, ByteOrder.BIG_ENDIAN, Unpooled.EMPTY_BUFFER, 0, false));

            serializer.writeArray(new cint[]{new cint(4)}, cint.class, 1, output, false, ByteOrder.BIG_ENDIAN);
            BigEndianLeaf leaf = leaf(5);
            serializer.writeArray(new BigEndianLeaf[]{leaf}, BigEndianLeaf.class, 1, output, false, ByteOrder.BIG_ENDIAN);
            assertThrows(TypeJudgmentException.class,
                         () -> serializer.writeArray(new String[0], String.class, 0, output, false, ByteOrder.BIG_ENDIAN));
            assertArrayEquals(new byte[]{0, 0, 0, 4, 0, 0, 0, 5}, bytes(output));
        }
        finally {
            input.release();
            structInput.release();
            output.release();
        }
    }

    @Test
    public void arrayWritersPadNullAndShortValuesAndHonorFlexibleLength() {
        StructSerializer serializer = new StructSerializer(AccessorBean.class);
        ByteBuf output = Unpooled.buffer();
        try {
            serializer.writeBasic(new cint((Integer) null), ByteOrder.BIG_ENDIAN, output);
            serializer.writeBasicArray(null, 4, 2, output, false, ByteOrder.BIG_ENDIAN);
            serializer.writeBasicArray(null, 4, 99, output, true, ByteOrder.BIG_ENDIAN);
            serializer.writeBasicArray(new cint[]{null, new cint((Integer) null), new cint(3)},
                                       4, 4, output, false, ByteOrder.BIG_ENDIAN);
            serializer.writeBasicArray(new cint[]{new cint(4)}, 4, 99, output, true, ByteOrder.BIG_ENDIAN);

            serializer.writeStructArray(null, BigEndianLeaf.class, 1, output, false);
            serializer.writeStructArray(null, BigEndianLeaf.class, 99, output, true);
            serializer.writeStructArray(new BigEndianLeaf[]{null, leaf(6)}, BigEndianLeaf.class, 3, output, false);
            serializer.writeStructArray(new BigEndianLeaf[]{leaf(7)}, BigEndianLeaf.class, 99, output, true);

            assertEquals(52, output.readableBytes());
            byte[] actual = bytes(output);
            assertEquals(3, actual[23]);
            assertEquals(6, actual[43]);
            assertEquals(7, actual[51]);
        }
        finally {
            output.release();
        }
    }

    @Test
    public void typeJudgmentResolvesClassesParameterizedTypesAndVariables() throws Exception {
        Type basicRoot = new TypeReference<GenericBox<cint>>() {}.getType();
        Type structRoot = new TypeReference<GenericBox<BigEndianLeaf>>() {}.getType();
        TypeVariable<?> variable = GenericBox.class.getTypeParameters()[0];

        StructSerializer basicSerializer = new StructSerializer(basicRoot);
        assertTrue(basicSerializer.isBasic(cint.class));
        assertFalse(basicSerializer.isBasic(Basic.class));
        assertFalse(basicSerializer.isBasic(String.class));
        assertTrue(basicSerializer.isBasic(variable));
        assertFalse(basicSerializer.isBasic(unknownType()));

        StructSerializer structSerializer = new StructSerializer(structRoot);
        assertTrue(structSerializer.isStruct(BigEndianLeaf.class));
        assertTrue(structSerializer.isStruct(structRoot));
        assertTrue(structSerializer.isStruct(variable));
        assertFalse(structSerializer.isStruct(String.class));
        assertFalse(structSerializer.isStruct(unknownType()));

        GenericArrayType genericArray = (GenericArrayType) GenericArrayHolder.class.getDeclaredField("values").getGenericType();
        assertThrows(TypeJudgmentException.class, () -> getStructDefinition(genericArray));
        Type wildcard = ((ParameterizedType) WildcardHolder.class.getDeclaredField("values").getGenericType())
                .getActualTypeArguments()[0];
        assertEquals(getStructDefinition(BigEndianLeaf.class), getStructDefinition(wildcard));
        assertThrows(TypeJudgmentException.class, () -> getStructDefinition(unknownType()));
    }

    @Test
    public void helperCoversLookupConstructionFieldAndArraySemantics() throws Exception {
        ParameterizedType parameterizedCint = parameterized(cint.class);
        assertEquals(4, StructHelper.findBasicSize(cint.class));
        assertEquals(4, StructHelper.findBasicSize(parameterizedCint));
        assertThrows(SerializeException.class, () -> StructHelper.findBasicSize(String.class));
        assertEquals(ByteOrder.nativeOrder(), StructHelper.getByteOrder(Unannotated.class));
        assertEquals(ByteOrder.BIG_ENDIAN, StructHelper.getByteOrder(BigEndianLeaf.class));

        assertEquals(cint[].class, StructHelper.newArray(cint.class, 2).getClass());
        assertEquals(GenericBox[].class, StructHelper.newArray(parameterized(GenericBox.class), 1).getClass());
        assertEquals(Object[].class, StructHelper.newArray(unknownType(), 0).getClass());
        assertThrows(SerializeException.class, () -> StructHelper.newStruct(String.class));

        assertTrue(StructHelper.legalStructField(FieldKinds.class.getDeclaredField("normal")));
        assertFalse(StructHelper.legalStructField(FieldKinds.class.getDeclaredField("staticValue")));
        assertFalse(StructHelper.legalStructField(FieldKinds.class.getDeclaredField("ignored")));
        assertFalse(StructHelper.legalStructField(FieldKinds.class.getDeclaredField("transientValue")));
        assertFalse(StructHelper.isIgnore(FieldKinds.class.getDeclaredField("normal")));
        assertTrue(StructHelper.isIgnore(FieldKinds.class.getDeclaredField("ignored")));
        assertTrue(StructHelper.isIgnore(FieldKinds.class.getDeclaredField("transientValue")));

        BiFunction<ByteBuf, ByteOrder, ?> original = StructSerializerContext.BASIC_CONSTRUCTOR_CACHE.get(cint.class);
        try {
            StructSerializerContext.BASIC_CONSTRUCTOR_CACHE.put(cint.class,
                    (buf, order) -> { throw new RuntimeException(new TooLessBytesException(4, buf.readableBytes())); });
            assertThrows(TooLessBytesException.class,
                         () -> StructHelper.newBasic(cint.class, ByteOrder.BIG_ENDIAN, Unpooled.EMPTY_BUFFER));

            StructSerializerContext.BASIC_CONSTRUCTOR_CACHE.put(cint.class,
                    (buf, order) -> { throw new IllegalStateException("constructor failure"); });
            assertThrows(SerializeException.class,
                         () -> StructHelper.newBasic(cint.class, ByteOrder.BIG_ENDIAN, Unpooled.EMPTY_BUFFER));
        }
        finally {
            StructSerializerContext.BASIC_CONSTRUCTOR_CACHE.put(cint.class, original);
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void defaultFieldHandlerReadsAndWritesEveryCategory() {
        StructField basicField = field(AccessorBean.class, "value");
        StructField structField = field(ConcreteStructHolder.class, "leaf");
        StructFieldHandler handler = DEFAULT_STRUCT_FIELD_HANDLER;
        StructSerializer serializer = new StructSerializer(AccessorBean.class);
        assertTrue(handler.isSingleton());

        ByteBuf basicInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 8});
        ByteBuf structInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 9});
        ByteBuf output = Unpooled.buffer();
        try {
            assertEquals(Integer.valueOf(8), ((cint) handler.doRead(serializer, AccessorBean.class, null,
                    basicField, cint.class, basicInput, null)).value());
            assertEquals(Integer.valueOf(9), ((BigEndianLeaf) handler.doRead(serializer, ConcreteStructHolder.class,
                    null, structField, BigEndianLeaf.class, structInput, null)).getValue().value());
            assertThrows(TypeJudgmentException.class,
                         () -> handler.doRead(serializer, AccessorBean.class, null, basicField,
                                              String.class, Unpooled.EMPTY_BUFFER, null));

            handler.doWrite(serializer, AccessorBean.class, null, basicField, cint.class, new cint(10), output, null);
            handler.doWrite(serializer, AccessorBean.class, null, basicField, cint.class, null, output, null);
            handler.doWrite(serializer, ConcreteStructHolder.class, null, structField,
                            BigEndianLeaf.class, leaf(11), output, null);
            handler.doWrite(serializer, ConcreteStructHolder.class, null, structField,
                            BigEndianLeaf.class, null, output, null);
            assertThrows(TypeJudgmentException.class,
                         () -> handler.doWrite(serializer, AccessorBean.class, null, basicField,
                                               String.class, "bad", output, null));
            assertEquals(16, output.readableBytes());
        }
        finally {
            basicInput.release();
            structInput.release();
            output.release();
        }
    }

    @Test
    public void handlerAnnotationResolutionIgnoresUnrelatedInterfacesAndSupportsInheritance() {
        assertEquals(TestHandlerAnnotation.class, StructSerializerContext.getTargetAnnotationType(MultiInterfaceHandler.class));
        assertEquals(TestHandlerAnnotation.class, StructSerializerContext.getTargetAnnotationType(InheritedHandler.class));
        assertNull(StructSerializerContext.getTargetAnnotationType(GenericHandler.class));
        assertNull(StructSerializerContext.getTargetAnnotationType(StructFieldHandler.class));
    }

    @Test
    public void contextValidationCoversInvalidStructAndBasicDefinitions() {
        new StructSerializerContext("java.lang");
        assertTrue(StructSerializerContext.isScannableClass(AccessorBean.class));
        assertFalse(StructSerializerContext.isScannableClass(String.class));
        assertFalse(StructSerializerContext.isScannableClass(Integer.class));
        assertFalse(StructSerializerContext.isScannableClass(StructFieldHandler.class));
        assertFalse(StructSerializerContext.isScannableClass(AbstractCoverageStruct.class));
        assertFalse(StructSerializerContext.isScannableClass(null));
        ExposedContext context = new ExposedContext();

        context.scanBasicTypes(Set.of(String.class, Basic.class));
        assertThrows(SerializeException.class, () -> context.scanBasicTypes(Set.of(MissingConstructorBasic.class)));
        assertThrows(SerializeException.class, () -> context.scanStructTypes(Set.of(AbstractCoverageStruct.class)));
        assertThrows(SerializeException.class,
                     () -> context.scanStructTypes(Set.of(InvalidNestedStructs.privateStructType())));
    }

    @Test
    public void structFieldMetadataRejectsInvalidPropertiesAndUnresolvedTypes() throws Exception {
        StructField basicField = field(AccessorBean.class, "value");
        assertEquals(basicField.wrapped().toString(), basicField.toString());

        StructField basicBase = new StructField(ByteOrder.BIG_ENDIAN,
                BasicBaseBean.class.getDeclaredField("value"));
        StructField plain = new StructField(ByteOrder.BIG_ENDIAN,
                PlainBean.class.getDeclaredField("value"));
        StructField handled = new StructField(ByteOrder.BIG_ENDIAN,
                HandledBean.class.getDeclaredField("value"));
        assertEquals(StructField.Category.HANDLER, basicBase.category());
        assertEquals(StructField.Category.HANDLER, plain.category());
        assertEquals(StructField.Category.HANDLER, handled.category());

        java.lang.reflect.Field typeResolver = StructField.class.getDeclaredField("type");
        typeResolver.setAccessible(true);
        Object original = typeResolver.get(basicField);
        try {
            typeResolver.set(basicField, (UnaryOperator<Type>) ignored -> null);
            assertThrows(TypeJudgmentException.class, () -> basicField.type(AccessorBean.class));
            typeResolver.set(basicField,
                             (UnaryOperator<Type>) ignored -> GenericBox.class.getTypeParameters()[0]);
            assertThrows(TypeJudgmentException.class, () -> basicField.type(AccessorBean.class));
        }
        finally {
            typeResolver.set(basicField, original);
        }

        assertInvalidProperty(StaticGetterBean.class);
        assertInvalidProperty(WrongGetterBean.class);
        assertInvalidProperty(PrivateGetterBean.class);
        assertInvalidProperty(PrivateSetterBean.class);
        assertInvalidProperty(StaticSetterBean.class);
        assertInvalidProperty(NonVoidSetterBean.class);
        assertInvalidProperty(MultiParameterSetterBean.class);
        assertInvalidProperty(SubtypeSetterBean.class);

        StructField generic = field(GenericBox.class, "value");
        assertThrows(TypeJudgmentException.class, () -> generic.type(GenericBox.class));
        assertThrows(TypeJudgmentException.class, () -> generic.type(String.class));
        Type unrelatedParameterized = new TypeReference<List<String>>() {}.getType();
        assertThrows(TypeJudgmentException.class, () -> generic.type(unrelatedParameterized));
        Type unrelatedClassHierarchy = new TypeReference<GenericPair<cint, cint>>() {}.getType();
        assertThrows(TypeJudgmentException.class, () -> generic.type(unrelatedClassHierarchy));
        assertThrows(TypeJudgmentException.class, () -> generic.type(unknownType()));

        GenericArrayType directArray = () -> BigEndianLeaf.class;
        assertEquals(getStructDefinition(BigEndianLeaf.class), getStructDefinition(directArray));
    }

    private static StructField field(Class<?> type, String name) {
        StructDefinition definition = getStructDefinition(type);
        for (StructField field : definition.fields()) {
            if (field.wrapped().getName().equals(name)) return field;
        }
        throw new AssertionError("field not found: " + name);
    }

    private static BigEndianLeaf leaf(int value) {
        BigEndianLeaf leaf = new BigEndianLeaf();
        leaf.setValue(new cint(value));
        return leaf;
    }

    private static byte[] bytes(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static Type unknownType() {
        return new Type() {
            @Override
            public String getTypeName() {
                return "unknown";
            }
        };
    }

    private static ParameterizedType parameterized(Class<?> rawType) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[0]; }
            @Override public Type getRawType() { return rawType; }
            @Override public Type getOwnerType() { return null; }
        };
    }

    private static void assertInvalidProperty(Class<?> beanType) throws Exception {
        java.lang.reflect.Field field = beanType.getDeclaredField("value");
        assertThrows(SerializeException.class, () -> new StructField(ByteOrder.BIG_ENDIAN, field));
    }

    private static class GenericArrayHolder<T> {
        T[] values;
    }

    private static class WildcardHolder {
        java.util.List<? extends BigEndianLeaf> values;
    }

    private static class Unannotated {
    }

    private static class FieldKinds {
        static int staticValue;
        int normal;
        @Ignore int ignored;
        transient int transientValue;
    }

    private @interface TestHandlerAnnotation {
    }

    private static class MultiInterfaceHandler
            implements Comparable<MultiInterfaceHandler>, StructFieldHandler<TestHandlerAnnotation> {
        @Override
        public int compareTo(MultiInterfaceHandler other) {
            return 0;
        }
    }

    private static class GenericHandler<A extends Annotation> implements StructFieldHandler<A> {
    }

    private static class InheritedHandler extends GenericHandler<TestHandlerAnnotation> {
    }

    private static class ExposedContext extends StructSerializerContext {
        ExposedContext() {
            super("package.that.does.not.exist");
        }

        void scanBasicTypes(Set<Class<?>> types) {
            super.scanBasic(types);
        }

        void scanStructTypes(Set<Class<?>> types) {
            super.scanStruct(types);
        }
    }

    public static class BasicBaseBean {
        private Basic value;
        public Basic getValue() { return value; }
        public void setValue(Basic value) { this.value = value; }
    }

    public static class PlainBean {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class HandledBean {
        @ToCharSequence(bufferLength = 1)
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class StaticGetterBean {
        private cint value;
        public static cint getValue() { return null; }
        public void setValue(cint value) { this.value = value; }
    }

    public static class WrongGetterBean {
        private cint value;
        public String getValue() { return ""; }
        public void setValue(cint value) { this.value = value; }
    }

    public static class StaticSetterBean {
        private cint value;
        public cint getValue() { return value; }
        public static void setValue(cint value) { }
    }

    public static class PrivateGetterBean {
        private cint value;
        private cint getValue() { return value; }
        public void setValue(cint value) { this.value = value; }
    }

    public static class PrivateSetterBean {
        private cint value;
        public cint getValue() { return value; }
        private void setValue(cint value) { this.value = value; }
    }

    public static class NonVoidSetterBean {
        private cint value;
        public cint getValue() { return value; }
        public cint setValue(cint value) { return this.value = value; }
    }

    public static class MultiParameterSetterBean {
        private cint value;
        public cint getValue() { return value; }
        public void setValue(cint value, int ignored) { this.value = value; }
    }

    public static class SubtypeSetterBean {
        private Basic value;
        public Basic getValue() { return value; }
        public void setValue(cint value) { this.value = value; }
    }
}
