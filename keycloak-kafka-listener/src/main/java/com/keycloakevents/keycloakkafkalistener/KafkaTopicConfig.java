package com.keycloakevents.keycloakkafkalistener;

import java.util.*;
import java.util.stream.Collectors;

final class KafkaTopicConfig {

    private KafkaTopicConfig() {}

    static String get(SafeConfig c, String key, String def) {
        String v = c.get(key);
        if (v == null) return def;
        v = v.trim();
        return v.isEmpty() ? def : v;
    }

    static boolean getBool(SafeConfig c, String key, boolean def) {
        String v = c.get(key);
        if (v == null) return def;
        v = v.trim().toLowerCase(Locale.ROOT);
        if (v.isEmpty()) return def;
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y");
    }

    static int getInt(SafeConfig c, String key, int def, int min, int max) {
        String v = c.get(key);
        if (v == null) return def;
        try {
            int n = Integer.parseInt(v.trim());
            if (n < min) return min;
            if (n > max) return max;
            return n;
        } catch (Exception e) {
            return def;
        }
    }

    static long getLong(SafeConfig c, String key, long def, long min, long max) {
        String v = c.get(key);
        if (v == null) return def;
        try {
            long n = Long.parseLong(v.trim());
            if (n < min) return min;
            if (n > max) return max;
            return n;
        } catch (Exception e) {
            return def;
        }
    }

    static Set<String> parseUpperCsv(String csv) {
        if (csv == null) return Collections.emptySet();
        String s = csv.trim();
        if (s.isEmpty()) return Collections.emptySet();

        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .map(x -> x.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    interface SafeConfig {
        String get(String key);
    }
}
