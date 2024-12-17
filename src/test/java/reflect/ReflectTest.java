package reflect;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ReflectUtil;
import com.esotericsoftware.reflectasm.ConstructorAccess;
import lombok.Data;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import org.junit.Test;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/8 22:25
 */

@SuppressWarnings("all")
public class ReflectTest {

    @Data
    public static class Order {
        private int       id;
        private String    username;
        private int       iffd;
        private String    usefdfrname;
        private int       idfd;
        private String    useasfdrname;
        private int       id1;
        private String    username2;
        private Integer   i34d;
        private Double    use34rname;
        private Integer   iff34d;
        private StopWatch usefd34frname = new StopWatch();
        private Integer   idf34d;
        private long      useas34fdrname;
        private int       id341;
        private char      user34name2;
    }

    private static final Constructor<Order>       refConstruct = ReflectUtil.getConstructor(Order.class);
    private static final MethodHandle             constructorHandle;
    private static final Supplier<Order>          supplier     = Order::new;
    private static final ConstructorAccess<Order> access       = ConstructorAccess.get(Order.class);
    private static final FastConstructor fastConstructor =  FastClass.create(ClassLoaderUtil.getClassLoader(), Order.class).getConstructor(new Class[]{});
    private static final Supplier<Order> orderSupplier;

    static {
        try {
            Lookup lookup = MethodHandles.lookup();
            constructorHandle = lookup.findConstructor(Order.class, methodType(void.class));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "get",
                    MethodType.methodType(Supplier.class),
                    constructorHandle.type().generic(),
                    constructorHandle,
                    constructorHandle.type());

            orderSupplier = (Supplier<Order>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testReflect() throws Throwable {

        Order fast1 = (Order) fastConstructor.newInstance();
        Order order = new Order();
        for (int round = 0; round < 5; round++) {
            int       times     = 10_000_000;
            StopWatch stopWatch = StopWatch.create("");
//        stopWatch.start("new");
//        for (int i = 0; i < times; i++) {
//            Order orderNew = new Order();
//        }
//        stopWatch.stop();
//
//        stopWatch.start("supplier");
//        for (int i = 0; i < times; i++) {
//            Order order = supplier.get();
//        }
//        stopWatch.stop();
//
//        stopWatch.start("reflect construct");
//        for (int i = 0; i < times; i++) {
//            Order orderRef = refConstruct.newInstance();
//        }
//        stopWatch.stop();
//
//        stopWatch.start("method handle");
//        for (int i = 0; i < times; i++) {
//            Object invoke = constructorHandle.invoke();
//        }
//        stopWatch.stop();

//            stopWatch.start("cglib fast meta factory");
//            for (int i = 0; i < times; i++) {
//                Order fast = (Order) fastConstructor.newInstance();
//            }
//            stopWatch.stop();

            stopWatch.start("lambda meta factory");
            for (int i = 0; i < times; i++) {
                Order lambda = (Order) orderSupplier.get();
            }
            stopWatch.stop();
//
//            stopWatch.start("reflectAsm");
//            for (int i = 0; i < times; i++) {
//                Order refAsm = access.newInstance();
//            }
//            stopWatch.stop();

            Console.log(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }

    }

}
