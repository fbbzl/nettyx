package org.fz.nettyx.serializer.xml.handler;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.util.BytesKit.Endian;

import java.util.function.Function;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public abstract class NumberHandler implements XmlPropHandler {

    @Override
    public String read(Prop prop, ByteBuf reading) {
        return toNumber(prop.getEndianKit()).apply(this.readBytes(prop, reading)).toString();
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        writing.writeBytes(fromNumber(prop.getEndianKit()).apply(prop.getText()));
    }

    protected abstract Function<byte[], Number> toNumber(Endian endian);

    protected abstract Function<String, byte[]> fromNumber(Endian endian);

    public static class ByteHandler extends NumberHandler {

        @Override
        public String forType() {
            return "byte";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toByteValue;
        }

        @Override
        protected Function<String, byte[]> fromNumber(Endian endian) {
            return str -> endian.fromByteValue(Byte.parseByte(str));
        }
    }

    public static class ShortHandler extends NumberHandler {

        @Override
        public String forType() {
            return "short";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toShortValue;
        }

        @Override
        protected Function<String, byte[]> fromNumber(Endian endian) {
            return str -> endian.fromShortValue(Short.parseShort(str));
        }
    }

    public static class IntHandler extends NumberHandler {

        @Override
        public String forType() {
            return "int";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toIntValue;
        }

        @Override
        protected Function<String, byte[]> fromNumber(Endian endian) {
            return str -> endian.fromIntValue(Integer.parseInt(str));
        }
    }

    public static class LongHandler extends NumberHandler {

        @Override
        public String forType() {
            return "long";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toLongValue;
        }

        @Override
        protected Function<String, byte[]> fromNumber(Endian endian) {
            return str -> endian.fromLongValue(Long.parseLong(str));
        }
    }

}
