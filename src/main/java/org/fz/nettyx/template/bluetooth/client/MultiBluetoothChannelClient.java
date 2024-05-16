package org.fz.nettyx.template.bluetooth.client;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.bluetooth.BluetoothChannel;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelEndpoint;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiBluetoothChannelClient<K> extends AbstractMultiChannelEndpoint<K, BluetoothChannel, BluetoothChannelConfig> {

    protected MultiBluetoothChannelClient(Map<K, BluetoothChannel.BluetoothDeviceAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
