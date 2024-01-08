package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/8 15:03
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface ToEnum {




    enum MatchType {
        BY_INDEX,
        BY_NAME,
        BY_NAME_IGNORE_CASE,
        BY_NAME_IGNORE_CASE_AND_IGNORE_UNDERLINE,;

    }

}
