package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.endpoint.AbstractSingleChannellEndpoint;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannellEndpoint extends AbstractSingleChannellEndpoint<RxtxChannel, RxtxChannelConfig> {

    protected SingleRxtxChannellEndpoint(String commAddress) {
        super(new SerialCommChannel.SerialCommAddress(commAddress));
    }

    protected SingleRxtxChannellEndpoint(SerialCommChannel.SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
