package jnr.ffi.provider.jffi;

import static jnr.ffi.provider.jffi.Primitives.unwrap;
import static jnr.ffi.provider.jffi.Primitives.wrap;

import java.lang.annotation.Annotation;
import java.math.BigInteger;

import jnr.ffi.NativeType;
import jnr.ffi.annotations.TypeDefinition;

public class Convert {

    private static boolean isNumberLike(Class<?> type) {
        Class<?> unwrap = unwrap(type);
        return unwrap == boolean.class || unwrap == char.class || Number.class.isAssignableFrom(wrap(type));
    }

    private static BigInteger toBigInteger(Object object) {
        if (object == null) {
            return null;
        }
        Number number;
        if (object instanceof Boolean) {
            number = ((Boolean) object) ? 1 : 0;
        } else if (object instanceof Character) {
            number = (int) ((Character) object);
        } else if (object instanceof BigInteger) {
            return (BigInteger) object;
        } else {
            number = (Number) object;
        }
        if (unwrap(number.getClass()).isPrimitive()) {
            return BigInteger.valueOf(number.longValue());
        }
        throw new IllegalArgumentException(number.getClass().getName());
    }

    private static <T> T toPrimitive(Number number, Class<T> type) throws Exception {
        if (number == null || unwrap(type) == void.class) {
            return null;
        }
        if (wrap(type) == Boolean.class) {
            // TODO only primitive wrapper type, non floating
            // TODO Float.NaN, -0 ??
            return wrap(type).cast(number.longValue() != 0);
        }
        if (wrap(type) == Character.class) {
            return wrap(type).cast((char) number.shortValue());
        }
        if (isNumberLike(unwrap(type))) {
            return wrap(type).cast(Number.class.getMethod(unwrap(type) + "Value").invoke(number));
        }
        throw new IllegalArgumentException(type.toString());
    }

    private static <T> T toNumber(Object object, Class<T> type) throws Exception {
        if (object instanceof Number) {
            return toPrimitive((Number) object, type);
        }
        BigInteger x = toBigInteger(object);
        return toPrimitive(x, type);
    }

    public static <T> T convert(Object value, Class<T> type) throws Exception {
        try {
            return wrap(type).cast(value);
        } catch (ClassCastException ex) {
            return toNumber(value, type);
        }
    }

    private static BigInteger clearHigherBits(BigInteger x, int bits, boolean unsigned) {
        if (x == null) {
            return x;
        }
        if (!unsigned && x.testBit(bits - 1)) {
            return BigInteger.valueOf(-1).shiftLeft(bits).or(x);
        } else {
            return BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE).and(x);
        }
    }

    private static boolean isUnsigned(NativeType nativeType) {
        switch (nativeType) {
            case ADDRESS:
            case STRUCT:
                return true;
        }
        return nativeType.name().startsWith("U");
    }

    private static <T> T convert(Object value, Class<T> type, jnr.ffi.Type jnrType) throws Exception {
        if (value == null) {
            return null;
        }
        Object tmp = value;
        if (value instanceof Float || value instanceof Double) {
            Class<T> unwrap = unwrap(type);
            if (unwrap == float.class || unwrap == double.class) {
                return toNumber(value, type);
            } else {
                tmp = toNumber(value, type);
            }
        }
        T convert = convert(tmp, type);
        BigInteger x = toBigInteger(convert);
        int bits = jnrType.size() << 3;
        boolean unsigned = isUnsigned(jnrType.getNativeType());
        x = clearHigherBits(x, bits, unsigned);
        return toNumber(x, type);
    }

    public static <T> T convert(Object value, Class<T> type, NativeType nativeType) throws Exception {
        return convert(value, type, jnr.ffi.Runtime.getSystemRuntime().findType(nativeType));
    }

    public static <T> T convert(Object value, Class<T> type, Class<? extends Annotation> pa) throws Exception {
        return convert(value, type, jnr.ffi.Runtime.getSystemRuntime().findType(pa.getAnnotation(TypeDefinition.class).alias()));
    }

    static <T> T normalize(T value, NativeType nativeType) throws Exception {
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T result = (T) convert(value, value.getClass(), nativeType);
        return result;
    }

    static <T> T normalize(T value, Class<? extends Annotation> pa) throws Exception {
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T result = (T) convert(value, value.getClass(), pa);
        return result;
    }

}
