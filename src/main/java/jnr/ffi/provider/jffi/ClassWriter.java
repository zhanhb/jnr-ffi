package jnr.ffi.provider.jffi;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class ClassWriter extends ClassVisitor {

    public static ClassWriter newInstance() {
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = Debug.getInstance().wrapClassWriter(cw);
        return new ClassWriter(cv, cw);
    }

    private final org.objectweb.asm.ClassWriter cw;
    private String className;

    ClassWriter(ClassVisitor cv, org.objectweb.asm.ClassWriter cw) {
        super(Opcodes.ASM4, cv);
        this.cw = cw;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Creates a new instance of SkinnyMethodAdapter.
     *
     * @param access The access flags.
     * @param name The name.
     * @param desc The descriptor.
     * @param signature The signature. null if the method has no generic types
     * @param exceptions The array of exceptions.
     */
    @Override
    public SkinnyMethodAdapter visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new SkinnyMethodAdapter(super.visitMethod(access, name, desc, signature, exceptions));
    }

    public byte[] toByteArray() {
        byte[] toByteArray = cw.toByteArray();
        if (className != null) {
            Debug.getInstance().trace(className, toByteArray);
        }
        return toByteArray;
    }

}
