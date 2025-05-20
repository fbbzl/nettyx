package org.fz.nettyx.exception;

import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:42
 */

@Getter
public class TooLessBytesException extends RuntimeException {

    private final int expectLength;

    private final int provideLength;

    public TooLessBytesException(int expectLength, int provideLength)
    {
        super("bytes missing when doing deserialization, field expect bytes length is [" + expectLength
              + "] but the provided readable bytes length is [" + provideLength + "]");
        this.expectLength  = expectLength;
        this.provideLength = provideLength;
    }
}
