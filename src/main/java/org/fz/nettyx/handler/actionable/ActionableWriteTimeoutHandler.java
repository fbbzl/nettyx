package org.fz.nettyx.handler.actionable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.action.ChannelExceptionAction;

/**
 * The type Actionable write timeout handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 1 /27/2022 1:59 PM
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ActionableWriteTimeoutHandler extends WriteTimeoutHandler {

    private final ChannelExceptionAction timeoutAction;
    private final boolean fireNext;

    /**
     * Instantiates a new Actionable write timeout handler.
     *
     * @param timeoutSeconds the timeout seconds
     * @param timeoutAction  the timeout action
     */
    public ActionableWriteTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
        this(timeoutSeconds, timeoutAction, true);
    }

    /**
     * Instantiates a new Actionable write timeout handler.
     *
     * @param timeout       the timeout
     * @param unit          the unit
     * @param timeoutAction the timeout action
     */
    public ActionableWriteTimeoutHandler(long timeout, TimeUnit unit, ChannelExceptionAction timeoutAction) {
        this(timeout, unit, timeoutAction, true);
    }

    /**
     * Instantiates a new Actionable write timeout handler.
     *
     * @param timeoutSeconds the timeout seconds
     * @param timeoutAction  the timeout action
     * @param fireNext       the fire next
     */
    public ActionableWriteTimeoutHandler(int timeoutSeconds, ChannelExceptionAction timeoutAction, boolean fireNext) {
        this(timeoutSeconds, TimeUnit.SECONDS, timeoutAction, fireNext);
    }

    /**
     * Instantiates a new Actionable write timeout handler.
     *
     * @param timeout       the timeout
     * @param unit          the unit
     * @param timeoutAction the timeout action
     * @param fireNext      the fire next
     */
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
