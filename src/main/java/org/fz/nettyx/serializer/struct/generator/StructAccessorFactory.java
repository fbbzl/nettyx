package org.fz.nettyx.serializer.struct.generator;

import cn.hutool.core.annotation.AnnotationUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructHelper;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates and caches direct constructor/getter/setter StructAccessor implementations with ASM.
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 * */
public final class StructAccessorFactory {

    private static volatile Map<Class<?>, StructAccessor> STRUCT_ACCESSOR_CACHE = Map.of();

    private static final String ACCESSOR_INTERNAL        = org.objectweb.asm.Type.getInternalName(StructAccessor.class);
    private static final String SERIALIZER_INTERNAL      = org.objectweb.asm.Type.getInternalName(StructSerializer.class);
    private static final String BYTE_BUF_INTERNAL        = org.objectweb.asm.Type.getInternalName(ByteBuf.class);
    private static final String SUPPORT_INTERNAL         = org.objectweb.asm.Type.getInternalName(StructAccessor.Support.class);
    private static final String FIELD_INTERNAL           = org.objectweb.asm.Type.getInternalName(StructField.class);
    private static final String HANDLER_INTERNAL         = org.objectweb.asm.Type.getInternalName(StructFieldHandler.class);
    private static final String BASIC_INTERNAL           = org.objectweb.asm.Type.getInternalName(Basic.class);
    private static final String BYTE_ORDER_INTERNAL      = org.objectweb.asm.Type.getInternalName(ByteOrder.class);
    private static final String STRUCT_HELPER_INTERNAL   = org.objectweb.asm.Type.getInternalName(StructHelper.class);
    private static final String TYPE_DESCRIPTOR          = org.objectweb.asm.Type.getDescriptor(Type.class);
    private static final String BYTE_ORDER_DESCRIPTOR    = org.objectweb.asm.Type.getDescriptor(ByteOrder.class);
    private static final String FIELD_ARRAY_DESCRIPTOR   = "[L" + FIELD_INTERNAL + ";";
    private static final String HANDLER_ARRAY_DESCRIPTOR = "[L" + HANDLER_INTERNAL + ";";
    private static final String BASIC_ARRAY_DESCRIPTOR   = "[L" + BASIC_INTERNAL + ";";

    private static final String RESOLVE_DESCRIPTOR = org.objectweb.asm.Type.getMethodDescriptor(
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(Type.class));

    private static final String READ_FIELD_DESCRIPTOR = org.objectweb.asm.Type.getMethodDescriptor(
            org.objectweb.asm.Type.getType(Object.class),
            org.objectweb.asm.Type.getType(StructSerializer.class),
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(Object.class),
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(StructField.class),
            org.objectweb.asm.Type.getType(StructFieldHandler.class),
            org.objectweb.asm.Type.getType(ByteBuf.class));

    private static final String WRITE_FIELD_DESCRIPTOR = org.objectweb.asm.Type.getMethodDescriptor(
            org.objectweb.asm.Type.VOID_TYPE,
            org.objectweb.asm.Type.getType(StructSerializer.class),
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(Object.class),
            org.objectweb.asm.Type.getType(Type.class),
            org.objectweb.asm.Type.getType(StructField.class),
            org.objectweb.asm.Type.getType(StructFieldHandler.class),
            org.objectweb.asm.Type.getType(Object.class),
            org.objectweb.asm.Type.getType(ByteBuf.class));

    private StructAccessorFactory() {
    }

    public static StructAccessor get(StructDefinition definition) {
        StructAccessor accessor = STRUCT_ACCESSOR_CACHE.get(definition.type());
        if (accessor == null) {
            throw new IllegalStateException("struct accessor not generated for type: " + definition.type());
        }
        return accessor;
    }

    public static void generate(Collection<StructDefinition> definitions) {
        if (isEmpty(definitions)) return;

        Map<Class<?>, StructAccessor> accessors = new HashMap<>(STRUCT_ACCESSOR_CACHE);
        int cachedSize = accessors.size();
        try {
            for (StructDefinition definition : definitions) {
                if (!accessors.containsKey(definition.type())) {
                    accessors.put(definition.type(), create(definition));
                }
            }
        }
        finally {
            if (accessors.size() != cachedSize) {
                STRUCT_ACCESSOR_CACHE = Map.copyOf(accessors);
            }
        }
    }

