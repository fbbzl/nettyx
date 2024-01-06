package org.fz.nettyx.serializer.struct.annotation.collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fz.nettyx.serializer.struct.handler.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.serializer.struct.annotation.SerializerHandler;

/**
 * The interface List.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:26
 */
@Target(FIELD)
@Retention(RUNTIME)
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
    class ArrayListHandler implements PropertyHandler.ReadWriteHandler<ToArrayList> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToArrayList toArrayList) {
            StructUtils.checkAssignable(field, List.class);
            Class<?> elementType = toArrayList.elementType();
            int bufferLength = toArrayList.bufferLength();

            return new ArrayList<>(Arrays.asList(
                StructSerializer.readArray(elementType, toArrayList.size(), serializer.readBytes(bufferLength))));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToArrayList toArrayList,
            ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, List.class);

            Class<?> elementType = toArrayList.elementType();
            int size = toArrayList.size();

            StructSerializer.writeArray(((ArrayList<?>) value).toArray(), elementType, size, writingBuffer);
        }
    }
}
