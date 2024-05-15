package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.AbstractSingleChannellEndpoint;
import org.fz.nettyx.endpoint.serial.SerialCommAddress;
import org.fz.nettyx.endpoint.serial.rxtx.support.RxtxChannel;
import org.fz.nettyx.endpoint.serial.rxtx.support.RxtxChannelConfig;

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
        super(new SerialCommAddress(commAddress));
    }

    protected SingleRxtxChannellEndpoint(SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
