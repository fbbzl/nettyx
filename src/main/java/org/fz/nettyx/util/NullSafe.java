package org.fz.nettyx.util;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * All methods in this class will  suppress null pointer exception,
 * for further operations * This class cannot completely replace Optional {@link java.util.Optional} * However,
 * some scenarios can get better code readability and conciseness than Optional
 * <p>
 * Example: NullSafe.nullDefault(() -> user.getName().getLength(), "xx")
 * <p>
 * In this case, the user object is null, and the whole method will return the specified default value "xx"
 *
 * @author fengbinbin
 * @version 1.0
 * @apiNote All Lambdas in this class are forbidden to use method references instead!!!!!
 * @since 2024/5/30 11:04
 */
@UtilityClass
public class NullSafe {

    //***************************************       nullable supplier start      *************************************//

    public static <T> T nullable(Supplier<T> supplier) {
        return nullHandled(supplier, null);
    }

    public static <T> T nullHandled(Supplier<T> supplier, Consumer<NullPointerException> handleNull) {
        try {
            return supplier.get();
        } catch (NullPointerException nullPoint) {
            if (handleNull != null) {
                handleNull.accept(nullPoint);
            }
            return null;
        }
    }

    public static <T> T nullDefault(Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (NullPointerException nullPoint) {
            return defaultValue;
        }
    }

    public static <T> T nullDefault(Supplier<T> supplier, Supplier<T> defaultValue) {
        try {
            return supplier.get();
        } catch (NullPointerException nullPoint) {
            return defaultValue.get();
        }
    }

    public static <T> T nullThrow(Supplier<T> supplier, RuntimeException exception) {
        try {
            return supplier.get();
        } catch (NullPointerException e) {
            throw exception;
        }
    }

    public static <T> T nullThrow(Supplier<T> supplier, Supplier<RuntimeException> exception) {
        try {
            return supplier.get();
        } catch (NullPointerException e) {
            throw exception.get();
        }
    }

    //***************************************       nullable runnable start      *************************************//

    public static void nullable(Runnable runnable) {
        nullHandled(runnable, null);
    }

    public static void nullHandled(Runnable runnable, Consumer<NullPointerException> handleNull) {
        try {
            runnable.run();
        } catch (NullPointerException nullPoint) {
            if (handleNull != null) {
                handleNull.accept(nullPoint);
            }
        }
    }

    public static void nullThrow(Runnable runnable, RuntimeException exception) {
        try {
            runnable.run();
        } catch (NullPointerException nullPoint) {
            throw exception;
        }
    }

    public static void nullThrow(Runnable runnable, Supplier<RuntimeException> exception) {
        try {
            runnable.run();
        } catch (NullPointerException nullPoint) {
            throw exception.get();
        }
    }

    //***************************************       nullable consumer start      *************************************//

    public static <T> void nullable(T arg, Consumer<T> consumer) {
        nullHandled(arg, consumer, null);
    }

    public static <T> void nullHandled(T arg, Consumer<T> consumer,
                                       Consumer<NullPointerException> handleNull) {
        try {
            consumer.accept(arg);
        } catch (NullPointerException nullPoint) {
            if (handleNull != null) {
                handleNull.accept(nullPoint);
            }
        }
    }

    public static <T> void nullThrow(T arg, Consumer<T> consumer, RuntimeException exception) {
        try {
            consumer.accept(arg);
        } catch (NullPointerException nullPoint) {
            throw exception;
        }
    }

    public static <T> void nullThrow(T arg, Consumer<T> consumer, Supplier<RuntimeException> exception) {
        try {
            consumer.accept(arg);
        } catch (NullPointerException nullPoint) {
            throw exception.get();
        }
    }

    //***************************************       nullable function start      *************************************//

    public static <T, R> R nullable(T arg, Function<T, R> fn) {
        return nullHandled(arg, fn, null);
    }

    public static <T, R> R nullHandled(T arg, Function<T, R> fn,
                                       Consumer<NullPointerException> handleNull) {
        try {
            return fn.apply(arg);
        } catch (NullPointerException nullPoint) {
            if (handleNull != null) {
                handleNull.accept(nullPoint);
            }
            return null;
        }
    }

    public static <T, R> R nullDefault(T arg, Function<T, R> fn, R defaultValue) {
        try {
            return fn.apply(arg);
        } catch (NullPointerException nullPoint) {
            return defaultValue;
        }
    }

