package org.fz.nettyx.template;


import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.template.tcp.client.SingleTcpChannellClientTemplate;

import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * It is used to detect whether it is the target server
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2/17/2023
 */

@Slf4j
@Getter
@Setter
@SuppressWarnings("all")
public abstract class RemoteDetector<M> {

    private static final int DEFAULT_DETECT_RETRY_TIMES   = 3;
    private static final int DEFAULT_WAIT_RESPONSE_MILLIS = 1000;

    private int detectRetryTimes   = DEFAULT_DETECT_RETRY_TIMES;
    private int waitResponseMillis = DEFAULT_WAIT_RESPONSE_MILLIS;

    private SingleTcpChannellClientTemplate template;

    /**
     * this is the state that if we got the response from server
     */
    private final AtomicBoolean responseState = new AtomicBoolean(false);

    protected RemoteDetector(SingleTcpChannellClientTemplate template) {
        this.template = template;
    }

    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                initDetectChannel(channel);

                channel.pipeline()
                       .addLast(new SimpleChannelInboundHandler<M>() {
                           @Override
                           protected void channelRead0(ChannelHandlerContext ctx, M msg) {
                               responseState.set(checkResponse(msg));
                           }
                       });
            }
        };
    }

    /**
     * the core mothod to detect server
     *
     * @param address
     * @return if is the correct server
     * @throws InterruptedException
     */
    public boolean doDetect() throws InterruptedException, ConnectException {
        try {
            this.responseState.set(false);
            // 1. do connect sync
            ChannelFuture connectFuture = template.connect().sync();

            // 2. check if connect success
            if (!connectFuture.isSuccess())
                throw new ConnectException("can not connect to address [" + template.getRemoteAddress() + "]");

            // 3. store channel
            template.storeChannel(connectFuture.channel());

            // 4. try-send detect req-message
            this.trySend(this.getDetectMessage(), this.detectRetryTimes, this.waitResponseMillis);

            return this.responseState.get();
        } finally {
            template.closeChannelGracefully();
        }
    }

    /**
     * send detect message in re-try mode
     *
     * @param detectMsg          detect message
     * @param retryTimes         re-try times
     * @param waitResponseMillis wait the server response
     */
    public void trySend(M detectMsg, int retryTimes, int waitResponseMillis) throws InterruptedException {
        do {
            try {
                ChannelPromise promise = template.writeAndFlush(detectMsg).await();

                if (promise.isSuccess()) log.info("success send detect message [{}]", detectMsg);
                else log.info("failed send detect message [{}]", detectMsg);
            } finally {
                retryTimes--;
                log.info("re-send-times left: [{}]", retryTimes);
            }
            Thread.sleep(waitResponseMillis);
        } while (retryTimes > 0 && !responseState.get());
    }

    /**
     * check if the response is valid
     */
    public abstract boolean checkResponse(M response);

    /**
     * protocol channel handlers
     */
    public abstract void initDetectChannel(NioSocketChannel channel);

    /**
     * the message use to detect the server, please choose the message that server response immediately
     */
    public abstract M getDetectMessage();
}
