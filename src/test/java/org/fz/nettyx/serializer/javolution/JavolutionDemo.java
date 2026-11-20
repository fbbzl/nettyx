package org.fz.nettyx.serializer.javolution;

import javolution.io.Struct;

/**
 * Demonstrates a Javolution binary layout with a nested struct and fixed-size fields.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025-05-08
 */
public class JavolutionDemo {
    /**
     * Defines the values encoded by the student struct's gender field.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025-05-08
     */
    public enum Gender { MALE, FEMALE }

    /**
     * Declares the nested date layout with unsigned fields and a fixed-size grades array.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025-05-08
     */
    public static class Date extends Struct {
        public final Unsigned16 year = new Unsigned16(22);
        public final Unsigned8 month = new Unsigned8(22);
        public final Unsigned8 day = new Unsigned8(11);
        public final Unsigned8 day1 = new Unsigned8(111);
        public final Unsigned8 da2y = new Unsigned8(111);
        public final Unsigned8 day3 = new Unsigned8(111);
        public final Unsigned8 day4 = new Unsigned8(111);
        public final Unsigned8 day5 = new Unsigned8(111);
        public final Unsigned8 day6 = new Unsigned8(111);
        public final Unsigned8 day7 = new Unsigned8(111);
        public final Unsigned8 day8 = new Unsigned8(111);
        public final Unsigned8 day9 = new Unsigned8(111);
        public final Unsigned16[]   grades = array(new Unsigned16[10]);
    }

    /**
     * Defines a binary record with a gender code, a fixed-size UTF-8 name, and a nested birth date.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025-05-08
     */
    public static class Student extends Struct {
        public final Enum32<Gender> gender = new Enum32<>(Gender.values());
        public final UTF8String     name   = new UTF8String(64);
        public final Date           birth  = inner(new Date());

    }
}
