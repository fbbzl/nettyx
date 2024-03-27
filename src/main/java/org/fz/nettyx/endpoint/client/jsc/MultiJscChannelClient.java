package org.fz.nettyx.endpoint.client.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.SerialCommAddress;
import org.fz.nettyx.endpoint.client.AbstractMultiChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiJscChannelClient<K> extends AbstractMultiChannelClient<K, JscChannel, JscChannelConfig> {

    protected MultiJscChannelClient(Map<K, SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
