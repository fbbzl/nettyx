package javolution;

import javolution.io.Struct;

public class JavolutionDemo {
    public enum Gender { MALE, FEMALE }

    public static class Date extends Struct {
        public final Unsigned16 year = new Unsigned16();
        public final Unsigned8 month = new Unsigned8();
        public final Unsigned8 day = new Unsigned8();
    }

    public static class Student extends Struct {
        public final Enum32<Gender> gender = new Enum32<>(Gender.values());
        public final UTF8String     name   = new UTF8String(64);
        public final Date           birth  = inner(new Date());
        public final Unsigned16[]   grades = array(new Unsigned16[10]);
    }
}