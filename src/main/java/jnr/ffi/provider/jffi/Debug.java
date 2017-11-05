package jnr.ffi.provider.jffi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

abstract class Debug {

    static Debug getInstance() {
        return Sigleton.INSTANCE;
    }

    public abstract boolean isOn();

    public abstract ClassVisitor wrapClassWriter(ClassVisitor cv);

    public abstract void trace(String internalName, byte[] content);

    private static class Sigleton extends Debug {

        static final Debug INSTANCE;

        static {
            boolean dump = Util.getBooleanProperty("jnr.ffi.compile.dump", false);
            String dir = Util.getStringProperty("jnr.ffi.compile.dump.dir", null);
            if ("".equals(dir)) {
                dir = null;
            }
            File baseDir = dir != null ? new File(dir) : null;
            INSTANCE = new Sigleton(dump, baseDir);
        }

        private static void doTrace(byte[] content) {
            ClassVisitor trace;
            try {
                trace = Class.forName("org.objectweb.asm.util.TraceClassVisitor")
                        .asSubclass(ClassVisitor.class)
                        .getConstructor(PrintWriter.class)
                        .newInstance(new PrintWriter(System.err, true));
            } catch (Throwable error) {
                return;
            }
            new ClassReader(content).accept(trace, 0);
        }

        private static ClassVisitor doWrap(ClassVisitor cv) {
            try {
                return Class.forName("org.objectweb.asm.util.CheckClassAdapter")
                        .asSubclass(ClassVisitor.class)
                        .getConstructor(ClassVisitor.class, boolean.class)
                        .newInstance(cv, false);
            } catch (Throwable t) {
                return cv;
            }
        }

        private static void writeToFile(File baseDir, String internalName, byte[] content) {
            try {
                File file = new File(baseDir, internalName + ".class");
                file.getParentFile().mkdirs();
                FileOutputStream is = new FileOutputStream(file);
                try {
                    is.write(content);
                } finally {
                    is.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private static boolean isClassPresent(String name) {
            try {
                Class.forName(name);
            } catch (Throwable ex) {
                return false;
            }
            return true;
        }

        private final boolean dump;
        private final File dir;
        private final boolean checkClassExists;
        private final boolean traceClassExists;

        private Sigleton(boolean dump, File dir) {
            this.dump = dump;
            this.dir = dir;
            this.checkClassExists = isClassPresent("org.objectweb.asm.util.CheckClassAdapter");
            this.traceClassExists = isClassPresent("org.objectweb.asm.util.TraceClassVisitor");
        }

        @Override
        public boolean isOn() {
            return dump;
        }

        @Override
        public ClassVisitor wrapClassWriter(ClassVisitor cv) {
            if (dump && checkClassExists) {
                return doWrap(cv);
            }
            return cv;
        }

        @Override
        public void trace(String internalName, byte[] content) {
            if (dump && traceClassExists) {
                doTrace(content);
            }
            if (dir != null) {
                writeToFile(dir, internalName, content);
            }
        }

    }

}
