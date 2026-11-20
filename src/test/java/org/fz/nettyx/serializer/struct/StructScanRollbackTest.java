package org.fz.nettyx.serializer.struct;

import org.fz.nettyx.beanmodel.rollback.AValidRollbackBean;
import org.fz.nettyx.beanmodel.valid.AccessorBean;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.fz.nettyx.serializer.struct.StructContext.*;
import static org.junit.Assert.*;

/**
 * Checks that failed scans restore registration caches and that lookups proceed while scanning is blocked.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
public class StructScanRollbackTest {

    @Test
    public void failedScanRestoresAllRegistrationCaches() {
        synchronized (StructContext.class) {
            var sizesBefore = new HashMap<>(BASIC_SIZE_CACHE);
            var constructorsBefore = new HashMap<>(BASIC_CONSTRUCTOR_CACHE);
            var handlersBefore = new HashMap<>(ANNOTATION_HANDLER_MAPPING_CACHE);
            Map<Class<?>, StructContext.StructDefinition> definitionsBefore = new HashMap<>(STRUCT_DEFINITION_CACHE);

            try {
                BASIC_SIZE_CACHE.clear();
                BASIC_CONSTRUCTOR_CACHE.clear();
                ANNOTATION_HANDLER_MAPPING_CACHE.clear();

                assertThrows(SerializeException.class,
                             () -> new StructContext("org.fz.nettyx.beanmodel.rollback"));

                assertTrue(BASIC_SIZE_CACHE.isEmpty());
                assertTrue(BASIC_CONSTRUCTOR_CACHE.isEmpty());
                assertTrue(ANNOTATION_HANDLER_MAPPING_CACHE.isEmpty());
                assertEquals(definitionsBefore, STRUCT_DEFINITION_CACHE);
                assertFalse(STRUCT_DEFINITION_CACHE.containsKey(AValidRollbackBean.class));
            }
            finally {
                restore(BASIC_SIZE_CACHE, sizesBefore);
                restore(BASIC_CONSTRUCTOR_CACHE, constructorsBefore);
                restore(ANNOTATION_HANDLER_MAPPING_CACHE, handlersBefore);
                restore(STRUCT_DEFINITION_CACHE, definitionsBefore);
            }
        }
    }

    @Test
    public void failedScanPreservesExistingRegistrations() {
        new StructContext("org.fz.nettyx.beanmodel.valid");
        Integer existingSize = BASIC_SIZE_CACHE.get(cint.class);
        var existingConstructor = BASIC_CONSTRUCTOR_CACHE.get(cint.class);
        StructContext.StructDefinition existingDefinition = STRUCT_DEFINITION_CACHE.get(AccessorBean.class);
        assertNotNull(existingSize);
        assertNotNull(existingConstructor);
        assertNotNull(existingDefinition);

        assertThrows(SerializeException.class,
                     () -> new StructContext("org.fz.nettyx.beanmodel.rollback"));

        assertSame(existingSize, BASIC_SIZE_CACHE.get(cint.class));
        assertSame(existingConstructor, BASIC_CONSTRUCTOR_CACHE.get(cint.class));
        assertSame(existingDefinition, STRUCT_DEFINITION_CACHE.get(AccessorBean.class));
        assertFalse(STRUCT_DEFINITION_CACHE.containsKey(AValidRollbackBean.class));
    }

    @Test
    public void registrationLookupsDoNotWaitForScanMonitor() throws Exception {
        new StructContext("org.fz.nettyx.beanmodel.valid");
        StructContext.StructDefinition definition = STRUCT_DEFINITION_CACHE.get(AccessorBean.class);
        CountDownLatch lookupStarted = new CountDownLatch(1);
        CountDownLatch lookupFinished = new CountDownLatch(1);
        AtomicReference<Boolean> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            lookupStarted.countDown();
            try {
                result.set(getBasicSize(cint.class) == Integer.BYTES
                           && getBasicConstructor(cint.class) != null
                           && getStructDefinition(AccessorBean.class) == definition);
            }
            catch (Throwable error) {
                failure.set(error);
            }
            finally {
                lookupFinished.countDown();
            }
        });
        reader.setDaemon(true);

        try {
            synchronized (StructContext.class) {
                reader.start();
                assertTrue(lookupStarted.await(5, TimeUnit.SECONDS));
                assertTrue("existing lookups must not wait for the scanner monitor",
                           lookupFinished.await(5, TimeUnit.SECONDS));
            }

            reader.join(TimeUnit.SECONDS.toMillis(5));
            assertFalse(reader.isAlive());
            assertNull(failure.get());
            assertEquals(Boolean.TRUE, result.get());
        }
        finally {
            if (reader.isAlive()) reader.interrupt();
        }
    }

    private static <K, V> void restore(Map<K, V> cache, Map<K, V> snapshot) {
        cache.clear();
        cache.putAll(snapshot);
    }
}
