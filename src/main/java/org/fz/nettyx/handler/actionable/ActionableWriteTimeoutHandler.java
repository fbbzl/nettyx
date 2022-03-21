package org.fz.nettyx.handler.actionable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.function.ChannelExceptionAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 1/27/2022 1:59 PM
 */

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableWriteTimeoutHandler extends WriteTimeoutHandler {

    private final ChannelExceptionAction timeoutAction;
    private final boolean fireNext;

    public ActionableWriteTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        this(timeoutSeconds, timeoutAction, true);
    }

    public ActionableWriteTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        this(timeout, unit, timeoutAction, true);
    }

    public ActionableWriteTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction, boolean fireNext) {
        this(timeoutSeconds, TimeUnit.SECONDS, timeoutAction, fireNext);
    }

    public ActionableWriteTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction, boolean fireNext) {
        super(timeout, unit);
        this.timeoutAction = timeoutAction;
        this.fireNext = fireNext;
    }

    @Override
    protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception {
        timeoutAction.act(ctx, WriteTimeoutException.INSTANCE);

        if (fireNext) super.writeTimedOut(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

}
