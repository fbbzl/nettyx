package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.ArrayAsTypeArgGeneric;
import org.fz.nettyx.codec.model.MultiDimArrayGeneric;
import org.fz.nettyx.codec.model.RecursiveGeneric;
import org.fz.nettyx.codec.model.WildcardGeneric;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.junit.Test;

import java.nio.ByteOrder;
import java.util.Arrays;

import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExtraGenericScenarioTest extends AbstractStructSerializerTest {

    @Test
    public void testIgnoredRecursiveGenericField() {
        ByteBuf input = Unpooled.buffer(Integer.BYTES);
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) input.writeInt(67);
        else                                                  input.writeIntLE(67);
        RecursiveGeneric<cint> value = toStruct(new TypeReference<RecursiveGeneric<cint>>() {}, input);

        assertEquals(Integer.valueOf(67), value.getValue().value());
        assertNull(value.getNext());
        input.release();
    }

    @Test(expected = StructDefinitionException.class)
    public void testMultiDimArrayGenericUnsupported() {
        byte[] bytes = new byte[256];
        Arrays.fill(bytes, (byte) 67);
        toStruct(new TypeReference<MultiDimArrayGeneric<cint>>() {}, Unpooled.wrappedBuffer(bytes));
    }

    @Test(expected = TypeJudgmentException.class)
    public void testArrayAsTypeArgGenericUnsupported() {
        byte[] bytes = new byte[256];
        Arrays.fill(bytes, (byte) 67);
        toStruct(new TypeReference<ArrayAsTypeArgGeneric<cint, cchar>>() {}, Unpooled.wrappedBuffer(bytes));
    }

    @Test(expected = TypeJudgmentException.class)
    public void testWildcardGenericUnsupported() {
        byte[] bytes = new byte[64];
        Arrays.fill(bytes, (byte) 67);
        toStruct(new TypeReference<WildcardGeneric>() {}, Unpooled.wrappedBuffer(bytes));
    }

}
