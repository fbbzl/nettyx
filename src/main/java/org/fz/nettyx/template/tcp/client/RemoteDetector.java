package org.fz.nettyx.template.tcp.client;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * It is used to detect whether it is the target server
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2/17/2023
 */

@Getter
@Setter
@SuppressWarnings("all")
public abstract class RemoteDetector<M> extends SingleChannelClientTemplate {

    private static final int            DEFAULT_DETECT_RETRY_TIMES   = 3;
    private static final int            DEFAULT_WAIT_RESPONSE_MILLIS = 1000;
    private static final InternalLogger log                          = InternalLoggerFactory.getInstance(RemoteDetector.class);

    private int detectRetryTimes   = DEFAULT_DETECT_RETRY_TIMES;
    private int waitResponseMillis = DEFAULT_WAIT_RESPONSE_MILLIS;

    /**
     * this is the state that if we got the response from server
     */
    private final AtomicBoolean responseState = new AtomicBoolean(false);

    protected RemoteDetector(InetSocketAddress address)
    {
        super(address);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer()
    {
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
     * @return if is the correct server
     * @throws InterruptedException
     */
    public boolean doDetect() throws InterruptedException, ConnectException
    {
        try {
            this.responseState.set(false);
            // 1. do connect sync
            this.connect().sync();

            // 2. try-send detect req-message
            this.trySend(this::getDetectMessage, this.detectRetryTimes, this.waitResponseMillis);

            return this.responseState.get();
        }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw interrupted;
        }
        catch (Exception connectException) {
            ConnectException error = new ConnectException(
                    "can not connect to address [" + this.getRemoteAddress() + "]: " + connectException.getMessage());
            error.initCause(connectException);
            throw error;
        }
        finally {
            this.closeChannelGracefully();
        }
    }

    /**
     * send detect message in re-try mode
     *
     * @param detectMsg          detect message
     * @param retryTimes         re-try times
     * @param waitResponseMillis wait the server response
     */
    public void trySend(
            M   detectMsg,
            int retryTimes,
            int waitResponseMillis) throws InterruptedException
    {
        if (retryTimes <= 1 || !(detectMsg instanceof ReferenceCounted)) {
            trySend(() -> detectMsg, retryTimes, waitResponseMillis);
            return;
        }

        Supplier<? extends M> supplier = switch (detectMsg) {
            case ByteBuf byteBuf       -> () -> (M) byteBuf.retainedDuplicate();
            case ByteBufHolder holder  -> () -> (M) holder.retainedDuplicate();
            default                    -> throw new IllegalArgumentException(
                    "reference-counted detect messages must be supplied separately for every retry: [" + detectMsg.getClass() + "]");
        };
        try {
            trySend(supplier, retryTimes, waitResponseMillis);
        }
        finally {
            ReferenceCountUtil.safeRelease(detectMsg);
        }
    }

    /**
     * Send a newly supplied detect message for every retry.
     */
    public void trySend(
            Supplier<? extends M> detectMsgSupplier,
            int                   retryTimes,
            int                   waitResponseMillis) throws InterruptedException
    {
        do {
            M detectMsg = detectMsgSupplier.get();
            try {
                ChannelPromise promise = super.writeAndFlush(detectMsg).await();

                if (promise.isSuccess()) log.info("success send detect message [{}]", detectMsg);
                else                     log.info("something wrong when sending detect message [{}]", detectMsg);
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
