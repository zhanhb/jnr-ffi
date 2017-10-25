/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.provider.converters;

import jnr.ffi.mapper.*;
import jnr.ffi.util.EnumMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Set;

@FromNativeConverter.Cacheable
@ToNativeConverter.Cacheable
public final class EnumSetConverter<E extends Enum<E>> implements DataConverter<Set<E>, Integer> {
    private final Class<E> enumClass;
    private final EnumMapper<E> enumMapper;
    private final EnumSet<E> allValues;

    private EnumSetConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.enumMapper = EnumMapper.getInstance(enumClass);
        this.allValues = EnumSet.allOf(enumClass);
    }

    public static <E extends Enum<E>> ToNativeConverter<Set<E>, Integer> getToNativeConverter(SignatureType type, ToNativeContext toNativeContext) {
        return getInstance(type.getGenericType());
    }

    public static <E extends Enum<E>> FromNativeConverter<Set<E>, Integer> getFromNativeConverter(SignatureType type, FromNativeContext fromNativeContext) {
        return getInstance(type.getGenericType());
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> EnumSetConverter<E> getInstance(Type parameterizedType) {
        if (!(parameterizedType instanceof ParameterizedType)) {
            return null;
        }

        if (((ParameterizedType) parameterizedType).getActualTypeArguments().length < 1) {
            return null;
        }

        Type enumType = ((ParameterizedType) parameterizedType).getActualTypeArguments()[0];
        if (!(enumType instanceof Class) || !Enum.class.isAssignableFrom((Class) enumType)) {
            return null;
        }

        return new EnumSetConverter<E>(((Class) enumType).asSubclass(Enum.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<E> fromNative(Integer nativeValue, FromNativeContext context) {
        EnumSet<E> enums = EnumSet.noneOf(enumClass);
        for (E e : allValues) {
            int enumValue = enumMapper.intValue(e);
            if ((nativeValue & enumValue) == enumValue) {
                enums.add(e);
            }
        }

        return enums;
    }

    @Override
    public Integer toNative(Set<E> value, ToNativeContext context) {
        int intValue = 0;
        for (E e : value) {
            intValue |= enumMapper.intValue(e);
        }

        return intValue;
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
