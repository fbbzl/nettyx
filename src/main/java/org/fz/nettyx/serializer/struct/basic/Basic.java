package org.fz.nettyx.serializer.struct.basic;

import io.netty.buffer.ByteBuf;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.TooLessBytesException;

import java.nio.ByteOrder;
import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

/**
 * The type Basic. The specific implementation can be enhanced
 *
 * @param <V> the type parameter, may be the java type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:26
 */

@FieldDefaults(level = PROTECTED)
public abstract class Basic<V extends Comparable<V>> implements Comparable<Basic<V>> {

    V value;

    protected Basic(ByteBuf byteBuf, ByteOrder byteOrder) {
        if (byteBuf.readableBytes() < size()) throw new TooLessBytesException(size(), byteBuf.readableBytes());
        this.value = this.read(byteBuf, byteOrder);
    }

    protected Basic(V value) {
        this.value = value;
    }

    public abstract int size();

    public abstract boolean hasSigned();

    public abstract void write(ByteBuf writingBuf, ByteOrder byteOrder);

    protected abstract V read(ByteBuf readingBuf, ByteOrder byteOrder);

    public final V value() {
        return value;
    }

    @Override
    public int hashCode() {
        V v = value != null ? value : null;
        return v != null ? v.hashCode() : 0;
    }

    @Override
    public boolean equals(Object anotherObj) {
        if (anotherObj == null) return false;

        if (anotherObj instanceof Basic<?> anotherBasic) {
            if (this.size() != anotherBasic.size()
                ||
                this.hasSigned() != anotherBasic.hasSigned()) {
                return false;
            }

            V thisVal = value != null ? value : null;
            Object thatVal = anotherBasic.value != null ? anotherBasic.value : null;
            return Objects.equals(thisVal, thatVal);
        }
        return false;
    }

    @Override
    public int compareTo(Basic<V> anotherBasic) {
        V thisVal = value != null ? value : null;
        V thatVal = anotherBasic.value != null ? anotherBasic.value : null;
        if (thisVal == null) throw new IllegalArgumentException("this value is null");
        if (thatVal == null) throw new IllegalArgumentException("another value is null");
        return thisVal.compareTo(thatVal);
    }
}
