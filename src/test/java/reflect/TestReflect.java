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
import java.util.function.Supplier;

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
    private static final Supplier<Order> supplier = Order::new;

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

        stopWatch.start("new");
        for (int i = 0; i < 1_000_000_000; i++) {
            Order order = new Order();
            int   i1    = order.hashCode();
        }
        stopWatch.stop();

        stopWatch.start("supplier");
        for (int i = 0; i < 1_000_000_000; i++) {
            Order order = supplier.get();
            int   i1    = order.hashCode();
        }
        stopWatch.stop();

        stopWatch.start("ref construct");
        for (int i = 0; i < 1_000_000_000; i++) {
            Order order = refConstruct.newInstance();
            int   i1    = order.hashCode();
        }
        stopWatch.stop();

        stopWatch.start("method handle");
        for (int i = 0; i < 1_000_000_000; i++) {
            Object order = methodHandle.invoke();
            int   i1    = order.hashCode();
        }
        stopWatch.stop();

        Console.log(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

}
