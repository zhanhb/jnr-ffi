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

import org.objectweb.asm.*;

import java.util.Map;

import static jnr.ffi.provider.jffi.CodegenUtils.*;

/**
 *
 * @author headius
 */
class SkinnyMethodAdapter extends MethodVisitor implements Opcodes {

    private static boolean PRINT_ENABLED;

    // visiable for testing
    static void setPrintEnabled(boolean enabled) {
        PRINT_ENABLED = enabled;
    }

    // visiable for testing
    static boolean isPrintEnabled() {
        return PRINT_ENABLED;
    }

    SkinnyMethodAdapter(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    public void aload(int arg0) {
        super.visitVarInsn(ALOAD, arg0);
    }

    public void aload(LocalVariable arg0) {
        super.visitVarInsn(ALOAD, arg0.idx);
    }

    public void aload(int... args) {
        for (int arg : args) {
            super.visitVarInsn(ALOAD, arg);
        }
    }

    public void aload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            super.visitVarInsn(ALOAD, arg.idx);
        }
    }
    
    public void iload(int arg0) {
        super.visitVarInsn(ILOAD, arg0);
    }

    public void iload(LocalVariable arg0) {
        super.visitVarInsn(ILOAD, arg0.idx);
    }

    public void iload(int... args) {
        for (int arg : args) {
            super.visitVarInsn(ILOAD, arg);
        }
    }

    public void iload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            super.visitVarInsn(ILOAD, arg.idx);
        }
    }
    
    public void lload(int arg0) {
        super.visitVarInsn(LLOAD, arg0);
    }

    public void lload(int... args) {
        for (int arg : args) {
            super.visitVarInsn(LLOAD, arg);
        }
    }

    public void lload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            super.visitVarInsn(LLOAD, arg.idx);
        }
    }
    
    public void fload(int arg0) {
        super.visitVarInsn(FLOAD, arg0);
    }

    public void fload(LocalVariable arg0) {
        super.visitVarInsn(FLOAD, arg0.idx);
    }

    public void fload(int... args) {
        for (int arg : args) {
            super.visitVarInsn(FLOAD, arg);
        }
    }
    
    public void dload(LocalVariable arg0) {
        super.visitVarInsn(DLOAD, arg0.idx);
    }

    public void dload(int arg0) {
        super.visitVarInsn(DLOAD, arg0);
    }

    public void dload(int... args) {
        for (int arg : args) {
            super.visitVarInsn(DLOAD, arg);
        }
    }
    
    public void astore(int arg0) {
        super.visitVarInsn(ASTORE, arg0);
    }

    public void astore(LocalVariable arg0) {
        super.visitVarInsn(ASTORE, arg0.idx);
    }
    
    public void istore(int arg0) {
        super.visitVarInsn(ISTORE, arg0);
    }

    public void istore(LocalVariable arg0) {
        super.visitVarInsn(ISTORE, arg0.idx);
    }
    
    public void lstore(int arg0) {
        super.visitVarInsn(LSTORE, arg0);
    }

    public void lstore(LocalVariable arg0) {
        super.visitVarInsn(LSTORE, arg0.idx);
    }
    
    public void fstore(int arg0) {
        super.visitVarInsn(FSTORE, arg0);
    }

    public void fstore(LocalVariable arg0) {
        super.visitVarInsn(FSTORE, arg0.idx);
    }
    
    public void dstore(int arg0) {
        super.visitVarInsn(DSTORE, arg0);
    }

    public void dstore(LocalVariable arg0) {
        super.visitVarInsn(DSTORE, arg0.idx);
    }
    
    public void ldc(Object arg0) {
        super.visitLdcInsn(arg0);
    }
    
    public void bipush(int arg) {
        super.visitIntInsn(BIPUSH, arg);
    }
    
    public void sipush(int arg) {
        super.visitIntInsn(SIPUSH, arg);
    }
        
    public void pushInt(int value) {
        if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
            switch (value) {
            case -1:
                iconst_m1();
                break;
            case 0:
                iconst_0();
                break;
            case 1:
                iconst_1();
                break;
            case 2:
                iconst_2();
                break;
            case 3:
                iconst_3();
                break;
            case 4:
                iconst_4();
                break;
            case 5:
                iconst_5();
                break;
            default:
                bipush(value);
                break;
            }
        } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
            sipush(value);
        } else {
            ldc(value);
        }
    }
        
    public void pushBoolean(boolean bool) {
        if (bool) iconst_1(); else iconst_0();
    }
    
    public void invokestatic(String arg1, String arg2, String arg3) {
        super.visitMethodInsn(INVOKESTATIC, arg1, arg2, arg3);
    }

    public void invokestatic(Class<?> recv, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        super.visitMethodInsn(INVOKESTATIC, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokespecial(String arg1, String arg2, String arg3) {
        super.visitMethodInsn(INVOKESPECIAL, arg1, arg2, arg3);
    }

    public void invokespecial(Class<?> recv, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        super.visitMethodInsn(INVOKESPECIAL, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokevirtual(String arg1, String arg2, String arg3) {
        super.visitMethodInsn(INVOKEVIRTUAL, arg1, arg2, arg3);
    }

    public void invokevirtual(Class<?> recv, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        super.visitMethodInsn(INVOKEVIRTUAL, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokeinterface(String arg1, String arg2, String arg3) {
        super.visitMethodInsn(INVOKEINTERFACE, arg1, arg2, arg3);
    }

    public void invokeinterface(Class<?> recv, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        super.visitMethodInsn(INVOKEINTERFACE, p(recv), methodName, sig(returnType, parameterTypes));
    }

    public void invokedynamic(String arg1, String arg2, String arg3) {
        super.visitMethodInsn(INVOKEDYNAMIC, arg1, arg2, arg3);
    }

    public void aprintln() {
        println(Object.class);
    }

    public void println(Class<?> klass) {
        if (!isPrintEnabled()) {
            return;
        }
        if (klass == void.class) {
            visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            visitLdcInsn("void");
            visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
            return;
        }
        String desc;
        if (byte.class == klass || klass == short.class || klass == char.class || klass == boolean.class || klass == int.class) {
            desc = "(I)V";
        } else if (klass == char[].class || klass.isPrimitive()) {
            desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(klass));
        } else {
            desc = "(Ljava/lang/Object;)V";
        }
        if (klass == double.class || klass == long.class) {
            dup2();
            getstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
            dup_x2();
            pop();
        } else {
            dup();
            getstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
            swap();
        }
        invokevirtual("java/io/PrintStream", "println", desc);
    }

    public void areturn() {
        super.visitInsn(ARETURN);
    }
    
    public void ireturn() {
        super.visitInsn(IRETURN);
    }
    
    public void freturn() {
        super.visitInsn(FRETURN);
    }
    
    public void lreturn() {
        super.visitInsn(LRETURN);
    }
    
    public void dreturn() {
        super.visitInsn(DRETURN);
    }
    
    public void newobj(String arg0) {
        super.visitTypeInsn(NEW, arg0);
    }
    
    public void dup() {
        super.visitInsn(DUP);
    }
    
    public void swap() {
        super.visitInsn(SWAP);
    }
    
    public void swap2() {
        dup2_x2();
        pop2();
    }
    
    public void getstatic(String arg1, String arg2, String arg3) {
        super.visitFieldInsn(GETSTATIC, arg1, arg2, arg3);
    }
    
    public void putstatic(String arg1, String arg2, String arg3) {
        super.visitFieldInsn(PUTSTATIC, arg1, arg2, arg3);
    }
    
    public void getfield(String arg1, String arg2, String arg3) {
        super.visitFieldInsn(GETFIELD, arg1, arg2, arg3);
    }
    
    public void putfield(String arg1, String arg2, String arg3) {
        super.visitFieldInsn(PUTFIELD, arg1, arg2, arg3);
    }
    
    public void voidreturn() {
        super.visitInsn(RETURN);
    }
    
    public void anewarray(String arg0) {
        super.visitTypeInsn(ANEWARRAY, arg0);
    }
    
    public void multianewarray(String arg0, int dims) {
        super.visitMultiANewArrayInsn(arg0, dims);
    }
    
    public void newarray(int arg0) {
        super.visitIntInsn(NEWARRAY, arg0);
    }
    
    public void iconst_m1() {
        super.visitInsn(ICONST_M1);
    }
    
    public void iconst_0() {
        super.visitInsn(ICONST_0);
    }
    
    public void iconst_1() {
        super.visitInsn(ICONST_1);
    }
    
    public void iconst_2() {
        super.visitInsn(ICONST_2);
    }
    
    public void iconst_3() {
        super.visitInsn(ICONST_3);
    }
    
    public void iconst_4() {
        super.visitInsn(ICONST_4);
    }
    
    public void iconst_5() {
        super.visitInsn(ICONST_5);
    }
    
    public void lconst_0() {
        super.visitInsn(LCONST_0);
    }
    
    public void aconst_null() {
        super.visitInsn(ACONST_NULL);
    }
    
    public void label(Label label) {
        super.visitLabel(label);
    }
    
    public void nop() {
        super.visitInsn(NOP);
    }
    
    public void pop() {
        super.visitInsn(POP);
    }
    
    public void pop2() {
        super.visitInsn(POP2);
    }
    
    public void arrayload() {
        super.visitInsn(AALOAD);
    }
    
    public void arraystore() {
        super.visitInsn(AASTORE);
    }
    
    public void iarrayload() {
        super.visitInsn(IALOAD);
    }
    
    public void barrayload() {
        super.visitInsn(BALOAD);
    }
    
    public void barraystore() {
        super.visitInsn(BASTORE);
    }
    
    public void aaload() {
        super.visitInsn(AALOAD);
    }
    
    public void aastore() {
        super.visitInsn(AASTORE);
    }
    
    public void iaload() {
        super.visitInsn(IALOAD);
    }
    
    public void iastore() {
        super.visitInsn(IASTORE);
    }
    
    public void laload() {
        super.visitInsn(LALOAD);
    }
    
    public void lastore() {
        super.visitInsn(LASTORE);
    }
    
    public void baload() {
        super.visitInsn(BALOAD);
    }
    
    public void bastore() {
        super.visitInsn(BASTORE);
    }
    
    public void saload() {
        super.visitInsn(SALOAD);
    }
    
    public void sastore() {
        super.visitInsn(SASTORE);
    }
    
    public void caload() {
        super.visitInsn(CALOAD);
    }
    
    public void castore() {
        super.visitInsn(CASTORE);
    }
    
    public void faload() {
        super.visitInsn(FALOAD);
    }
    
    public void fastore() {
        super.visitInsn(FASTORE);
    }
    
    public void daload() {
        super.visitInsn(DALOAD);
    }
    
    public void dastore() {
        super.visitInsn(DASTORE);
    }
    
    public void fcmpl() {
        super.visitInsn(FCMPL);
    }
    
    public void fcmpg() {
        super.visitInsn(FCMPG);
    }
    
    public void dcmpl() {
        super.visitInsn(DCMPL);
    }
    
    public void dcmpg() {
        super.visitInsn(DCMPG);
    }
    
    public void dup_x2() {
        super.visitInsn(DUP_X2);
    }
    
    public void dup_x1() {
        super.visitInsn(DUP_X1);
    }
    
    public void dup2_x2() {
        super.visitInsn(DUP2_X2);
    }
    
    public void dup2_x1() {
        super.visitInsn(DUP2_X1);
    }
    
    public void dup2() {
        super.visitInsn(DUP2);
    }
    
    public void trycatch(Label arg0, Label arg1, Label arg2,
                                   String arg3) {
        super.visitTryCatchBlock(arg0, arg1, arg2, arg3);
    }
    
    public void trycatch(String type, Runnable body, Runnable catchBody) {
        Label before = new Label();
        Label after = new Label();
        Label catchStart = new Label();
        Label done = new Label();

        trycatch(before, after, catchStart, type);
        label(before);
        body.run();
        label(after);
        go_to(done);
        if (catchBody != null) {
            label(catchStart);
            catchBody.run();
        }
        label(done);
    }
    
    public void go_to(Label arg0) {
        super.visitJumpInsn(GOTO, arg0);
    }
    
    public void lookupswitch(Label arg0, int[] arg1, Label[] arg2) {
        super.visitLookupSwitchInsn(arg0, arg1, arg2);
    }
    
    public void athrow() {
        super.visitInsn(ATHROW);
    }
    
    public void instance_of(String arg0) {
        super.visitTypeInsn(INSTANCEOF, arg0);
    }
    
    public void ifeq(Label arg0) {
        super.visitJumpInsn(IFEQ, arg0);
    }

    public void iffalse(Label arg0) {
        ifeq(arg0);
    }
    
    public void ifne(Label arg0) {
        super.visitJumpInsn(IFNE, arg0);
    }

    public void iftrue(Label arg0) {
        ifne(arg0);
    }
    
    public void if_acmpne(Label arg0) {
        super.visitJumpInsn(IF_ACMPNE, arg0);
    }
    
    public void if_acmpeq(Label arg0) {
        super.visitJumpInsn(IF_ACMPEQ, arg0);
    }
    
    public void if_icmple(Label arg0) {
        super.visitJumpInsn(IF_ICMPLE, arg0);
    }
    
    public void if_icmpgt(Label arg0) {
        super.visitJumpInsn(IF_ICMPGT, arg0);
    }

    public void if_icmpge(Label arg0) {
        super.visitJumpInsn(IF_ICMPGE, arg0);
    }

    public void if_icmplt(Label arg0) {
        super.visitJumpInsn(IF_ICMPLT, arg0);
    }
    
    public void if_icmpne(Label arg0) {
        super.visitJumpInsn(IF_ICMPNE, arg0);
    }
    
    public void if_icmpeq(Label arg0) {
        super.visitJumpInsn(IF_ICMPEQ, arg0);
    }
    
    public void checkcast(String arg0) {
        super.visitTypeInsn(CHECKCAST, arg0);
    }

    public void checkcast(Class<?> clazz) {
        super.visitTypeInsn(CHECKCAST, p(clazz));
    }
    
    public void start() {
        super.visitCode();
    }

    public void line(int line) {
        Label label = new Label();
        label(label);
        visitLineNumber(line, label);
    }

    public void line(int line, Label label) {
        visitLineNumber(line, label);
    }
    
    public void ifnonnull(Label arg0) {
        super.visitJumpInsn(IFNONNULL, arg0);
    }
    
    public void ifnull(Label arg0) {
        super.visitJumpInsn(IFNULL, arg0);
    }
    
    public void iflt(Label arg0) {
        super.visitJumpInsn(IFLT, arg0);
    }
    
    public void ifle(Label arg0) {
        super.visitJumpInsn(IFLE, arg0);
    }
    
    public void ifgt(Label arg0) {
        super.visitJumpInsn(IFGT, arg0);
    }
    
    public void ifge(Label arg0) {
        super.visitJumpInsn(IFGE, arg0);
    }
    
    public void arraylength() {
        super.visitInsn(ARRAYLENGTH);
    }
    
    public void ishr() {
        super.visitInsn(ISHR);
    }
    
    public void ishl() {
        super.visitInsn(ISHL);
    }
    
    public void iushr() {
        super.visitInsn(IUSHR);
    }
    
    public void lshr() {
        super.visitInsn(LSHR);
    }
    
    public void lshl() {
        super.visitInsn(LSHL);
    }
    
    public void lushr() {
        super.visitInsn(LUSHR);
    }
    
    public void lcmp() {
        super.visitInsn(LCMP);
    }
    
    public void iand() {
        super.visitInsn(IAND);
    }
    
    public void ior() {
        super.visitInsn(IOR);
    }
    
    public void ixor() {
        super.visitInsn(IXOR);
    }
    
    public void land() {
        super.visitInsn(LAND);
    }
    
    public void lor() {
        super.visitInsn(LOR);
    }
    
    public void lxor() {
        super.visitInsn(LXOR);
    }
    
    public void iadd() {
        super.visitInsn(IADD);
    }
    
    public void ladd() {
        super.visitInsn(LADD);
    }
    
    public void fadd() {
        super.visitInsn(FADD);
    }
    
    public void dadd() {
        super.visitInsn(DADD);
    }
    
    public void isub() {
        super.visitInsn(ISUB);
    }
    
    public void lsub() {
        super.visitInsn(LSUB);
    }
    
    public void fsub() {
        super.visitInsn(FSUB);
    }
    
    public void dsub() {
        super.visitInsn(DSUB);
    }
    
    public void idiv() {
        super.visitInsn(IDIV);
    }
    
    public void irem() {
        super.visitInsn(IREM);
    }
    
    public void ineg() {
        super.visitInsn(INEG);
    }
    
    public void i2d() {
        super.visitInsn(I2D);
    }
    
    public void i2l() {
        super.visitInsn(I2L);
    }
    
    public void i2f() {
        super.visitInsn(I2F);
    }
    
    public void i2s() {
        super.visitInsn(I2S);
    }
    
    public void i2c() {
        super.visitInsn(I2C);
    }
    
    public void i2b() {
        super.visitInsn(I2B);
    }
    
    public void ldiv() {
        super.visitInsn(LDIV);
    }
    
    public void lrem() {
        super.visitInsn(LREM);
    }
    
    public void lneg() {
        super.visitInsn(LNEG);
    }
    
    public void l2d() {
        super.visitInsn(L2D);
    }
    
    public void l2i() {
        super.visitInsn(L2I);
    }
    
    public void l2f() {
        super.visitInsn(L2F);
    }
    
    public void fdiv() {
        super.visitInsn(FDIV);
    }
    
    public void frem() {
        super.visitInsn(FREM);
    }
    
    public void fneg() {
        super.visitInsn(FNEG);
    }
    
    public void f2d() {
        super.visitInsn(F2D);
    }
    
    public void f2i() {
        super.visitInsn(F2D);
    }
    
    public void f2l() {
        super.visitInsn(F2L);
    }
    
    public void ddiv() {
        super.visitInsn(DDIV);
    }
    
    public void drem() {
        super.visitInsn(DREM);
    }
    
    public void dneg() {
        super.visitInsn(DNEG);
    }
    
    public void d2f() {
        super.visitInsn(D2F);
    }
    
    public void d2i() {
        super.visitInsn(D2I);
    }
    
    public void d2l() {
        super.visitInsn(D2L);
    }
    
    public void imul() {
        super.visitInsn(IMUL);
    }
    
    public void lmul() {
        super.visitInsn(LMUL);
    }
    
    public void fmul() {
        super.visitInsn(FMUL);
    }
    
    public void dmul() {
        super.visitInsn(DMUL);
    }
    
    public void iinc(int arg0, int arg1) {
        super.visitIincInsn(arg0, arg1);
    }

    public void iinc(LocalVariable arg0, int arg1) {
        super.visitIincInsn(arg0.idx, arg1);
    }
    
    public void monitorenter() {
        super.visitInsn(MONITORENTER);
    }
    
    public void monitorexit() {
        super.visitInsn(MONITOREXIT);
    }
    
    public void jsr(Label branch) {
        super.visitJumpInsn(JSR, branch);
    }
    
    public void ret(int arg0) {
        super.visitVarInsn(RET, arg0);
    }
    
    public void visitAnnotationWithFields(String name, boolean visible, Map<String,Object> fields) {
        AnnotationVisitor visitor = visitAnnotation(name, visible);
        visitAnnotationFields(visitor, fields);
        visitor.visitEnd();
    }

    public void visitParameterAnnotationWithFields(int param, String name, boolean visible, Map<String,Object> fields) {
        AnnotationVisitor visitor = visitParameterAnnotation(param, name, visible);
        visitAnnotationFields(visitor, fields);
        visitor.visitEnd();
    }

    public void tableswitch(int min, int max, Label defaultLabel, Label[] cases) {
        super.visitTableSwitchInsn(min, max, defaultLabel, cases);
    }


}