    private static StructAccessor create(StructDefinition definition) {
        try {
            Class<? extends StructAccessor> accessorClass = defineAccessor(definition);
            return instantiate(accessorClass, definition.fields());
        }
        catch (ReflectiveOperationException error) {
            throw new SerializeException("failed to create ASM struct accessor for [" + definition.type() + "]", error);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends StructAccessor> defineAccessor(StructDefinition definition) throws IllegalAccessException {
        Class<?> structType = definition.type();
        String className = structType.getName() + "$NettyxStructAccessor";
        String internalName = className.replace('.', '/');
        String structInternal = org.objectweb.asm.Type.getInternalName(structType);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };
        writer.visit(Opcodes.V21, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, internalName, null,
                     "java/lang/Object", new String[]{ACCESSOR_INTERNAL});

        StructField[] fields = definition.fields();
        for (int i = 0; i < fields.length; i++) {
            if (needsSupportState(fields[i])) {
                writer.visitField(ACC_PRIVATE | ACC_FINAL, fieldStateName(i),
                                  "L" + FIELD_INTERNAL + ";", null, null).visitEnd();
                writer.visitField(ACC_PRIVATE | ACC_FINAL, handlerStateName(i),
                                  "L" + HANDLER_INTERNAL + ";", null, null).visitEnd();
            }
        }

        addConstructor(writer, internalName, fields);
        addNewInstance(writer, structInternal);
        addRead(writer, internalName, structInternal, fields);
        addWrite(writer, internalName, structInternal, fields);
        writer.visitEnd();

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(structType, MethodHandles.lookup());
        return (Class<? extends StructAccessor>) lookup.defineClass(writer.toByteArray());
    }

    private static void addConstructor(ClassWriter writer, String internalName, StructField[] fields) {
        String descriptor = "(" + FIELD_ARRAY_DESCRIPTOR + HANDLER_ARRAY_DESCRIPTOR + ")V";
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        for (int i = 0; i < fields.length; i++) {
            if (needsSupportState(fields[i])) {
                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 1);
                pushInt(method, i);
                method.visitInsn(AALOAD);
                method.visitFieldInsn(PUTFIELD, internalName, fieldStateName(i), "L" + FIELD_INTERNAL + ";");

                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 2);
                pushInt(method, i);
                method.visitInsn(AALOAD);
                method.visitFieldInsn(PUTFIELD, internalName, handlerStateName(i), "L" + HANDLER_INTERNAL + ";");
            }
        }

