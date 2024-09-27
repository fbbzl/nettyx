package org.fz.nettyx.template.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelTemplate extends AbstractSingleChannelTemplate<RxtxChannel, RxtxChannelConfig> {

    protected SingleRxtxChannelTemplate(String commAddress) {
        super(new SerialCommChannel.SerialCommAddress(commAddress));
    }

    protected SingleRxtxChannelTemplate(SerialCommChannel.SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
