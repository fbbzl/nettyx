package org.fz.nettyx.endpoint.client.jsc.support;

import io.netty.channel.Channel;
import io.netty.channel.oio.OioByteStreamChannel;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
@SuppressWarnings("deprecation")
abstract class PublicDoReadChannel extends OioByteStreamChannel {

    protected PublicDoReadChannel(Channel parent) {
        super(parent);
    }

    @Override
    public void doRead() {
        super.doRead();
    }
}