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

import jnr.ffi.NativeType;
import jnr.ffi.provider.SigType;
import org.objectweb.asm.Label;

final class NumberUtil {
    private NumberUtil() {}

    private static boolean isDecimal(Class<?> cl) {
        return cl == float.class || cl == double.class;
    }

    static void convertPrimitive(SkinnyMethodAdapter mv, final Class<?> from, final Class<?> to) {
        if (from == to) {
            return;
        }
        if (!from.isPrimitive() || !to.isPrimitive()) {
            return;
        }
        if (from == void.class || to == void.class) {
            return;
        }
        if (isDecimal(from) || isDecimal(to)) {
            // no type alias for float type yet
            return;
        }
        if (boolean.class == to) {
            if (long.class == from) {
                mv.lconst_0();
                mv.lcmp();
            }
            /* Equivalent to
               return result == 0 ? true : false;
            */
            Label zero = new Label();
            Label ret = new Label();
            mv.ifeq(zero);
            mv.iconst_1();
            mv.go_to(ret);
            mv.label(zero);
            mv.iconst_0();
            mv.label(ret);
        } else if (long.class == to) {
            mv.i2l();
        } else {
            if (long.class == from) {
                mv.l2i();
            }
            if (byte.class == to) {
                mv.i2b();
            } else if (short.class == to) {
                mv.i2s();
            } else if (char.class == to) {
                mv.i2c();
            }
        }
    }

    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class<?> from, final Class<?> to, final NativeType nativeType) {
        if (!from.isPrimitive() || !to.isPrimitive()) {
            return;
        }
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case SLONGLONG:
            case ULONGLONG:
            case ADDRESS:
                break;
            case FLOAT:
            case DOUBLE:
            case VOID:
            case STRUCT:
            default:
                return;
        }
        if (to == void.class) {
            return;
        }
        int sizeofNativeType = sizeof(nativeType);
        if (boolean.class == to) {
            switch (sizeofNativeType) {
                case 1:
                    convertPrimitive(mv, from, byte.class);
                    convertPrimitive(mv, int.class, boolean.class);
                    break;
                case 2:
                    convertPrimitive(mv, from, short.class);
                    convertPrimitive(mv, int.class, boolean.class);
                    break;
                case 4:
                    convertPrimitive(mv, from, int.class);
                    convertPrimitive(mv, int.class, boolean.class);
                    break;
                case 8:
                    convertPrimitive(mv, from, long.class);
                    convertPrimitive(mv, long.class, boolean.class);
                    break;
                default: // impossible
                    break;
            }
            return;
        }
        int sizeofTo = sizeof(to);
        if (sizeofTo <= sizeofNativeType) {
            convertPrimitive(mv, from, to);
            return;
        }
        boolean unsigned = false;
        switch (nativeType) {
            case UCHAR:
            case USHORT:
            case UINT:
            case ULONG:
            case ULONGLONG:
            case ADDRESS:
                unsigned = true;
                break;
            default:
                break;
        }
        switch (sizeofNativeType) {
            case 1:
                if (unsigned) {
                    convertPrimitive(mv, from, int.class);
                    mv.pushInt(0xff);
                    mv.iand();
                } else {
                    convertPrimitive(mv, from, byte.class);
                }
                convertPrimitive(mv, int.class, to);
                break;
            case 2:
                if (unsigned) {
                    convertPrimitive(mv, from, char.class);
                } else {
                    convertPrimitive(mv, from, short.class);
                }
                convertPrimitive(mv, int.class, to);
                break;
            case 4: // to must be long
                if (unsigned) {
                    convertPrimitive(mv, from, long.class);
                    mv.ldc(0xffffffffL);
                    mv.land();
                } else {
                    convertPrimitive(mv, from, int.class);
                    mv.i2l();
                }
                break;
            default: // impossible
                break;
        }
    }

    private static int sizeof(Class<?> to) {
        if (to == byte.class) {
            return 1;
        } else if (char.class == to || short.class == to) {
            return 2;
        } else if (int.class == to) {
            return 4;
        } else if (long.class == to) {
            return 8;
        }
        return 0x100;
    }

    static int sizeof(SigType type) {
        return sizeof(type.getNativeType());
    }

    static int sizeof(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
                return com.kenai.jffi.Type.SCHAR.size();

            case UCHAR:
                return com.kenai.jffi.Type.UCHAR.size();

            case SSHORT:
                return com.kenai.jffi.Type.SSHORT.size();

            case USHORT:
                return com.kenai.jffi.Type.USHORT.size();

            case SINT:
                return com.kenai.jffi.Type.SINT.size();

            case UINT:
                return com.kenai.jffi.Type.UINT.size();

            case SLONG:
                return com.kenai.jffi.Type.SLONG.size();

            case ULONG:
                return com.kenai.jffi.Type.ULONG.size();

            case SLONGLONG:
                return com.kenai.jffi.Type.SLONG_LONG.size();

            case ULONGLONG:
                return com.kenai.jffi.Type.ULONG_LONG.size();

            case FLOAT:
                return com.kenai.jffi.Type.FLOAT.size();

            case DOUBLE:
                return com.kenai.jffi.Type.DOUBLE.size();

            case ADDRESS:
                return com.kenai.jffi.Type.POINTER.size();

            case VOID:
                return 0;

            default:
                throw new UnsupportedOperationException("cannot determine size of " + nativeType);
        }
    }

}
