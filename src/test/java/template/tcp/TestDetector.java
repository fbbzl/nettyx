package template.tcp;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.template.tcp.client.Detector;

import java.net.InetSocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/16 18:39
 */
public class TestDetector extends Detector {
    protected TestDetector(InetSocketAddress address, int detectRetryTimes, int waitResponseMillis) {
        super(address, detectRetryTimes, waitResponseMillis);
    }

    @Override
    public boolean checkResponse(Object response) {
        return false;
    }

    @Override
    public void initChannel(NioSocketChannel ch) {
        ch.pipeline().addLast();
    }

    @Override
    public Object getDetectMessage() {
        return null;
    }
}
