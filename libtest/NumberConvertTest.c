// this source help the call stack to be correct
// perform the type convention natively

typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;

typedef unsigned char u_int8_t;
typedef unsigned short u_int16_t;
typedef unsigned int u_int32_t;
typedef unsigned long long u_int64_t;

const int64_t ERROR = 0xCCCCCCCCCCCCCCCCLL;

#define CONVERT(a, b)                           \
b a##2##b(a x) {                                \
    return x;                                   \
}

#define DEF(x, y)                               \
CONVERT(int##x##_t, int##y##_t)                 \
CONVERT(u_int##x##_t, int##y##_t)               \
CONVERT(int##x##_t, u_int##y##_t)               \
CONVERT(u_int##x##_t, u_int##y##_t)

DEF(8, 8);
DEF(8, 16);
DEF(8, 32);
DEF(8, 64);

DEF(16, 8);
DEF(16, 16);
DEF(16, 32);
DEF(16, 64);

DEF(32, 8);
DEF(32, 16);
DEF(32, 32);
DEF(32, 64);

DEF(64, 8);
DEF(64, 16);
DEF(64, 32);
DEF(64, 64);
#undef DEF

// boolean in java is an native char
// but what we got is int32
// @see jnr.ffi.provider.jffi.Types#lookupType(jnr.ffi.Runtime, java.lang.Class, java.util.Collection)
#define boolean int32_t

#define CONVERT_BOOLEAN(T)          \
boolean T##2boolean(T x) {          \
    return !!x;                     \
}                                   \
T boolean2##T(boolean x) {          \
    if (!!x != x) {                 \
        return (T) ERROR;           \
    }                               \
    return x;                       \
}

#define DEFB(SIZE)                  \
CONVERT_BOOLEAN(int##SIZE##_t)      \
CONVERT_BOOLEAN(u_int##SIZE##_t)

DEFB(8);
DEFB(16);
DEFB(32);
DEFB(64);

boolean boolean2boolean(boolean x) {
    if (!!x != x) {
        return (boolean) ERROR;
    }
    return x;
}

#define I2F(i)                      \
CONVERT(int##i##_t, float)          \
CONVERT(int##i##_t, double)         \
CONVERT(u_int##i##_t, float)        \
CONVERT(u_int##i##_t, double)

I2F(8)
I2F(16)
I2F(32)
I2F(64)

#define F2I(i)                      \
CONVERT(float, int##i##_t)          \
CONVERT(double, int##i##_t)         \
CONVERT(float, u_int##i##_t)        \
CONVERT(double, u_int##i##_t)

F2I(8)
F2I(16)
F2I(32)
F2I(64)

CONVERT_BOOLEAN(double)
CONVERT_BOOLEAN(float)

CONVERT(float, float)
CONVERT(float, double)
CONVERT(double, float)
CONVERT(double, double)
