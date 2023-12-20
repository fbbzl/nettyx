import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.annotation.FieldHandler;
import org.fz.nettyx.annotation.Length;
import org.fz.nettyx.annotation.Struct;
import org.fz.nettyx.serializer.ByteBufHandler;
import org.fz.nettyx.serializer.TypedSerializer;
import org.fz.nettyx.serializer.type.c.signed.*;
import org.fz.nettyx.serializer.type.c.unsigned.Cuint;

import java.lang.reflect.Field;
import java.util.Arrays;



/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class TypedSerializerTest {

    public static void main(String[] args) {
        // these bytes may from nio, netty, input-stream, output-stream.....
        byte[] bytes = {12, -11, -45, -123, -67, -57, -90, -99, -11, -22, -78, -90, -45, -33, -67, -56, -67, -77, -7, -55, -66, -45, -77, -77, -45, -55, -62,
            -90, -45, -3, -111, -77, -55, -52, -56, -77, -45, -54, -45, -52, -23, -44, -35, -11, -78, -51, -112, -42, -22, -45, -33, -52, -45, -99, -33, -11, -66,
            -6, -78, -1, 48, 111, 12};

        User demo = TypedSerializer.read(Unpooled.wrappedBuffer(bytes), User.class);
        System.err.println("Read data: " + demo);

        final byte[] userWriteBytes = TypedSerializer.writeBytes(demo);
        System.err.println(userWriteBytes.length);

        System.err.println("Write data: " + Arrays.toString(userWriteBytes));
    }

    public static class InnerEntityHandler implements ByteBufHandler.ReadWriteHandler<TypedSerializer> {

        @Override
        public String doRead(TypedSerializer serializer, Field field) {
            return "■■■■■■this value may from DB or the other way■■■■■";
        }

        @Override
        public void doWrite(TypedSerializer serializer, Field field, Object value, ByteBuf add) {
            add.writeBytes(new byte[]{99, 99, 99, 99, 99});
        }
    }

    @Getter
    @Struct
    public static class Bill {

        @FieldHandler(InnerEntityHandler.class)
        private String bid;

        public void setBid(String bid) {
            this.bid = bid;
        }
    }

    @Getter
    @Setter
    @Struct
    public static class User {

        private Clong4 uid;//4
        private Cchar uname;//1
        private Cint isMarried;//2
        private Cchar sex;//1
        private Cfloat address;//4
        private Cdouble platformId;//2
        private Clong8 description;//2
        private Bill bill;//2
        @Length(2)
        private Cuint[] loginNames;// 26

        @Override
        public String toString() {
            return "User{" +
                "uid=" + uid.getValue() +
                ", uname=" + uname.getValue() +
                ", isMarried=" + isMarried.getValue() +
                ", sex=" + sex.getValue() +
                ", address=" + address.getValue() +
                ", platformId=" + platformId.getValue() +
                ", description=" + description.getValue() +
                ", bill=" + bill +
                ", loginNames=" + Arrays.toString(loginNames) +
                '}';
        }
    }
}
