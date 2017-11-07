/*
 * Copyright (C) 2008-2012 Wayne Meissner
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * InvocationHandler used to map invocations on a java interface to the correct native function.
 */
public class NativeInvocationHandler implements InvocationHandler {
    private final ConcurrentMap<Method, Invoker> fastLookupTable;
    private final Map<Method, Invoker> invokerMap;

    /**
     * Creates a new InvocationHandler instance.
     * 
     * @param invokers A map of method invokers
     *
     */
    public NativeInvocationHandler(Map<Method, Invoker> invokers) {
        this.invokerMap = invokers;
        this.fastLookupTable = new ConcurrentHashMap<Method, Invoker>();
    }

    @Override
    public Object invoke(Object self, Method method, Object[] argArray) throws Throwable {
        return lookupAndCacheInvoker(method).invoke(self, argArray);
    }

    private Invoker lookupAndCacheInvoker(Method method) {
        Invoker invoker = fastLookupTable.get(method);
        if (invoker != null) {
            return invoker;
        }
        invoker = invokerMap.get(method);
        if (invoker == null) {
            throw new UnsatisfiedLinkError("no invoker for native method " + method.getName());
        }
        Invoker old = fastLookupTable.putIfAbsent(method, invoker);
        return old != null ? old : invoker;
    }

}