    public static <T, R> R nullDefault(T arg, Function<T, R> fn, Supplier<R> defaultValue) {
        try {
            return fn.apply(arg);
        } catch (NullPointerException nullPoint) {
            return defaultValue.get();
        }
    }

    public static <T, R> R nullThrow(T arg, Function<T, R> fn, RuntimeException exception) {
        try {
            return fn.apply(arg);
        } catch (NullPointerException nullPoint) {
            throw exception;
        }
    }

    public static <T, R> R nullThrow(T arg, Function<T, R> fn, Supplier<RuntimeException> exception) {
        try {
            return fn.apply(arg);
        } catch (NullPointerException nullPoint) {
            throw exception.get();
        }
    }


    /**
     * 包装lambda, 对空指针异常进行处理
     */
    public final static class NullSafeLambda {

        public static <T> Supplier<T> nullable(Supplier<T> supplier) {
            return nullHandled(supplier, null);
        }

        public static <T> Supplier<T> nullHandled(Supplier<T> supplier,
                                                  Consumer<NullPointerException> handleNull) {
            return () -> NullSafe.nullHandled(supplier, handleNull);
        }

        public static <T> Supplier<T> nullDefault(Supplier<T> supplier, T defaultValue) {
            return () -> NullSafe.nullDefault(supplier, defaultValue);
        }

        public static <T> Supplier<T> nullDefault(Supplier<T> supplier, Supplier<T> defaultValue) {
            return () -> NullSafe.nullDefault(supplier, defaultValue);
        }

        public static <T> Supplier<T> nullThrow(Supplier<T> supplier, Supplier<RuntimeException> exception) {
            return () -> NullSafe.nullThrow(supplier, exception);
        }

        public static <T> Supplier<T> nullThrow(Supplier<T> supplier, RuntimeException exception) {
            return () -> NullSafe.nullThrow(supplier, exception);
        }

        public static Runnable nullable(Runnable runnable) {
            return nullHandled(runnable, null);
        }

        public static Runnable nullHandled(Runnable runnable, Consumer<NullPointerException> handleNull) {
            return () -> NullSafe.nullHandled(runnable, handleNull);
        }

        public Runnable nullThrow(Runnable runnable, Supplier<RuntimeException> exception) {
            return () -> NullSafe.nullThrow(runnable, exception);
        }

        public Runnable nullThrow(Runnable runnable, RuntimeException exception) {
            return () -> NullSafe.nullThrow(runnable, exception);
        }

        public static <T> Consumer<T> nullable(Consumer<T> consumer) {
            return nullHandled(consumer, null);
        }

        public static <T> Consumer<T> nullHandled(Consumer<T> consumer,
                                                  Consumer<NullPointerException> handleNull) {
            return t -> NullSafe.nullHandled(t, consumer, handleNull);
        }

        public static <T> Consumer<T> nullThrow(Consumer<T> consumer, RuntimeException exception) {
            return t -> NullSafe.nullThrow(t, consumer, exception);
        }

        public static <T> Consumer<T> nullThrow(Consumer<T> consumer, Supplier<RuntimeException> exception) {
            return t -> NullSafe.nullThrow(t, consumer, exception);
        }

        public static <T, R> Function<T, R> nullable(Function<T, R> fn) {
            return nullHandled(fn, null);
        }

        public static <T, R> Function<T, R> nullHandled(Function<T, R> fn,
                                                        Consumer<NullPointerException> handleNull) {
            return t -> NullSafe.nullHandled(t, fn, handleNull);
        }

        public static <T, R> Function<T, R> nullDefault(Function<T, R> fn, R defaultValue) {
            return t -> NullSafe.nullDefault(t, fn, defaultValue);
        }

        public static <T, R> Function<T, R> nullDefault(Function<T, R> fn, Supplier<R> defaultValue) {
            return t -> NullSafe.nullDefault(t, fn, defaultValue);
        }

        public static <T, R> Function<T, R> nullThrow(Function<T, R> fn, RuntimeException exception) {
            return t -> NullSafe.nullThrow(t, fn, exception);
        }

        public static <T, R> Function<T, R> nullThrow(Function<T, R> fn, Supplier<RuntimeException> exception) {
            return t -> NullSafe.nullThrow(t, fn, exception);
        }
    }

}
