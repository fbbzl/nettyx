package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.serializer.type.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 20:00
 **/
public class cbyte extends Basic<Short> {

    public cbyte(Short value) {
        super(value);
    }

    public cbyte(int value) {
        super((short) value);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size()).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedByte();
    }

    @Override
    public int size() {
        return 1;
    }

}
