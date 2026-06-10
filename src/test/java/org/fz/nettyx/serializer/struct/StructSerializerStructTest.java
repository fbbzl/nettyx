package org.fz.nettyx.serializer.struct;

import org.fz.nettyx.codec.model.Bill;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.codec.model.Lover;
import org.fz.nettyx.codec.model.You;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fz.nettyx.serializer.struct.StructSerializer.toBytes;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

public class StructSerializerStructTest {

    @BeforeClass
    public static void init() {
        new StructSerializerContext("org.fz.nettyx.codec.model");
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

        byte[] bytes = toBytes(you);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        You result = toStruct(You.class, bytes);
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getIsMarried().read());
    }

    @Test
    public void testStructWithString() {
        Lover lover = new Lover();
        byte[] bytes = toBytes(lover);
        assertNotNull(bytes);
        assertEquals(2, bytes.length);
    }

    @Test
    public void testStructWithToCharSequence() {
        GirlFriend gf = new GirlFriend();
        gf.setCup("C");

        byte[] bytes = toBytes(GirlFriend.class, gf);
        assertNotNull(bytes);
    }

    @Test
    public void testStructWithEnum() {
        Bill bill = new Bill();
        bill.setBid(new cuchar(1));
        bill.setOrgName("test");
        bill.setBillType(Bill.BillType.CCC);

        byte[] bytes = toBytes(Bill.class, bill);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        Bill result = toStruct(Bill.class, bytes);
        assertNotNull(result);
        assertEquals(Bill.BillType.CCC, result.getBillType());
    }

    @Test(expected = StructDefinitionException.class)
    public void testNonStructClassThrows() {
        toStruct(String.class, new byte[]{1, 2, 3});
    }
}
