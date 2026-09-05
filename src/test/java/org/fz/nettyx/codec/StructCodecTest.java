package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.serializer.type.StructSerializerContext;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StructCodecTest {

    @BeforeClass
    public static void scanStructs() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.valid");
    }

    @Test
    public void defaultCodecEncodesAndSkipsTrailingBytes() {
        TestAccessorCodec codec = new TestAccessorCodec();
        AccessorBean bean = new AccessorBean();
        bean.setValue(new cint(0x01020304));
        ByteBuf encoded = Unpooled.buffer();

        codec.encodeValue(bean, encoded);
        byte[] actual = new byte[encoded.readableBytes()];
        encoded.getBytes(encoded.readerIndex(), actual);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, actual);
        assertEquals(AccessorBean.class, codec.getType());
        assertTrue(codec.isSkipLeftBytes());

        encoded.writeByte(0x55);
        AccessorBean decoded = codec.decodeValue(encoded);
        assertEquals(Integer.valueOf(0x01020304), decoded.getValue().value());
        assertEquals(0, encoded.readableBytes());
        encoded.release();
    }

    @Test
    public void configuredCodecCanKeepTrailingBytes() {
        TestAccessorCodec codec = new TestAccessorCodec(false);
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 0x55});

        AccessorBean decoded = codec.decodeValue(input);

        assertEquals(Integer.valueOf(0x01020304), decoded.getValue().value());
        assertFalse(codec.isSkipLeftBytes());
        assertEquals(1, input.readableBytes());
        assertEquals(0x55, input.readUnsignedByte());
        input.release();
    }

    private static final class TestAccessorCodec extends StructCodec<AccessorBean> {
        private TestAccessorCodec() {
        }

        private TestAccessorCodec(boolean skipLeftBytes) {
            super(skipLeftBytes);
        }

        private void encodeValue(AccessorBean value, ByteBuf out) {
            super.encode(null, value, out);
        }

        private AccessorBean decodeValue(ByteBuf input) {
            List<Object> output = new ArrayList<>();
            super.decode(null, input, output);
            assertEquals(1, output.size());
            return (AccessorBean) output.get(0);
        }
    }
}
