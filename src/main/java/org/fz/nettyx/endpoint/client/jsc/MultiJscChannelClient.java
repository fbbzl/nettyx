package org.fz.nettyx.endpoint.client.jsc;

import io.netty.channel.jsc.JSerialCommChannel;
import io.netty.channel.jsc.JSerialCommDeviceAddress;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.MultiChannelClient;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiJscChannelClient<K> extends MultiChannelClient<K, JSerialCommChannel,
        JSerialCommDeviceAddress> {

    protected MultiJscChannelClient(Map<K, JSerialCommDeviceAddress> addressMap) {
        super(new OioEventLoopGroup(), addressMap);
    }

    @Override
    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_jsc_channel_key$");
    }
}
