package org.fz.nettyx.exception;

/**
 * Reports a value that is not recognized for the given configuration option.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/20 12:18
 */

public class UnknownConfigException extends IllegalArgumentException {

    public UnknownConfigException(Object config, Object value)
    {
        super("unknown config: [" + config + "], value: " + value);
    }
}
