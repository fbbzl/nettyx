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
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class StructSerializerTest {

    @Test
    public void test() {
        byte[] bytes = new byte[1024 * 1024];
        Arrays.fill(bytes, (byte) 1);
        TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>> typeRefer = new TypeRefer<User<Son<Clong4, Clong4>, Wife, GirlFriend>>() {
        };

        // these bytes may from nio, netty, input-stream, output-stream.....
        User<Son<Clong4, Clong4>, Wife, GirlFriend> user = StructSerializer.read(
                Unpooled.wrappedBuffer(bytes), typeRefer);

        System.err.println("read :" + user);
//            user.setAddress(null);
//            user.setLoginNames(null);
//            user.setQqNames(null);
//            user.setWives(null);
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
    public static class User<T, W, G> {

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
        private Cppushort[] qqNames;

        private T sonsbaba;
        @ToLinkedList(size = 50)
        private List<T> sons;
        @ToArray(length = 50)
        private T[] sons11;

        private W wwife;
        @ToArrayList(size = 50)
        private List<W> wives;
        @ToArray(length = 50)
        private W[] wives121212;

        @ToArray(length = 100)
        private G[] gfs;
    }
}
