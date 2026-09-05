package org.fz.nettyx.serializer.type;

import org.fz.nettyx.beanmodel.rollback.AValidRollbackBean;
import org.fz.nettyx.exception.SerializeException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.fz.nettyx.serializer.type.StructSerializerContext.STRUCT_DEFINITION_CACHE;
import static org.junit.Assert.*;

public class StructScanRollbackTest {

    @Test
    public void failedScanRestoresStructDefinitionCache() {
        Map<Class<?>, StructSerializerContext.StructDefinition> before =
                new HashMap<>(STRUCT_DEFINITION_CACHE);

        assertThrows(SerializeException.class,
                     () -> new StructSerializerContext("org.fz.nettyx.beanmodel.rollback"));

        assertEquals(before, STRUCT_DEFINITION_CACHE);
        assertFalse(STRUCT_DEFINITION_CACHE.containsKey(AValidRollbackBean.class));
    }
}
