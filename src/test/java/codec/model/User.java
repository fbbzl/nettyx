package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class User<T, W, G> {

    private Bom<T> b;

//    private Clong4 uid;
//    private Cchar uname;
//    private Cint isMarried;
//    private Cchar sex;
//    private Cfloat address;
//    private Cdouble platformId;
//    private Clong8 description;
//    private Culong8 interest;
//    private Bill bill;
//    private Cchar cchar;
//    private Cdouble cdouble;
//    private Cfloat cfloat;
//    private Cint cint;
//    private Clong4 clong4;
//    private Clong8 clong8;
//    private Clonglong clonglong;
//    private Cshort cshort;
//    private Cuchar cuchar;
//    private Cuint cuint;
//    private Culong4 culong4;
//    private Culong8 culong8;
//    private Culonglong culonglong;
//    private Cushort cushort;
//    private Cpp8tchar cpp8tchar;
//    private Cpp16tchar cpp16tchar;
//    private Cpp32tchar cpp32tchar;
//    private Cppdouble cppdouble;
//    private Cppfloat cppfloat;
//    private Cppint cppint;
//    private Cpplong4 cpplong4;
//    private Cpplong8 cpplong8;
//    private Cpplonglong cpplonglong;
//    private Cppshort cppshort;
//    private Cppuchar cppuchar;
//    private Cppuint cppuint;
//    private Cppulong4 cppulong4;
//    private Cppulong8 cppulong8;
//    private Cppulonglong cppulonglong;
//    private Cppushort cppushort;
//    private CppBool cppBool;

//    @ToArray(length = 100)
//    private Cppushort[] qqNames;
//
//    private T sonsbaba;
//    @ToLinkedList(size = 100)
//    private List<T> sons;
//    @ToArray(length = 100)
//    private T[] sons11;
//
//    private W wwife;
//    @ToArrayList(size = 100)
//    private List<W> wives;
//    @ToArray(length = 100)
//    private W[] wives121212;
//
//    @ToArray(length = 500)
//    private G[] gfs;
}