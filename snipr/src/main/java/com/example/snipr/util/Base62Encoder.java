package com.example.snipr.util;

/**
 * Turns a numeric ID into a short alphanumeric string.
 *
 * Why base62 instead of a random string or hash?
 * - Zero collision risk: the DB's auto-increment id is already unique, so
 *   encoding it can never produce a duplicate. Random strings would need a
 *   "does this already exist?" check (and a retry loop) on every creation.
 * - It's reversible: decode(encode(x)) == x. We don't strictly need decoding
 *   for the shortener itself (we look up by shortCode in the DB), but it's
 *   a useful property to understand -- this is a pure encoding, not hashing.
 *
 * 62 = 10 digits + 26 lowercase + 26 uppercase letters.
 * With 6 characters, that's 62^6 ≈ 56 billion unique codes -- plenty.
 */
public final class Base62Encoder {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    private Base62Encoder() {} // utility class, never instantiated

    public static String encode(long id) {
        if (id == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb = new StringBuilder();
        long value = id;
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(ALPHABET.charAt(remainder));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + ALPHABET.indexOf(c);
        }
        return result;
    }
}