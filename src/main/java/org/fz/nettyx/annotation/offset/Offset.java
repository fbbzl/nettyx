package org.fz.nettyx.annotation.offset;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fengbinbin
 * @since 2022-01-02 10:45
 **/

@Target({FIELD, TYPE})
@Retention(RUNTIME)
public @interface Offset {

    

}
