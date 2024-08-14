package serializer;

import cn.hutool.core.lang.Console;
import codec.model.*;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
public class SerializerTest {
    static final TypeRefer<User<Bill, Wife<GirlFriend, Son<Clong4, Bill>>, Clong8>> userTypeRefer = new TypeRefer<User<Bill,
            Wife<GirlFriend, Son<Clong4, Bill>>, Clong8>>() {
    };

    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[1024 * 6];
        Arrays.fill(bytes, (byte) 67);

        User user = new User<>();

        Console.log("read :" + user);
        user.setAddress(null);
        user.setQqNames(null);
        user.setSs(null);
        user.setWives(null);
        user.setWives121212(null);
        user.setG111fs(null);
        user.setBs2d(null);
        user.setSonsbaba(null);
        user.setSonff(null);
        user.setSo111ns(null);
        user.setWwife(null);
        user.setWives(null);
        user.setWives121212(null);


        final byte[] userWriteBytes = StructSerializer.writeBytes(userTypeRefer, user);
        System.err.println("userWriteBytes: " + userWriteBytes.length);
        User turn = StructSerializer.read(userTypeRefer, userWriteBytes);

        System.err.println(turn.equals(user));
        byte[] bytes1 = StructSerializer.writeBytes(userTypeRefer, new User<>());
        System.err.println("bytes1: " + bytes1.length);
        Console.log(Arrays.toString(bytes1));
        Console.log("turn :" + turn);

        Assert.assertNotNull(turn);
    }

}
