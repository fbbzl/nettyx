package org.fz.nettyx.handler;

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

    public ActionableWriteTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        super(timeoutSeconds);
        this.timeoutAction = timeoutAction;
    }

    public ActionableWriteTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        super(timeout, unit);
        this.timeoutAction = timeoutAction;
    }

    @Override
    protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception {
        timeoutAction.act(ctx, WriteTimeoutException.INSTANCE);
        super.writeTimedOut(ctx);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

}
