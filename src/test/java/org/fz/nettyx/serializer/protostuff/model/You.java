package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;

import java.util.List;

/**
 * Provides boxed scalars, a list, and a nested record in the Protostuff sample model.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/19 16:48
 */

@Data

public class You {

    Character   uname;
    Integer    isMarried;
    Character   sex;
    Float  address;
    Double platformId;

    List<Long> description;
    Long interest;
    Hit     c;
    Character   uname1;
    Integer    isMarried1;
    Character   sex1;
    Float  address1;
    Double platformId1;
    Character   uname2;
    Integer    isMarried2;
    Character   sex2;
    Float  address2;


    /**
     * Provides the nested interest value referenced by the Protostuff sample model.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025-04-20
     */
    @Data

    public static class Hit {
        Long interest;
    }
}
