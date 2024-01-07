package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * The top-level parent class of all custom serialization processors
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
@SuppressWarnings("unchecked")
public interface PropertyHandler<A extends Annotation> {

    default <T extends Annotation> boolean isTargetAnnotation(Class<T> otherAnnotationType) {
        Class<Annotation> targetAnnotationType = getTargetAnnotationType(this.getClass());
        return targetAnnotationType == otherAnnotationType;
    }

    static <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz) {
        if (clazz.isEnum() || clazz.isInterface()) {
            return null;
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) genericInterface;
                Type[] actualTypeArguments = type.getActualTypeArguments();
                if (type.getOwnerType() == PropertyHandler.class && actualTypeArguments.length > 0) {
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
    static <S extends PropertyHandler<?>> boolean isReadHandler(Class<S> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is write handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isWriteHandler(Class<S> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is read handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isReadHandler(S handler) {
        return handler instanceof ReadHandler;
    }

    /**
     * Is write handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isWriteHandler(S handler) {
        return handler instanceof WriteHandler;
    }

    /**
     * The interface Read handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface ReadHandler<A extends Annotation> extends PropertyHandler<A> {

        /**
         * Do read object. if not override, this method will return null
         *
         * @param serializer the serializer
         * @param field the field
         * @return the final returned field length
         */
        Object doRead(StructSerializer serializer, Field field, A annotation);

    }

    /**
     * The interface Write handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface WriteHandler<A extends Annotation> extends PropertyHandler<A> {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param field the field
         * @param value the length
         * @param writingBuffer the writingBuffer
         */
        void doWrite(StructSerializer serializer, Field field, Object value, A annotation, ByteBuf writingBuffer);
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
