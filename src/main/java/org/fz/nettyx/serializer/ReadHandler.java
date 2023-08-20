package org.fz.nettyx.serializer;

import org.fz.nettyx.handler.ByteBufHandler;

import java.lang.reflect.Field;

/**
 * @author fengbinbin
 * @since 2022-01-16 13:37
 **/
public interface ReadHandler<S extends Serializer> extends ByteBufHandler {

    /**
     * Do read object. if not override, this method will return null
     *
     * @param serializer the serializer
     * @param field      the field
     * @return the final returned field value
     */
    Object doRead(S serializer, Field field);

}
