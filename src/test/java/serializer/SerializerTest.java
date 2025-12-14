package serializer;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import codec.model.*;
import org.fz.erwin.lang.TypeRefer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
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

    static final         TypeRefer<You>          youTypeRefer = new TypeRefer<>() {};
    static final         TypeRefer<Brother>      brotherRefer = new TypeRefer<>() {};
    private static final StructSerializerContext context      =
            new StructSerializerContext("codec.model");

    @Before
    public void correctnessTest() {
        TypeRefer<User<Bill, Wife<Son<clong4, Bill>, Son<clong4, Bill>>, GirlFriend>> userTypeRefer =
                new TypeRefer<>() {};

        byte[] bytes = new byte[1200];
        Arrays.fill(bytes, (byte) 67);
        User   user     = toStruct(userTypeRefer, bytes);
        byte[] buf      = toBytes(userTypeRefer, user);
        byte[] emptyBuf = toBytes(userTypeRefer, new User<>());
        System.err.println(emptyBuf.length);

        byte[] bytes1 = {16,23,32,11,12,13,14,15,16,17,18,19,20,21,22,22,11,15,26,127,46,34,43,68,24,12,34,12,33,34,35,36,37,38,39,40,41,43,44,45,46,48,49,50,51,52,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79};

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
        struct.setIsMarried(new cint(1));
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


