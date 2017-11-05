package jnr.ffi.provider.jffi;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.V1_6;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

public class DummyClassLoader extends ClassLoader {

    private static final AtomicInteger id = new AtomicInteger();

    static DummyClassLoader getInstance() {
        return new DummyClassLoader();
    }

    private final ConcurrentMap<String, Class<?>> map = new ConcurrentHashMap<String, Class<?>>();

    private DummyClassLoader() {
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = map.get(name);
        if (c == null) {
            throw new ClassNotFoundException(name);
        }
        return c;
    }

    public Class<?> defineClass(String name, byte[] bytes) {
        Class<?> cl = super.defineClass(name, bytes, 0, bytes.length);
//        File file = new File("target/dump", cl.getName().replace(".", "/").concat(".class"));
//        File parentFile = file.getParentFile();
//        if (parentFile != null) {
//            parentFile.mkdirs();
//        }
//        try {
//            FileOutputStream output = new FileOutputStream(file);
//            try {
//                output.write(bytes);
//            } finally {
//                output.close();
//            }
//        } catch (IOException ex) {
//        }
        try {
            return Class.forName(cl.getName(), false, cl.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw (Error) new VerifyError().initCause(ex);
        }
    }

    public Class<?> clean(Consumer<ClassWriter> consumer) {
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        consumer.accept(cw);
        return defineClass(null, cw.toByteArray());
    }

    private String name() {
        return "Dummy" + id.incrementAndGet();
    }

    public Class<?> genClass(final Consumer<ClassWriter> consumer) {
        return clean(new Consumer<ClassWriter>() {
            @Override
            public void accept(ClassWriter cw) {
                cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, name(), null, "java/lang/Object", null);
                consumer.accept(cw);
            }
        });
    }

    public Class<?> getInterface(final Consumer<ClassWriter> consumer) {
        return clean(new Consumer<ClassWriter>() {
            @Override
            public void accept(ClassWriter cw) {
                cw.visit(V1_6, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, name(), null, "java/lang/Object", null);
                consumer.accept(cw);
            }
        });
    }

    public Method genMethod(final Consumer<? super SkinnyMethodAdapter> consumer,
            final String name, Class<?> retClass, Class<?>... params) {
        final Type retType = Type.getType(retClass);
        final Type[] types = new Type[params.length];
        for (int i = 0; i < params.length; ++i) {
            types[i] = Type.getType(params[i]);
        }
        final String signature = Type.getMethodDescriptor(retType, types);
        try {
            return genClass(new Consumer<ClassWriter>() {
                @Override
                public void accept(ClassWriter cw) {
                    SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cw.visitMethod(ACC_PUBLIC + ACC_STATIC, name, signature, null, null));
                    mv.visitCode();
                    consumer.accept(mv);
                    mv.visitInsn(retType.getOpcode(IRETURN));
                    mv.visitMaxs(0, 0);
                }
            }).getMethod(name, params);
        } catch (NoSuchMethodException ex) {
            throw (Error) new VerifyError().initCause(ex);
        }
    }

}
