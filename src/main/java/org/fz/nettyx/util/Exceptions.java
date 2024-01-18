package org.fz.nettyx.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/14 14:13
 */
public final class Exceptions {

    private Exceptions() {
        throw new UnsupportedOperationException();
    }

    public static RuntimeException newRuntimeException() {
        return new RuntimeException();
    }

    public static RuntimeException newRuntimeException(String exceptionMessage) {
        return new RuntimeException(exceptionMessage);
    }

    public static RuntimeException newRuntimeException(Throwable throwable) {
        return new RuntimeException(throwable);
    }

    public static RuntimeException newRuntimeException(String exceptionMessage, Throwable throwable) {
        return new RuntimeException(exceptionMessage, throwable);
    }

    public static IllegalArgumentException newIllegalArgException() {
        return new IllegalArgumentException();
    }

    public static IllegalArgumentException newIllegalArgException(String exceptionMessage) {
        return new IllegalArgumentException(exceptionMessage);
    }

    public static IllegalArgumentException newIllegalArgException(Throwable throwable) {
        return new IllegalArgumentException(throwable);
    }

    public static IllegalArgumentException newIllegalArgException(String exceptionMessage, Throwable throwable) {
        return new IllegalArgumentException(exceptionMessage, throwable);
    }

    public static UnsupportedOperationException newUnSupportException() {
        return new UnsupportedOperationException();
    }

    public static UnsupportedOperationException newUnSupportException(String exceptionMessage) {
        return new UnsupportedOperationException(exceptionMessage);
    }

    public static UnsupportedOperationException newUnSupportException(Throwable throwable) {
        return new UnsupportedOperationException(throwable);
    }

    public static UnsupportedOperationException newUnSupportException(String exceptionMessage, Throwable throwable) {
        return new UnsupportedOperationException(exceptionMessage, throwable);
    }

    /**
     * coordinate Optional.orElseThrow {@link Optional#orElseThrow(Supplier)}  } In order to support the Optional API, this method is provided
     *
     * @param exceptionMessage exception message
     * @return exception supplier
     */
    public static Supplier<RuntimeException> exception(String exceptionMessage) {
        return () -> new RuntimeException(exceptionMessage);
    }

    public static Supplier<IllegalArgumentException> illegal(String exceptionMessage) {
        return () -> newIllegalArgException(exceptionMessage);
    }

    public static Supplier<UnsupportedOperationException> unSupport(String exceptionMessage) {
        return () -> newUnSupportException(exceptionMessage);
    }
}
