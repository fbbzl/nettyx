package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.annotation.ToArrayList;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong4;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.Cppushort;

import java.util.List;

@Data
@Struct
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {



    Clong4 uid;
    @ToArray(length = 5)
    Cppushort[]     qqNames;
    @ToArrayList(size = 5)
    List<Cppushort> ss;
    @ToArrayList(size = 5)
    List<Cppushort> tts;

    Bom<T, W, G> b;
    @ToArray(length = 5)
    Bom<T, W, Clong4>[]                         g111fs;
    @ToArrayList(size = 5)
    List<Bom<T, Son<Cuchar, Bill>, GirlFriend>> bs2d;

    T sonsbaba;
    @ToArray(length = 5)
    T[]     sonff;
    @ToArrayList(size = 5)
    List<T> so111ns;

    W wwife;
    @ToArrayList(size = 5)
    List<W> wives;
    @ToArray(length = 5)
    W[]     wives121212;

    Cchar        uname;

}