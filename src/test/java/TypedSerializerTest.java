import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.annotation.Length;
import org.fz.nettyx.annotation.Struct;
import org.fz.nettyx.serializer.TypedSerializer;
import org.fz.nettyx.serializer.type.c.signed.Cchar;
import org.fz.nettyx.serializer.type.c.signed.Cdouble;
import org.fz.nettyx.serializer.type.c.signed.Cfloat;
import org.fz.nettyx.serializer.type.c.signed.Cint;
import org.fz.nettyx.serializer.type.c.signed.Clong4;
import org.fz.nettyx.serializer.type.c.signed.Clong8;
import org.fz.nettyx.serializer.type.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.type.c.unsigned.Cuint;




/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class TypedSerializerTest {

    public static void main(String[] args) {
        // these bytes may from nio, netty, input-stream, output-stream.....
        User user = new User();
        user.setUid(null);
        user.setUname(null);
        user.setIsMarried(null);
        user.setSex(null);
        user.setAddress(null);
        user.setPlatformId(null);
        user.setDescription(null);
        user.setBill(null);
        user.setLoginNames(null);

        final byte[] userWriteBytes = TypedSerializer.writeBytes(user);
        System.err.println(userWriteBytes.length);

        System.err.println("Write data: " + Arrays.toString(userWriteBytes));

        User read = TypedSerializer.read(userWriteBytes, User.class);
        System.err.println(read);

    }

    @Getter
    @Setter
    @Struct
    public static class Bill {

        //@FieldHandler(InnerEntityHandler.class)
        private Cuchar bid;

        @Override
        public String toString() {
            return "Bill{" + "bid='" + bid + '\'' + '}';
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
