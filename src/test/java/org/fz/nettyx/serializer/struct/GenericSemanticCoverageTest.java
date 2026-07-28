package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.generic.BigEndianGenericValue;
import org.fz.nettyx.beanmodel.generic.ConcreteGenericHierarchy;
import org.fz.nettyx.beanmodel.generic.GenericStructArrayBean;
import org.fz.nettyx.beanmodel.generic.LittleEndianGenericValue;
import org.fz.nettyx.beanmodel.valid.FlexibleGenericBasicArrayBean;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

public class GenericSemanticCoverageTest {

    @BeforeClass
    public static void scanGenericModels() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.generic", "org.fz.nettyx.beanmodel.valid",
                                    "org.fz.nettyx.codec.model");
    }

    @Test
    public void genericBasicUsesResolvedTypeValueAndDeclaredByteOrder() {
        Type bigEndianType = new TypeReference<BigEndianGenericValue<cint>>() {}.getType();
        BigEndianGenericValue<cint> bigEndian = deserialize(bigEndianType, new byte[]{1, 2, 3, 4});
        assertEquals(cint.class, bigEndian.getValue().getClass());
        assertEquals(Integer.valueOf(0x01020304), bigEndian.getValue().value());

        Type littleEndianType = new TypeReference<LittleEndianGenericValue<cint>>() {}.getType();
        LittleEndianGenericValue<cint> littleEndian = deserialize(littleEndianType, new byte[]{4, 3, 2, 1});
        assertEquals(cint.class, littleEndian.getValue().getClass());
        assertEquals(Integer.valueOf(0x01020304), littleEndian.getValue().value());

        assertArrayEquals(new byte[]{1, 2, 3, 4}, serialize(bigEndianType, bigEndian));
        assertArrayEquals(new byte[]{4, 3, 2, 1}, serialize(littleEndianType, littleEndian));
    }

    @Test
    public void sameRawGenericTypeKeepsActualTypesIsolated() {
        Type intType = new TypeReference<BigEndianGenericValue<cint>>() {}.getType();
        Type charType = new TypeReference<BigEndianGenericValue<cchar>>() {}.getType();

        BigEndianGenericValue<cint> intValue = deserialize(intType, new byte[]{0, 0, 0, 42});
        BigEndianGenericValue<cchar> charValue = deserialize(charType, new byte[]{7});
        BigEndianGenericValue<cint> intValueAgain = deserialize(intType, new byte[]{0, 0, 0, 43});

        assertEquals(cint.class, intValue.getValue().getClass());
        assertEquals(Integer.valueOf(42), intValue.getValue().value());
        assertEquals(cchar.class, charValue.getValue().getClass());
        assertEquals(Byte.valueOf((byte) 7), charValue.getValue().value());
        assertEquals(cint.class, intValueAgain.getValue().getClass());
        assertEquals(Integer.valueOf(43), intValueAgain.getValue().value());
    }

    @Test
    public void concreteChildResolvesReorderedTypesAcrossMultipleGenericLevels() {
        byte[] bytes = {127, 1, 2, 3, 4};
        ConcreteGenericHierarchy decoded = deserialize(ConcreteGenericHierarchy.class, bytes);

        assertEquals(cchar.class, decoded.getFirst().getClass());
        assertEquals(Byte.valueOf((byte) 127), decoded.getFirst().value());
        assertEquals(cint.class, decoded.getSecond().getClass());
        assertEquals(Integer.valueOf(0x01020304), decoded.getSecond().value());
        assertArrayEquals(bytes, serialize(ConcreteGenericHierarchy.class, decoded));
    }

    @Test
    public void parameterizedStructArraysSupportFixedAndFlexibleElements() {
        Type type = new TypeReference<GenericStructArrayBean<BigEndianGenericValue<cint>>>() {}.getType();
        byte[] bytes = {
                0, 0, 0, 1,
                0, 0, 0, 2,
                0, 0, 0, 3
        };

        GenericStructArrayBean<BigEndianGenericValue<cint>> decoded = deserialize(type, bytes);
        assertEquals(2, decoded.getFixed().length);
        assertEquals(1, decoded.getFixed()[0].getValue().value().intValue());
        assertEquals(2, decoded.getFixed()[1].getValue().value().intValue());
        assertEquals(1, decoded.getFlexible().length);
        assertEquals(3, decoded.getFlexible()[0].getValue().value().intValue());
        assertArrayEquals(bytes, serialize(type, decoded));
    }

    @Test
    public void nullFixedAndEmptyFlexibleGenericStructArraysHaveDeterministicEncoding() {
        Type type = new TypeReference<GenericStructArrayBean<BigEndianGenericValue<cint>>>() {}.getType();
        GenericStructArrayBean<BigEndianGenericValue<cint>> value = new GenericStructArrayBean<>();
        value.setFlexible(new BigEndianGenericValue[0]);

        byte[] bytes = serialize(type, value);
        assertArrayEquals(new byte[8], bytes);

        GenericStructArrayBean<BigEndianGenericValue<cint>> decoded = deserialize(type, bytes);
        assertEquals(2, decoded.getFixed().length);
        assertEquals(Integer.valueOf(0), decoded.getFixed()[0].getValue().value());
        assertEquals(Integer.valueOf(0), decoded.getFixed()[1].getValue().value());
        assertEquals(0, decoded.getFlexible().length);
    }

    @Test
    public void flexibleGenericBasicArraySupportsEmptyInputAndRejectsPartialElement() {
        Type type = new TypeReference<FlexibleGenericBasicArrayBean<cint>>() {}.getType();
        FlexibleGenericBasicArrayBean<cint> empty = deserialize(type, new byte[0]);
        assertEquals(0, empty.getValues().length);

        ByteBuf partial = Unpooled.wrappedBuffer(new byte[]{1, 0, 0, 0, 2, 0});
        try {
            assertThrows(TooLessBytesException.class, () -> toStruct(type, partial));
            assertEquals(4, partial.readerIndex());
        }
        finally {
            partial.release();
        }
    }

    @Test
    public void unresolvedRawWildcardAndUnsupportedActualTypesAreRejected() {
        assertThrows(TypeJudgmentException.class,
                     () -> deserialize(GenericBox.class, new byte[]{0, 0, 0, 1}));

        Type stringType = new TypeReference<GenericBox<String>>() {}.getType();
        assertThrows(TypeJudgmentException.class,
                     () -> deserialize(stringType, new byte[]{0, 0, 0, 1}));

        Type wildcardType = new TypeReference<GenericBox<? extends cint>>() {}.getType();
        assertThrows(TypeJudgmentException.class,
                     () -> deserialize(wildcardType, new byte[]{0, 0, 0, 1}));

        Type arrayType = new TypeReference<GenericBox<cint[]>>() {}.getType();
        assertThrows(TypeJudgmentException.class,
                     () -> deserialize(arrayType, new byte[]{0, 0, 0, 1}));
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserialize(Type type, byte[] bytes) {
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        try {
            T value = toStruct(type, input);
            assertEquals(bytes.length, input.readerIndex());
            return value;
        }
        finally {
            input.release();
        }
    }

    private static byte[] serialize(Type type, Object value) {
        ByteBuf output = Unpooled.buffer();
        try {
            toByteBuf(type, value, output);
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        }
        finally {
            output.release();
        }
    }
}
