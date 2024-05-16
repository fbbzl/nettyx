package template.tcp;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.tcp.client.Detector;

import java.net.InetSocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/16 18:39
 */
public class TestDetector extends Detector<String> {
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

    public static void main(String[] args) {
        TestDetector testClient = new TestDetector(new InetSocketAddress(9888));

        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((ls, cf) -> {


                    System.err.println(cf.channel().localAddress() + ": ok");
                })


                .whenDone((ls, cf) -> System.err.println("done"));

        testClient.connect().addListener(listener);
    }

}
