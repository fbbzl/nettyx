package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.annotation.ToArrayList;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned.cuint8_t;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppushort;

import java.util.List;

@Data
@Struct
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {

    clong4 uid;
    @ToArray(length = 5)
    cppushort[]     qqNames;
    @ToArrayList(size = 5)
    List<cppushort> ss;
    @ToArrayList(size = 5)
    List<cppushort> tts;

    Bom<T, W, G> b;
    @ToArray(length = 5)
    Bom<T, W, clong4>[]                         g111fs;
    @ToArrayList(size = 5)
    List<Bom<T, Son<cuchar, Bill>, GirlFriend>> bs2d;

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

    transient cuint8_t uname;

    @Ignore
    culong8 deviceS;
}