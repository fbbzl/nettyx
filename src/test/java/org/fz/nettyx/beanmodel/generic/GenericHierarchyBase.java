package org.fz.nettyx.beanmodel.generic;

import lombok.Getter;
import lombok.Setter;

/**
 * Declares two inherited fields whose types are resolved through the subclass hierarchy.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Getter
@Setter
public class GenericHierarchyBase<A, B> {

    private A first;
    private B second;
}
