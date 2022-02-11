package org.fz.nettyx.handler;

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

    private ChannelExceptionAction timeoutAction;

    public ActionableWriteTimeoutHandler(int timeoutSeconds) {
        super(timeoutSeconds);
    }

    public ActionableWriteTimeoutHandler(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

}
