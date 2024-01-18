package org.fz.nettyx.serializer.struct.annotation;

import static cn.hutool.core.collection.CollUtil.newLinkedList;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.annotation.ToStructArray.ToStructArrayHandler.readStructArray;
import static org.fz.nettyx.serializer.struct.annotation.ToStructArray.ToStructArrayHandler.writeStructCollection;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.util.Throws;

/**
 * The interface To linked list.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/8 13:15
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface ToLinkedList {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType() default Object.class;

    /**
     * list size int.
     *
     * @return the int
     */
    int size();

    /**
     * The type To linked list handler.
     */
    class ToLinkedListHandler implements PropertyHandler.ReadWriteHandler<ToLinkedList> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToLinkedList toLinkedList) {
            Class<?> elementType =
                (elementType = toLinkedList.elementType()) == Object.class ? serializer.getFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            return newLinkedList(readStructArray(elementType, toLinkedList.size(), serializer.getByteBuf()));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToLinkedList toLinkedList,
            ByteBuf writing) {
            Class<?> elementType =
                (elementType = toLinkedList.elementType()) == Object.class ? serializer.getFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            List<?> list = (List<?>) defaultIfNull(value, () -> newLinkedList());

            writeStructCollection(list, elementType, toLinkedList.size(), writing);
        }
    }

}