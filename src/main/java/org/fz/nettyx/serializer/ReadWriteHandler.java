package org.fz.nettyx.serializer;

/**
 * @author fengbinbin
 * @since 2022-01-20 19:46
 **/
public interface ReadWriteHandler<S extends Serializer> extends ReadHandler<S>, WriteHandler<S> {

}
