package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.util.BytesKit.Endian;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public abstract class NumberConverter implements TypeConverter {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        return toNumber(prop.getEndianKit()).apply(this.readBytes(prop, byteBuf)).toString();
    }

    protected abstract Function<byte[], Number> toNumber(Endian endian);

    public static class ByteConverter extends NumberConverter {

        @Override
        public String forType() {
            return "byte";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toByteValue;
        }
    }

    public static class ShortConverter extends NumberConverter {

        @Override
        public String forType() {
            return "short";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toShortValue;
        }
    }

    public static class IntConverter extends NumberConverter {

        @Override
        public String forType() {
            return "int";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toIntValue;
        }
    }

    public static class LongConverter extends NumberConverter {

        @Override
        public String forType() {
            return "long";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toLongValue;
        }
    }

}
