package jnr.ffi.provider.jffi;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Samples {

    private static final Map<Class<?>, List<?>> SAMPLES;

    static {
        Map<Class<?>, List<?>> samples = new HashMap<Class<?>, List<?>>();
        try {
            addSample(samples, boolean.class, "true", "false");
            addSample(samples, int.class, "0", "1", "2", "-1", "0x456789AB", "0xBA987654", "0x7FFFFFFF", "0x80000000");
            addSample(samples, byte.class, "0", "1", "2", "-1", "0x78", "0x87", "0x7F", "0x80");
            addSample(samples, short.class, "0", "1", "2", "-1", "0x6789", "0x9876", "0x7FFF", "0x8000");
            addSample(samples, long.class, "0", "1", "2", "-1", "0x0123456789ABCDEF", "0xFEDCBA9876543210", "0x7FFFFFFFFFFFFFFF", "0x8000000000000000");
            addSample(samples, long.class, "0x78", "0x87", "0x7F", "0x80");
            addSample(samples, long.class, "0x6789", "0x9876", "0x7FFF", "0x8000");
            addSample(samples, long.class, "0x456789AB", "0xBA987654", "0x7FFFFFFF", "0x80000000");
            addSample(samples, float.class, "0", "1", "2", "-1", "NaN", "Infinity", "-Infinity", "1.1", "-1.1", "0.1", "-0.1", "0x1.abcdefp5");
            addSample(samples, double.class, "0", "1", "2", "-1", "NaN", "Infinity", "-Infinity", "1.1", "-1.1", "0.1", "-0.1", "0x1.0123456789abcp-4");
            addSample(samples, char.class, "\u0000", "\u0001", "\uffff", " ", "\t", "\u6789", "\u9876", "\ufeff", "\u7FFF", "\u8000");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
        SAMPLES = samples;
    }

    private static <T> List<T> getList(Map<Class<?>, List<?>> map, Class<T> klass) {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) map.get(klass);
        return list;
    }

    private static <T> void addSample(Map<Class<?>, List<?>> map, Class<T> klass, String... samples) throws Exception {
        List<T> list = getList(map, klass);
        if (list == null) {
            list = new ArrayList<T>(samples.length);
            map.put(klass, list);
        }
        for (String sample : samples) {
            list.add(decode(sample, klass));
        }
    }

    private static <T> T decode(String value, Class<T> type) throws Exception {
        Class<T> primitive = Primitives.unwrap(type);
        Class<T> wrapper = Primitives.wrap(type);
        if (primitive == boolean.class) {
            if ("true".equals(value)) {
                return wrapper.cast(Boolean.TRUE);
            } else if ("false".equals(value)) {
                return wrapper.cast(Boolean.FALSE);
            }
            throw new IllegalStateException("decode '" + value + "' to " + type);
        }
        if (Number.class.isAssignableFrom(wrapper)) {
            Number temp;
            if (primitive == float.class || primitive == double.class) {
                return wrapper.cast(wrapper.getMethod("parse" + wrapper.getSimpleName(), String.class).invoke(null, value));
            } else if ("0".equals(value)) {
                temp = BigInteger.ZERO;
            } else {
                int index = 0, radix = 10;
                if (value.startsWith("0x")) {
                    radix = 16;
                    index = 2;
                }
                temp = new BigInteger(value.substring(index), radix);
            }
            return wrapper.cast(Number.class.getMethod(primitive + "Value").invoke(temp));
        }
        if (primitive == void.class) {
            return null;
        }
        if (primitive == char.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException(value);
            }
            return wrapper.cast(value.charAt(0));
        }
        Method method = wrapper.getMethod("valueOf", String.class);
        return wrapper.cast(method.invoke(null, value)); // check cast
    }

    public static <T> List<T> getSamples(Class<T> primitive) throws Throwable {
        List<T> sample = getList(SAMPLES, primitive);
        return sample == null ? Collections.<T>emptyList() : sample;
    }

}
