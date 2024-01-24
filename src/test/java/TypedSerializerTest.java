import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.TypeRefer;
import org.fz.nettyx.serializer.struct.annotation.*;
import org.fz.nettyx.serializer.struct.basic.c.signed.*;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.*;
import org.fz.nettyx.serializer.struct.basic.cpp.CppBool;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.*;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.*;
import org.fz.nettyx.util.HexBins;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
        int times = 10_000;
        TypeRefer<User<Son<Clong4, Clong4>, Wife, Wife, GirlFriend, Wife>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, Wife, GirlFriend, Wife>>() {
        };
        for (int i = 0; i < times; i++) {

            // these bytes may from nio, netty, input-stream, output-stream.....
            User<Son<Clong4, Clong4>, Wife, Wife, GirlFriend, Wife> user = StructSerializer.read(
                Unpooled.wrappedBuffer(bytes), typeRefer);

            //  System.err.println("read :" + user);
//            user.setAddress(null);
//            user.setLoginNames(null);
//            user.setQqNames(null);
//            user.setWifes(null);
//            user.setSons(null);
//            user.setFirstWifes(null);
//            user.setBigSons(null);

            final ByteBuf userWriteBytes = StructSerializer.write(user, typeRefer);

//            User<Son<Clong4, Clong4>, Wife, Cchar, GirlFriend, Clong8> turn = StructSerializer.read(userWriteBytes, typeRefer);
//            turn = new User<>();

            //byte[] bytes1 = StructSerializer.writeBytes(turn, typeRefer);

            //System.err.println(Arrays.toString(bytes1));
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
    public static class Son<B, Y> {

        private B name;
        private Y sonOrder;

    }

    @Data
    @Struct
    public static class User<T, W, L, G, R> {

        private Clong4 uid;
        private Cchar uname;
        private Cint isMarried;
        private Cchar sex;
        private Cfloat address;
        private Cdouble platformId;
        private Clong8 description;
        private Culong8 interest;
        private Bill bill;
        private Cchar cchar;
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

        @ToArray(length = 2)
        private L[] loginNames;
        @ToArray(length = 2)
        private Cppushort[] qqNames;

        private W wwife;
        @ToArrayList(size = 2)
        private List<W> wifes;
        @ToArray(length = 2)
        private W[] wifes121212;

        private T sons11111111;
        @ToLinkedList(size = 2)
        private List<T> sons;
        @ToArray(length = 2)
        private T[] sons11;

        @ToArray(length = 2)
        private G[] gfs;

        private R firstWife;

        private R firstWife2;
        private R firstWif213e;
        private R fi213rstWife1;
        private R first23Wife2;
        private R f23irstWife;
        private R figfrstWife1;
        private R firsasdftWife2;
        private R firsasdftWife;
        private R firstWifasdfe1;
        private R firstffWife2;
        private R firstfWife;
        private R asdf;
        private R firstdsfWife2;
        private R firstdfWife;
        private R firstasdfWife1;
        private R firstsdWife2;
        private R firstWdfdfife;
        private R firstdfdfWife1;
        private R firstzxcvWife2;
        private R firsmtWife;
        private R firstWhhife1;
        private R firsmnbmtWife2;

        private R fitrstfWife;
        private R ashdf;
        private R first324fgdsfWife2;
        private R firstdfd45fWife;
        private R firstasdffWife1;
        private R firstsdasdfWife2;
        private R firstWdasdffdfife;
        private R firstdasdffdfWife1;
        private R firstzasdfxcvWife2;
        private R firsmtasdfWife;
        private R firstWhhasdfife1;
        private R firsmnbmasdftWife2;

        @ToArray(length = 100)
        private R[] firstasdfsdasdfWife2;
        @ToArray(length = 100)
        private R[] firstfdsfgWdasdffdfife;
        @ToArray(length = 100)
        private R[] firstdasererdffdfWife1;
        @ToArray(length = 100)
        private R[] firstzaefesdfxcvWife2;
        @ToArray(length = 100)
        private R[] firsmtasderdffWife;
        @ToArray(length = 100)
        private R[] firstWhhasdfdfdfife1;
        @ToArrayList(size = 100)
        private List<R> firsmnbdfmasdftWife2;
    }
}
