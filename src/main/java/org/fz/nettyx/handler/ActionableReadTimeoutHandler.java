package org.fz.nettyx.handler;

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

    public ActionableReadTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        super(timeoutSeconds);
        this.timeoutAction = timeoutAction;
    }

    public ActionableReadTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        super(timeout, unit);
        this.timeoutAction = timeoutAction;
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        this.timeoutAction.act(ctx, ReadTimeoutException.INSTANCE);
        super.readTimedOut(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}