        method.visitInsn(RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addNewInstance(ClassWriter writer, String structInternal) {
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
        method.visitCode();
        method.visitTypeInsn(NEW, structInternal);
        method.visitInsn(DUP);
        method.visitMethodInsn(INVOKESPECIAL, structInternal, "<init>", "()V", false);
        method.visitInsn(ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addRead(
            ClassWriter writer,
            String internalName,
            String structInternal,
            StructField[] fields) {
        String descriptor = "(L" + SERIALIZER_INTERNAL + ";" + TYPE_DESCRIPTOR + TYPE_DESCRIPTOR
                            + "L" + BYTE_BUF_INTERNAL + ";)Ljava/lang/Object;";
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "read", descriptor, null, null);
        method.visitCode();
        method.visitTypeInsn(NEW, structInternal);
        method.visitInsn(DUP);
        method.visitMethodInsn(INVOKESPECIAL, structInternal, "<init>", "()V", false);
        method.visitVarInsn(ASTORE, 5);
        method.visitVarInsn(ALOAD, 2);
        method.visitVarInsn(ALOAD, 3);
        method.visitMethodInsn(INVOKESTATIC, SUPPORT_INTERNAL, "resolveStructType", RESOLVE_DESCRIPTOR, false);
        method.visitVarInsn(ASTORE, 6);

        for (int i = 0; i < fields.length; i++) {
            addFieldRead(method, internalName, structInternal, fields[i], i);
        }

        method.visitVarInsn(ALOAD, 5);
        method.visitInsn(ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addFieldRead(
            MethodVisitor method,
            String internalName,
            String structInternal,
            StructField field,
            int index) {
        Class<?> fieldType = field.wrapped().getType();

        if (isDirectBasic(field)) {
            method.visitVarInsn(ALOAD, 5);
            newBasic(method, rawFieldClass(field), field.byteOrder());
            callSetter(method, structInternal, field, fieldType);
            return;
        }

        if (isDirectStruct(field)) {
            method.visitVarInsn(ALOAD, 5);
            method.visitVarInsn(ALOAD, 1);
            loadClass(method, rawFieldClass(field));
            method.visitVarInsn(ALOAD, 4);
            method.visitMethodInsn(INVOKEVIRTUAL, SERIALIZER_INTERNAL, "readStruct",
                                   "(" + TYPE_DESCRIPTOR + "L" + BYTE_BUF_INTERNAL + ";)Ljava/lang/Object;", false);
            castOrUnbox(method, fieldType);
            callSetter(method, structInternal, field, fieldType);
            return;
        }

        Annotation annotation = field.annotation();
        if (annotation instanceof ToArray toArray) {
            Class<?> componentClass = concreteComponentClass(field);
            if (componentClass != null && Basic.class.isAssignableFrom(componentClass)) {
                addConcreteBasicArrayRead(method, structInternal, field, fieldType, componentClass, toArray);
                return;
            }
            if (componentClass != null && AnnotationUtil.hasAnnotation(componentClass, Struct.class)) {
                addConcreteStructArrayRead(method, structInternal, field, fieldType, componentClass, toArray);
                return;
            }
        }

        method.visitVarInsn(ALOAD, 5);
        callSupportRead(method, internalName, index);
        castOrUnbox(method, fieldType);
        callSetter(method, structInternal, field, fieldType);
    }

    private static void addWrite(
            ClassWriter writer,
            String internalName,
            String structInternal,
            StructField[] fields) {
        String descriptor = "(L" + SERIALIZER_INTERNAL + ";" + TYPE_DESCRIPTOR + TYPE_DESCRIPTOR
                            + "Ljava/lang/Object;L" + BYTE_BUF_INTERNAL + ";)V";
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "write", descriptor, null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 4);
        method.visitTypeInsn(CHECKCAST, structInternal);
        method.visitVarInsn(ASTORE, 6);
        method.visitVarInsn(ALOAD, 2);
        method.visitVarInsn(ALOAD, 3);
        method.visitMethodInsn(INVOKESTATIC, SUPPORT_INTERNAL, "resolveStructType", RESOLVE_DESCRIPTOR, false);
        method.visitVarInsn(ASTORE, 7);

        for (int i = 0; i < fields.length; i++) {
            addFieldWrite(method, internalName, structInternal, fields[i], i);
        }
        method.visitInsn(RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addFieldWrite(
            MethodVisitor method,
            String internalName,
            String structInternal,
            StructField field,
            int index) {
        Class<?> fieldType = field.wrapped().getType();

        if (isDirectBasic(field)) {
            addBasicFieldWrite(method, structInternal, field, fieldType);
            return;
        }

        if (isDirectStruct(field)) {
            addStructFieldWrite(method, structInternal, field, fieldType, rawFieldClass(field));
            return;
        }

        Annotation annotation = field.annotation();
        if (annotation instanceof ToArray toArray) {
            Class<?> componentClass = concreteComponentClass(field);
            if (componentClass != null && Basic.class.isAssignableFrom(componentClass)) {
                addConcreteBasicArrayWrite(method, structInternal, field, fieldType, componentClass, toArray);
                return;
            }
            if (componentClass != null && AnnotationUtil.hasAnnotation(componentClass, Struct.class)) {
                addConcreteStructArrayWrite(method, structInternal, field, fieldType, componentClass, toArray);
                return;
            }
        }

        method.visitVarInsn(ALOAD, 6);
        callGetter(method, structInternal, field, fieldType);
        box(method, fieldType);
        method.visitVarInsn(ASTORE, 8);
        callSupportWrite(method, internalName, index, 8);
    }

    private static void addConcreteBasicArrayRead(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType,
            Class<?> componentClass,
            ToArray toArray) {
        method.visitVarInsn(ALOAD, 5);
        if (toArray.flexible()) {
            readFlexibleBasicArray(method, componentClass, field.byteOrder());
        }
        else {
            readFixedBasicArray(method, componentClass, field.byteOrder(), toArray.length());
        }
        castOrUnbox(method, fieldType);
        callSetter(method, structInternal, field, fieldType);
    }

    private static void readFixedBasicArray(
            MethodVisitor method,
            Class<?> componentClass,
            ByteOrder byteOrder,
            int length) {
        String componentInternal = org.objectweb.asm.Type.getInternalName(componentClass);
        pushInt(method, length);
        method.visitTypeInsn(ANEWARRAY, componentInternal);
        method.visitVarInsn(ASTORE, 8);
        pushInt(method, 0);
        method.visitVarInsn(ISTORE, 9);

        Label loopStart = new Label();
        Label loopEnd = new Label();
        method.visitLabel(loopStart);
        method.visitVarInsn(ILOAD, 9);
        pushInt(method, length);
        method.visitJumpInsn(IF_ICMPGE, loopEnd);
        method.visitVarInsn(ALOAD, 8);
        method.visitVarInsn(ILOAD, 9);
        newBasic(method, componentClass, byteOrder);
        method.visitInsn(AASTORE);
        method.visitIincInsn(9, 1);
        method.visitJumpInsn(GOTO, loopStart);
        method.visitLabel(loopEnd);
        method.visitVarInsn(ALOAD, 8);
    }

    private static void readFlexibleBasicArray(
            MethodVisitor method,
            Class<?> componentClass,
            ByteOrder byteOrder) {
        String componentInternal = org.objectweb.asm.Type.getInternalName(componentClass);
        method.visitTypeInsn(NEW, "java/util/ArrayList");
        method.visitInsn(DUP);
        method.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        method.visitVarInsn(ASTORE, 8);

        Label loopStart = new Label();
        Label loopEnd = new Label();
        method.visitLabel(loopStart);
        method.visitVarInsn(ALOAD, 4);
        method.visitMethodInsn(INVOKEVIRTUAL, BYTE_BUF_INTERNAL, "isReadable", "()Z", false);
        method.visitJumpInsn(IFEQ, loopEnd);
        method.visitVarInsn(ALOAD, 8);
        newBasic(method, componentClass, byteOrder);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
        method.visitInsn(POP);
        method.visitJumpInsn(GOTO, loopStart);
        method.visitLabel(loopEnd);

        method.visitVarInsn(ALOAD, 8);
        method.visitVarInsn(ALOAD, 8);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "size", "()I", false);
        method.visitTypeInsn(ANEWARRAY, componentInternal);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "toArray",
                               "([Ljava/lang/Object;)[Ljava/lang/Object;", false);
    }

    private static void addConcreteStructArrayRead(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType,
            Class<?> componentClass,
            ToArray toArray) {
        method.visitVarInsn(ALOAD, 5);
        method.visitVarInsn(ALOAD, 1);
        loadClass(method, componentClass);
        method.visitVarInsn(ALOAD, 4);
        pushInt(method, toArray.length());
        pushBoolean(method, toArray.flexible());
        method.visitMethodInsn(INVOKEVIRTUAL, SERIALIZER_INTERNAL, "readStructArray",
                               "(" + TYPE_DESCRIPTOR + "L" + BYTE_BUF_INTERNAL + ";IZ)[Ljava/lang/Object;", false);
        castOrUnbox(method, fieldType);
        callSetter(method, structInternal, field, fieldType);
    }

    private static void addBasicFieldWrite(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType) {
        method.visitVarInsn(ALOAD, 6);
        callGetter(method, structInternal, field, fieldType);
        method.visitVarInsn(ASTORE, 8);

        Label notNull = new Label();
        Label end = new Label();
        method.visitVarInsn(ALOAD, 8);
        method.visitJumpInsn(IFNONNULL, notNull);
        method.visitVarInsn(ALOAD, 5);
        pushInt(method, StructHelper.findBasicSize(rawFieldClass(field)));
        method.visitMethodInsn(INVOKEVIRTUAL, BYTE_BUF_INTERNAL, "writeZero", "(I)L" + BYTE_BUF_INTERNAL + ";", false);
        method.visitInsn(POP);
        method.visitJumpInsn(GOTO, end);

        method.visitLabel(notNull);
        method.visitVarInsn(ALOAD, 8);
        method.visitTypeInsn(CHECKCAST, BASIC_INTERNAL);
        method.visitVarInsn(ASTORE, 9);

        Label hasValue = new Label();
        method.visitVarInsn(ALOAD, 9);
        method.visitMethodInsn(INVOKEVIRTUAL, BASIC_INTERNAL, "value", "()Ljava/lang/Comparable;", false);
        method.visitJumpInsn(IFNONNULL, hasValue);
        method.visitVarInsn(ALOAD, 5);
        method.visitVarInsn(ALOAD, 9);
        method.visitMethodInsn(INVOKEVIRTUAL, BASIC_INTERNAL, "size", "()I", false);
        method.visitMethodInsn(INVOKEVIRTUAL, BYTE_BUF_INTERNAL, "writeZero", "(I)L" + BYTE_BUF_INTERNAL + ";", false);
        method.visitInsn(POP);
        method.visitJumpInsn(GOTO, end);

        method.visitLabel(hasValue);
        method.visitVarInsn(ALOAD, 9);
        method.visitVarInsn(ALOAD, 5);
        loadByteOrder(method, field.byteOrder());
        method.visitMethodInsn(INVOKEVIRTUAL, BASIC_INTERNAL, "write",
                               "(L" + BYTE_BUF_INTERNAL + ";" + BYTE_ORDER_DESCRIPTOR + ")V", false);
        method.visitLabel(end);
    }

    private static void addStructFieldWrite(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType,
            Class<?> fieldClass) {
        method.visitVarInsn(ALOAD, 6);
        callGetter(method, structInternal, field, fieldType);
        method.visitVarInsn(ASTORE, 8);

        Label notNull = new Label();
        method.visitVarInsn(ALOAD, 8);
        method.visitJumpInsn(IFNONNULL, notNull);
        loadClass(method, fieldClass);
        method.visitMethodInsn(INVOKESTATIC, STRUCT_HELPER_INTERNAL, "newStruct",
                               "(" + TYPE_DESCRIPTOR + ")Ljava/lang/Object;", false);
        method.visitVarInsn(ASTORE, 8);
        method.visitLabel(notNull);
        method.visitVarInsn(ALOAD, 1);
        loadClass(method, fieldClass);
        method.visitVarInsn(ALOAD, 8);
        method.visitVarInsn(ALOAD, 5);
        method.visitMethodInsn(INVOKEVIRTUAL, SERIALIZER_INTERNAL, "writeStruct",
                               "(" + TYPE_DESCRIPTOR + "Ljava/lang/Object;L" + BYTE_BUF_INTERNAL + ";)V", false);
    }

    private static void addConcreteBasicArrayWrite(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType,
            Class<?> componentClass,
            ToArray toArray) {
        method.visitVarInsn(ALOAD, 1);
        method.visitVarInsn(ALOAD, 6);
        callGetter(method, structInternal, field, fieldType);
        method.visitTypeInsn(CHECKCAST, BASIC_ARRAY_DESCRIPTOR);
        pushInt(method, StructHelper.findBasicSize(componentClass));
        pushInt(method, toArray.length());
        method.visitVarInsn(ALOAD, 5);
        pushBoolean(method, toArray.flexible());
        loadByteOrder(method, field.byteOrder());
        method.visitMethodInsn(INVOKEVIRTUAL, SERIALIZER_INTERNAL, "writeBasicArray",
                               "(" + BASIC_ARRAY_DESCRIPTOR + "IIL" + BYTE_BUF_INTERNAL + ";Z"
                               + BYTE_ORDER_DESCRIPTOR + ")V", false);
    }

    private static void addConcreteStructArrayWrite(
            MethodVisitor method,
            String structInternal,
            StructField field,
            Class<?> fieldType,
            Class<?> componentClass,
            ToArray toArray) {
        method.visitVarInsn(ALOAD, 1);
        method.visitVarInsn(ALOAD, 6);
        callGetter(method, structInternal, field, fieldType);
        loadClass(method, componentClass);
        pushInt(method, toArray.length());
        method.visitVarInsn(ALOAD, 5);
        pushBoolean(method, toArray.flexible());
        method.visitMethodInsn(INVOKEVIRTUAL, SERIALIZER_INTERNAL, "writeStructArray",
                               "(Ljava/lang/Object;" + TYPE_DESCRIPTOR + "IL" + BYTE_BUF_INTERNAL + ";Z)V", false);
    }

    private static void callSupportRead(MethodVisitor method, String internalName, int index) {
        method.visitVarInsn(ALOAD, 1);
        method.visitVarInsn(ALOAD, 2);
        method.visitVarInsn(ALOAD, 5);
        method.visitVarInsn(ALOAD, 6);
        loadStateField(method, internalName, fieldStateName(index), FIELD_INTERNAL);
        loadStateField(method, internalName, handlerStateName(index), HANDLER_INTERNAL);
        method.visitVarInsn(ALOAD, 4);
        method.visitMethodInsn(INVOKESTATIC, SUPPORT_INTERNAL, "readField", READ_FIELD_DESCRIPTOR, false);
    }

    private static void callSupportWrite(MethodVisitor method, String internalName, int index, int valueLocal) {
        method.visitVarInsn(ALOAD, 1);
        method.visitVarInsn(ALOAD, 2);
        method.visitVarInsn(ALOAD, 6);
        method.visitVarInsn(ALOAD, 7);
        loadStateField(method, internalName, fieldStateName(index), FIELD_INTERNAL);
        loadStateField(method, internalName, handlerStateName(index), HANDLER_INTERNAL);
        method.visitVarInsn(ALOAD, valueLocal);
        method.visitVarInsn(ALOAD, 5);
        method.visitMethodInsn(INVOKESTATIC, SUPPORT_INTERNAL, "writeField", WRITE_FIELD_DESCRIPTOR, false);
    }

    private static void newBasic(MethodVisitor method, Class<?> basicClass, ByteOrder byteOrder) {
        String basicInternal = org.objectweb.asm.Type.getInternalName(basicClass);
        method.visitTypeInsn(NEW, basicInternal);
        method.visitInsn(DUP);
        method.visitVarInsn(ALOAD, 4);
        loadByteOrder(method, byteOrder);
        method.visitMethodInsn(INVOKESPECIAL, basicInternal, "<init>",
                               "(L" + BYTE_BUF_INTERNAL + ";" + BYTE_ORDER_DESCRIPTOR + ")V", false);
    }

    private static void callGetter(MethodVisitor method, String structInternal, StructField field, Class<?> fieldType) {
        method.visitMethodInsn(INVOKEVIRTUAL, structInternal, field.getterName(),
                               org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(fieldType)), false);
    }

    private static void callSetter(MethodVisitor method, String structInternal, StructField field, Class<?> fieldType) {
        method.visitMethodInsn(INVOKEVIRTUAL, structInternal, field.setterName(),
                               org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.VOID_TYPE,
                                                                          org.objectweb.asm.Type.getType(fieldType)), false);
    }

    private static void loadStateField(MethodVisitor method, String internalName, String name, String fieldInternal) {
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, internalName, name, "L" + fieldInternal + ";");
    }

