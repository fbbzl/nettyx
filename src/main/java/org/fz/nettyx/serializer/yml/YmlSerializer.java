package org.fz.nettyx.serializer.yml;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.Serializer;

/**
 * 扫描指定路径下的 yml文件, 然后需要定义通用的 对象结构来进行yml处理
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/1 22:26
 */
public class YmlSerializer implements Serializer {

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
