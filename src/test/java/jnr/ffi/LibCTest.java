package jnr.ffi;

import java.io.IOException;
import jnr.ffi.types.size_t;
import org.junit.BeforeClass;
import org.junit.Test;

public class LibCTest {

    private static Libc libc;

    public static interface Libc {
        @size_t int strlen(byte[] str); // this may fail on x64
    }

    @BeforeClass
    public static void setupClass() {
        libc = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getStandardCLibraryName());
    }

    @Test
    public void testStrlen() throws IOException {
        byte[] bytes = "hello,world.\u0000".getBytes("UTF-8");
        libc.strlen(bytes);
    }

}
