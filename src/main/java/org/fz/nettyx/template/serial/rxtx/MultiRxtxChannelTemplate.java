package org.fz.nettyx.template.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;


/**
 * A multichannel client that uses channel-key to retrieve and manipulate the specified channel
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelTemplate<K> extends AbstractMultiChannelTemplate<K, RxtxChannel, RxtxChannelConfig> {

    protected MultiRxtxChannelTemplate(Map<K, SerialCommChannel.SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
