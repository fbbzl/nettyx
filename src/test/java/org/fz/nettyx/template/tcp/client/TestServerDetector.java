package org.fz.nettyx.template.tcp.client;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/16 18:39
 */
public class TestServerDetector extends RemoteDetector<String> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TestServerDetector.class);

    protected TestServerDetector(InetSocketAddress address) {
        super(address);
    }

    @Override
    public boolean checkResponse(String response) {
        return StrUtil.equals(response, "ack");
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
        TestServerDetector testDetector = new TestServerDetector(new InetSocketAddress(9888));
        testDetector.setDetectRetryTimes(2);
        testDetector.setWaitResponseMillis(5000);
        log.info("{}", testDetector.doDetect());
    }

}
