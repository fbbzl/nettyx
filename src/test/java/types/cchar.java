package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.serializer.type.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 19:58
 **/
public class cchar extends Basic<Byte> {

    public cchar(Byte value) {
        super(value);
    }

    public cchar(int value) {
        super((byte) value);
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
    protected ByteBuf toByteBuf(Byte value) {
        return Unpooled.buffer(size()).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    @Override
    public int size() {
        return 1;
    }

}
