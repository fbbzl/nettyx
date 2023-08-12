package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.serializer.type.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 19:39
 **/
public class cdword extends Basic<Long> {

    public cdword(Long value) {
        super(value);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected ByteBuf toByteBuf(Long value) {
        return Unpooled.buffer(size()).writeLongLE(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedIntLE();
    }

    @Override
    public int size() {
        return 4;
    }

}
