package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * struct annotation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/4 10:17
 */

@Target(TYPE)
@Retention(RUNTIME)
public @interface Struct {

    Map<Class<?>, Field[]> STRUCT_FIELD_CACHE = new ConcurrentHashMap<>(512);

}
