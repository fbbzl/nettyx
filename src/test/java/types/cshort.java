package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.serializer.type.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 18:00
 **/
public class cshort extends Basic<Short> {

    public static cshort of(int value) {
        return of((short) value);
    }

    public static cshort of(short value) {
        return new cshort().setValue(value);
    }

    @Override
    public boolean hasSinged() {
        return true;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size()).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }

    @Override
    public int size() {
        return 2;
    }


}
