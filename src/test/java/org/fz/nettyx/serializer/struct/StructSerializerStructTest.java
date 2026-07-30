package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.FlexibleBasicArrayBean;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.codec.model.Lover;
import org.fz.nettyx.codec.model.You;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

public class StructSerializerStructTest {

    @BeforeClass
    public static void init() {
        new StructSerializerContext("org.fz.nettyx.codec.model", "org.fz.nettyx.beanmodel.valid");
    }

    @Test
    public void testSimpleStructRoundTrip() {
        String base     = "org.fz.nettyx.codec.model";
        new StructSerializerContext(base);
        You you = new You();

        you.setChunk(new byte[]{1, 2, 3, 4, 5});
        you.setUname(new cchar(65));
        you.setIsMarried(new cint(1));
        you.setSex(new cuchar(1));
        you.setAddress(new cfloat(1.5f));
        you.setPlatformId(new cdouble(123.456));
        you.setInterest(new culong8(java.math.BigInteger.valueOf(789)));

        ByteBuf youBuf = Unpooled.buffer();
        toByteBuf(you, youBuf);
        byte[] bytes = new byte[youBuf.readableBytes()];
        youBuf.readBytes(bytes);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        You result = toStruct(You.class, Unpooled.wrappedBuffer(bytes));
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getIsMarried().value());
    }

    @Test
    public void testStructWithString() {
        Lover lover = new Lover();
        ByteBuf loverBuf = Unpooled.buffer();
        toByteBuf(lover, loverBuf);
        byte[] bytes = new byte[loverBuf.readableBytes()];
        loverBuf.readBytes(bytes);
        assertNotNull(bytes);
        assertEquals(2, bytes.length);
    }

    @Test
    public void testStructWithToCharSequence() {
        GirlFriend gf = new GirlFriend();
        gf.setCup("C");

        ByteBuf gfBuf = Unpooled.buffer();
        toByteBuf(GirlFriend.class, gf, gfBuf);
        byte[] bytes = new byte[gfBuf.readableBytes()];
        gfBuf.readBytes(bytes);
        assertNotNull(bytes);
    }

    @Test
    public void flexibleBasicArrayUsesAllCompleteElements() {
        FlexibleBasicArrayBean empty = toStruct(FlexibleBasicArrayBean.class, Unpooled.EMPTY_BUFFER);
        assertEquals(0, empty.getValues().length);

        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 0, 0, 0, 2, 0, 0, 0});
        FlexibleBasicArrayBean decoded = toStruct(FlexibleBasicArrayBean.class, input);
        assertEquals(2, decoded.getValues().length);
        assertEquals(Integer.valueOf(1), decoded.getValues()[0].value());
        assertEquals(Integer.valueOf(2), decoded.getValues()[1].value());
        assertEquals(8, input.readerIndex());
    }

    @Test
    public void flexibleBasicArrayRejectsPartialLastElement() {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 0, 0, 0, 2, 0});
        assertThrows(TooLessBytesException.class,
                     () -> toStruct(FlexibleBasicArrayBean.class, input));
        assertEquals(4, input.readerIndex());
    }

    @Test(expected = StructDefinitionException.class)
    public void testNonStructClassThrows() {
        toStruct(String.class, Unpooled.wrappedBuffer(new byte[]{1, 2, 3}));
    }
}
