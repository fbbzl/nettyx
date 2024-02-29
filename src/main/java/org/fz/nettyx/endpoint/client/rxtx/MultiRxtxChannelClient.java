package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.MultiChannelClient;

import java.util.Map;


/**
 * A multi-channel client that uses channel-key to retrieve and manipulate the specified channel
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelClient<K> extends MultiChannelClient<K, RxtxChannel, RxtxDeviceAddress> {

    protected MultiRxtxChannelClient(EventLoopGroup eventLoopGroup, Map<K, RxtxDeviceAddress> addressMap) {
        super(new OioEventLoopGroup(), addressMap);
    }

    @Override
    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_rxtx_channel_key$");
    }
}
