package protostuff.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data

@FieldDefaults(level = AccessLevel.PRIVATE)
public class User<T, W, G> {

    Long uid;

    Short[]     qqNames;

    List<Short> ss;

    List<Short> tts;

    Bom<T, W, G> b;

    Bom<T, W, Long>[]                         g111fs;

    List<Bom<T, Son<Character, Bill>, GirlFriend>> bs2d;

    T sonsbaba;

    T[]     sonff;

    List<T> so111ns;

    W wwife;

    List<W> wives;

    W[]     wives121212;

    Character        uname;

}