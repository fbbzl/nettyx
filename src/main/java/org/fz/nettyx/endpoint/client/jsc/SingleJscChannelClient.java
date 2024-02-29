package org.fz.nettyx.endpoint.client.jsc;

import io.netty.channel.jsc.JSerialCommChannel;
import io.netty.channel.jsc.JSerialCommDeviceAddress;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.SingleChannelClient;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:09
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleJscChannelClient extends SingleChannelClient<JSerialCommChannel, JSerialCommDeviceAddress> {

    protected SingleJscChannelClient(JSerialCommDeviceAddress remoteAddress) {
        super(new OioEventLoopGroup(), remoteAddress);
    }

}
