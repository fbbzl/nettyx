package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



/**
 * Holds a name and order value whose types are supplied by the Protostuff sample model.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data

public class Son<B, Y> {

    private B name;
    private Y sonOrder;

}
