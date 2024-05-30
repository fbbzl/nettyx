package org.fz.nettyx.template.bluetooth.client;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelConfig;
import org.fz.nettyx.channel.bluetooth.BluetoothDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BluetoothChannel;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiBtChannelTemplate<K> extends AbstractMultiChannelTemplate<K, BluetoothChannel, BluetoothChannelConfig> {

    protected MultiBtChannelTemplate(Map<K, BluetoothDeviceAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
