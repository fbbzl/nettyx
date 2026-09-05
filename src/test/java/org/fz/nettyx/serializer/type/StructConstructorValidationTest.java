package org.fz.nettyx.serializer.type;

import org.fz.nettyx.exception.SerializeException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class StructConstructorValidationTest {

    @Test
    public void missingNoArgConstructorFailsDuringScan()
    {
        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.invalidmodel.missing"));
    }

    @Test
    public void privateNoArgConstructorFailsDuringScan()
    {
        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.invalidmodel.privatector"));
    }
}
