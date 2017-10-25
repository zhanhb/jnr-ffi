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


@ToNativeConverter.NoContext
@FromNativeConverter.NoContext
@ToNativeConverter.Cacheable
@FromNativeConverter.Cacheable
public final class EnumConverter<E extends Enum<E>> implements DataConverter<E, Integer> {
    private final EnumMapper<E> mapper;

    public static <E extends Enum<E>> EnumConverter<E> getInstance(Class<E> enumClass) {
        return new EnumConverter<E>(enumClass);
    }

    private EnumConverter(Class<E> enumClass) {
        this.mapper = EnumMapper.getInstance(enumClass);
    }

    @Override
    public E fromNative(Integer nativeValue, FromNativeContext context) {
        return mapper.valueOf(nativeValue);
    }

    @Override
    public Integer toNative(E value, ToNativeContext context) {
        return mapper.integerValue(value);
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}
