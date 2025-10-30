package org.fz.nettyx.template.bluetooth.client;


import cn.hutool.core.map.MapUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.bluetooth.BtChannelConfig;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;

/**
 * multi blue tooth channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */
@Getter
@SuppressWarnings("deprecation")
public abstract class MultiBtChannelTemplate<K> extends AbstractMultiChannelTemplate<K, BtChannel, BtChannelConfig> {

    private final Map<K, BtDeviceAddress> addressMap;

    protected MultiBtChannelTemplate(Map<K, BtDeviceAddress> addressMap) {
        super(addressMap);
        this.addressMap = addressMap;
    }

    public Map<K, ChannelFuture> connectAll() {
        return MapUtil.map(addressMap, (k, v) -> this.connect(k));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
