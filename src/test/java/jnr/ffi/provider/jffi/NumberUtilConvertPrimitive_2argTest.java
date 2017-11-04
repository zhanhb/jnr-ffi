package jnr.ffi.provider.jffi;

import java.lang.reflect.InvocationTargetException;
import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.ILOAD;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Type;

@RunWith(Parameterized.class)
public class NumberUtilConvertPrimitive_2argTest<F, T> {

    @Parameterized.Parameters(name = "{0},{1}")
    public static List<Object[]> createParameters() {
        Set<Class<?>> primitives = Primitives.allPrimitiveTypes();

        ArrayList<Class<?>> fromTypes = new ArrayList<Class<?>>(primitives);
        fromTypes.remove(void.class); // no argument will be with type void
        fromTypes.remove(float.class);
        fromTypes.remove(double.class);

        ArrayList<Class<?>> toTypes = new ArrayList<Class<?>>(primitives);
        toTypes.remove(float.class);
        toTypes.remove(double.class);

        List<Object[]> result = new ArrayList<Object[]>(fromTypes.size() * toTypes.size());
        for (Class<?> from : fromTypes) {
            for (Class<?> to : toTypes) {
                result.add(new Object[]{from, to});
            }
        }
        result.add(new Object[]{float.class, float.class});
        result.add(new Object[]{double.class, double.class});
        return result;
    }

    private final Class<F> fromClass;
    private final Class<T> toClass;

    public NumberUtilConvertPrimitive_2argTest(Class<F> from, Class<T> to) {
        this.fromClass = from;
        this.toClass = to;
    }

    @Test
    public void testConvertPrimitive() throws Throwable {
        Method method = convertPrimitive(fromClass, toClass);
        assertEquals("expect from", fromClass, method.getParameterTypes()[0]);
        assertEquals("expect to", toClass, method.getReturnType());
        for (F sample : Samples.getSamples(fromClass)) {
            T expect = Convert.convert(sample, toClass);
            try {
                assertEquals("convert " + sample + " to " + toClass, expect, method.invoke(null, sample));
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
    }

    public Method convertPrimitive(final Class<?> from, final Class<?> to) {
        return DummyClassLoader.getInstance().genMethod(new Consumer<SkinnyMethodAdapter>() {
            @Override
            public void accept(SkinnyMethodAdapter skinnyMethodAdapter) {
                int argIndex = 0; // static method, the first parameter
                skinnyMethodAdapter.visitVarInsn(Type.getType(from).getOpcode(ILOAD), argIndex);
                NumberUtil.convertPrimitive(skinnyMethodAdapter, from, to);
            }
        }, "convertPrimitive", to, from);
    }

}
