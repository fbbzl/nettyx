package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single jsc channel template
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
