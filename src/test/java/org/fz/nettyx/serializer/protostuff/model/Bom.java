package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



/**
 * Groups three generic values in the Protostuff sample model.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/19 11:17
 */

@Data

public class Bom<T, W, U> {

    private T t;
    private W gg;
    private U mm;

}
