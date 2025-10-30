package org.fz.nettyx.template.bluetooth.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.bluetooth.BtChannelConfig;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single blue tooth channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 12:21
 */
@Getter
@SuppressWarnings("deprecation")
public abstract class SingleBtChannelTemplate extends AbstractSingleChannelTemplate<BtChannel, BtChannelConfig> {

    private final BtDeviceAddress remoteAddress;

    protected SingleBtChannelTemplate(String address)
    {
        this(new BtDeviceAddress(address));
    }

    protected SingleBtChannelTemplate(BtDeviceAddress address)
    {
        super(address);
        this.remoteAddress = address;
    }

    @Override
    protected EventLoopGroup newEventLoopGroup()
    {
        return new OioEventLoopGroup();
    }
}
