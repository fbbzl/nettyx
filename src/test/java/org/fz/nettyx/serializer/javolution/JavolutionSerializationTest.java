package org.fz.nettyx.serializer.javolution;

import cn.hutool.core.date.StopWatch;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JavolutionSerializationTest {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(JavolutionSerializationTest.class);

    @Test
    public void testDeserializeOneMillionTimes() throws IOException {
        // 创建一个 Student 对象并设置字段值
        JavolutionDemo.Student student = new JavolutionDemo.Student();

        // 序列化为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        student.write(baos);
        byte[] serializedData = baos.toByteArray();
        log.info("字节数组长度:" + serializedData.length);
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
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}
