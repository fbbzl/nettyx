package org.fz.nettyx.beanmodel.rollback;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Provides a valid struct whose tentative registration is rolled back after the scan fails.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class AValidRollbackBean {
    private cint value;
}
