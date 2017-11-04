package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jnr.ffi.types.int16_t;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.int64_t;
import jnr.ffi.types.int8_t;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

@RunWith(Parameterized.class)
public class NumberConvertTest<F, T> extends AbstractNumberConvertTest {

    @Parameterized.Parameters(name = "{index}: {0} {1} {2} {3}")
    public static List<Object[]> createParameters() {
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>(Primitives.allPrimitiveTypes());
        parameterTypes.remove(void.class);

        ArrayList<Class<?>> returning = new ArrayList<Class<?>>(Primitives.allPrimitiveTypes());

        Class<?>[] annotationTypes = {
            null,
            int8_t.class,
            int16_t.class,
            int32_t.class,
            int64_t.class,
            u_int8_t.class,
            u_int16_t.class,
            u_int32_t.class,
            u_int64_t.class
        };

        List<Object[]> result = new ArrayList<Object[]>(returning.size() * annotationTypes.length * annotationTypes.length * parameterTypes.size() + 2);
        for (Class<?> a : annotationTypes) {
            for (Class<?> ret : returning) {
                add(result, a, ret, null, double.class);
                add(result, a, ret, int64_t.class, long.class);
                add(result, a, ret, u_int64_t.class, long.class);
            }
            for (Class<?> p : parameterTypes) {
                add(result, null, double.class, a, p);
                add(result, int64_t.class, long.class, a, p);
                add(result, u_int64_t.class, long.class, a, p);
            }
        }
        return result;
    }

    private static void add(
            List<Object[]> result,
            Class<?> reta,
            Class<?> ret,
            Class<?> pa,
            Class<?> p) {
        if (reta == null && ret == void.class) {
            return;
        }
        // TODO native long or int64?
        if (reta == null && ret == long.class) {
            return;
        }
        // TODO native long or int64?
        if (pa == null && p == long.class) {
            return;
        }
        if (pa != null && typeof(p) == 2) {
            return;
        }
        if (reta != null && typeof(ret) == 2) {
            return;
        }
        result.add(new Object[]{reta, ret, pa, p});
        result.add(new Object[]{reta, ret, pa, Primitives.wrap(p)});
        if (void.class != ret) {
            result.add(new Object[]{reta, Primitives.wrap(ret), pa, p});
            result.add(new Object[]{reta, Primitives.wrap(ret), pa, Primitives.wrap(p)});
        }
    }

    private static int typeof(Class<?> p) {
        if (p == void.class) {
            return 0;
        } else if (p == float.class || p == double.class) {
            return 2;
        } else {
            return 1;
        }
    }

    private final Class<? extends Annotation> reta;
    private final Class<T> ret;
    private final Class<? extends Annotation> pa;
    private final Class<F> p;

    public NumberConvertTest(Class<? extends Annotation> reta, Class<T> ret, Class<? extends Annotation> pa, Class<F> p) {
        this.reta = reta;
        this.ret = ret;
        this.pa = pa;
        this.p = p;
    }

    @Test
    public void test() throws Throwable {
        super.doTest(p, ret, pa, reta, false);
    }

}
