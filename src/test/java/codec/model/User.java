package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;

@Data
@Struct
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {

    //    @ToCharSequence(bufferLength = 2)
//    String xname;
//    clong4 uid;
//    @ToArray(length = 5)
//    cppushort[]     qqNames;
//    @ToArray(length = 5)
//    List<cppushort> ss;
//    @ToArray(length = 5)
//    List<cppushort> tts;
//
//    Bom<T, W, G> b;
//    @ToArray(length = 5)
//    Bom<T, W, clong4>[]                         g111fs;
    @ToArray(length = 2)
    Bom<T, Son<cuchar, Bill>, GirlFriend>[] bs2d;

//    T sonsbaba;
//    @ToArray(length = 5)
//    T[]     sonff;
//    @ToArray(length = 5)
//    List<T> so111ns;
//
//    W wwife;
//    @ToArray(length = 5)
//    List<W> wives;
//    @ToArray(length = 5)
//    W[]     wives121212;
//
//    transient cuint8_t uname;
//
//    @Ignore
//    culong8 deviceS;
}