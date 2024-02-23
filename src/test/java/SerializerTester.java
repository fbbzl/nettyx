import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Dict;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import model.GirlFriend;
import model.Son;
import model.User;
import model.Wife;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.xml.XmlSerializer;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.dtd.Model;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/23 21:35
 */
@Slf4j
public class SerializerTester {

    @BeforeClass
    public static void initContext() {
        String u = "fengbinbin";
        File file = new File("C:\\Users\\" + u + "\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\" + u + "\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        new StructSerializerContext("org.fz.nettyx");
    }

    @Test
    public void testXmlSerializer() {
        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 0);
        Model model1 = XmlSerializerContext.findModel("school", "student");
        Dict doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        Assert.assertNotNull(doc);
        Console.print(doc);
    }

    @Test
    public void testStructSerializer() {

        byte[] bytes = new byte[1024 * 1024];
        Arrays.fill(bytes, (byte) 1);
        TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>>() {
        };

        // these bytes may from nio, netty, input-stream, output-stream.....
        User<Son<Clong4, Clong4>, Wife, GirlFriend> user = StructSerializer.read(
                Unpooled.wrappedBuffer(bytes), typeRefer);

        System.err.println("read :" + user);
//            user.setAddress(null);
//            user.setLoginNames(null);
//            user.setQqNames(null);
//            user.setWives(null);
//            user.setSons(null);
//            user.setFirstWifes(null);
//            user.setBigSons(null);

        final ByteBuf userWriteBytes = StructSerializer.write(user, typeRefer);

//            User<Son<Clong4, Clong4>, Wife, Cchar, GirlFriend, Clong8> turn = StructSerializer.read(userWriteBytes, typeRefer);
//            turn = new User<>();

        //byte[] bytes1 = StructSerializer.writeBytes(turn, typeRefer);

        //System.err.println(Arrays.toString(bytes1));
        //   System.err.println("turn :" + turn);

        //  System.err.println(turn.equals(user));
    }

}
