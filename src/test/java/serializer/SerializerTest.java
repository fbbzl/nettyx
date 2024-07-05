package serializer;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Dict;
import codec.model.*;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.xml.XmlSerializer;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
@Slf4j
public class SerializerTest {

   // @BeforeClass
    public static void initContext() {
        String u = "pc";
        File file = new File("C:\\Users\\" + u + "\\Desktop");
        File file2 = new File("C:\\Users\\" + u + "\\Desktop");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

    }

    @Test
    public void testXmlSerializer() {
        byte[] bytes = new byte[1024 * 2];
        Arrays.fill(bytes, (byte) 0);
        Model model1 = XmlSerializerContext.findModel("school", "student");
        Dict doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        Assert.assertNotNull(doc);
        Console.print(doc);
    }


   static final TypeRefer<User<Bill, Wife<GirlFriend, Son<Clong4, Bill>>, GirlFriend>> typeRefer = new TypeRefer<User<Bill,
            Wife<GirlFriend, Son<Clong4, Bill>>, GirlFriend>>() {
    };
    @Test
    public void testStructSerializer() {
        byte[] bytes = new byte[1024 * 6];
        Arrays.fill(bytes, (byte) 67);

        User user = StructSerializer.read(typeRefer, Unpooled.wrappedBuffer(bytes));

        Console.log("read :" + user);
//        user.setAddress(null);
//        user.setQqNames(null);
//        user.setQqNames(null);
//        user.setWives(null);
//        user.setSons(null);
//        user.setWives121212(null);
//        user.setWives(null);
//        user.setWwife(null);

        final byte[] userWriteBytes = StructSerializer.writeBytes(typeRefer, user);
        Console.log("userWriteBytes: " + userWriteBytes.length);
        User turn = StructSerializer.read(typeRefer, userWriteBytes);

        System.err.println(turn.equals(user));
        byte[] bytes1 = StructSerializer.writeBytes(typeRefer, turn);
        Console.log("bytes1: " + bytes1.length);
        Console.log(Arrays.toString(bytes1));
        Console.log("turn :" + turn);

        Assert.assertNotNull(turn);
    }

}
