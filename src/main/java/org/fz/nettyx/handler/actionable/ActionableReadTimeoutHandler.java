package org.fz.nettyx.handler.actionable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.function.ChannelExceptionAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 1/27/2022 1:58 PM
 */

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableReadTimeoutHandler extends ReadTimeoutHandler {

    private final ChannelExceptionAction timeoutAction;
    private final boolean fireNext;

    public ActionableReadTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        this(timeoutSeconds, timeoutAction, true);
    }

    public ActionableReadTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        this(timeout, unit, timeoutAction, true);
    }

    public ActionableReadTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction, boolean fireNext) {
        this(timeoutSeconds, TimeUnit.SECONDS, timeoutAction, fireNext);
    }

    public ActionableReadTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction, boolean fireNext) {
        super(timeout, unit);
        this.timeoutAction = timeoutAction;
        this.fireNext = fireNext;
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        this.timeoutAction.act(ctx, ReadTimeoutException.INSTANCE);

        if (fireNext) super.readTimedOut(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}
