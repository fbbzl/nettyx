package org.fz.nettyx.serializer.struct.comparison;

import cn.hutool.core.lang.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fz.nettyx.codec.model.Bill;
import org.fz.nettyx.codec.model.Bom;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.codec.model.Son;
import org.fz.nettyx.codec.model.User;
import org.fz.nettyx.codec.model.Wife;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.unsigned.culong8;
import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.fz.nettyx.serializer.struct.comparison.GenericTypeCandidates.Provider;
import static org.junit.Assert.assertTrue;

/**
 * Compares generic resolvers against the type resolution requirements of the struct serializer.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class GenericTypeContractsTest {

    public static final Type ROOT =
            new TypeReference<User<clong4, Wife<culong8, Son<clong4, Bill>>, GirlFriend>>() {}.getType();
    public static final Type EXPECTED_ELEMENT =
            new TypeReference<Bom<clong4, Son<Wife<culong8, Son<clong4, Bill>>, Bill>, GirlFriend>>() {}.getType();

    @Test
    public void comparableProvidersKeepTheFullSerializerContract() throws Exception {
        for (Provider provider : List.of(Provider.GEANTYREF, Provider.GUAVA)) {
            assertTrue(provider + " failed: " + inspect(provider), eligible(inspect(provider)));
        }
    }

    @Test
    public void allCandidatesProduceAnExplicitCapabilityResult() throws Exception {
        for (Provider provider : Provider.values()) assertTrue(inspect(provider).containsKey("eligible"));
    }

    public static Map<String, Object> inspect(Provider provider) throws Exception {
        Type field = User.class.getDeclaredField("bs2d").getGenericType();
        Type element = ((GenericArrayType) field).getGenericComponentType();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider.name());
        try {
            Type resolved = GenericTypeCandidates.resolve(provider, ROOT, element, User.class);
            boolean equivalent = EXPECTED_ELEMENT.equals(resolved) && resolved.equals(EXPECTED_ELEMENT)
                                 && EXPECTED_ELEMENT.hashCode() == resolved.hashCode();
            result.put("nestedTypeAndEquality", equivalent);
            result.put("actualNestedType", resolved.getTypeName());
            Type raw = Generic.class.getDeclaredField("value").getGenericType();
            Type inherited = GenericTypeCandidates.resolve(provider, Model.class, raw, Generic.class);
            result.put("staticOwnerDoesNotPolluteParent", inherited == cint.class);
            Type unbound = GenericTypeCandidates.resolve(provider, Generic.class, raw, Generic.class);
            result.put("rawVariablePreserved", unbound instanceof TypeVariable<?> && unbound.equals(raw));
            List<Type> repeats = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                repeats.add(GenericTypeCandidates.resolve(provider, ROOT, element, User.class));
            }
            result.put("equalKeysAfter100", new HashSet<>(repeats).size());
            result.put("eligible", equivalent && inherited == cint.class && unbound instanceof TypeVariable<?>
                                   && new HashSet<>(repeats).size() == 1);
        }
        catch (RuntimeException error) {
            result.put("eligible", false);
            result.put("unsupported", error.getClass().getName() + ": " + error.getMessage());
        }
        return result;
    }

    public static boolean eligible(Map<String, Object> result) {
        return Boolean.TRUE.equals(result.get("eligible"));
    }

    public static void main(String[] args) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Provider provider : Provider.values()) results.add(inspect(provider));
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(results);
        if (args.length == 0) System.out.println(json);
        else Files.writeString(Path.of(args[0]), json, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Declares a field whose type must be resolved from a concrete generic subclass.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class Generic<T> { public T value; }
    /**
     * Declares the nested generic interface used to test owner type handling.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class Container extends Generic<cchar> {
        /**
         * Declares a generic interface whose type argument differs from the enclosing class argument.
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2026-11-11
         */
        public interface Tag<T> {}
    }
    /**
     * Binds a generic base class and a nested interface to different type arguments.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class Model extends Generic<cint> implements Container.Tag<String> {}
}
