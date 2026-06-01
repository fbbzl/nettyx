package org.fz.nettyx.serializer.struct.basic;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PROTECTED;

/**
 * The type Basic. The specific implementation can be enhanced
 *
 * @param <V> the type parameter, may be the java type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:26
 */
@Getter
@FieldDefaults(level = PROTECTED, makeFinal = true)
public abstract class Basic<V extends Comparable<V>> implements Comparable<Basic<V>> {

    @NonFinal
    @Setter
    ByteOrder byteOrder;
    int       size;
    @Getter(PROTECTED)
    @NonFinal
    ByteBuf bytesBuf;
    @Getter(NONE)
    @NonFinal
    V      value;

    protected Basic(ByteBuf byteBuf, int size) {
        this.size = size;
        if (byteBuf.readableBytes() < size) throw new TooLessBytesException(size, byteBuf.readableBytes());

        this.bytesBuf = byteBuf.readRetainedSlice(size);
    }

    protected Basic(V value, int size) {
        this.size  = size;
        this.value = value;
    }

    /**
     * Has singed boolean.
     *
     * @return the boolean
     */
    public abstract boolean hasSigned();

    /**
     * To byte buf.
     *
     * @param value     the value
     * @param byteOrder the byte order
     * @return the byte buf
     */
    protected abstract ByteBuf toByteBuf(V value, ByteOrder byteOrder);

    /**
     * To value v.
     *
     * @param byteBuf   the byte buf
     * @param byteOrder the byte order
     * @return the v
     */
    protected abstract V toValue(ByteBuf byteBuf, ByteOrder byteOrder);

    /**
     * Read the Java value from internal ByteBuf, auto-release bytesBuf after reading.
     *
     * @return the java value
     */
    public V read() {
        if (this.bytesBuf != null && this.value == null) {
            this.value = this.toValue(this.bytesBuf, byteOrder);
            ReferenceCountUtil.release(this.bytesBuf);
            this.bytesBuf = null;
        }
        return value;
    }

    /**
     * Write the bytes to the target ByteBuf, auto-release bytesBuf after writing.
     *
     * @param writingBuf the target ByteBuf to write into
     */
    public void write(ByteBuf writingBuf) {
        if (this.value != null && this.bytesBuf == null) {
            this.bytesBuf = this.toByteBuf(this.value, byteOrder);
            this.fill(this.bytesBuf, this.size);
        }
        if (this.bytesBuf == null) { writingBuf.writeZero(size); return; }
        writingBuf.writeBytes(this.bytesBuf);
        ReferenceCountUtil.release(this.bytesBuf);
        this.bytesBuf = null;
    }


    private void fill(ByteBuf buf, int requiredSize) {
        int fillLength = requiredSize - buf.readableBytes();
        if (fillLength > 0) {
            buf.writeZero(fillLength);
        }
    }

    /**
     * Release the internal ByteBuf if it is retained. Idempotent.
     */
    public void release() {
        ReferenceCountUtil.release(this.bytesBuf);
        this.bytesBuf = null;
    }

    @Override
    public int hashCode() {
        V v = value != null ? value : read();
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

            V thisVal = value != null ? value : read();
            Object thatVal = cBasic.value != null ? cBasic.value : cBasic.read();
            return thisVal.equals(thatVal);
        }
        return false;
    }

    @Override
    public int compareTo(Basic<V> anotherCBasic) {
        V thisVal = value != null ? value : read();
        V thatVal = anotherCBasic.value != null ? anotherCBasic.value : anotherCBasic.read();
        return thisVal.compareTo(thatVal);
    }
}
