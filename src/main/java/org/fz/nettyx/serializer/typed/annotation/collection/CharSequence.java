package org.fz.nettyx.serializer.typed.annotation.collection;

import java.nio.charset.StandardCharsets;

/**
 * The interface Char sequence.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:40
 */
public @interface CharSequence {

    /**
     * Charset string.
     *
     * @return the legal charset
     * @see StandardCharsets
     */
    String charset() default "UTF-8";

    /**
     * Buffer size int.
     *
     * @return the buffer occupied by this char sequence
     */
    int bufferSize() default 0;

}
