package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.serial.SerialCommChannel.SerialCommAddress;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single jsc channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:09
 */

@Getter
@SuppressWarnings("deprecation")
public abstract class SingleJscChannelTemplate extends AbstractSingleChannelTemplate<SerialCommAddress, JscChannel, JscChannelConfig> {

    protected SingleJscChannelTemplate(String commAddress) {
        this(new SerialCommAddress(commAddress));
    }

    protected SingleJscChannelTemplate(SerialCommAddress commAddress) {
        super(commAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
