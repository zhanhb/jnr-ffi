/*
 * Copyright (C) 2013 Wayne Meissner
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

package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 *
 */
public class ToNativeType extends SigType implements jnr.ffi.mapper.ToNativeType {
    private final ToNativeConverter<?, ?> toNativeConverter;
    private final ToNativeContext toNativeContext;

    public ToNativeType(Class<?> javaType, NativeType nativeType, Collection<Annotation> annotations,
                 ToNativeConverter<?, ?> toNativeConverter, ToNativeContext toNativeContext) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
        this.toNativeContext = toNativeContext;
    }

    @Override
    public final ToNativeConverter<?, ?> getToNativeConverter() {
        return toNativeConverter;
    }

    public ToNativeContext getToNativeContext() {
        return toNativeContext;
    }
}
