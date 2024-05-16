package template.tcp.client;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.template.tcp.client.ServerDetector;

import java.net.InetSocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/16 18:39
 */
public class TestDetector extends ServerDetector<String> {
    protected TestDetector(InetSocketAddress address) {
        super(address);
    }

    @Override
    public boolean checkResponse(String response) {
        return false;
    }

    @Override
    public void initDetectChannel(NioSocketChannel ch) {
        ch.pipeline().addLast();
    }

    @Override
    public String getDetectMessage() {
        return "this is detect string type msg";
    }

    public static void main(String[] args) throws Exception {
        TestDetector testDetector = new TestDetector(new InetSocketAddress(9888));
        System.err.println(testDetector.doDetect());
    }

}