    private static void loadClass(MethodVisitor method, Class<?> clazz) {
        method.visitLdcInsn(org.objectweb.asm.Type.getType(clazz));
    }

    private static void loadByteOrder(MethodVisitor method, ByteOrder byteOrder) {
        method.visitFieldInsn(GETSTATIC, BYTE_ORDER_INTERNAL, byteOrderFieldName(byteOrder), BYTE_ORDER_DESCRIPTOR);
    }

    private static void pushBoolean(MethodVisitor method, boolean value) {
        method.visitInsn(value ? ICONST_1 : ICONST_0);
    }

    private static void pushInt(MethodVisitor method, int value) {
        if (value >= -1 && value <= 5) method.visitInsn(ICONST_0 + value);
        else if (value <= Byte.MAX_VALUE) method.visitIntInsn(BIPUSH, value);
        else if (value <= Short.MAX_VALUE) method.visitIntInsn(SIPUSH, value);
        else method.visitLdcInsn(value);
    }

    private static void castOrUnbox(MethodVisitor method, Class<?> type) {
        if (!type.isPrimitive()) {
            method.visitTypeInsn(CHECKCAST, org.objectweb.asm.Type.getType(type).getInternalName());
            return;
        }
        Primitive primitive = Primitive.of(type);
        method.visitTypeInsn(CHECKCAST, primitive.wrapperInternal);
        method.visitMethodInsn(INVOKEVIRTUAL, primitive.wrapperInternal, primitive.unboxMethod,
                               "()" + org.objectweb.asm.Type.getDescriptor(type), false);
    }

