package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.jsc.JscChannel;
import org.fz.nettyx.channel.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelEndpoint;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiJscChannelEndpoint<K> extends AbstractMultiChannelEndpoint<K, JscChannel, JscChannelConfig> {

    protected MultiJscChannelEndpoint(Map<K, SerialCommChannel.SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
