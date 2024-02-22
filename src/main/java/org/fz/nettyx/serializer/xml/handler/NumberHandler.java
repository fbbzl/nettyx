package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.util.NumberUtil;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import org.fz.nettyx.serializer.xml.dtd.Model.Prop;
import org.fz.nettyx.util.EndianKit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public abstract class NumberHandler implements PropTypeHandler {

    @Override
    public String read(Prop prop, ByteBuf reading) {
        return toNumber(prop.getEndianKit()).apply(this.readBytes(prop, reading)).toString();
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        writing.writeBytes(fromNumber(prop.getEndianKit()).apply(NumberUtil.parseNumber(prop.getText())));
    }

    protected abstract Function<byte[], Number> toNumber(EndianKit endian);

    protected abstract Function<Number, byte[]> fromNumber(EndianKit endian);

    public static class ByteHandler extends NumberHandler {

        @Override
        public String forType() {
            return "byte";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toByteValue;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromByteValue(num.byteValue());
        }
    }

    public static class ShortHandler extends NumberHandler {

        @Override
        public String forType() {
            return "short";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toShort;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromShort(num.shortValue());
        }
    }

    public static class IntHandler extends NumberHandler {

        @Override
        public String forType() {
            return "int";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toInt;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromInt(num.intValue());
        }
    }

    public static class LongHandler extends NumberHandler {

        @Override
        public String forType() {
            return "long";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toLong;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromLong(num.longValue());
        }
    }

    public static class FloatHandler extends NumberHandler {

        @Override
        public String forType() {
            return "float";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toFloat;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromFloat(num.floatValue());
        }
    }

    public static class DoubleHandler extends NumberHandler {

        @Override
        public String forType() {
            return "double";
        }

        @Override
        public Function<byte[], Number> toNumber(EndianKit endian) {
            return endian::toDouble;
        }

        @Override
        protected Function<Number, byte[]> fromNumber(EndianKit endian) {
            return num -> endian.fromDouble(num.doubleValue());
        }
    }
}
