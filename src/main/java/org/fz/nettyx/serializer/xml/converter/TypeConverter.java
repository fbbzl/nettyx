package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/7 10:51
 */
public interface TypeConverter {

    String convert(Prop prop, ByteBuf byteBuf);

}
