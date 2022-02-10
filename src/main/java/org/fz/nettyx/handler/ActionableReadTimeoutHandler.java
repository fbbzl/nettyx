package org.fz.nettyx.handler;

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

    private ChannelExceptionAction timeoutAction;

    @Override
    public boolean isSharable() {
        return true;
    }

    public ActionableReadTimeoutHandler(int timeoutSeconds) {
        super(timeoutSeconds);
    }

    public ActionableReadTimeoutHandler(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }
}
