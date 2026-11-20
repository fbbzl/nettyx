package org.fz.nettyx.serializer.struct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes private scanner operations without running a full package scan.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-13
 */
public final class StructScannerTestSupport {

    private StructScannerTestSupport() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(String name, Class<?> parameterType, Object argument) {
        try {
            Class<?> scanner = Class.forName(StructContext.class.getName() + "$StructScanner");
            Constructor<?> constructor = scanner.getDeclaredConstructor(String[].class);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance((Object) new String[]{"package.that.does.not.exist"});
            Method method = scanner.getDeclaredMethod(name, parameterType);
            method.setAccessible(true);
            return (T) method.invoke(instance, argument);
        }
        catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) throw runtimeException;
            if (cause instanceof Error error) throw error;
            throw new AssertionError(cause);
        }
        catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}