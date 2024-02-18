package org.fz.nettyx.serializer.xml.converter;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.BytesKit.Endian;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public abstract class NumberConverter implements TypeConverter {

    /**
     * key is type value, value is converter
     */
    private static final Map<String, NumberConverter> NUMBER_CONVERTERS = new SafeConcurrentHashMap<>();

    public static boolean convertible(Prop prop) {
        return NUMBER_CONVERTERS.containsKey(prop.getType().getValue());
    }

    public static boolean convertible(String typeValue) {
        return NUMBER_CONVERTERS.containsKey(typeValue);
    }

    public static NumberConverter getConverter(Prop prop) {
        PropType type = prop.getType();
        return NUMBER_CONVERTERS.getOrDefault(type.getValue(), new ZeroedConverter());
    }

    public static NumberConverter getConverter(String numType) {
        return NUMBER_CONVERTERS.get(numType);
    }

    static {
        scanNumberConverters();
    }

    //************************************            private start              *************************************//

    private static synchronized void scanNumberConverters() {
        Set<Class<?>> numConverterClasses = ClassScanner.scanPackageBySuper(EMPTY, NumberConverter.class);
        for (Class<?> numConverterClass : numConverterClasses) {
            NumberConverter converter = (NumberConverter) Singleton.get(numConverterClass);
            String forNumType = converter.forNumberType();

            NUMBER_CONVERTERS.put(forNumType, converter);
        }
    }

    //************************************            private end                *************************************//

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        return toNumber(prop.getEndianKit()).apply(this.readBytes(prop, byteBuf)).toString();
    }

    protected abstract String forNumberType();

    protected abstract Function<byte[], Number> toNumber(Endian endian);

    public static class ByteConverter extends NumberConverter {

        @Override
        protected String forNumberType() {
            return "byte";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toByteValue;
        }
    }

    public static class ShortConverter extends NumberConverter {

        @Override
        protected String forNumberType() {
            return "short";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toShortValue;
        }
    }

    public static class IntConverter extends NumberConverter {

        @Override
        protected String forNumberType() {
            return "int";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toIntValue;
        }
    }

    public static class LongConverter extends NumberConverter {

        @Override
        protected String forNumberType() {
            return "long";
        }

        @Override
        public Function<byte[], Number> toNumber(Endian endian) {
            return endian::toLongValue;
        }
    }

}
