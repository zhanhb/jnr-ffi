/*
 * Copyright (C) 2011 Wayne Meissner
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

import jnr.ffi.Pointer;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.ClosureManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
final class NativeClosureManager implements ClosureManager {
    private volatile ConcurrentMap<Class<?>, NativeClosureFactory<?>> factories = new ConcurrentHashMap<Class<?>, NativeClosureFactory<?>>();
    private final jnr.ffi.Runtime runtime;
    private final SignatureTypeMapper typeMapper;
    private final AsmClassLoader classLoader;

    NativeClosureManager(jnr.ffi.Runtime runtime, SignatureTypeMapper typeMapper, AsmClassLoader classLoader) {
        this.runtime = runtime;
        this.typeMapper = new CompositeTypeMapper(typeMapper, new CachingTypeMapper(new ClosureTypeMapper()));
        this.classLoader = classLoader;
    }

    <T> NativeClosureFactory<T> getClosureFactory(Class<T> closureClass) {
        @SuppressWarnings("unchecked")
        NativeClosureFactory<T> factory = (NativeClosureFactory<T>) factories.get(closureClass);
        if (factory != null) {
            return factory;
        }

        return initClosureFactory(closureClass);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <T> T newClosure(Class<? extends T> closureClass, T instance) {
        @SuppressWarnings("unchecked")
        NativeClosureFactory<T> factory = (NativeClosureFactory<T>) factories.get(closureClass);
        if (factory != null) {
            //return factory.newClosure(instance);
        }
        return null;
    }

    @Override
    public final <T> jnr.ffi.Pointer getClosurePointer(Class<? extends T> closureClass, T instance) {
        return getClosureFactory(closureClass).getClosureReference(instance).getPointer();
    }

    <T> NativeClosureFactory<T> initClosureFactory(Class<T> closureClass) {
        NativeClosureFactory<T> factory = NativeClosureFactory.newClosureFactory(runtime, closureClass, typeMapper, classLoader);
        @SuppressWarnings("unchecked")
        NativeClosureFactory<T> old = (NativeClosureFactory<T>) factories.putIfAbsent(closureClass, factory);
        return old != null ? old : factory;
    }

    <T> ToNativeConverter<T, Pointer> newClosureSite(Class<T> closureClass) {
        return new ClosureSite<T>(getClosureFactory(closureClass));
    }

    @ToNativeConverter.NoContext
    public static final class ClosureSite<T> implements ToNativeConverter<T, Pointer> {
        private final NativeClosureFactory<T> factory;
        private NativeClosureFactory.ClosureReference closureReference = null;

        private ClosureSite(NativeClosureFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public Pointer toNative(T value, ToNativeContext context) {
            if (value == null) {
                return null;
            }
            
            // If passing down a function pointer, don't re-wrap it
            if (value instanceof ClosureFromNativeConverter.AbstractClosurePointer) {
                return (ClosureFromNativeConverter.AbstractClosurePointer) value;
            }

            NativeClosureFactory.ClosureReference ref = closureReference;
            // Fast path - same delegate as last call to this site - just re-use the native closure
            if (ref != null && ref.getCallable() == value) {
                return ref.getPointer();
            }

            ref = factory.getClosureReference(value);
            // Cache the new native closure, if this site has no valid native closure
            if (closureReference == null || closureReference.get() == null) {
                closureReference = ref;
            }

            return ref.getPointer();
        }

        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
    }
}
