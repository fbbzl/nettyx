package org.fz.nettyx.codec;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies struct codec round trips, trailing byte handling, and inherited type resolution.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class StructCodecTest {

    @BeforeClass
    public static void scanStructs() {
        new StructContext("org.fz.nettyx.beanmodel.valid", "org.fz.nettyx.codec.model");
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

    @Test
    public void inheritedCodecUsesTheStructParameterRatherThanTheFirstSubclassParameter() {
        IntBoxCodec codec = new IntBoxCodec();
        assertEquals(new TypeReference<GenericBox<cint>>() {}.getType(), codec.getType());
        GenericBox<cint> box = new GenericBox<>();
        box.setValue(new cint(0x01020304));
        ByteBuf buffer = Unpooled.buffer();
        try {
            codec.encode(null, box, buffer);
            assertEquals(Integer.BYTES, buffer.readableBytes());
            List<Object> output = new ArrayList<>();
            codec.decode(null, buffer, output);
            assertEquals(Integer.valueOf(0x01020304), ((GenericBox<cint>) output.get(0)).getValue().value());
            assertEquals(0, buffer.readableBytes());
        }
        finally {
            buffer.release();
        }
    }

    /**
     * Forwards its second type parameter to {@code StructCodec}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private abstract static class ReorderedCodec<A, B> extends StructCodec<B> {}

    /**
     * Binds the inherited codec to a generic integer box through its second type parameter.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private static final class IntBoxCodec extends ReorderedCodec<String, GenericBox<cint>> {}

    /**
     * Exposes encoding and decoding of {@code AccessorBean} for direct buffer assertions.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
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
