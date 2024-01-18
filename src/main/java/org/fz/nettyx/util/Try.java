package org.fz.nettyx.util;


import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Use Lambda to handle exceptions gracefully, temporarily provide two implementations
 *
 * @author fengbinbin
 * @since 2020-02-13 14:23
 */
@Slf4j
@UtilityClass
@SuppressWarnings("all")
public class Try {

    public Runnable run(UncheckedRunnable runnable) {
        Objects.requireNonNull(runnable);
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    public <T, R> Function<T, R> apply(UncheckedFunction<T, R> function) {
        Objects.requireNonNull(function);
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    public <T> Consumer<T> accept(UncheckedConsumer<T> consumer) {
        Objects.requireNonNull(consumer);
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    public <T, U> BiConsumer<T, U> accept(UncheckedBiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer);
        return (k, v) -> {
            try {
                consumer.accept(k, v);
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    public <T> Supplier<T> get(UncheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    public <T> Predicate<T> test(UncheckedPredicate<T> predicate) {
        Objects.requireNonNull(predicate);
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                log.debug(e.getMessage());
                throw new LambdasException(e);
            }
        };
    }

    /**
     * Use this method with caution!! Capture, but it can be used in scenarios where only information is printed, and it
     * is not recommended for other scenarios
     */
    public Runnable suppress(UncheckedRunnable run) {
        return () -> {
            try {
                run.run();
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
        };
    }

    public <T> Consumer<T> suppress(UncheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
        };
    }


    @FunctionalInterface
    public interface UncheckedConsumer<T> {

        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedBiConsumer<T, U> {

        void accept(T t, U u) throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedFunction<T, R> {

        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedRunnable {

        void run() throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedSupplier<T> {

        T get() throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedPredicate<T> {

        boolean test(T t) throws Exception;
    }

    public static class LambdasException extends RuntimeException {

        public LambdasException(String message) {
            super(message);
        }

        public LambdasException(String message, Throwable cause) {
            super(message, cause);
        }

        public LambdasException(Throwable cause) {
            super(cause);
        }

        public LambdasException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
