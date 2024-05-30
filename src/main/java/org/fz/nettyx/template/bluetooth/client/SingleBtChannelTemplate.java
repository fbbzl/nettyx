package org.fz.nettyx.template.bluetooth.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelConfig;
import org.fz.nettyx.channel.bluetooth.BluetoothDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BluetoothChannel;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 12:21
 */
@SuppressWarnings("deprecation")
public abstract class SingleBtChannelTemplate extends AbstractSingleChannelTemplate<BluetoothChannel, BluetoothChannelConfig> {

    protected SingleBtChannelTemplate(String commAddress) {
        super(new BluetoothDeviceAddress(commAddress));
    }

    protected SingleBtChannelTemplate(BluetoothDeviceAddress address) {
        super(address);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }
}
