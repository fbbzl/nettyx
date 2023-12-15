package org.fz.nettyx.serializer.typed.annotation.collection;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.typed.ByteBufHandler;
import org.fz.nettyx.serializer.typed.TypedSerializer;
import org.fz.nettyx.serializer.typed.annotation.FieldHandler;
import org.fz.nettyx.util.StructUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The interface List.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:26
 */
@Target(FIELD)
@Retention(RUNTIME)
@FieldHandler(ToArrayList.ArrayListHandler.class)
public @interface ToArrayList {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType();

    /**
     * Size int.
     *
     * @return the int
     */
    int size() default 0;

    /**
     * Buffer length int.
     *
     * @return the int
     */
    int bufferLength() default 0;

    /**
     * The type Array list handler.
     */
    class ArrayListHandler implements ByteBufHandler.ReadWriteHandler<TypedSerializer> {

        @Override
        public Object doRead(TypedSerializer serializer, Field field) {
            StructUtils.checkAssignable(field, List.class);

            ToArrayList toArrayList = field.getAnnotation(ToArrayList.class);
            Class<?> elementType = toArrayList.elementType();
            int bufferLength = toArrayList.bufferLength();

            return new ArrayList<>(Arrays.asList(TypedSerializer.readArray(elementType, toArrayList.size(), serializer.readBytes(bufferLength))));
        }

        @Override
        public void doWrite(TypedSerializer serializer, Field field, Object value, ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, List.class);

            ToArrayList toArrayList = field.getAnnotation(ToArrayList.class);
            Class<?> elementType = toArrayList.elementType();
            int size = toArrayList.size();

            TypedSerializer.writeArray(((ArrayList<?>) value).toArray(), elementType, size, writingBuffer);
        }
    }
}
