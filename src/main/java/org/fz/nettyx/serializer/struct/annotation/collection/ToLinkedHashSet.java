package org.fz.nettyx.serializer.struct.annotation.collection;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.collection.CollUtil.newLinkedHashSet;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
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
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.util.Throws;

/**
 * The interface To linked hash set.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/8 13:15
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
    Class<?> elementType() default Object.class;

    /**
     * set size int.
     *
     * @return the int
     */
    int size();

    /**
     * The type To linked hash set handler.
     */
    class ToLinkedHashSetHandler implements PropertyHandler.ReadWriteHandler<ToLinkedHashSet> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToLinkedHashSet toLinkedHashSet) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = (elementType = StructUtils.getFieldParameterizedType(field)) == Object.class
                ? toLinkedHashSet.elementType() : elementType;

            Throws.ifTrue(elementType == Object.class,
                new TypeJudgmentException("can not determine field [" + field + "] parameterized type"));

            return newHashSet(Arrays.asList(readArray(elementType, toLinkedHashSet.size(), serializer.getByteBuf())));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToLinkedHashSet toLinkedHashSet,
            ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = (elementType = StructUtils.getFieldParameterizedType(field)) == Object.class
                ? toLinkedHashSet.elementType() : elementType;

            Throws.ifTrue(elementType == Object.class,
                new TypeJudgmentException("can not determine field [" + field + "] parameterized type"));

            Set<?> set = (HashSet<?>) defaultIfNull(value, () -> newLinkedHashSet());
            writeArray(set.toArray(), elementType, toLinkedHashSet.size(), writingBuffer);
        }

    }

}
