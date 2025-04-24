package serializer;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.TypeUtil;
import codec.model.*;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;
import org.fz.nettyx.util.TypeRefer;
import org.fz.nettyx.util.TypeRefer.TypeTable;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static codec.UserCodec.TEST_USER;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
public class SerializerTest {
    static final TypeRefer<User<Bill, Wife<GirlFriend, Son<Clong4, Bill>>, Clong8>> userTypeRefer =
            new TypeRefer<>() {};

    static final TypeRefer<You> youCLass = new TypeRefer<>() {};

    private static final StructSerializerContext context =
            new StructSerializerContext("codec.model");

    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[64];
        Arrays.fill(bytes, (byte) 67);
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(TEST_USER.getClass());

        TypeTable lassTypeTable = youCLass.getTypeTable();
        StopWatch stopWatch = StopWatch.create("反序列");
        stopWatch.start();
        for (int i = 0; i < 1_000_000; i++) {
            StructSerializer.toStruct(youCLass, lassTypeTable, bytes);
        }
        stopWatch.stop();
        Console.print(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testHutool() throws NoSuchFieldException {
        Field sonsbaba         = User.class.getDeclaredField("sonsbaba");
        Type  fieldActualType1 = TypeUtil.getActualType(userTypeRefer, sonsbaba);

        StopWatch stopWatch = StopWatch.create("1111");
        stopWatch.start();
        for (int i = 0; i < 9_000_000; i++) {
            Type fieldActualType =
                    TypeUtil.getActualType(userTypeRefer, sonsbaba);
        }
        stopWatch.stop();
        Console.print(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }}


