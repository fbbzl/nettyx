package org.fz.nettyx.endpoint.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.endpoint.AbstractSingleChannellEndpoint;
import org.fz.nettyx.endpoint.serial.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.serial.jsc.support.JscChannelConfig;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:09
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleJscChannelEndpoint extends AbstractSingleChannellEndpoint<JscChannel, JscChannelConfig> {

    protected SingleJscChannelEndpoint(String commAddress) {
        super(new SerialCommChannel.SerialCommAddress(commAddress));
    }

    protected SingleJscChannelEndpoint(SerialCommChannel.SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
