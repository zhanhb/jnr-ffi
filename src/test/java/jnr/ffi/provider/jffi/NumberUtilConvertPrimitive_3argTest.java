package jnr.ffi.provider.jffi;

import java.lang.reflect.InvocationTargetException;
import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.ILOAD;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Type;

import jnr.ffi.NativeType;

@RunWith(Parameterized.class)
public class NumberUtilConvertPrimitive_3argTest<F, T> {

    @Parameterized.Parameters(name = "{0},{1},{2}")
    public static List<Object[]> createParameters() {
        Set<Class<?>> primitives = Primitives.allPrimitiveTypes();

        ArrayList<Class<?>> toTypes = new ArrayList<Class<?>>(primitives);
        toTypes.remove(float.class);
        toTypes.remove(double.class);

        Set<List<?>> set = new LinkedHashSet<List<?>>();

        for (NativeType nativeType : EnumSet.allOf(NativeType.class)) {
            Class<?> nativeClass;
            try {
                nativeClass = NativeClosureProxy.getNativeClass(nativeType);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            set.add(Arrays.asList(nativeType, nativeClass));
        }

        List<Object[]> result = new ArrayList<Object[]>(set.size() * toTypes.size());
        for (List<?> list : set) {
            Object nativeType = list.get(0);
            Object from = list.get(1);
            if (from == void.class || from == float.class || from == double.class) {
                continue;
            }
            for (Class<?> to : toTypes) {
                result.add(new Object[]{from, to, nativeType});
            }
        }
        result.add(new Object[]{float.class, float.class, NativeType.FLOAT});
        result.add(new Object[]{double.class, double.class, NativeType.DOUBLE});
        return result;
    }

    private final Class<F> fromClass;
    private final Class<T> toClass;
    private final NativeType nativeType;

    public NumberUtilConvertPrimitive_3argTest(Class<F> from, Class<T> to, NativeType nativeType) {
        this.fromClass = from;
        this.toClass = to;
        this.nativeType = nativeType;
    }

    @Test
    public void testConvertPrimitive() throws Throwable {
        Method method = convertPrimitive(fromClass, toClass, nativeType);
        assertEquals("expect from", fromClass, method.getParameterTypes()[0]);
        assertEquals("expect to", toClass, method.getReturnType());
        for (F sample : Samples.getSamples(fromClass)) {
            T expect = Convert.convert(sample, toClass);
            expect = Convert.normalize(expect, nativeType);
            try {
                assertEquals("convert " + sample + " to " + toClass, expect, method.invoke(null, sample, nativeType));
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
    }

    public Method convertPrimitive(final Class<?> from, final Class<?> to, final NativeType nativeType) {
        return DummyClassLoader.getInstance().genMethod(new Consumer<SkinnyMethodAdapter>() {
            @Override
            public void accept(SkinnyMethodAdapter skinnyMethodAdapter) {
                skinnyMethodAdapter.visitVarInsn(Type.getType(from).getOpcode(ILOAD), 0);
                NumberUtil.convertPrimitive(skinnyMethodAdapter, from, to, nativeType);
            }
        }, "convertPrimitive", to, from, NativeType.class);
    }

}
