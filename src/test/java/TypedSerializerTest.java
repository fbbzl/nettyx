import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArrayList;
import org.fz.nettyx.serializer.struct.annotation.ToBasicArray;
import org.fz.nettyx.serializer.struct.annotation.ToLinkedList;
import org.fz.nettyx.serializer.struct.annotation.ToString;
import org.fz.nettyx.serializer.struct.annotation.ToStructArray;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clonglong;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cshort;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuint;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong4;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culonglong;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cushort;
import org.fz.nettyx.serializer.struct.basic.cpp.CppBool;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpp16tchar;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpp32tchar;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpp8tchar;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cppdouble;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cppfloat;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cppint;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpplong4;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpplong8;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cpplonglong;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.Cppshort;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppuchar;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppuint;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppulong4;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppulong8;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppulonglong;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppushort;
import org.fz.nettyx.util.HexBins;
import org.fz.nettyx.util.TypeRefer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class TypedSerializerTest {

    public static void main(String[] args) {
        String x = CharSequenceUtil.repeat("11", 1024 * 1024);

        byte[] bytes = HexBins.decode(x);
        // bytes = HexBins.decode("11");
        long l = System.currentTimeMillis();
        int times = 1;
        for (int i = 0; i < times; i++) {
            TypeRefer<User<Son, Wife, Cchar, GirlFriend>> typeRefer = new TypeRefer<User<Son, Wife, Cchar, GirlFriend>>() {
            };

            // these bytes may from nio, netty, input-stream, output-stream.....
            User<Son, Wife, Cchar, GirlFriend> user = StructSerializer.read(Unpooled.wrappedBuffer(bytes), typeRefer);

          //  System.err.println("read :" + user);
//            user.setAddress(null);
//            user.setLoginNames(null);
//            user.setQqNames(null);
//            user.setWifes(null);
//            user.setSons(null);
//            user.setFirstWifes(null);
//            user.setBigSons(null);

            final ByteBuf userWriteBytes = StructSerializer.write(user, typeRefer);

            User<Son, Wife, Cchar, GirlFriend> turn = StructSerializer.read(userWriteBytes, typeRefer);
            turn = new User<>();

            byte[] bytes1 = StructSerializer.writeBytes(turn, typeRefer);

            System.err.println(Arrays.toString(bytes1));
            //   System.err.println("turn :" + turn);

          //  System.err.println(turn.equals(user));

        }
        BigDecimal l1 = new BigDecimal((System.currentTimeMillis() - l) / 1000 + "");
        System.err.println(l1);

        System.err.println(l1.divide(new BigDecimal(times + ""), 10, RoundingMode.HALF_UP));
    }

    @Data
    @Struct
    public static class Bill {

        private Cuchar bid;

        @Override
        public String toString() {
            return "Bill{" + "bid='" + bid + '\'' + '}';
        }
    }

    @Data
    @Struct
    public static class GirlFriend {

        @ToString(bufferLength = 2)
        private String cup;
    }

    @Data
    @Struct
    public static class Wife {

        @ToString(bufferLength = 2)
        private String name;
    }

    @Data
    @Struct
    public static class Son {

        @ToString(bufferLength = 2)
        private String name;

    }

    @Data
    @Struct
    public static class User<T, W, L, G> {

        private Clong4 uid;
        private Cchar uname;
        private Cint isMarried;
        private Cchar sex;
        private Cfloat address;
        private Cdouble platformId;
        private Clong8 description;
        private Culong8 interest;
        private Bill bill;
        private Cchar  cchar;
        private Cdouble cdouble;
        private Cfloat cfloat;
        private Cint cint;
        private Clong4 clong4;
        private Clong8 clong8;
        private Clonglong clonglong;
        private Cshort cshort;
        private Cuchar cuchar;
        private Cuint cuint;
        private Culong4 culong4;
        private Culong8 culong8;
        private Culonglong culonglong;
        private Cushort cushort;
        private Cpp8tchar cpp8tchar;
        private Cpp16tchar cpp16tchar;
        private Cpp32tchar cpp32tchar;
        private Cppdouble cppdouble;
        private Cppfloat cppfloat;
        private Cppint cppint;
        private Cpplong4 cpplong4;
        private Cpplong8 cpplong8;
        private Cpplonglong cpplonglong;
        private Cppshort cppshort;
        private Cppuchar cppuchar;
        private Cppuint cppuint;
        private Cppulong4 cppulong4;
        private Cppulong8 cppulong8;
        private Cppulonglong cppulonglong;
        private Cppushort cppushort;
        private CppBool cppBool;

        @ToBasicArray(length = 2)
        private L[] loginNames;
        @ToBasicArray(length = 2)
        private Cppushort[] qqNames;
        @ToArrayList(size = 2)
        private List<W> wifes;
        @ToLinkedList(size = 2)
        private List<T> sons;
        @ToStructArray(length = 2)
        private G[] gfs;

        private W firstWife;

    }
}
