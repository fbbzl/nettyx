package serializer;

import cn.hutool.core.util.ArrayUtil;
import codec.model.*;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
public class SerializerTest {
    static final TypeRefer<User<Bill, Wife<GirlFriend, Son<Clong4, Bill>>, Clong8>> userTypeRefer =
            new TypeRefer<User<Bill,
                    Wife<GirlFriend, Son<Clong4, Bill>>, Clong8>>() {
            };

    private static final StructSerializerContext context = new StructSerializerContext("codec.model");

    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[9999];
        Arrays.fill(bytes, (byte) 67);

        User turn = StructSerializer.toStruct(userTypeRefer, bytes);
        System.err.println(turn.getBill().getBid().getValue());
        final byte[] userWriteBytes = StructSerializer.toBytes(userTypeRefer, turn);

        System.err.println(ArrayUtil.equals(bytes, userWriteBytes));
    }

    public void setNullForTest(User user) {
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
    }
}
