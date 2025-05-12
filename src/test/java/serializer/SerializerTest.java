package serializer;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.*;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;
import org.fz.nettyx.util.TypeRefer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.serializer.struct.StructSerializer.toBytes;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
public class SerializerTest {

    static final TypeRefer<You> youTypeRefer = new TypeRefer<>() {};

    private static final StructSerializerContext context =
            new StructSerializerContext("codec.model");

    @Before
    public void correctnessTest() {
        TypeRefer<User<Bill, Wife<Son<clong4, Bill>, Son<clong4, Bill>>, clong8>> userTypeRefer =
                new TypeRefer<>() {};

        byte[] bytes = new byte[900];
        Arrays.fill(bytes, (byte) 67);
        User struct = toStruct(userTypeRefer, bytes);
        byte[] buf = toBytes(userTypeRefer, struct);
        byte[] emptyBuf = toBytes(userTypeRefer, new User<>());
        Console.log(">correctness test passed!!!!!!!!<");
        Console.log("");
    }

    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[900];
        Arrays.fill(bytes, (byte) 67);

        StopWatch stopWatch = StopWatch.create("反序列");
        stopWatch.start();
        for (int i = 0; i < 1_000_000; i++) {
            You you = toStruct(youTypeRefer, bytes);
        }
        stopWatch.stop();
        Console.print(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}


