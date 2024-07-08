package reflect;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/8 22:25
 */
public class TestReflect {

    @Data
    public static class Order {
        private Integer id;
        private String  username;
    }

    private static final Constructor<Order> refConstruct = ReflectUtil.getConstructor(Order.class);
    private static final MethodHandle       methodHandle;

    static {
        try {
            methodHandle = MethodHandles.lookup().findConstructor(Order.class, methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReflect() throws Throwable {
        StopWatch stopWatch = StopWatch.create("");

        stopWatch.start("new ");
        for (int i = 0; i < 10000; i++) {
            Order order = new Order();
        }
        stopWatch.stop();

        stopWatch.start("ref construct");
        for (int i = 0; i < 10000; i++) {
            Order order = refConstruct.newInstance();
        }
        stopWatch.stop();

        stopWatch.start("method handle");
        for (int i = 0; i < 10000; i++) {
            Object invoke = methodHandle.invoke();
        }
        stopWatch.stop();

        Console.log(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

}
