package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.serializer.type.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 19:46
 **/
public class cword extends Basic<Integer> {
    public cword(Integer value) {
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
    protected ByteBuf toByteBuf(Integer value) {
        return Unpooled.buffer(size()).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedShortLE();
    }

    @Override
    public int size() {
        return 2;
    }

}
