package org.fz.nettyx.exception;

import io.netty.channel.ChannelException;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 1/27/2022 2:18 PM
 */
public class ClosingChannelException extends ChannelException {

    public ClosingChannelException() {
    }

    public ClosingChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClosingChannelException(String message) {
        super(message);
    }

    public ClosingChannelException(Throwable cause) {
        super(cause);
    }

    public ClosingChannelException(String message, Throwable cause, boolean shared) {
        super(message, cause, shared);
    }
}
