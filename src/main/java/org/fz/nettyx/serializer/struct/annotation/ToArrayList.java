package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.util.exception.Throws;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


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
     * list size
     *
     * @return the int
     */
    int size();

    /**
     * The type Array list handler.
     */
    class ToArrayListHandler implements StructFieldHandler.ReadWriteHandler<ToArrayList> {
        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(StructSerializer serializer, Type fieldType, Field field, ToArrayList toArrayList) {
            Type elementType = serializer.getElementType(fieldType);

            Throws.ifTrue(elementType == Object.class, () -> new ParameterizedTypeException(field));

            return serializer.readList(elementType, toArrayList.size(), new ArrayList<>(10));
        }

        @Override
        public void doWrite(StructSerializer serializer, Type fieldType, Field field, Object value, ToArrayList toArrayList, ByteBuf writing) {
            Type elementType = serializer.getElementType(fieldType);

            Throws.ifTrue(elementType == Object.class, () -> new ParameterizedTypeException(field));

            serializer.writeList(defaultIfNull((List<?>) value, Collections::emptyList), elementType, toArrayList.size(), writing);
        }
    }
}
