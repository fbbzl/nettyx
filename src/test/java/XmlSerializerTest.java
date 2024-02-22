import cn.hutool.core.lang.Dict;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.util.Arrays;
import org.fz.nettyx.serializer.xml.XmlSerializer;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.dtd.Model;
import org.junit.Test;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/22 16:44
 */

public class XmlSerializerTest {


    @Test
    public void test() {
        test("pc");
    }

    public void test(String u) {
        File file = new File("C:\\Users\\" + u + "\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\" + u + "\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 0);
        Model model1 = XmlSerializerContext.findModel("school", "student");
        Dict doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc);
    }

}
