package org.fz.nettyx.exception;

import lombok.Getter;

/**
 * The type No such port exception.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /5/17
 */
@Getter
public class NoSuchPortException extends Exception {

    private final String missingPortName;

    /**
     * Instantiates a new No such port exception.
     *
     * @param missingPortName the missing port name
     */
    public NoSuchPortException(String missingPortName) {
        super("can not find port [" + missingPortName + "], please check");
        this.missingPortName = missingPortName;
    }

}
