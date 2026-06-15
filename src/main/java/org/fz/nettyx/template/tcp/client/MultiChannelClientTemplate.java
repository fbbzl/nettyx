package org.fz.nettyx.template.tcp.client;

import cn.hutool.core.map.MapUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * multi tcp channel template
 *
 * @param <K> the channel channelKey type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:58
 */
@Getter
public abstract class MultiChannelClientTemplate<K>
        extends AbstractMultiChannelTemplate<K, NioSocketChannel, SocketChannelConfig> {

    private final Map<K, InetSocketAddress> addressMap;

    protected MultiChannelClientTemplate(Map<K, InetSocketAddress> addressMap) {
        super(addressMap);
        this.addressMap = addressMap;
    }

    public Map<K, ChannelFuture> connectAll() {
        return MapUtil.map(addressMap, (k, v) -> this.connect(k));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

}
