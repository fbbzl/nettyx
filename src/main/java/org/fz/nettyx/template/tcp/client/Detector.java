package org.fz.nettyx.template.tcp.client;


import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2/17/2023
 */

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public abstract class Detector<M> extends SingleTcpChannellClient {

    private final int detectRetryTimes;
    private final int waitResponseMillis;

    /**
     * this is the state that if we got the response from device
     */
    private final AtomicBoolean responseState = new AtomicBoolean(false);

    protected Detector(InetSocketAddress address, int detectRetryTimes, final int waitResponseMillis) {
        super(address);
        this.detectRetryTimes   = detectRetryTimes;
        this.waitResponseMillis = waitResponseMillis;
    }

    @Override
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
     * the core mothod to detect if the comm port
     *
     * @param address
     * @return if is the correct device
     * @throws InterruptedException
     */
    public boolean doDetect() throws InterruptedException {
        this.responseState.set(false);
        // 1. do connect sync
        ChannelFuture connectFuture = this.connect().sync();

        // 2. send detect-message when connect success
        // 2.1 get the channel
        Channel detectChannel = connectFuture.channel();
        // 2.2 store channel
        super.storeChannel(detectChannel);

        // 2.3 try-send detect req-message
        this.trySend(this.getDetectMessage(), this.detectRetryTimes, this.waitResponseMillis);

        // 3. close channel after detect
        this.closeChannelGracefully();

        return this.responseState.get();
    }

    /**
     * send detect message in re-try mode
     *
     * @param detectMsg          detect message
     * @param retryTimes         re-try times
     * @param waitResponseMillis wait the device response
     * @param retryCondition     the re-try condition
     */
    public void trySend(M detectMsg, int retryTimes, int waitResponseMillis) throws InterruptedException {
        do {
            try {
                ChannelPromise promise = super.writeAndFlush(detectMsg).sync();

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
     * the message use to detect the device, please choose the message that device response immediately
     */
    public abstract M getDetectMessage();
}
