package org.fz.nettyx.serializer.struct.annotation.collection;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructUtils.nullDefault;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.readArray;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.writeArray;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/8 13:15
 */

@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface ToLinkedHashSet {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType();

    /**
     * set size int.
     *
     * @return the int
     */
    int size() default 0;

    class ToLinkedHashSetHandler implements PropertyHandler.ReadWriteHandler<ToLinkedHashSet> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToLinkedHashSet toLinkedHashSet) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = toLinkedHashSet.elementType();
            int size = toLinkedHashSet.size();

            return newHashSet(Arrays.asList(readArray(elementType, size, serializer.getByteBuf())));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToLinkedHashSet toLinkedHashSet,
            ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = toLinkedHashSet.elementType();
            int size = toLinkedHashSet.size();

            writeArray(((HashSet<?>) nullDefault(value, HashSet::new)).toArray(), elementType, size, writingBuffer);
        }

    }

}
