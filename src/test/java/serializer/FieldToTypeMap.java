package serializer;

import codec.model.You;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldToTypeMap {

    public static Map<Type, Class<?>> getClassFieldsTypes(Class<?> clazz) {
        Map<String, Class<?>> typeMap = new HashMap<>();
        Field[]               fields  = clazz.getDeclaredFields();

        for (Field field : fields) {
            Type fieldType = field.getGenericType();
            if (fieldType instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?> rawClass) {
                    typeMap.put(rawClass.getSimpleName(), rawClass);
                }
                for (Type actualType : parameterizedType.getActualTypeArguments()) {
                    if (actualType instanceof Class<?> actualClass) {
                        typeMap.put(actualClass.getSimpleName(), actualClass);
                    }
                    else if (actualType instanceof ParameterizedType nestedParameterizedType) {
                        Type nestedRawType = nestedParameterizedType.getRawType();
                        if (nestedRawType instanceof Class<?> nestedRawClass) {
                            typeMap.put(nestedRawClass.getSimpleName(), nestedRawClass);
                        }
                    }
                }
            }
            else if (fieldType instanceof Class<?> fieldClass) {
                typeMap.put(fieldClass.getSimpleName(), fieldClass);
            }
        }
        return typeMap;
    }

    public static void main(String[] args) {
        class Example {
            private String               name;
            private int                  age;
            private List<String>         hobbies;
            private Map<String, Integer> scores;
        }

        Map<String, Class<?>> typeMap = getClassFieldsTypes(You.class);
        for (Map.Entry<String, Class<?>> entry : typeMap.entrySet()) {
            System.out.println("Type: " + entry.getKey() + ", Class: " + entry.getValue());
        }
    }
}