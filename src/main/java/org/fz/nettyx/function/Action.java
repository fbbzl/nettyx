package org.fz.nettyx.function;

/**
 * The interface Action.
 *
 * @param <T> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/11 10:23
 */
@FunctionalInterface
interface Action<T> {

    /**
     * Act.
     *
     * @param t the t
     */
    void act(T t);

}
