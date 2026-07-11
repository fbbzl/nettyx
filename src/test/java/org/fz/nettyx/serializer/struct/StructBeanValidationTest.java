package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.beanmodel.valid.InheritedAccessorBean;
import org.fz.nettyx.exception.SerializeException;
import org.junit.Test;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.*;

public class StructBeanValidationTest {

    @Test
    public void generatedAccessorCallsBeanGetterAndSetter() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.valid");

        AccessorBean decoded = toStruct(AccessorBean.class,
                                        Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 42}));
        assertEquals(1, decoded.setterCalls());
        assertEquals(Integer.valueOf(42), decoded.getValue().value());
        int getterCallsBeforeWrite = decoded.getterCalls();

        ByteBuf output = Unpooled.buffer();
        try {
            toByteBuf(decoded, output);
            assertEquals(getterCallsBeforeWrite + 1, decoded.getterCalls());
            byte[] actual = new byte[output.readableBytes()];
            output.readBytes(actual);
            assertArrayEquals(new byte[]{0, 0, 0, 42}, actual);
        }
        finally {
            output.release();
        }
    }

    @Test
    public void inheritedBeanAccessorsAreSupported() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.valid");

        InheritedAccessorBean decoded = toStruct(InheritedAccessorBean.class,
                                                  Unpooled.wrappedBuffer(new byte[]{42, 0, 0, 0}));
        assertEquals(Integer.valueOf(42), decoded.getInherited().value());
    }

    @Test
    public void missingGetterIsRejectedDuringScan() {
        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.beanmodel.missinggetter"));
    }

    @Test
    public void missingSetterIsRejectedDuringScan() {
        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.beanmodel.missingsetter"));
    }

    @Test
    public void nonVoidSetterIsRejectedDuringScan() {
        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.beanmodel.wrongsetter"));
    }
}
