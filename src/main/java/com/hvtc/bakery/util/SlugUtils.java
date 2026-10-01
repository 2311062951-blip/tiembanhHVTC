package com.hvtc.bakery.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern DUPLICATE_HYPHENS = Pattern.compile("-+");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);

        // Replace Vietnamese specific characters like Đ/đ
        normalized = normalized.replace('Đ', 'D').replace('đ', 'd');

        // Remove diacritics
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = DUPLICATE_HYPHENS.matcher(slug).replaceAll("-");

        return slug.toLowerCase(Locale.ENGLISH).replaceAll("^-|-$", "");
    }
}
