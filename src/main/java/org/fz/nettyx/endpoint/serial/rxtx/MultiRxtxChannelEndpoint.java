package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.ReadAsyncOioByteStreamChannel;
import org.fz.nettyx.endpoint.AbstractMultiChannelEndpoint;
import org.fz.nettyx.endpoint.serial.rxtx.support.RxtxChannel;
import org.fz.nettyx.endpoint.serial.rxtx.support.RxtxChannelConfig;

import java.util.Map;


/**
 * A multichannel client that uses channel-key to retrieve and manipulate the specified channel
 *
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class MultiRxtxChannelEndpoint<K> extends AbstractMultiChannelEndpoint<K, RxtxChannel, RxtxChannelConfig> {

    protected MultiRxtxChannelEndpoint(Map<K, ReadAsyncOioByteStreamChannel.SerialCommAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
