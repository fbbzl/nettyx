package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.Serializer;

/**
 * 用户会用xml来定义消息模板, xml用attr来指定字段类型,解析规则等
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/1 22:26
 */
public class XmlSerializer implements Serializer {

    @Override
    public ByteBuf getByteBuf() {
        return null;
    }

    @Override
    public <T> T doDeserialize() {
        return null;
    }

    @Override
    public ByteBuf doSerialize() {
        return null;
    }
}
