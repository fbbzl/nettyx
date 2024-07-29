package org.fz.nettyx.serializer.struct;

import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.hutool.core.util.TypeUtil.getActualType;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

/**
 * The top-level parent class of all custom serialization processors default is not singleton
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
@SuppressWarnings("all")
public interface StructPropHandler<A extends Annotation> {

    /**
     * cache annotation and handler class
     */
    Map<Class<? extends Annotation>, Class<? extends StructPropHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING = new SafeConcurrentHashMap<>(16);

    /**
     * config the handler instance if is singleton
     *
     * @return if is singleton-handler
     */
    default boolean isSingleton() {
        return false;
    }

    static <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz) {
        if (!ClassUtil.isNormalClass(clazz)) {
            return null;
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType type                = (ParameterizedType) genericInterface;
                Type[]            actualTypeArguments = type.getActualTypeArguments();
                if (type.getOwnerType() == StructPropHandler.class && actualTypeArguments.length > 0) {
                    return (Class<A>) actualTypeArguments[0];
                }
            }
        }
        return null;
    }

    /**
     * Is read handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends StructPropHandler<?>> boolean isReadHandler(Class<S> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is write handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends StructPropHandler<?>> boolean isWriteHandler(Class<S> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is read handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends StructPropHandler<?>> boolean isReadHandler(S handler) {
        return handler instanceof ReadHandler;
    }

    /**
     * Is write handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends StructPropHandler<?>> boolean isWriteHandler(S handler) {
        return handler instanceof ReadHandler.WriteHandler;
    }

    /**
     * The interface Read handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface ReadHandler<A extends Annotation> extends StructPropHandler<A> {

        /**
         * Do read object. if not override, this method will return null
         *
         * @param serializer the serializer
         * @param fieldType  field type
         * @param field      the field
         * @return the final returned field length
         */
        default Object doRead(StructSerializer serializer, Type fieldType, Field field, A annotation) {
            if (serializer.isBasic(fieldType))  return serializer.readBasic(fieldType);
            if (serializer.isStruct(fieldType)) return serializer.readStruct(fieldType);

            throw new TypeJudgmentException(field);
        }

        default void preReadHandle(StructSerializer serializer, Field field, A annotation) {
            // default is no nothing
        }

        default void postReadHandle(StructSerializer serializer, Field field, A annotation) {
            // default is no nothing
        }

        default void afterReadThrow(StructSerializer serializer, Field field, A annotation, Throwable throwable) {
            // default is no nothing
        }
    }

    /**
     * The interface Write handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface WriteHandler<A extends Annotation> extends StructPropHandler<A> {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param fieldType
         * @param field      the field
         * @param value      the length
         * @param writing
         */
        default void doWrite(StructSerializer serializer, Type fieldType, Field field, Object value, A annotation, ByteBuf writing) {
            Type rootType = serializer.getRootType();

            if (serializer.isBasic(field)) serializer.writeBasic((Basic<?>) defaultIfNull(value, () -> newBasic(getActualType(rootType, field), buffer())), writing);
            if (serializer.isStruct(field)) serializer.writeStruct(rootType, defaultIfNull(value, () -> newStruct(getActualType(rootType, field))), writing);

            throw new TypeJudgmentException(field);
        }

        default void preWriteHandle(StructSerializer serializer, Field field, Object value, A annotation, ByteBuf writing) {
            // default is no nothing
        }

        default void postWriteHandle(StructSerializer serializer, Field field, Object value, A annotation, ByteBuf writing) {
            // default is no nothing
        }

        default void afterWriteThrow(StructSerializer serializer, Field field, Object value, A annotation, ByteBuf writing, Throwable throwable) {
            // default is no nothing
        }

    }

    /**
     * The interface Read write handler. support read and write
     *
     * @author fengbinbin
     * @since 2022 -01-20 19:46
     */
    interface ReadWriteHandler<A extends Annotation> extends ReadHandler<A>, WriteHandler<A> {

    }

}
