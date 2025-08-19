package org.fz.nettyx.template.serial.rxtx;

import cn.hutool.core.map.MapUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.SerialCommChannel.SerialCommAddress;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;


/**
 * multi rxtx channel template
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/
@Getter
@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelTemplate<K> extends AbstractMultiChannelTemplate<K, RxtxChannel,
        RxtxChannelConfig> {

    private final Map<K, SerialCommAddress> addressMap;

    protected MultiRxtxChannelTemplate(Map<K, SerialCommChannel.SerialCommAddress> addressMap) {
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
