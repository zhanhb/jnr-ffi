package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import jnr.ffi.TstUtil;
import jnr.ffi.types.int16_t;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.int64_t;
import jnr.ffi.types.int8_t;
import jnr.ffi.types.u_int16_t;
import static org.junit.Assert.assertEquals;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import org.objectweb.asm.Type;

public abstract class AbstractNumberConvertTest {

    protected <F, T> void doTest(
            Class<F> p, Class<T> ret,
            Class<? extends Annotation> pa,
            Class<? extends Annotation> reta,
            boolean noX86) throws Throwable {
        try {
            Method method = genMethod(p, ret, pa, reta, noX86);
            Object lib = TstUtil.loadTestLib(method.getDeclaringClass());
            for (F sample : Samples.getSamples(p)) {
                boolean finite = isFinite(sample);
                if (!finite && (pa != null || reta != null || !isFloat(ret))) {
                    // TODO the result is undefined
                    return;
                }
                F tmp = pa == null ? sample : Convert.normalize(sample, pa);
                if (reta != null) {
                    // @int8_t boolean int64_t2int8_t(@int64_t long x) // 0xFF00000000
                    tmp = Convert.normalize(tmp, reta);
                }
                T expect;
                if (pa != null) {
                    expect = Convert.convert(tmp, ret, pa);
                } else {
                    expect = Convert.convert(tmp, ret);
                }
                if (reta != null) {
                    expect = Convert.normalize(expect, reta);
                }
                T result = Primitives.wrap(ret).cast(method.invoke(lib, sample));
                assertEquals("convert " + sample + " with " + method, expect, result);
            }
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private String getName(Class<?> t, Class<? extends Annotation> annotation) {
        Class<? extends Annotation> result = annotation;
        if (result == null) {
            Class<?> unwrap = Primitives.unwrap(t);
            if (unwrap == boolean.class) {
                return unwrap.getSimpleName();
            } else if (unwrap == byte.class) {
                result = int8_t.class;
            } else if (unwrap == short.class) {
                result = int16_t.class;
            } else if (unwrap == char.class) {
                result = u_int16_t.class;
            } else if (unwrap == int.class) {
                result = int32_t.class;
            } else if (unwrap == long.class) {
                int longSize = jnr.ffi.Runtime.getSystemRuntime().longSize();
                // java long without any annotation is mapped to a native long type
                result = longSize == 4 ? int32_t.class : int64_t.class;
            } else if (unwrap == float.class || unwrap == double.class) {
                return unwrap.getName();
            } else {
                throw new IllegalArgumentException(t.getName());
            }
        }
        return result.getSimpleName();
    }

    private <F, T> Method genMethod(
            Class<F> p, Class<T> ret,
            final Class<? extends Annotation> pa,
            final Class<? extends Annotation> reta,
            final boolean noX86) {
        final String name = getName(p, pa) + "2" + getName(ret, reta);
        final String description = Type.getMethodDescriptor(Type.getType(ret), Type.getType(p));
        try {
            return DummyClassLoader.getInstance().getInterface(new Consumer<ClassWriter>() {
                @Override
                public void accept(ClassWriter cw) {
                    if (noX86) {
                        cw.visitAnnotation("Ljnr/ffi/provider/jffi/NoX86;", true).visitEnd();
                    }
                    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, name, description, null, null);
                    if (reta != null) {
                        mv.visitAnnotation(Type.getType(reta).getDescriptor(), true).visitEnd();
                    }
                    if (pa != null) {
                        mv.visitParameterAnnotation(0, Type.getType(pa).getDescriptor(), true).visitEnd();
                    }
                }
            }).getMethod(name, p);
        } catch (NoSuchMethodException ex) {
            throw (Error) new VerifyError().initCause(ex);
        }
    }

    private boolean isFinite(Object x) {
        if (x instanceof Double) {
            double value = (Double) x;
            return !Double.isNaN(value) && !Double.isInfinite(value);
        } else if (x instanceof Float) {
            float value = (Float) x;
            return !Float.isNaN(value) && !Float.isInfinite(value);
        }
        return true;
    }

    private boolean isFloat(Class<?> ret) {
        Class<?> unwrap = Primitives.unwrap(ret);
        return unwrap == float.class || unwrap == double.class;
    }

}
