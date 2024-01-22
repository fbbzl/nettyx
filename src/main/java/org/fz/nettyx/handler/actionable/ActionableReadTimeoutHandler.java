package org.fz.nettyx.handler.actionable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.action.ChannelExceptionAction;

/**
 * The type Actionable read timeout handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 1 /27/2022 1:58 PM
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableReadTimeoutHandler extends ReadTimeoutHandler {

    private final ChannelExceptionAction timeoutAction;
    private final boolean fireNext;

    /**
     * Instantiates a new Actionable read timeout handler.
     *
     * @param timeoutSeconds the timeout seconds
     * @param timeoutAction  the timeout action
     */
    public ActionableReadTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        this(timeoutSeconds, timeoutAction, true);
    }

    /**
     * Instantiates a new Actionable read timeout handler.
     *
     * @param timeout       the timeout
     * @param unit          the unit
     * @param timeoutAction the timeout action
     */
    public ActionableReadTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        this(timeout, unit, timeoutAction, true);
    }

    /**
     * Instantiates a new Actionable read timeout handler.
     *
     * @param timeoutSeconds the timeout seconds
     * @param timeoutAction  the timeout action
     * @param fireNext       the fire next
     */
    public ActionableReadTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction, boolean fireNext) {
        this(timeoutSeconds, TimeUnit.SECONDS, timeoutAction, fireNext);
    }

    /**
     * Instantiates a new Actionable read timeout handler.
     *
     * @param timeout       the timeout
     * @param unit          the unit
     * @param timeoutAction the timeout action
     * @param fireNext      the fire next
     */
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
