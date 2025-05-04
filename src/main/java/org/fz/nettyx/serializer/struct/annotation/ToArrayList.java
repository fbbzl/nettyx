package org.fz.nettyx.serializer.struct.annotation;

import cn.hutool.core.collection.CollUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructHelper;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.util.exception.Throws;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructHelper.findBasicSize;
import static org.fz.nettyx.serializer.struct.StructHelper.newStruct;


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
            if (isBasic(root, elementType))  return (List<T>) readBasicList(root, (Class<? extends Basic<?>>) elementType, byteBuf, length);
            if (isStruct(root, elementType)) return readStructList(root, elementType, byteBuf, length);
            else                             throw new TypeJudgmentException();
        }

        <B extends Basic<?>> List<B> readBasicList(
                Type     root,
                Class<?> elementType,
                ByteBuf  byteBuf,
                int      length)
        {
            return CollUtil.newArrayList(readBasicArray(elementType, byteBuf, length));
        }

        <T> List<T> readStructList(
                Type    root,
                Type    elementType,
                ByteBuf byteBuf,
                int     length)
        {
            return CollUtil.newArrayList(readStructArray(root, elementType, byteBuf, length));
        }

        void writeList(
                Type    root,
                List<?> list,
                Type    elementType,
                int     length,
                ByteBuf writing)
        {
            if (isBasic(root, elementType))  writeBasicList(list, findBasicSize(elementType), length, writing);
            else
            if (isStruct(root, elementType)) writeStructList(list, elementType, length, writing);
            else
                throw new TypeJudgmentException();
        }

        void writeBasicList(
                List<?> list,
                int     elementBytesSize,
                int     length,
                ByteBuf writing)
        {
            Iterator<?> iterator = list.iterator();
            for (int i = 0; i < length; i++) {
                if (iterator.hasNext()) {
                    Basic<?> basic = (Basic<?>) iterator.next();
                    if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                    else writing.writeBytes(basic.getBytes());
                }
                else writing.writeBytes(new byte[elementBytesSize]);
            }
        }

        void writeStructList(
                List<?> list,
                Type    elementType,
                int     length,
                ByteBuf writing)
        {
            Iterator<?> iterator = list.iterator();
            for (int i = 0; i < length; i++) {
                if (iterator.hasNext())
                    writing.writeBytes(StructSerializer.toByteBuf(elementType, structNullDefault(iterator.next(), elementType)));
                else
                    writing.writeBytes(StructSerializer.toByteBuf(elementType, newStruct(elementType)));
            }
        }
    }
}
