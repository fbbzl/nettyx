package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.StructPropHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.util.Throws;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import static cn.hutool.core.collection.CollUtil.newLinkedList;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * The interface To linked list.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/8 13:15
 */

@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToLinkedList {

    /**
     * list size int.
     *
     * @return the int
     */
    int size();

    /**
     * The type To linked list handler.
     */
    class ToLinkedListHandler implements StructPropHandler.ReadWriteHandler<ToLinkedList> {
        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(StructSerializer serializer, Type fieldType, Field field, ToLinkedList toLinkedList) {
            Type elementType = serializer.getElementType(fieldType);

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            return serializer.readList(elementType, toLinkedList.size(), new LinkedList<>());
        }

        @Override
        public void doWrite(StructSerializer serializer, Type fieldType, Field field, Object value, ToLinkedList toLinkedList, ByteBuf writing) {
            Type elementType = serializer.getElementType(fieldType);

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            serializer.writeList((List<?>) defaultIfNull(value, () -> newLinkedList()), elementType, toLinkedList.size(), writing);
        }
    }

}
