package org.fz.nettyx.beanmodel.valid;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Declares the integer field and accessors inherited by the struct fixture.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Getter
@Setter
public class InheritedAccessorBase {
    private cint inherited;
}
