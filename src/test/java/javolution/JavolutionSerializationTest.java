package javolution;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JavolutionSerializationTest {

    @Test
    public void testDeserializeOneMillionTimes() throws IOException {
        // 创建一个 Student 对象并设置字段值
        JavolutionDemo.Student student = new JavolutionDemo.Student();

        // 序列化为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        student.write(baos);
        byte[] serializedData = baos.toByteArray();
        Console.log("字节数组长度:" + serializedData.length);
        // 测试反序列化 100 万次
        StopWatch stopWatch = StopWatch.create("反序列");
        stopWatch.start("反序列化");
        for (int i = 0; i < 1_000_000; i++) {
            JavolutionDemo.Student deserializedStudent = new JavolutionDemo.Student();
            ByteArrayInputStream   bais                = new ByteArrayInputStream(serializedData);
            deserializedStudent.read(bais);
        }
        stopWatch.stop();

        // 计算总耗时
        Console.log(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}