package protostuff;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import protostuff.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ProtostuffDemo {
    public static void main(String[] args) {

        // 创建 User 对象
        User<Bill, Wife<Son<Long, Bill>, Son<Long, Bill>>, Long> user = new User<>();
        user.setUid(0L);
        user.setQqNames(new Short[0]);
        user.setSs(new ArrayList<Short>());
        user.setTts(new ArrayList<Short>());
        user.setB(new Bom<>());
        user.setG111fs(new Bom[0]);
        user.setBs2d(Collections.emptyList());
        user.setSonsbaba(new Bill());
        user.setSonff(new Bill[0]);
        user.setSo111ns(new ArrayList<Bill>());
        user.setWwife(new Wife<>());
        user.setWives(Collections.emptyList());
        user.setWives121212(new Wife[0]);
        user.setUname('9');

        // 获取 Schema
        Schema<User> schema = RuntimeSchema.getSchema(User.class);

        // 序列化

        LinkedBuffer buffer = LinkedBuffer.allocate(1024);

        // 反序列化
        byte[] data = ProtostuffIOUtil.toByteArray(user, schema, buffer);
//        for (int i = 0; i < 1_000_000; i++) {
//            try {
//
//
//            } finally {
//                buffer.clear();
//            }
//        }
        System.err.println(data.length);
        StopWatch stopWatch = StopWatch.create("反序列");
        stopWatch.start();
        for (int i = 0; i < 1_000_000; i++) {
            ProtostuffIOUtil.mergeFrom(data, user, schema);
           // List age = newUser.getAge();
        }
        stopWatch.stop();

        Console.print(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}