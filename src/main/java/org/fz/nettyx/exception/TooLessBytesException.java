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
        super("to less bytes, expect bytes length is [" + expectSize + "] but provide bytes length is [" + provideSize + "]");
        this.expectSize = expectSize;
        this.provideSize = provideSize;
    }
}