    private static void box(MethodVisitor method, Class<?> type) {
        if (!type.isPrimitive()) return;
        Primitive primitive = Primitive.of(type);
        String descriptor = "(" + org.objectweb.asm.Type.getDescriptor(type) + ")L" + primitive.wrapperInternal + ";";
        method.visitMethodInsn(INVOKESTATIC, primitive.wrapperInternal, "valueOf", descriptor, false);
    }

    private static StructAccessor instantiate(
            Class<? extends StructAccessor> accessorClass,
            StructField[] fields) throws ReflectiveOperationException {
        StructFieldHandler[] handlers = new StructFieldHandler[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].category() == StructField.Category.HANDLER) handlers[i] = fields[i].handler();
        }
        Constructor<? extends StructAccessor> constructor = accessorClass.getConstructor(StructField[].class,
                                                                                         StructFieldHandler[].class);
        return constructor.newInstance(fields, handlers);
    }

    private static boolean needsSupportState(StructField field) {
        if (isDirectBasic(field) || isDirectStruct(field)) return false;
        Annotation annotation = field.annotation();
        if (annotation instanceof ToArray) {
            Class<?> componentClass = concreteComponentClass(field);
            return componentClass == null
                   || (!Basic.class.isAssignableFrom(componentClass)
                       && !AnnotationUtil.hasAnnotation(componentClass, Struct.class));
        }
        return true;
    }

    private static boolean isDirectBasic(StructField field) {
        return field.category() == StructField.Category.BASIC;
    }

    private static boolean isDirectStruct(StructField field) {
        return field.category() == StructField.Category.STRUCT;
    }

    private static Class<?> rawFieldClass(StructField field) {
        return (Class<?>) field.wrapped().getGenericType();
    }

    private static Class<?> concreteComponentClass(StructField field) {
        Type fieldType = field.wrapped().getGenericType();
        return fieldType instanceof Class<?> fieldClass ? fieldClass.getComponentType() : null;
    }

    private static String byteOrderFieldName(ByteOrder byteOrder) {
        return byteOrder == ByteOrder.BIG_ENDIAN ? "BIG_ENDIAN" : "LITTLE_ENDIAN";
    }

    private static String fieldStateName(int index) {
        return "field" + index;
    }

    private static String handlerStateName(int index) {
        return "handler" + index;
    }

    private record Primitive(String wrapperInternal, String unboxMethod) {
        static Primitive of(Class<?> type) {
            if (type == boolean.class) return new Primitive("java/lang/Boolean", "booleanValue");
            if (type == byte.class) return new Primitive("java/lang/Byte", "byteValue");
            if (type == char.class) return new Primitive("java/lang/Character", "charValue");
            if (type == short.class) return new Primitive("java/lang/Short", "shortValue");
            if (type == int.class) return new Primitive("java/lang/Integer", "intValue");
            if (type == long.class) return new Primitive("java/lang/Long", "longValue");
            if (type == float.class) return new Primitive("java/lang/Float", "floatValue");
            if (type == double.class) return new Primitive("java/lang/Double", "doubleValue");
            throw new IllegalArgumentException("unsupported primitive type: " + type);
        }
    }
}
