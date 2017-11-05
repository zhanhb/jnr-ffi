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

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Closure;
import com.kenai.jffi.ClosureMagazine;
import com.kenai.jffi.ClosureManager;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;
import jnr.ffi.util.ref.FinalizableWeakReference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import jnr.ffi.Pointer;

import static jnr.ffi.provider.jffi.ClosureUtil.getParameterType;
import static jnr.ffi.provider.jffi.ClosureUtil.getResultType;
import static jnr.ffi.provider.jffi.InvokerUtil.getCallContext;
import static jnr.ffi.provider.jffi.InvokerUtil.getNativeCallingConvention;

/**
 *
 */
public final class NativeClosureFactory<T> {
    private final jnr.ffi.Runtime runtime;
    private final ConcurrentMap<Object, ClosureReference> closures = new ConcurrentWeakIdentityHashMap<Object, ClosureReference>();
    private final CallContext callContext;
    private final NativeClosureProxy.Factory closureProxyFactory;
    private final ConcurrentLinkedQueue<NativeClosurePointer> freeQueue = new ConcurrentLinkedQueue<NativeClosurePointer>();
    private ClosureMagazine currentMagazine;


    protected NativeClosureFactory(jnr.ffi.Runtime runtime, CallContext callContext,
                                   NativeClosureProxy.Factory closureProxyFactory) {
        this.runtime = runtime;
        this.closureProxyFactory = closureProxyFactory;
        this.callContext = callContext;
    }

    static <T> NativeClosureFactory<T> newClosureFactory(jnr.ffi.Runtime runtime, Class<T> closureClass,
                                                      SignatureTypeMapper typeMapper, AsmClassLoader classLoader) {

        Method callMethod = null;
        for (Method m : closureClass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class)
                    && (m.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC) {
                callMethod = m;
                break;
            }
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no public non-static delegate method defined in " + closureClass.getName());
        }

        Class<?>[] parameterTypes = callMethod.getParameterTypes();
        FromNativeType[] parameterSigTypes = new FromNativeType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            parameterSigTypes[i] = getParameterType(runtime, callMethod, i, typeMapper);
        }
        ToNativeType resultType = getResultType(runtime, callMethod, typeMapper);

        return new NativeClosureFactory<T>(runtime, getCallContext(resultType, parameterSigTypes, getNativeCallingConvention(callMethod), false),
                NativeClosureProxy.newProxyFactory(runtime, callMethod, resultType, parameterSigTypes, classLoader));
    }

    private void recycle(NativeClosurePointer ptr) {
        freeQueue.add(ptr);
    }

    static final class ClosureReference extends FinalizableWeakReference<Object> {
        private final NativeClosureFactory<?> factory;
        private final NativeClosurePointer pointer;


        private ClosureReference(Object referent, NativeClosureFactory<?> factory, NativeClosurePointer pointer) {
            super(referent, NativeFinalizer.getInstance().getFinalizerQueue());
            this.factory = factory;
            this.pointer = pointer;
        }

        @Override
        public void finalizeReferent() {
            clear();
            factory.recycle(pointer);
        }

        Object getCallable() {
            return get();
        }

        Pointer getPointer() {
            return pointer;
        }
    }

    private NativeClosurePointer allocateClosurePointer() {
        NativeClosurePointer closurePointer = freeQueue.poll();
        if (closurePointer != null) {
            return closurePointer;
        }

        NativeClosureProxy proxy = closureProxyFactory.newClosureProxy();
        Closure.Handle closureHandle = null;

        synchronized (this) {
            do {
                if (currentMagazine == null || ((closureHandle = currentMagazine.allocate(proxy)) == null)) {
                    currentMagazine = ClosureManager.getInstance().newClosureMagazine(callContext,
                            closureProxyFactory.getInvokeMethod());
                }
            } while (closureHandle == null);
        }

        return new NativeClosurePointer(runtime, closureHandle, proxy);
    }

    private ClosureReference newClosureReference(Object callable) {

        NativeClosurePointer ptr = allocateClosurePointer();
        ClosureReference ref = new ClosureReference(callable, this, ptr);
        ptr.proxy.closureReference = ref;
        ClosureReference old = closures.putIfAbsent(callable, ref);
        return old == null ? ref : old;
    }

    ClosureReference getClosureReference(Object callable) {
        ClosureReference ref = closures.get(callable);
        if (ref != null) {
            return ref;
        }

        return newClosureReference(callable);
    }
}
