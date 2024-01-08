package org.fz.nettyx.serializer.struct.annotation.collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructUtils.nullDefault;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.readArray;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.writeArray;

import cn.hutool.core.collection.ListUtil;
import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
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
public @interface ToLinkedList {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType();

    /**
     * list size int.
     *
     * @return the int
     */
    int size() default 0;

    class ToLinkedListHandler implements PropertyHandler.ReadWriteHandler<ToLinkedList> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToLinkedList toLinkedList) {
            StructUtils.checkAssignable(field, List.class);
            Class<?> elementType = toLinkedList.elementType();

            return ListUtil.toLinkedList(readArray(elementType, toLinkedList.size(), serializer.getByteBuf()));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToLinkedList toLinkedList,
            ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, List.class);

            Class<?> elementType = toLinkedList.elementType();
            int size = toLinkedList.size();

            writeArray(((LinkedList<?>) nullDefault(value, LinkedList::new)).toArray(), elementType, size, writingBuffer);
        }
    }

}
