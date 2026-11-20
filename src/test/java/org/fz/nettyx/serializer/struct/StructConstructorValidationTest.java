package org.fz.nettyx.serializer.struct;

import org.fz.nettyx.exception.SerializeException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

/**
 * Checks that registration rejects structs without an accessible no-argument constructor.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
public class StructConstructorValidationTest {

    @Test
    public void missingNoArgConstructorFailsDuringScan()
    {
        assertThrows(SerializeException.class,
                     () -> new StructContext("org.fz.nettyx.invalidmodel.missing"));
    }

    @Test
    public void privateNoArgConstructorFailsDuringScan()
    {
        assertThrows(SerializeException.class,
                     () -> new StructContext("org.fz.nettyx.invalidmodel.privatector"));
    }
}
