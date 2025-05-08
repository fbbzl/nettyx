package javolution;

import javolution.io.Struct;

public class JavolutionDemo {
    public enum Gender { MALE, FEMALE }

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

    public static class Student extends Struct {
        public final Enum32<Gender> gender = new Enum32<>(Gender.values());
        public final UTF8String     name   = new UTF8String(64);
        public final Date           birth  = inner(new Date());

    }
}