package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannelConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractMultiChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannel;


/**
 * A multi-channel client that uses channel-key to retrieve and manipulate the specified channel
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelClient<K> extends AbstractMultiChannelClient<K, RxtxChannel, RxtxChannelConfig> {

    protected MultiRxtxChannelClient(Map<K, XRxtxDeviceAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
