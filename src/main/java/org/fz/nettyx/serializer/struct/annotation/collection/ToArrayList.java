package org.fz.nettyx.serializer.struct.annotation.collection;

import static cn.hutool.core.collection.CollUtil.newArrayList;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.annotation.ToArray.ToArrayHandler.readArray;
import static org.fz.nettyx.serializer.struct.annotation.ToArray.ToArrayHandler.writeArray;

import cn.hutool.core.collection.ListUtil;
import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.util.Throws;

/**
 * The interface List.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:26
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToArrayList {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType() default Object.class;

    /**
     * list size
     *
     * @return the int
     */
    int size();

    /**
     * The type Array list handler.
     */
    class ToArrayListHandler implements PropertyHandler.ReadWriteHandler<ToArrayList> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToArrayList toArrayList) {
            StructUtils.checkAssignable(field, List.class);

            ParameterizedType structParameterizedType = serializer.getStructParameterizedType();
            System.err.println(structParameterizedType);
            Class<?> elementType =
                (elementType = StructUtils.getFieldParameterizedType(field)) == Object.class ? toArrayList.elementType()
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            return ListUtil.toList(readArray(elementType, toArrayList.size(), serializer.getByteBuf()));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToArrayList toArrayList,
            ByteBuf writing) {
            StructUtils.checkAssignable(field, List.class);

            Class<?> elementType =
                (elementType = StructUtils.getFieldParameterizedType(field)) == Object.class ? toArrayList.elementType()
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            List<?> list = (List<?>) defaultIfNull(value, () -> newArrayList());
            writeArray(list.toArray(), elementType, toArrayList.size(), writing);
        }
    }
}
