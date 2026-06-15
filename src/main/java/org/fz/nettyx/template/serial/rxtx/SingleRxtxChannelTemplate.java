package org.fz.nettyx.template.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.serial.SerialCommChannel.SerialCommAddress;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single rxtx channel template
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Getter
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelTemplate extends AbstractSingleChannelTemplate<RxtxChannel, RxtxChannelConfig> {

    private final SerialCommAddress remoteAddress;

    protected SingleRxtxChannelTemplate(String commAddress) {
        this(new SerialCommAddress(commAddress));
    }

    protected SingleRxtxChannelTemplate(SerialCommAddress commAddress) {
        super(commAddress);
        this.remoteAddress = commAddress;
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
