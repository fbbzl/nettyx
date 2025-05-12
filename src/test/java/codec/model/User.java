package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned.cuint8_t;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppushort;

@Data
@Struct
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {

        @ToCharSequence(bufferLength = 2)
    String xname;
    clong4 uid;
    @ToArray(length = 5)
    cppushort[]     qqNames;
    @ToArray(length = 5)
    cppushort[] ss;
    @ToArray(length = 5)
    cppushort[] tts;

    Bom<T, W, G> b;
    @ToArray(length = 2)
    Bom<T, Son<W, Bill>, GirlFriend>[] bs2d;

    T sonsbaba;
    @ToArray(length = 5)
    T[]     sonff;

    W wwife;
    @ToArray(length = 5)
    W[]     wives121212;

    transient cuint8_t uname;

    @Ignore
    culong8 deviceS;
}