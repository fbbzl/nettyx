package codec.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.annotation.*;
import org.fz.nettyx.serializer.struct.basic.c.signed.*;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.*;
import org.fz.nettyx.serializer.struct.basic.cpp.CppBool;
import org.fz.nettyx.serializer.struct.basic.cpp.signed.*;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.*;

import java.util.List;

@Data
@Struct
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {

    @ToNamedEnum(enumType = Ccc.class, bufferLength = 2)
    Ccc cccEnum;

    Clong4 uid;
    @ToArray(length = 50)
    Cppushort[]     qqNames;
    @ToLinkedList(size = 50)
    List<Cppushort> ss;
    @ToArrayList(size = 50)
    List<Cppushort> tts;

    Bom<T, W, G> b;
    @ToArray(length = 50)
    Bom<T, W, Clong4>[]                         g111fs;
    @ToArrayList(size = 50)
    List<Bom<T, Son<Cuchar, Bill>, GirlFriend>> bs2d;

    T sonsbaba;
    @ToArray(length = 50)
    T[]     sonff;
    @ToArrayList(size = 50)
    List<T> so111ns;

    W wwife;
    @ToArrayList(size = 50)
    List<W> wives;
    @ToArray(length = 50)
    W[]     wives121212;

    Cchar        uname;
    Cint         isMarried;
    Cchar        sex;
    Cfloat       address;
    Cdouble      platformId;
    Clong8       description;
    Culong8      interest;
    Bill         bill;
    Cchar        cchar;
    Cdouble      cdouble;
    Cfloat       cfloat;
    Cint         cint;
    Clong4       clong4;
    Clong8       clong8;
    Clonglong    clonglong;
    Cshort       cshort;
    Cuchar       cuchar;
    Cuint        cuint;
    Culong4      culong4;
    Culong8      culong8;
    Culonglong   culonglong;
    Cushort      cushort;
    Cpp8tchar    cpp8tchar;
    Cpp16tchar   cpp16tchar;
    Cpp32tchar   cpp32tchar;
    Cppdouble    cppdouble;
    Cppfloat     cppfloat;
    Cppint       cppint;
    Cpplong4     cpplong4;
    Cpplong8     cpplong8;
    Cpplonglong  cpplonglong;
    Cppshort     cppshort;
    Cppuchar     cppuchar;
    Cppuint      cppuint;
    Cppulong4    cppulong4;
    Cppulong8    cppulong8;
    Cppulonglong cppulonglong;
    Cppushort    cppushort;
    CppBool      cppBool;

    public enum Ccc {
        TT,
        CC,
        ;
    }

}