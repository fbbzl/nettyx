package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;

/**
 * used to work with expressions
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/14 8:05
 */

public class ExpressionConverter implements TypeConverter<String> {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        // 读取值, 根据type进行转换
        // 表达式使用的mvel2
        //调用表达式, $v来引用

        //最后计算后值返回
        return null;
    }
}
