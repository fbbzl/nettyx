package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.jsc.JscChannel;
import org.fz.nettyx.channel.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:09
 */

@SuppressWarnings("deprecation")
public abstract class SingleJscChannelTemplate extends AbstractSingleChannelTemplate<JscChannel, JscChannelConfig> {

    protected SingleJscChannelTemplate(String commAddress) {
        super(new SerialCommChannel.SerialCommAddress(commAddress));
    }

    protected SingleJscChannelTemplate(SerialCommChannel.SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
