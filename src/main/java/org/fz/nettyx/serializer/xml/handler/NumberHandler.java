package org.fz.nettyx.serializer.xml.handler;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.util.BytesKit.Endian;

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
        prop.getEndianKit().fromNumber(toNumber(prop.getText()));
    }

    protected abstract Function<byte[], Number> toNumber(Endian endian);

    protected abstract Number toNumber(String text);

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
        protected Number toNumber(String text) {
            return Byte.parseByte(text);
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
        protected Number toNumber(String text) {
            return Short.parseShort(text);
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
        protected Number toNumber(String text) {
            return Integer.parseInt(text);
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
        protected Number toNumber(String text) {
            return Long.parseLong(text);
        }
    }

}
