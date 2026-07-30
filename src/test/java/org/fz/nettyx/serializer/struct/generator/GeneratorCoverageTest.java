package org.fz.nettyx.serializer.struct.generator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.coverage.BytecodeCoverageStruct;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fz.nettyx.serializer.struct.StructSerializerContext.getStructDefinition;
import static org.junit.Assert.*;

public class GeneratorCoverageTest {

    @BeforeClass
    public static void scanCoverageModel() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.coverage");
    }

    @Test
    public void generatorUsesAllIntegerConstantFormsAndUnsupportedArrayFallback() {
        StructDefinition definition = getStructDefinition(BytecodeCoverageStruct.class);
        assertNotNull(definition);
        assertNotNull(StructAccessorFactory.get(definition));

        StructField unsupported = field(definition, "unsupported");
        StructSerializer serializer = new StructSerializer(BytecodeCoverageStruct.class);
        BytecodeCoverageStruct bean = new BytecodeCoverageStruct();
        ByteBuf output = Unpooled.buffer();
        try {
            assertThrows(RuntimeException.class,
                         () -> StructAccessor.Support.readField(serializer, BytecodeCoverageStruct.class, bean,
                                                                BytecodeCoverageStruct.class, unsupported,
                                                                unsupported.handler(), Unpooled.EMPTY_BUFFER));
            assertThrows(RuntimeException.class,
                         () -> StructAccessor.Support.writeField(serializer, BytecodeCoverageStruct.class, bean,
                                                                 BytecodeCoverageStruct.class, unsupported,
                                                                 unsupported.handler(), new String[]{"x"}, output));
        }
        finally {
            output.release();
        }
    }

    @Test
    public void asmAccessorFactoryIsRemoved() {
        assertThrows(ClassNotFoundException.class,
                     () -> Class.forName("org.fz.nettyx.serializer.struct.generator.AsmStructAccessorFactory"));
    }

    @Test
    public void asmFactoryOwnsAccessorLifecycle() {
        assertTrue(Arrays.stream(StructAccessorFactory.class.getDeclaredMethods())
                         .anyMatch(method -> method.getName().equals("get")));
        assertTrue(Arrays.stream(StructAccessorFactory.class.getDeclaredMethods())
                         .anyMatch(method -> method.getName().equals("generate")));
    }

    @Test
    public void asmFactoryRejectsMissingAndUnbindableDefinitions() {
        StructDefinition missing = new StructDefinition(Unregistered.class, new StructField[0]);
        assertThrows(IllegalStateException.class, () -> StructAccessorFactory.get(missing));
        assertThrows(SerializeException.class,
                     () -> StructAccessorFactory.generate(List.of(
                             new StructDefinition(String.class, new StructField[0]))));

        StructAccessorFactory.generate(null);
        StructAccessorFactory.generate(Collections.emptyList());
        StructAccessorFactory.generate(List.of(getStructDefinition(BytecodeCoverageStruct.class)));
    }

    @Test
    public void primitiveMappingRejectsVoid() throws Exception {
        Class<?> primitive = Class.forName(StructAccessorFactory.class.getName() + "$Primitive");
        Method of = primitive.getDeclaredMethod("of", Class.class);
        of.setAccessible(true);
        InvocationTargetException error = assertThrows(InvocationTargetException.class,
                                                       () -> of.invoke(null, void.class));
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void integerEmitterSupportsValuesBelowMinusOne() throws Exception {
        Method pushInt = StructAccessorFactory.class.getDeclaredMethod("pushInt", MethodVisitor.class, int.class);
        pushInt.setAccessible(true);
        int[] instruction = new int[2];
        MethodVisitor visitor = new MethodVisitor(Opcodes.ASM9) {
            @Override
            public void visitIntInsn(int opcode, int operand) {
                instruction[0] = opcode;
                instruction[1] = operand;
            }
        };

        pushInt.invoke(null, visitor, -2);
        assertEquals(Opcodes.BIPUSH, instruction[0]);
        assertEquals(-2, instruction[1]);
    }

    private static StructField field(StructDefinition definition, String name) {
        for (StructField field : definition.fields()) {
            if (field.wrapped().getName().equals(name)) return field;
        }
        throw new AssertionError("field not found: " + name);
    }

    private static class Unregistered {
    }
}
