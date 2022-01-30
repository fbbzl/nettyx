package org.fz.nettyx.handler;


import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.function.ChannelHandlerContextAction;

/**
 * @author fengbinbin
 * @since 2021-12-29 18:46
 **/
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableIdleStateHandler extends IdleStateHandler {

    private ChannelHandlerContextAction idleAction;

    public ActionableIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public ActionableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public ActionableIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }
}
