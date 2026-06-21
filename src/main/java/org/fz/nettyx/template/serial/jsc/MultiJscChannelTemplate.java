package org.fz.nettyx.template.serial.jsc;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.Getter;
import org.fz.nettyx.channel.serial.SerialCommChannel.SerialCommAddress;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.util.Map;

/**
 * multi jsc channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:12
 */
@Getter
@SuppressWarnings("deprecation")
public abstract class MultiJscChannelTemplate<K> extends AbstractMultiChannelTemplate<K, SerialCommAddress, JscChannel, JscChannelConfig> {

    protected MultiJscChannelTemplate(Map<K, SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
