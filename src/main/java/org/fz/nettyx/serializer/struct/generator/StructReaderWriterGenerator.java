package org.fz.nettyx.serializer.struct.generator;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.squareup.javapoet.*;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructHelper;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.Basic;

import javax.lang.model.element.Modifier;
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteOrder;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.hutool.core.collection.CollUtil.isEmpty;

/**
 * Runtime code generator for struct serializer using JavaPoet.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("all")
public final class StructReaderWriterGenerator
{

    private static final JavaCompiler                      COMPILER            = ToolProvider.getSystemJavaCompiler();
    private static final Map<Class<?>, StructReaderWriter> READER_WRITER_CACHE = MapUtil.newHashMap(128);

    private static final ClassName
            CN_STRUCT_READER_WRITER = ClassName.get(StructReaderWriter.class),
            CN_STRUCT_SERIALIZER    = ClassName.get(StructSerializer.class),
            CN_BYTE_BUF             = ClassName.get(ByteBuf.class),
            CN_TYPE                 = ClassName.get(Type.class),
            CN_CLASS                = ClassName.get(Class.class),
            CN_BYTE_ORDER           = ClassName.get(ByteOrder.class),
            CN_SUPPLIER             = ClassName.get(Supplier.class),
            CN_BICONSUMER           = ClassName.get(BiConsumer.class),
            CN_FUNCTION             = ClassName.get(Function.class),
            CN_STRUCT_FIELD         = ClassName.get(StructField.class),
            CN_STRUCT_FIELD_HANDLER = ClassName.get(StructFieldHandler.class),
            CN_BASIC                = ClassName.get(Basic.class),
            CN_ANNOTATION           = ClassName.get(Annotation.class),
            CN_TYPE_UTIL            = ClassName.get(cn.hutool.core.util.TypeUtil.class),
            CN_TO_ARRAY             = ClassName.get(ToArray.class),
            CN_TO_ARRAY_HANDLER     = ClassName.get(ToArray.ToArrayHandler.class),
            CN_STRUCT_HELPER        = ClassName.get(StructHelper.class);

    private record SourceDef(StructDefinition definition, String className, String source)
    {
    }

    public static StructReaderWriter getReaderWriter(StructDefinition definition)
    {
        StructReaderWriter readerWriter = READER_WRITER_CACHE.get(definition.type());
        if (readerWriter == null) {
            throw new IllegalStateException("reader-writer not generated for type: " + definition.type());
        }
        return readerWriter;
    }

    public static void generate(Collection<StructDefinition> definitions)
    {
        if (isEmpty(definitions)) return;

        List<SourceDef> sourceDefs = buildSourceDefs(definitions);
        if (isEmpty(sourceDefs)) return;

        Map<String, Class<?>> classes = compileAndLoad(sourceDefs);
        for (SourceDef sd : sourceDefs) {
            StructReaderWriter readerWriter = instantiateReaderWriter(classes.get(sd.className), sd.definition);
            READER_WRITER_CACHE.put(sd.definition.type(), readerWriter);
        }
    }

    private static List<SourceDef> buildSourceDefs(Collection<StructDefinition> definitions)
    {
        List<SourceDef> sourceDefs = new ArrayList<>(definitions.size());
        for (StructDefinition definition : definitions) {
            if (READER_WRITER_CACHE.containsKey(definition.type())) {
                continue;
            }
            Class<?> type       = definition.type();
            String   className  = type.getPackageName() + "." + type.getSimpleName() + "_StructReaderWriter";
            String   simpleName = type.getSimpleName() + "_StructReaderWriter";
            TypeSpec classSpec  = buildReaderWriterClass(definition, simpleName);
            String   source     = JavaFile.builder(type.getPackageName(), classSpec).build().toString();
            sourceDefs.add(new SourceDef(definition, className, source));
        }
        return sourceDefs;
    }

    private static Map<String, Class<?>> compileAndLoad(List<SourceDef> sourceDefs)
    {
        List<JavaFileObject> compilationUnits = new ArrayList<>(sourceDefs.size());
        StringBuilder        allSources       = new StringBuilder();
        for (SourceDef sd : sourceDefs) {
            compilationUnits.add(new JavaSourceFromString(sd.className, sd.source));
            allSources.append(sd.source).append("\n");
        }

        StandardJavaFileManager stdManager = COMPILER.getStandardFileManager(null, null, null);
        MemoryJavaFileManager   manager    = new MemoryJavaFileManager(stdManager);

        List<String>                        options     = CollUtil.newArrayList("-cp", System.getProperty("java.class.path"), "--release", "21");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task    = COMPILER.getTask(null, manager, diagnostics, options, null, compilationUnits);
        boolean                      success = task.call();
        if (!success) {
            StringBuilder err = new StringBuilder("batch compilation failed\n");
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                err.append(d).append("\n");
            }
            err.append("sources:\n").append(allSources);
            throw new RuntimeException(err.toString());
        }

        SingleClassLoader     loader = new SingleClassLoader(StructReaderWriterGenerator.class.getClassLoader());
        Map<String, Class<?>> result = MapUtil.newHashMap(sourceDefs.size());
        for (SourceDef sd : sourceDefs) {
            byte[] bytes = manager.getClassBytes(sd.className);
            if (bytes == null) {
                throw new RuntimeException("compiled class bytes not found: " + sd.className);
            }
            result.put(sd.className, loader.defineClass(sd.className, bytes));
        }
        return result;
    }

    private static TypeSpec buildReaderWriterClass(StructDefinition definition, String simpleName)
    {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(simpleName)
                                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                                .addSuperinterface(CN_STRUCT_READER_WRITER);

        addConstructorAndFields(classBuilder, definition.fields());
        classBuilder.addMethod(buildReadMethod(definition.fields()));
        classBuilder.addMethod(buildWriteMethod(definition.fields()));

        return classBuilder.build();
    }

    private static void addConstructorAndFields(TypeSpec.Builder classBuilder, StructField[] fields)
    {
        MethodSpec.Builder ctorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        addClassField(classBuilder, ctorBuilder, "constructor", CN_SUPPLIER);

        for (int i = 0; i < fields.length; i++) {
            addClassField(classBuilder, ctorBuilder, "setter" + i, CN_BICONSUMER);
            addClassField(classBuilder, ctorBuilder, "getter" + i, CN_FUNCTION);
            addClassField(classBuilder, ctorBuilder, "field" + i, CN_STRUCT_FIELD);

            if (fields[i].category() == StructField.Category.HANDLER) {
                addClassField(classBuilder, ctorBuilder, "handler" + i, CN_STRUCT_FIELD_HANDLER);
            }
        }

        classBuilder.addMethod(ctorBuilder.build());
    }

    private static void addClassField(
            TypeSpec.Builder classBuilder,
            MethodSpec.Builder ctorBuilder,
            String name,
            ClassName type)
    {
        classBuilder.addField(FieldSpec.builder(type, name, Modifier.PRIVATE, Modifier.FINAL).build());
        ctorBuilder.addParameter(type, name);
        ctorBuilder.addStatement("this.$N = $N", name, name);
    }

    private static MethodSpec buildReadMethod(StructField[] fields)
    {
        MethodSpec.Builder readMethod =
                MethodSpec.methodBuilder("read")
                          .addModifiers(Modifier.PUBLIC)
                          .addAnnotation(Override.class)
                          .returns(Object.class)
                          .addParameter(CN_STRUCT_SERIALIZER, "serializer")
                          .addParameter(CN_TYPE, "root")
                          .addParameter(CN_TYPE, "structType")
                          .addParameter(CN_BYTE_BUF, "buf");

        readMethod.addStatement("Object struct = constructor.get()");
        readMethod.addStatement("$T actualStructType = (structType instanceof $T) ? structType : $T.getActualType(root, structType)",
                                CN_TYPE, CN_CLASS, CN_TYPE_UTIL);

        for (int i = 0; i < fields.length; i++) {
            readMethod.addCode("{\n");
            buildFieldRead(readMethod, fields[i], i);
            readMethod.addCode("}\n");
        }

        readMethod.addStatement("return struct");
        return readMethod.build();
    }

    private static void buildFieldRead(MethodSpec.Builder readMethod, StructField field, int i)
    {
        String vName = "v" + i;

        if (field.category() == StructField.Category.BASIC) {
            buildBasicFieldRead(readMethod, field, i, vName);
        } else if (field.category() == StructField.Category.STRUCT) {
            buildStructFieldRead(readMethod, field, i, vName);
        } else {
            buildHandlerFieldRead(readMethod, field, i, vName);
        }

        readMethod.addStatement("setter$L.accept(struct, $N)", i, vName);
    }

    private static void buildBasicFieldRead(MethodSpec.Builder readMethod, StructField field, int i, String vName)
    {
        Class<?> basicClass = basicClass(field);
        if (!Basic.class.isAssignableFrom(basicClass)) {
            throw new IllegalArgumentException("basic field must be Basic type, field: [" + field + "], actual type: [" + basicClass + "]");
        }
        ClassName cn = ClassName.get(basicClass);
        readMethod.addStatement("$T $N = new $T(buf, $T.$L)", Object.class, vName, cn, CN_BYTE_ORDER,
                                byteOrderFieldName(field));
    }

    private static void buildStructFieldRead(MethodSpec.Builder readMethod, StructField field, int i, String vName)
    {
        Class<?> fieldClass = fieldClass(field);
        if (fieldClass != null) {
            readMethod.addStatement("$T $N = serializer.readStruct($T.class, buf)", Object.class, vName, ClassName.get(fieldClass));
        } else {
            String ftName = "ft" + i;
            readMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
            readMethod.addStatement("$T $N = serializer.readStruct($N, buf)", Object.class, vName, ftName);
        }
    }

    private static void buildHandlerFieldRead(MethodSpec.Builder readMethod, StructField field, int i, String vName)
    {
        Annotation ann = field.annotation();
        if (ann instanceof ToArray) {
            buildToArrayRead(readMethod, field, i, vName);
        } else if (ann instanceof Ignore) {
            readMethod.addStatement("$T $N = null", Object.class, vName);
        } else {
            String ftName = "ft" + i;
            readMethod.addStatement("$T ann$L = field$L.annotation()", CN_ANNOTATION, i, i);
            Class<?> fieldClass = fieldClass(field);
            if (fieldClass != null) {
                readMethod.addStatement("$T $N = handler$L.doRead(serializer, root, struct, field$L, $T.class, buf, ann$L)",
                                        Object.class, vName, i, i, ClassName.get(fieldClass), i);
            } else {
                readMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
                readMethod.addStatement("$T $N = handler$L.doRead(serializer, root, struct, field$L, $N, buf, ann$L)",
                                        Object.class, vName, i, i, ftName, i);
            }
        }
    }

    private static void buildToArrayRead(MethodSpec.Builder readMethod, StructField field, int i, String vName)
    {
        ToArray  toArray                  = (ToArray) field.annotation();
        int      toArrayLength            = toArray.length();
        boolean  toArrayFlexible          = toArray.flexible();
        Class<?> determinedComponentClass = determineComponentClass(field);

        readMethod.addStatement("$T ann$L = field$L.annotation()", CN_ANNOTATION, i, i);
        if (determinedComponentClass != null && Basic.class.isAssignableFrom(determinedComponentClass)) {
            readMethod.addStatement("$T $N = serializer.readBasicArray($T.class, $T.$L, buf, $L, $L)",
                                    Object.class, vName, ClassName.get(determinedComponentClass), CN_BYTE_ORDER,
                                    byteOrderFieldName(field),
                                    toArrayLength, toArrayFlexible);
        } else if (determinedComponentClass != null && AnnotationUtil.hasAnnotation(determinedComponentClass, Struct.class)) {
            readMethod.addStatement("$T $N = serializer.readStructArray($T.class, buf, $L, $L)",
                                    Object.class, vName, ClassName.get(determinedComponentClass), toArrayLength, toArrayFlexible);
        } else {
            String ftName   = "ft" + i;
            String ctName   = "ct" + i;
            String lenName  = "len" + i;
            String flexName = "flex" + i;
            readMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
            readMethod.addStatement("$T $N = $T.getComponentType(root, $N)", CN_TYPE, ctName, CN_TO_ARRAY_HANDLER, ftName);
            readMethod.addStatement("int $N = $L", lenName, toArrayLength);
            readMethod.addStatement("boolean $N = $L", flexName, toArrayFlexible);
            readMethod.addStatement("$T $N", Object.class, vName);
            readMethod.beginControlFlow("if (serializer.isBasic($N))", ctName);
            readMethod.addStatement("$N = serializer.readBasicArray((Class) $N, $T.$L, buf, $N, $N)",
                                    vName, ctName, CN_BYTE_ORDER,
                                    byteOrderFieldName(field),
                                    lenName, flexName);
            readMethod.nextControlFlow("else");
            readMethod.addStatement("$N = serializer.readStructArray($N, buf, $N, $N)",
                                    vName, ctName, lenName, flexName);
            readMethod.endControlFlow();
        }
    }

    private static MethodSpec buildWriteMethod(StructField[] fields)
    {
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                                                   .addModifiers(Modifier.PUBLIC)
                                                   .addAnnotation(Override.class)
                                                   .addParameter(CN_STRUCT_SERIALIZER, "serializer")
                                                   .addParameter(CN_TYPE, "root")
                                                   .addParameter(CN_TYPE, "structType")
                                                   .addParameter(Object.class, "struct")
                                                   .addParameter(CN_BYTE_BUF, "buf");

        writeMethod.addStatement("$T actualStructType = (structType instanceof $T) ? structType : $T.getActualType(root, structType)",
                                 CN_TYPE, CN_CLASS, CN_TYPE_UTIL);

        for (int i = 0; i < fields.length; i++) {
            writeMethod.addCode("{\n");
            buildFieldWrite(writeMethod, fields[i], i);
            writeMethod.addCode("}\n");
        }

        return writeMethod.build();
    }

    private static void buildFieldWrite(MethodSpec.Builder writeMethod, StructField field, int i)
    {
        String vName = "v" + i;
        writeMethod.addStatement("$T $N = getter$L.apply(struct)", Object.class, vName, i);

        if (field.category() == StructField.Category.BASIC) {
            buildBasicFieldWrite(writeMethod, field, vName);
        } else if (field.category() == StructField.Category.STRUCT) {
            buildStructFieldWrite(writeMethod, field, i, vName);
        } else {
            buildHandlerFieldWrite(writeMethod, field, i, vName);
        }
    }

    private static void buildBasicFieldWrite(MethodSpec.Builder writeMethod, StructField field, String vName)
    {
        Class<?>  basicClass = basicClass(field);
        ClassName cn         = ClassName.get(basicClass);
        writeMethod.beginControlFlow("if ($N != null)", vName);
        writeMethod.addStatement("$T typed = ($T) $N", cn, cn, vName);
        writeMethod.beginControlFlow("if (typed.value() != null)");
        writeMethod.addStatement("typed.write(buf, $T.$L)", CN_BYTE_ORDER, byteOrderFieldName(field));
        writeMethod.nextControlFlow("else");
        writeMethod.addStatement("buf.writeZero(typed.size())");
        writeMethod.endControlFlow();
        writeMethod.nextControlFlow("else");
        writeMethod.addStatement("buf.writeZero($T.findBasicSize($T.class))", CN_STRUCT_HELPER, cn);
        writeMethod.endControlFlow();
    }

    private static void buildStructFieldWrite(MethodSpec.Builder writeMethod, StructField field, int i, String vName)
    {
        Class<?> fieldClass = fieldClass(field);
        if (fieldClass != null) {
            ClassName cn = ClassName.get(fieldClass);
            writeMethod.beginControlFlow("if ($N != null)", vName);
            writeMethod.addStatement("serializer.writeStruct($T.class, $N, buf)", cn, vName);
            writeMethod.nextControlFlow("else");
            writeMethod.addStatement("serializer.writeStruct($T.class, $T.newStruct($T.class), buf)", cn, CN_STRUCT_HELPER, cn);
            writeMethod.endControlFlow();
        } else {
            String ftName = "ft" + i;
            writeMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
            writeMethod.beginControlFlow("if ($N != null)", vName);
            writeMethod.addStatement("serializer.writeStruct($N, $N, buf)", ftName, vName);
            writeMethod.nextControlFlow("else");
            writeMethod.addStatement("serializer.writeStruct($N, $T.newStruct($N), buf)", ftName, CN_STRUCT_HELPER, ftName);
            writeMethod.endControlFlow();
        }
    }

    private static void buildHandlerFieldWrite(MethodSpec.Builder writeMethod, StructField field, int i, String vName)
    {
        Annotation ann = field.annotation();
        writeMethod.addStatement("$T ann$L = field$L.annotation()", CN_ANNOTATION, i, i);
        if (ann instanceof ToArray) {
            buildToArrayWrite(writeMethod, field, i, vName);
        } else if (ann instanceof Ignore) {
            // skip
        } else {
            Class<?> fieldClass = fieldClass(field);
            if (fieldClass != null) {
                writeMethod.addStatement("handler$L.doWrite(serializer, root, struct, field$L, $T.class, $N, buf, ann$L)",
                                         i, i, ClassName.get(fieldClass), vName, i);
            } else {
                String ftName = "ft" + i;
                writeMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
                writeMethod.addStatement("handler$L.doWrite(serializer, root, struct, field$L, $N, $N, buf, ann$L)",
                                         i, i, ftName, vName, i);
            }
        }
    }

    private static void buildToArrayWrite(MethodSpec.Builder writeMethod, StructField field, int i, String vName)
    {
        ToArray  toArray                  = (ToArray) field.annotation();
        int      toArrayLength            = toArray.length();
        boolean  toArrayFlexible          = toArray.flexible();
        Class<?> determinedComponentClass = determineComponentClass(field);

        if (determinedComponentClass != null && Basic.class.isAssignableFrom(determinedComponentClass)) {
            writeMethod.addStatement("serializer.writeBasicArray(($T[]) $N, $T.findBasicSize($T.class), $L, buf, $L, $T.$L)",
                                     CN_BASIC, vName, CN_STRUCT_HELPER, ClassName.get(determinedComponentClass),
                                     toArrayLength, toArrayFlexible, CN_BYTE_ORDER,
                                     byteOrderFieldName(field));
        } else if (determinedComponentClass != null && AnnotationUtil.hasAnnotation(determinedComponentClass, Struct.class)) {
            writeMethod.addStatement("serializer.writeStructArray($N, $T.class, $L, buf, $L)",
                                     vName, ClassName.get(determinedComponentClass), toArrayLength, toArrayFlexible);
        } else {
            String ftName   = "ft" + i;
            String ctName   = "ct" + i;
            String lenName  = "len" + i;
            String flexName = "flex" + i;
            writeMethod.addStatement("$T $N = field$L.type(actualStructType)", CN_TYPE, ftName, i);
            writeMethod.addStatement("$T $N = $T.getComponentType(root, $N)", CN_TYPE, ctName, CN_TO_ARRAY_HANDLER, ftName);
            writeMethod.addStatement("int $N = $L", lenName, toArrayLength);
            writeMethod.addStatement("boolean $N = $L", flexName, toArrayFlexible);
            writeMethod.beginControlFlow("if (serializer.isBasic($N))", ctName);
            writeMethod.addStatement("serializer.writeBasicArray(($T[]) $N, $T.findBasicSize($N), $N, buf, $N, $T.$L)",
                                     CN_BASIC, vName, CN_STRUCT_HELPER, ctName, lenName, flexName, CN_BYTE_ORDER,
                                     byteOrderFieldName(field));
            writeMethod.nextControlFlow("else");
            writeMethod.addStatement("serializer.writeStructArray($N, $N, $N, buf, $N)",
                                     vName, ctName, lenName, flexName);
            writeMethod.endControlFlow();
        }
    }

    private static StructReaderWriter instantiateReaderWriter(Class<?> clazz, StructDefinition definition)
    {
        try {
            StructField[]  fields        = definition.fields();
            List<Class<?>> paramTypeList = new ArrayList<>();
            List<Object>   argList       = new ArrayList<>();
            paramTypeList.add(Supplier.class);
            argList.add(definition.constructor());
            for (StructField field : fields) {
                paramTypeList.add(BiConsumer.class);
                argList.add(field.setter());
                paramTypeList.add(Function.class);
                argList.add(field.getter());
                paramTypeList.add(StructField.class);
                argList.add(field);
                if (field.category() == StructField.Category.HANDLER) {
                    paramTypeList.add(StructFieldHandler.class);
                    argList.add(field.handler());
                }
            }
            Class<?>[]                       paramTypes = paramTypeList.toArray(new Class[0]);
            Object[]                         args       = argList.toArray();
            java.lang.reflect.Constructor<?> ctor       = clazz.getConstructor(paramTypes);
            return (StructReaderWriter) ctor.newInstance(args);
        }
        catch (Exception e) {
            throw new RuntimeException("failed to instantiate generated reader-writer for " + definition.type(), e);
        }
    }

    private static Class<?> fieldClass(StructField field)
    {
        Type fieldType = field.wrapped().getGenericType();
        if (fieldType instanceof Class<?> clazz && !clazz.isArray()) {
            return clazz;
        }
        return null;
    }

    private static Class<?> determineComponentClass(StructField field)
    {
        Type fieldType = field.wrapped().getGenericType();
        if (fieldType instanceof Class<?> clazz && clazz.isArray()) {
            return clazz.getComponentType();
        }
        return null;
    }

    private static Class<?> basicClass(StructField field)
    {
        Type raw = field.wrapped().getGenericType();
        return raw instanceof Class<?> ? (Class<?>) raw : Object.class;
    }

    private static String byteOrderFieldName(StructField field)
    {
        return field.byteOrder() == ByteOrder.BIG_ENDIAN ? "BIG_ENDIAN" : "LITTLE_ENDIAN";
    }

    private static final class JavaSourceFromString extends SimpleJavaFileObject
    {
        private final String code;

        JavaSourceFromString(String name, String code)
        {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
        {
            return code;
        }
    }

    private static final class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
    {
        private final Map<String, ByteArrayOutputStream> classOutputs = MapUtil.newHashMap();

        MemoryJavaFileManager(StandardJavaFileManager delegate)
        {
            super(delegate);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            classOutputs.put(className, baos);
            return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind)
            {
                @Override
                public OutputStream openOutputStream()
                {
                    return baos;
                }
            };
        }

        byte[] getClassBytes(String className)
        {
            ByteArrayOutputStream baos = classOutputs.get(className);
            return baos == null ? null : baos.toByteArray();
        }
    }

    private static final class SingleClassLoader extends SecureClassLoader
    {
        SingleClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        Class<?> defineClass(String name, byte[] bytes)
        {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
