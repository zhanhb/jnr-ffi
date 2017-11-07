/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider.jffi;

import com.kenai.jffi.Platform;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

final class AsmUtil {
    private AsmUtil() {}

    public static Class<?> unboxedReturnType(Class<?> type) {
        return unboxedType(type);
    }

    public static Class<?> unboxedType(Class<?> boxedType) {
        Class<?> unwrap = Primitives.unwrap(boxedType);
        if (unwrap == void.class) {
            return boxedType;
        } else if (Pointer.class.isAssignableFrom(unwrap) || Address.class == unwrap) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;
        }
        return unwrap;
    }

    public static <T> Class<T> boxedType(Class<T> type) {
        if (type == void.class) {
            return type;
        }
        return Primitives.wrap(type);
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class<?> type) {
        return long.class == type || double.class == type ? 2 : 1;
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SigType type) {
        return calculateLocalVariableSpace(type.getDeclaredType());
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class<?>... types) {
        int size = 0;

        for (int i = 0; i < types.length; ++i) {
            size += calculateLocalVariableSpace(types[i]);
        }

        return size;
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SigType... types) {
        int size = 0;

        for (SigType type : types) {
            size += calculateLocalVariableSpace(type);
        }

        return size;
    }

    private static void unboxPointerOrStruct(final SkinnyMethodAdapter mv, final Class<?> type, final Class<?> nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
                sig(nativeType, type));
    }

    static void unboxPointer(final SkinnyMethodAdapter mv, final Class<?> nativeType) {
        unboxPointerOrStruct(mv, Pointer.class, nativeType);
    }

    static void unboxBoolean(final SkinnyMethodAdapter mv, final Class<?> nativeType) {
        mv.invokevirtual("java/lang/Boolean", "booleanValue", "()Z");
        convertPrimitive(mv, boolean.class, nativeType);
    }

    static void unboxNumber(final SkinnyMethodAdapter mv, final Class<?> boxedType, final Class<?> unboxedType,
                                  final jnr.ffi.NativeType nativeType) {

        if (Number.class.isAssignableFrom(boxedType)) {

            switch (nativeType) {
                case SCHAR:
                case UCHAR:
                    mv.invokevirtual(p(boxedType), "byteValue", "()B");
                    convertPrimitive(mv, byte.class, unboxedType, nativeType);
                    break;

                case SSHORT:
                case USHORT:
                    mv.invokevirtual(p(boxedType), "shortValue", "()S");
                    convertPrimitive(mv, short.class, unboxedType, nativeType);
                    break;

                case SINT:
                case UINT:
                case SLONG:
                case ULONG:
                case ADDRESS:
                    if (sizeof(nativeType) == 4) {
                        mv.invokevirtual(p(boxedType), "intValue", "()I");
                        convertPrimitive(mv, int.class, unboxedType, nativeType);
                    } else {
                        mv.invokevirtual(p(boxedType), "longValue", "()J");
                        convertPrimitive(mv, long.class, unboxedType, nativeType);
                    }
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    mv.invokevirtual(p(boxedType), "longValue", "()J");
                    convertPrimitive(mv, long.class, unboxedType);
                    break;

                case FLOAT:
                    mv.invokevirtual(p(boxedType), "floatValue", "()F");
                    break;

                case DOUBLE:
                    mv.invokevirtual(p(boxedType), "doubleValue", "()D");
                    break;
            }


        } else if (Boolean.class == boxedType) {
            unboxBoolean(mv, unboxedType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }


    static void unboxNumber(final SkinnyMethodAdapter mv, final Class<?> boxedType, final Class<?> nativeType) {

        if (Number.class.isAssignableFrom(boxedType)) {
            String description = Type.getMethodDescriptor(Type.getType(nativeType));
            String p = p(boxedType);
            if (byte.class == nativeType || short.class == nativeType || int.class == nativeType
                    || long.class == nativeType || float.class == nativeType || double.class == nativeType) {
                mv.invokevirtual(p, nativeType + "Value", description);
            } else {
                throw new IllegalArgumentException("unsupported Number subclass: " + boxedType);
            }

        } else if (char.class == nativeType) {
            convertPrimitive(mv, nativeType, char.class);
            mv.invokevirtual(p(boxedType), "charValue", "()C");

        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            unboxBoolean(mv, nativeType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }

    static void boxValue(AsmBuilder builder, SkinnyMethodAdapter mv, Class<?> boxedType, Class<?> unboxedType) {
        if (boxedType == unboxedType || boxedType.isPrimitive()) {

        } else if (Boolean.class == boxedType) {
            convertPrimitive(mv, unboxedType, boolean.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);

        } else if (Pointer.class.isAssignableFrom(boxedType)) {
            getfield(mv, builder, builder.getRuntimeField());
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, unboxedType, jnr.ffi.Runtime.class);

        } else if (Address.class == boxedType) {
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else if (Number.class.isAssignableFrom(boxedType) && boxedType(unboxedType) == boxedType) {
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else if (Character.class == boxedType) {
            convertPrimitive(mv, unboxedType, char.class);
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else {
            throw new IllegalArgumentException("cannot box value of type " + unboxedType + " to " + boxedType);
        }
    }

    static int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ArrayFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ArrayFlags.OUT : 0;
        nflags |= (ParameterFlags.isNulTerminate(flags) || ParameterFlags.isIn(flags))
                ? com.kenai.jffi.ArrayFlags.NULTERMINATE : 0;
        return nflags;
    }

    static int getNativeArrayFlags(Collection<Annotation> annotations) {
        return getNativeArrayFlags(ParameterFlags.parse(annotations));
    }

    static LocalVariable[] getParameterVariables(ParameterType[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(parameterTypes[i].getDeclaredType(), lvar);
            lvar += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static LocalVariable[] getParameterVariables(Class<?>[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int idx = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(parameterTypes[i], idx);
            idx += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static void load(SkinnyMethodAdapter mv, Class<?> parameterType, LocalVariable parameter) {
        mv.visitVarInsn(Type.getType(parameterType).getOpcode(ILOAD), parameter.idx);
    }

    static void getfield(SkinnyMethodAdapter mv, AsmBuilder builder, AsmBuilder.ObjectField field) {
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), field.name, ci(field.klass));
    }

    static void tryfinally(SkinnyMethodAdapter mv, Runnable codeBlock, Runnable finallyBlock) {
        Label before = new Label(), after = new Label(), ensure = new Label(), done = new Label();
        mv.trycatch(before, after, ensure, null);
        mv.label(before);
        codeBlock.run();
        mv.label(after);
        if (finallyBlock != null) finallyBlock.run();
        mv.go_to(done);
        if (finallyBlock != null) {
            mv.label(ensure);
            finallyBlock.run();
            mv.athrow();
        }
        mv.label(done);
    }

    static void emitToNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, ToNativeType toNativeType) {
        ToNativeConverter<?, ?> parameterConverter = toNativeType.getToNativeConverter();
        if (parameterConverter != null) {
            // Method toNativeMethod ToNativeConverter.toNative(Object, ToNativeContext)

            if (toNativeType.getDeclaredType().isPrimitive()) {
                boxValue(builder, mv, Primitives.wrap(toNativeType.getDeclaredType()), toNativeType.getDeclaredType());
            }
            mv.aload(0);
            AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterConverter);
            mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));
            if (!ToNativeConverter.class.isAssignableFrom(toNativeConverterField.klass)) {
                mv.checkcast(ToNativeConverter.class);
            }

            // Re-order so the value to be converted is on the top of the stack
            mv.swap();

            // load context parameter (if there is one)
            if (toNativeType.getToNativeContext() != null) {
                getfield(mv, builder, builder.getToNativeContextField(toNativeType.getToNativeContext()));
            } else {
                mv.aconst_null();
            }

            mv.invokeinterface(ToNativeConverter.class, "toNative",
                    Object.class, Object.class, ToNativeContext.class);
            mv.checkcast(p(parameterConverter.nativeType()));
        }
    }

    static void emitFromNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, FromNativeType fromNativeType, Class<?> nativeClass) {
        // If there is a result converter, retrieve it and put on the stack
        FromNativeConverter<?, ?> fromNativeConverter = fromNativeType.getFromNativeConverter();
        if (fromNativeConverter != null) {
            convertPrimitive(mv, nativeClass, unboxedType(fromNativeConverter.nativeType()), fromNativeType.getNativeType());
            boxValue(builder, mv, fromNativeConverter.nativeType(), nativeClass);

            // Method FromNativeConverter.fromNative(Object, FromNativeContext)
            getfield(mv, builder, builder.getFromNativeConverterField(fromNativeConverter));
            mv.swap();
            if (fromNativeType.getFromNativeContext() != null) {
                getfield(mv, builder, builder.getFromNativeContextField(fromNativeType.getFromNativeContext()));
            } else {
                mv.aconst_null();
            }

            mv.invokeinterface(FromNativeConverter.class, "fromNative", Object.class, Object.class, FromNativeContext.class);

            if (fromNativeType.getDeclaredType().isPrimitive()) {
                // The actual return type is a primitive, but there was a converter for it - extract the primitive value
                Class<?> boxedType = Primitives.wrap(fromNativeType.getDeclaredType());
                mv.checkcast(p(boxedType));
                unboxNumber(mv, boxedType, fromNativeType.getDeclaredType(), fromNativeType.getNativeType());

            } else {
                mv.checkcast(p(fromNativeType.getDeclaredType()));
            }

        } else if (!fromNativeType.getDeclaredType().isPrimitive()) {
            Class<?> unboxedType = unboxedType(fromNativeType.getDeclaredType());
            convertPrimitive(mv, nativeClass, unboxedType, fromNativeType.getNativeType());
            boxValue(builder, mv, fromNativeType.getDeclaredType(), unboxedType);

        }
    }

}
