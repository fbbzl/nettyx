package org.fz.nettyx.serializer.struct.accessor;

import org.fz.nettyx.beanmodel.support.BigEndianLeaf;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Checks package scanning and registration without callbacks on the context.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class StructContextScannerTest {

    @Test
    public void scannerOwnsPackageConfigurationAndScanningEntryPoints() throws Exception {
        Class<?> scanner = scannerClass();
        assertEquals(String[].class, scanner.getDeclaredField("basePackages").getType());
        assertThrows(NoSuchFieldException.class, () -> StructContext.class.getDeclaredField("basePackages"));
        for (String name : Set.of("getBasePackages", "scan", "classesForScan", "scanHandler", "scanBasic",
                                  "scanStruct", "isScannableClass", "getTargetAnnotationType")) {
            assertFalse(name, Arrays.stream(StructContext.class.getDeclaredMethods())
                                    .anyMatch(method -> method.getName().equals(name)));
        }
        assertFalse(Arrays.stream(scanner.getDeclaredMethods())
                          .anyMatch(method -> method.getName().equals("initialize")));
    }

    @Test
    public void scannerNormalizesDefaultAndExplicitPackages() throws Exception {
        assertArrayEquals(new String[]{""}, packages(newScanner()));
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                                                            () -> newScanner((String[]) null));
        assertTrue(exception.getCause() instanceof NullPointerException);
        assertArrayEquals(new String[]{""}, packages(newScanner((String) null)));
        assertArrayEquals(new String[]{"one", "two"}, packages(newScanner("one", null, "two")));
    }

    @Test
    public void scannerDiscoversExplicitAndBuiltInPackages() throws Exception {
        Object scanner = newScanner("org.fz.nettyx.beanmodel.support");
        Method discover = scannerClass().getDeclaredMethod("classesForScan");
        discover.setAccessible(true);
        Set<Class<?>> classes = (Set<Class<?>>) discover.invoke(scanner);

        assertTrue(classes.contains(BigEndianLeaf.class));
        assertTrue(classes.contains(ScannedContextPackageStruct.class));
        assertTrue(classes.contains(cint.class));

        new StructContext("org.fz.nettyx.beanmodel.support");
        assertNotNull(StructContext.getStructDefinition(BigEndianLeaf.class));
        assertNotNull(StructContext.getStructDefinition(ScannedContextPackageStruct.class));
        assertEquals(Integer.BYTES, StructContext.getBasicSize(cint.class));
    }

    @Test
    public void constructionDoesNotCallContextSubclassScanningMethods() {
        new CallbackContext();
    }

    private static Class<?> scannerClass() throws ClassNotFoundException {
        return Class.forName(StructContext.class.getName() + "$StructScanner");
    }

    private static Object newScanner(String... packages) throws Exception {
        Constructor<?> constructor = scannerClass().getDeclaredConstructor(String[].class);
        constructor.setAccessible(true);
        return constructor.newInstance((Object) packages);
    }

    private static String[] packages(Object scanner) throws Exception {
        Field packages = scannerClass().getDeclaredField("basePackages");
        packages.setAccessible(true);
        return (String[]) packages.get(scanner);
    }

    /**
     * Fails if the scanner calls package lookup, scanning, or class discovery on the context.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private static final class CallbackContext extends StructContext {
        private CallbackContext() {
            super("package.that.does.not.exist");
        }

        public String[] getBasePackages() {
            throw new AssertionError("context package callback");
        }

        protected void scan() {
            throw new AssertionError("context scan callback");
        }

        protected Set<Class<?>> classesForScan() {
            throw new AssertionError("context discovery callback");
        }
    }

    /**
     * Provides an annotated struct for discovery in the scanner's test package.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class ScannedContextPackageStruct {
    }
}