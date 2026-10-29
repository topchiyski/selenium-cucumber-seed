package com.example.e2e.logging;

import java.util.Locale;
import java.util.regex.Pattern;

/** Keeps secrets out of logs. */
public final class Masking {

    public static final String MASK = "******";
    private static final Pattern SENSITIVE_NAME =
            Pattern.compile(".*(password|passwd|secret|token|api[-_.]?key|credential|authorization|cookie).*");
    private static final Pattern INLINE_SECRET =
            Pattern.compile("(?i)(password|passwd|secret|token|api[-_ ]?key)(\\W{0,3}\\s*)\"[^\"]*\"");

    private Masking() {
    }

    /** True if a config key or header name looks like it holds a secret. */
    public static boolean isSensitive(String name) {
        return name != null && SENSITIVE_NAME.matcher(name.toLowerCase(Locale.ROOT)).matches();
    }

    /** Returns the value, or a mask if the name looks sensitive. */
    public static String mask(String name, String value) {
        return isSensitive(name) ? MASK : value;
    }

    /**
     * Masks quoted values that follow a secret-looking word in free text, e.g. a Gherkin step
     * {@code I log in with password "hunter2"} becomes {@code ... password "******"}.
     */
    public static String maskInline(String text) {
        return text == null ? null : INLINE_SECRET.matcher(text).replaceAll("$1$2\"" + MASK + "\"");
    }

    /** Shortens long payloads (response bodies, page sources) for log lines. */
    public static String truncate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "... (" + (text.length() - maxChars) + " more characters)";
    }
}
