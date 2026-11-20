package org.fz.nettyx.coveragemodel;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Exposes the private nested struct class to registration tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public final class InvalidNestedStructs {

    private InvalidNestedStructs() {
    }

    public static Class<?> privateStructType() {
        return PrivateStruct.class;
    }

    /**
     * Provides a private nested struct to exercise scanner visibility checks.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Struct(endian = Struct.Endian.BE)
    private static class PrivateStruct {
    }
}
