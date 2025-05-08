package javolution;

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

        // 测试反序列化 100 万次
        long startTime = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            JavolutionDemo.Student deserializedStudent = new JavolutionDemo.Student();
            ByteArrayInputStream   bais                = new ByteArrayInputStream(serializedData);
            deserializedStudent.read(bais);
        }
        long endTime = System.nanoTime();

        // 计算总耗时
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("反序列化 100 万次的总耗时： " + duration + " 毫秒");
    }
}