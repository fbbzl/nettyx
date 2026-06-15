package org.fz.nettyx.serializer.struct.basic;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

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

@FieldDefaults(level = PROTECTED, makeFinal = true)
public abstract class Basic<V extends Comparable<V>> implements Comparable<Basic<V>> {
    @NonFinal ByteOrder byteOrder;
    @NonFinal V         value;
    @Getter   int       size;

    protected Basic(ByteOrder byteOrder, ByteBuf byteBuf, int size) {
        this.byteOrder = Objects.requireNonNull(byteOrder, "byteOrder");
        this.size = size;
        if (byteBuf.readableBytes() < size) throw new TooLessBytesException(size, byteBuf.readableBytes());
        this.value = this.read(byteBuf);
    }

    protected Basic(ByteOrder byteOrder, V value, int size) {
        this.byteOrder = Objects.requireNonNull(byteOrder, "byteOrder");
        this.size      = size;
        this.value     = value;
    }

    /**
     * Has singed boolean.
     *
     * @return the boolean
     */
    public abstract boolean hasSigned();

    /**
     * Write the Java value to target byte buf.
     *
     * @param writingBuf the target byte buf
     */
    public abstract void write(ByteBuf writingBuf);

    /**
     * Read the Java value from source byte buf.
     *
     * @param byteBuf the byte buf
     * @return the v
     */
    protected abstract V read(ByteBuf byteBuf);

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

        if (anotherObj instanceof Cbasic<?> cBasic) {
            if (this.getSize()   != cBasic.getSize()
                ||
                this.hasSigned() != cBasic.hasSigned()) {
                return false;
            }

            V thisVal = value != null ? value : null;
            if (thisVal == null) return false;

            Object thatVal = cBasic.value != null ? cBasic.value : null;
            return thisVal.equals(thatVal);
        }
        return false;
    }

    @Override
    public int compareTo(Basic<V> anotherCBasic) {
        V thisVal = value != null ? value : null;
        V thatVal = anotherCBasic.value != null ? anotherCBasic.value : null;
        if (thisVal == null) throw new IllegalArgumentException("this value is null");
        return thisVal.compareTo(thatVal);
    }
}
