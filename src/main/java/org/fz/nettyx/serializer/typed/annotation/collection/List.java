package org.fz.nettyx.serializer.typed.annotation.collection;

/**
 * The interface List.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:26
 */
public @interface List {

    /**
     * Type class.
     *
     * @return the class
     */
    Class<? extends java.util.List> type() default java.util.ArrayList.class;

    /**
     * Size int.
     *
     * @return the int
     */
    int size() default 0;

    /**
     * Buffer size int.
     *
     * @return the buffer occupied by this char list
     */
    int bufferSize() default 0;

}
