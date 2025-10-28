package serializer;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.*;
import org.fz.erwin.lang.TypeRefer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
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

    static final         TypeRefer<You>          youTypeRefer = new TypeRefer<You>() {};
    static final         TypeRefer<Brother>      brotherRefer = new TypeRefer<Brother>() {};
    private static final StructSerializerContext context      =
            new StructSerializerContext("codec.model");

    @Before
    public void correctnessTest() {
        TypeRefer<User<Bill, Wife<Son<clong4, Bill>, Son<clong4, Bill>>, GirlFriend>> userTypeRefer =
                new TypeRefer<User<Bill, Wife<Son<clong4, Bill>, Son<clong4, Bill>>, GirlFriend>>() {};

        byte[] bytes = new byte[1200];
        Arrays.fill(bytes, (byte) 67);
        User   user     = toStruct(userTypeRefer, bytes);
        byte[] buf      = toBytes(userTypeRefer, user);
        byte[] emptyBuf = toBytes(userTypeRefer, new User<>());
        System.err.println(emptyBuf.length);
        byte[] bytes1 = new byte[16];
        Arrays.fill(bytes1, (byte) 67);
        Brother brother = toStruct(brotherRefer, bytes1);
        byte[]  bytes2  = toBytes(brotherRefer, brother);
        Console.log(">correctness test passed!!!!!!!!<");
        Console.log("");
    }

    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[900];
        Arrays.fill(bytes, (byte) 67);
        You struct = toStruct(youTypeRefer, bytes);
        struct.setC(null);
        struct.setChunk(null);
        byte[] bytes1 = toBytes(youTypeRefer, struct);
        System.err.println(bytes1.length);

        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = StopWatch.create("反序列");
            stopWatch.start();
            for (int j = 0; j < 1_000_000; j++) {
                toStruct(youTypeRefer, bytes);
            }
            stopWatch.stop();
            Console.print(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
    }
}


