package org.fz.nettyx.exception;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;

public class ExceptionConstructorTest
{
    @Test
    public void allStandardExceptionConstructorsAreUsable() throws Exception
    {
        for (Class<? extends RuntimeException> type : new Class[]{
                SerializeException.class, StopRedoException.class,
                StructDefinitionException.class, StructFieldHandlerException.class}) {
            assertNotNull(type.getConstructor().newInstance());
            assertNotNull(type.getConstructor(String.class).newInstance("message"));
            assertNotNull(type.getConstructor(String.class, Throwable.class).newInstance("message", new Exception()));
            assertNotNull(type.getConstructor(Throwable.class).newInstance(new Exception()));
            Constructor<?> protectedConstructor = type.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class);
            protectedConstructor.setAccessible(true);
            assertNotNull(protectedConstructor.newInstance("message", new Exception(), true, true));
        }
    }

    @Test
    public void typeJudgmentAndLengthExceptionsExposeTheirDetails() throws Exception
    {
        Field field = Holder.class.getDeclaredField("value");
        assertNotNull(new TypeJudgmentException(field));
        assertNotNull(new TypeJudgmentException(field.getGenericType()));
        assertNotNull(new TypeJudgmentException(new Exception()));
        assertNotNull(new TypeJudgmentException("message"));
        TooLessBytesException error = new TooLessBytesException(4, 2);
        assertNotNull(error.getMessage());
        org.junit.Assert.assertEquals(4, error.getExpectLength());
        org.junit.Assert.assertEquals(2, error.getProvideLength());
        assertNotNull(new UnknownConfigException("mode", "value"));
    }

    private static final class Holder
    {
        private int value;
    }
}
