package org.fz.nettyx.endpoint.client.jsc.support;

import io.netty.channel.Channel;
import io.netty.channel.oio.OioByteStreamChannel;

abstract class PublicDoReadChannel extends OioByteStreamChannel {

    public PublicDoReadChannel(Channel parent) {
        super(parent);
    }

    @Override
    public void doRead() {
        super.doRead();
    }
}