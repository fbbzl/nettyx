package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.Struct;

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
    Bom<T, Son<W, Bill>, GirlFriend>[] bs2d;

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