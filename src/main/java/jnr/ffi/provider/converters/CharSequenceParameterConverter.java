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

import jnr.ffi.annotations.Encoding;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Arrays;
import java.util.Collection;

import static jnr.ffi.provider.converters.StringUtil.getEncoder;
import static jnr.ffi.provider.converters.StringUtil.throwException;

/**
 * Converts a CharSequence (e.g. String) to a primitive ByteBuffer array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class CharSequenceParameterConverter implements ToNativeConverter<CharSequence, ByteBuffer> {
    private static final ToNativeConverter<CharSequence, ByteBuffer> DEFAULT = new CharSequenceParameterConverter(Charset.defaultCharset());
    private final ThreadLocal<Reference<CharsetEncoder>> localEncoder = new ThreadLocal<Reference<CharsetEncoder>>();

    private final Charset charset;


    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(Charset charset, ToNativeContext toNativeContext) {
        return Charset.defaultCharset().equals(charset) ? DEFAULT : new CharSequenceParameterConverter(charset);
    }

    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(ToNativeContext toNativeContext) {
        Charset charset = StringUtil.getCharset(toNativeContext);
        return getInstance(charset, toNativeContext);
    }

    private CharSequenceParameterConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ByteBuffer toNative(CharSequence string, ToNativeContext context) {
        if (string == null) {
            return null;
        }

        CharsetEncoder encoder = getEncoder(charset, localEncoder);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) (string.length() * encoder.averageBytesPerChar()) + 4);
        CharBuffer charBuffer = CharBuffer.wrap(string);

        encoder.reset();
        while (charBuffer.hasRemaining()) {
            CoderResult result = encoder.encode(charBuffer, byteBuffer, true);

            if (result.isUnderflow() && (result = encoder.flush(byteBuffer)).isUnderflow()) {
                break;

            } else if (result.isOverflow()) {
                // Output buffer is full; expand and continue encoding
                byteBuffer = grow(byteBuffer);

            } else {
                throwException(result);
            }
        }

        // ensure native memory is NUL terminated (assume max wchar_t 4 byte termination needed)
        if (byteBuffer.remaining() <= 4) byteBuffer = grow(byteBuffer);
        byteBuffer.position(byteBuffer.position() + 4);

        byteBuffer.flip();

        return byteBuffer;
    }

    private static ByteBuffer grow(ByteBuffer oldBuffer) {
        ByteBuffer buf = ByteBuffer.allocate(oldBuffer.capacity() * 2);
        oldBuffer.flip();
        buf.put(oldBuffer);
        return buf;
    }

    @Override
    @In
    @NulTerminate
    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }
}
