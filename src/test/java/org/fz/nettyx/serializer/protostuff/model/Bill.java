package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



/**
 * Provides a Protostuff sample value with a character identifier and an organization name.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data

public class Bill {

    private char bid;

    private String orgName;


}
