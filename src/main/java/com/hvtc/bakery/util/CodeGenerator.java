package com.hvtc.bakery.util;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateOrderCode() {
        StringBuilder sb = new StringBuilder("HVTC-");
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }

    public static String generateProductCode() {
        StringBuilder sb = new StringBuilder("BAKE-");
        for (int i = 0; i < 5; i++) {
            int index = RANDOM.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }
}
