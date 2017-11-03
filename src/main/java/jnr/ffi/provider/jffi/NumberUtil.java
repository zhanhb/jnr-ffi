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

public final class NumberUtil {
    private NumberUtil() {}
    
    static <T> Class<T> getBoxedClass(Class<T> c) {
        return Primitives.wrap(c);
    }

    static <T> Class<T> getPrimitiveClass(Class<T> c) {
        Class<T> unwrap = Primitives.unwrap(c);
        if (!unwrap.isPrimitive()) {
            throw new IllegalArgumentException("unsupported number class");
        }
        return unwrap;
    }

    public static boolean isPrimitiveInt(Class<?> c) {
        return byte.class == c || char.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static void widen(SkinnyMethodAdapter mv, Class<?> from, Class<?> to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();

        } else if (boolean.class == to && boolean.class != from && isPrimitiveInt(from)) {
            // Ensure only 0x0 and 0x1 values are used for boolean
            mv.iconst_1();
            mv.iand();
        }
    }

    public static void widen(SkinnyMethodAdapter mv, Class<?> from, Class<?> to, NativeType nativeType) {
        if (isPrimitiveInt(from)) {
            if (nativeType == NativeType.UCHAR) {
                mv.pushInt(0xff);
                mv.iand();

            } else if (nativeType == NativeType.USHORT) {
                mv.pushInt(0xffff);
                mv.iand();
            }

            if (long.class == to) {
                mv.i2l();
                switch (nativeType) {
                    case UINT:
                    case ULONG:
                    case ADDRESS:
                        if (sizeof(nativeType) < 8) {
                            // strip off bits 32:63
                            mv.ldc(0xffffffffL);
                            mv.land();
                        }
                        break;
                }
            }
        }
    }


    public static void narrow(SkinnyMethodAdapter mv, Class<?> from, Class<?> to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to || boolean.class == to) {
                if (long.class == from) {
                    mv.l2i();
                }

                if (byte.class == to) {
                    mv.i2b();

                } else if (short.class == to) {
                    mv.i2s();

                } else if (char.class == to) {
                    mv.i2c();

                } else if (boolean.class == to) {
                    // Ensure only 0x0 and 0x1 values are used for boolean
                    mv.iconst_1();
                    mv.iand();
                }
            }
        }
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class<?> from, final Class<?> to) {
        // class to is not a wrapper class of primitive
        narrow(mv, from, to);
        widen(mv, from, to);
    }


    public static void convertPrimitive(SkinnyMethodAdapter mv, final Class<?> from, final Class<?> to, final NativeType nativeType) {
        if (boolean.class == to) {
            narrow(mv, from, to);
            return;
        }

        switch (nativeType) {
            case SCHAR:
                narrow(mv, from, byte.class);
                widen(mv, byte.class, to);
                break;

            case SSHORT:
                narrow(mv, from, short.class);
                widen(mv, short.class, to);
                break;

            case SINT:
                narrow(mv, from, int.class);
                widen(mv, int.class, to);
                break;

            case UCHAR:
                narrow(mv, from, int.class);
                mv.pushInt(0xff);
                mv.iand();
                widen(mv, int.class, to);
                break;

            case USHORT:
                narrow(mv, from, int.class);
                mv.pushInt(0xffff);
                mv.iand();
                widen(mv, int.class, to);
                break;

            case UINT:
            case ULONG:
            case ADDRESS:
                if (sizeof(nativeType) <= 4) {
                    narrow(mv, from, int.class);
                    if (long.class == to) {
                        mv.i2l();
                        // strip off bits 32:63
                        mv.ldc(0xffffffffL);
                        mv.land();
                    }
                } else {
                    widen(mv, from, to);
                }
                break;


            case FLOAT:
            case DOUBLE:
                break;

            default:
                narrow(mv, from, to);
                widen(mv, from, to);
                break;
        }
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
