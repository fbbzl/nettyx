package org.fz.nettyx.exception;

import io.netty.channel.ChannelException;
import org.fz.nettyx.handler.AdvisableChannelInitializer;

/**
 * When you use {@link AdvisableChannelInitializer} initialization channel pipeline,
 * if a particular channel handler throws this exception, then the channel will be shut down
 *
 * @author fengbinbin
 * @version 1.0
 * @since 1 /27/2022 2:18 PM
 */
public class ClosingChannelException extends ChannelException {

    /**
     * Instantiates a new Closing channel exception.
     */
    public ClosingChannelException() {
    }

    /**
     * Instantiates a new Closing channel exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ClosingChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Closing channel exception.
     *
     * @param message the message
     */
    public ClosingChannelException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Closing channel exception.
     *
     * @param cause the cause
     */
    public ClosingChannelException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Closing channel exception.
     *
     * @param message the message
     * @param cause   the cause
     * @param shared  the shared
     */
    public ClosingChannelException(String message, Throwable cause, boolean shared) {
        super(message, cause, shared);
    }
}
