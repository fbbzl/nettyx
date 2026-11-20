package org.fz.nettyx.serializer.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.fz.nettyx.serializer.protostuff.model.*;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Builds a sample user, serializes it with a Protostuff runtime schema, and prints the byte count.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025-05-08
 */
public class ProtostuffDemo {
    public static void main(String[] args) {

        // Create the User object
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

        // Resolve the schema
        Schema<User> schema = RuntimeSchema.getSchema(User.class);

        // Allocate the serialization buffer

        LinkedBuffer buffer = LinkedBuffer.allocate(1024);

        // Serialize the object
        byte[] data = ProtostuffIOUtil.toByteArray(user, schema, buffer);
        System.err.println(data.length);
    }
}
