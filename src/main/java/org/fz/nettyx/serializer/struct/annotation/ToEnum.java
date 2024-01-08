package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.annotation.ToEnum.MatchType.BY_INDEX;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/8 15:03
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface ToEnum {

    MatchType match() default BY_INDEX;

    enum MatchType {
        BY_INDEX,
        BY_NAME,
        BY_NAME_IGNORE_CASE,
        BY_NAME_IGNORE_CASE_AND_IGNORE_UNDERLINE,;
    }

    class ToEnumHandler implements PropertyHandler.ReadWriteHandler<ToEnum> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToEnum annotation) {
            return null;
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToEnum annotation,
            ByteBuf writingBuffer) {

        }
    }


}
