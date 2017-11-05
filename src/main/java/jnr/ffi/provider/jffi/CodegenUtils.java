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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.Map;

public class CodegenUtils {
    /**
     * Creates a dotted class name from a path/package name.
     *
     * @param p The path/package name.
     * @return The dotted class name.
     */
    public static String c(String p) {
        return p.replace('/', '.');
    }

    /**
     * Creates a class path name, from a Class.
     *
     * @param n A class.
     * @return The class path name.
     */
    public static String p(Class<?> n) {
        return Type.getInternalName(n);
    }

    /**
     * Creates a class path name, from a class name.
     *
     * @param n A class name.
     * @return A class path name.
     */
    public static String p(String n) {
        return n.replace('.', '/');
    }

    /**
     * Creates a class identifier of form Labc/abc;, from a Class.
     *
     * @param n A class.
     * @return The class identifier.
     */
    public static String ci(Class<?> n) {
        return Type.getDescriptor(n);
    }

    /**
     * Create a method signature from the given param types and return values.
     *
     * @param retval The return value class.
     * @param params The parameters.
     * @return A method signature.
     */
    public static String sig(Class<?> retval, Class<?>... params) {
        return sigParams(params) + ci(retval);
    }

    public static String sig(Class<?> retval, String descriptor, Class<?>... params) {
        return sigParams(descriptor, params) + ci(retval);
    }

    public static String sigParams(Class<?>... params) {
        StringBuilder signature = new StringBuilder("(");
        
        for (Class<?> param : params) {
            signature.append(ci(param));
        }
        
        signature.append(")");
        
        return signature.toString();
    }

    public static String sigParams(String descriptor, Class<?>... params) {
        StringBuilder signature = new StringBuilder("(");

        signature.append(descriptor);
        
        for (Class<?> param : params) {
            signature.append(ci(param));
        }

        signature.append(")");

        return signature.toString();
    }

    public static void visitAnnotationFields(AnnotationVisitor visitor, Map<String, Object> fields) {
        for (Map.Entry<String, Object> fieldEntry : fields.entrySet()) {
            Object value = fieldEntry.getValue();
            if (value.getClass().isArray()) {
                Object[] values = (Object[]) value;
                AnnotationVisitor arrayV = visitor.visitArray(fieldEntry.getKey());
                for (Object value1 : values) {
                    arrayV.visit(null, value1);
                }
                arrayV.visitEnd();
            } else if (value.getClass().isEnum()) {
                visitor.visitEnum(fieldEntry.getKey(), ci(value.getClass()), Enum.class.cast(value).name());
            } else if (value instanceof Class) {
                visitor.visit(fieldEntry.getKey(), Type.getType((Class)value));
            } else {
                visitor.visit(fieldEntry.getKey(), value);
            }
        }
    }
}
