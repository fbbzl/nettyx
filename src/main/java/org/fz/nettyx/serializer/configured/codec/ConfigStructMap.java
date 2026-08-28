package org.fz.nettyx.serializer.configured.codec;

import org.fz.nettyx.serializer.configured.ConfigStruct;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map result for a configured struct. Fixed XML fields are written by index so deserialization
 * does not allocate a hash-table entry for every field. The map is materialized only when a
 * caller needs entry iteration or mutation.
 */
final class ConfigStructMap extends AbstractMap<String, Object> {

    private final ConfigStruct struct;
    private final String[] fieldNames;
    private final Object[] values;
    private final byte[][] charBuffers;
    private Map<String, Object> materialized;

    ConfigStructMap(ConfigStruct struct)
    {
        this.struct = struct;
        fieldNames = struct.fieldNames();
        values = new Object[fieldNames.length];
        charBuffers = new byte[fieldNames.length][];
    }

    void put(int index, Object value)
    {
        if (materialized == null) values[index] = value;
        else                      materialized.put(fieldNames[index], value);
    }

    Object valueAt(int index)
    {
        return materialized == null ? values[index] : materialized.get(fieldNames[index]);
    }

    boolean belongsTo(ConfigStruct struct)
    {
        return this.struct == struct;
    }

    byte[] charBuffer(int index, int length)
    {
        byte[] buffer = charBuffers[index];
        if (buffer == null || buffer.length != length) {
            buffer = new byte[length];
            charBuffers[index] = buffer;
        }
        return buffer;
    }

    @Override
    public Object get(Object key)
    {
        if (materialized != null) return materialized.get(key);

        int index = indexOf(key);
        return index < 0 ? null : values[index];
    }

    @Override
    public boolean containsKey(Object key)
    {
        return materialized != null ? materialized.containsKey(key) : indexOf(key) >= 0;
    }

    @Override
    public int size()
    {
        return materialized == null ? fieldNames.length : materialized.size();
    }

    @Override
    public Object put(String key, Object value)
    {
        return materialize().put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return materialize().remove(key);
    }

    @Override
    public void clear()
    {
        materialize().clear();
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return materialize().entrySet();
    }

    private int indexOf(Object key)
    {
        if (!(key instanceof String)) return -1;

        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i].equals(key)) return i;
        }
        return -1;
    }

    private Map<String, Object> materialize()
    {
        if (materialized != null) return materialized;

        Map<String, Object> map = new LinkedHashMap<>(fieldNames.length * 2);
        for (int i = 0; i < fieldNames.length; i++) map.put(fieldNames[i], values[i]);
        materialized = map;
        return map;
    }
}
