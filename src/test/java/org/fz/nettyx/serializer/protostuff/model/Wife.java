package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



/**
 * Combines a string name and two generic values in the Protostuff sample model.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data

public class Wife<I, V> {

    private I      intt;

    private String name;

    private V vv;
}
