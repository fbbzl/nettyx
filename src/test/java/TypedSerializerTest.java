import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.collection.ToArray;
import org.fz.nettyx.serializer.struct.annotation.collection.ToArrayList;
import org.fz.nettyx.serializer.struct.annotation.collection.ToHashSet;
import org.fz.nettyx.serializer.struct.annotation.collection.ToString;
import org.fz.nettyx.serializer.struct.c.signed.Cchar;
import org.fz.nettyx.serializer.struct.c.signed.Cdouble;
import org.fz.nettyx.serializer.struct.c.signed.Cfloat;
import org.fz.nettyx.serializer.struct.c.signed.Cint;
import org.fz.nettyx.serializer.struct.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.c.signed.Clong8;
import org.fz.nettyx.serializer.struct.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.struct.c.unsigned.Cuint;
import org.fz.nettyx.util.HexBins;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class TypedSerializerTest {

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        int times = 1_00_000;
        for (int i = 0; i < times; i++) {
            // these bytes may from nio, netty, input-stream, output-stream.....
            String x = "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
            User user = StructSerializer.read(HexBins.decode(x), User.class);

//            user.setUid(new Clong4(1));
//            user.setUname(null);
//            user.setIsMarried(null);
//            user.setSex(null);
//            user.setAddress(null);
//            user.setPlatformId(null);
//            user.setDescription(null);
//            user.setBill(null);
//            user.setLoginNames(new Cuint[]{new Cuint(1L), new Cuint(1L)});

            //final ByteBuf userWriteBytes = StructSerializer.write(user);
            //System.err.println(userWriteBytes.length);

            //System.err.println("Write data: " + Arrays.toString(userWriteBytes));

          //  User read = StructSerializer.read(userWriteBytes, User.class);
            // System.err.println(read);
        }
        BigDecimal l1 = new BigDecimal((System.currentTimeMillis() - l)/1000 + "");
        System.err.println(l1);

        System.err.println(l1.divide(new BigDecimal(times+""), 10, RoundingMode.HALF_UP));
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

    @Data
    @Struct
    public static class Wife {

        @ToString
        private String name;
    }

    @Data
    @Struct
    public static class Son {

        @ToString
        private String name;

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

        @ToArray(length = 2)
        private Cuint[] loginNames;// 26
        @ToArrayList(elementType = Wife.class)
        private List<Wife> wifes;
        @ToHashSet(elementType = Son.class)
        private Set<Son> sons;

        @Override
        public String toString() {
            return "User{" + "uid=" + uid + ", uname=" + uname + ", isMarried=" + isMarried + ", sex=" + sex
                + ", address=" + address + ", platformId=" + platformId + ", description=" + description + ", bill="
                + bill + ", loginNames=" + Arrays.toString(loginNames) + ", wifes=" + wifes + ", sons=" + sons + '}';
        }
    }
}
