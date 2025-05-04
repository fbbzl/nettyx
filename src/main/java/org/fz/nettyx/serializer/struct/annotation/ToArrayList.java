package org.fz.nettyx.serializer.struct.annotation;

import cn.hutool.core.collection.CollUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructHelper;
import org.fz.util.exception.Throws;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
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
    class ToArrayListHandler implements StructFieldHandler<ToArrayList> {
        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(
                Type        root,
                Object      earlyObject,
                StructField field,
                ByteBuf     reading,
                ToArrayList toArrayList)
        {
            Type elementType = StructHelper.getElementType(root, field.type(root));

            Throws.ifTrue(elementType == Object.class, () -> new ParameterizedTypeException(field));

            return readList(root, elementType, reading, toArrayList.size());
        }

        @Override
        public void doWrite(
                Type        root,
                Object      struct,
                StructField field,
                Object      fieldVal,
                ByteBuf     writing,
                ToArrayList toArrayList)
        {
            Type elementType = StructHelper.getElementType(root, field.type(root));

            Throws.ifTrue(elementType == Object.class, () -> new ParameterizedTypeException(field));

            writeList(root, defaultIfNull((List<?>) fieldVal, Collections::emptyList), elementType,
                      toArrayList.size(), writing);
        }

        <T> List<T> readList(
                Type    root,
                Type    elementType,
                ByteBuf byteBuf,
                int     length)
        {
            return CollUtil.newArrayList(readArray(root, elementType, byteBuf, length));
        }

        void writeList(
                Type    root,
                List<?> list,
                Type    elementType,
                int     length,
                ByteBuf writing)
        {
            // TOdo array
            writeArray(root, list, elementType, length, writing);
        }
    }
}
