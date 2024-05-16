package org.fz.nettyx.template.bluetooth.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.bluetooth.BluetoothChannel;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelConfig;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 12:21
 */
@SuppressWarnings("deprecation")
public abstract class SingleBluetoothChannelClient extends AbstractSingleChannelTemplate<BluetoothChannel, BluetoothChannelConfig> {

    protected SingleBluetoothChannelClient(String commAddress) {
        super(new BluetoothChannel.BluetoothDeviceAddress(commAddress));
    }

    protected SingleBluetoothChannelClient(BluetoothChannel.BluetoothDeviceAddress address) {
        super(address);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }
}