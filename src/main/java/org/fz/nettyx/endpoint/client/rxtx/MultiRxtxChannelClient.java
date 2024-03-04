package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.util.AttributeKey;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractMultiChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.NettyxRxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.NettyxRxtxDeviceAddress;


/**
 * A multi-channel client that uses channel-key to retrieve and manipulate the specified channel
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelClient<K> extends
                                                AbstractMultiChannelClient<K, NettyxRxtxChannel, RxtxChannelConfig> {

    protected MultiRxtxChannelClient(Map<K, NettyxRxtxDeviceAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

    @Override
    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_rxtx_channel_key$");
    }
}
