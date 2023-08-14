package types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.typed.Basic;

import java.nio.ByteOrder;

/**
 * @author fengbinbin
 * @since 2021-10-19 19:42
 **/
public class cboolean extends Basic<Short> {

    // define const boolean
    public static final cboolean TRUE, FALSE;

    static {
        TRUE = new cboolean((short) 1);
        FALSE = new cboolean((short) 0);
    }

    public cboolean(Short value) {
        super(value);
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
    public int size() {
        return 2;
    }

    @Override
    public ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size()).writeShortLE(value);
    }

    @Override
    public Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }
}
