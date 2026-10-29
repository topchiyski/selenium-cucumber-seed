package com.example.e2e.logging;

import java.util.Locale;
import java.util.regex.Pattern;

/** Keeps secrets out of logs. */
public final class Masking {

    /** Replacement text for masked values. */
    public static final String MASK = "******";
    private static final Pattern SENSITIVE_NAME =
            Pattern.compile(".*(password|passwd|secret|token|api[-_.]?key|credential|authorization|cookie).*");
    private static final Pattern INLINE_SECRET =
            Pattern.compile("(?i)(password|passwd|secret|token|api[-_ ]?key)(\\W{0,3}\\s*)\"[^\"]*\"");

    private Masking() {
    }

    /**
     * True if a config key or header name looks like it holds a secret.
     *
     * @param name config key or header name
     * @return {@code true} if the name suggests a secret
     */
    public static boolean isSensitive(String name) {
        return name != null && SENSITIVE_NAME.matcher(name.toLowerCase(Locale.ROOT)).matches();
    }

    /**
     * Returns the value, or a mask if the name looks sensitive.
     *
     * @param name config key or header name
     * @param value the value
     * @return {@link #MASK} for sensitive names, otherwise {@code value}
     */
    public static String mask(String name, String value) {
        return isSensitive(name) ? MASK : value;
    }

    /**
     * Masks quoted values that follow a secret-looking word in free text, e.g. a Gherkin step
     * {@code I log in with password "hunter2"} becomes {@code ... password "******"}.
     *
     * @param text free text such as a Gherkin step
     * @return the text with such values replaced by {@link #MASK}
     */
    public static String maskInline(String text) {
        return text == null ? null : INLINE_SECRET.matcher(text).replaceAll("$1$2\"" + MASK + "\"");
    }

    /**
     * Shortens long payloads (response bodies, page sources) for log lines.
     *
     * @param text the text
     * @param maxChars maximum characters to keep
     * @return the text, cut with a note on how much was removed if longer than {@code maxChars}
     */
    public static String truncate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "... (" + (text.length() - maxChars) + " more characters)";
    }
}
