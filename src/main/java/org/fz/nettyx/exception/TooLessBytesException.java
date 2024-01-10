package org.fz.nettyx.exception;

import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:42
 */

@Getter
public class TooLessBytesException extends RuntimeException {

    private final int expectSize;

    private final int provideSize;

    public TooLessBytesException(int expectSize, int provideSize) {
        super("expect size is [" + expectSize + "] but provide [" + provideSize + "]");
        this.expectSize = expectSize;
        this.provideSize = provideSize;
    }
}
