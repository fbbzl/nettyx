package org.fz.nettyx.exception;

import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/17
 */
@Getter
public class NoSuchPortException extends Exception {

    private final String missingPortName;

    public NoSuchPortException(String missingPortName) {
        super("can not find port [" + missingPortName + "], please check");
        this.missingPortName = missingPortName;
    }

}
