package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.jsc.JscChannel;
import org.fz.nettyx.channel.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;

/**
 * template with multi-jsc channel
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@SuppressWarnings("deprecation")
public abstract class MultiJscChannelTemplate<K> extends AbstractMultiChannelTemplate<K, JscChannel, JscChannelConfig> {

    protected MultiJscChannelTemplate(Map<K, SerialCommChannel.SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
