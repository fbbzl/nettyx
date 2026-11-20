package org.fz.nettyx.coveragemodel;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Provides an annotated abstract class to exercise rejection of non-instantiable structs.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
public abstract class AbstractCoverageStruct {
}
