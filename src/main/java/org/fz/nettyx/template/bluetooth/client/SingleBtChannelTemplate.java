package org.fz.nettyx.template.bluetooth.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.bluetooth.BtChannelConfig;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * single blue tooth channel template
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 12:21
 */
@SuppressWarnings("deprecation")
public abstract class SingleBtChannelTemplate extends AbstractSingleChannelTemplate<BtChannel, BtChannelConfig> {

    protected SingleBtChannelTemplate(String address)
    {
        super(new BtDeviceAddress(address));
    }

    protected SingleBtChannelTemplate(BtDeviceAddress address)
    {
        super(address);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup()
    {
        return new OioEventLoopGroup();
    }
}
