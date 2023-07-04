package org.fz.nettyx.serializer;

import org.fz.nettyx.serializer.serializer.type.Basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengbinbin
 * @since 2022-01-01 09:07
 **/
class BasicTypeFeature {

    static final Map<Class<? extends Basic<?>>, BasicTypeFeature> BASIC_FEATURE_CACHE = new ConcurrentHashMap<>(8);
    static final Map<Class<?>, BasicTypeFeature> STRUCT_FEATURE_CACHE = new ConcurrentHashMap<>(32);

    /**
     * byte size
     */
    private int size;

    public BasicTypeFeature(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
