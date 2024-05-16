package org.fz.nettyx.template.tcp.client;


import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.jsc.JscChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2/17/2023
 */

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public abstract class Detector extends SingleTcpChannellClient {

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

    protected boolean getResponseState() {
        return responseState.get();
    }

    protected boolean noneResponse() {
        return !responseState.get();
    }

    protected final ChannelInitializer<NioSocketChannel> channelInitializer = new ChannelInitializer<NioSocketChannel>() {
        @Override
        protected void initChannel(NioSocketChannel channel) {
            channel.pipeline()
                   .addLast(getProtocolHandlers())
                   .addLast(newResponseValidator());
        }
    };

    protected ChannelInitializer<JscChannel> detectorChannelInitializer() {

    }

    /**
     * the simple channel handler to check response from the device
     */
    protected SimpleChannelInboundHandler<Object> newResponseValidator() {
        return new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object res) throws Exception {
                if (validResponse(res)) {
                    responseState.set(true);
                    InetSocketAddress inetAddress = (InetSocketAddress) ctx.channel().remoteAddress();

                    afterDetectSuccess(ctx, res);
                }
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
        this.trySend(this.getDetectMessage(), this.detectRetryTimes, this.waitResponseMillis, this::noneResponse);

        // 3. close channel after detect
        this.closeChannelGracefully();

        return this.getResponseState();
    }

    /**
     * send detect message in re-try mode
     *
     * @param detectMsg          detect message
     * @param retryTimes         re-try times
     * @param waitResponseMillis wait the device response
     * @param retryCondition     the re-try condition
     */
    public void trySend(Object detectMsg, int retryTimes, int waitResponseMillis, BooleanSupplier retryCondition) throws InterruptedException {
        do {
            try {
                ChannelPromise promise = super.writeAndFlush(detectMsg).sync();

                // TODO
                if (promise.isSuccess()) log.info("success send detect message [{}]", detectMsg);
                else log.info("failed send detect message [{}]", detectMsg);

                Thread.sleep(waitResponseMillis);
            } finally {
                retryTimes--;
                log.info("re-send-times left: [{}]", retryTimes);
            }
        } while (retryTimes > 0 && retryCondition.getAsBoolean());
    }

    /**
     * check if the response is valid
     */
    public abstract boolean validResponse(Object response);

    /**
     * protocol channel handlers
     */
    public abstract ChannelHandler[] getProtocolHandlers();

    /**
     * the message use to detect the device, please choose the message that device response immediately
     */
    public abstract Object getDetectMessage();

    public void afterDetectSuccess(ChannelHandlerContext ctx, Object msg) throws Exception {
        // default do nothing
    }
}
