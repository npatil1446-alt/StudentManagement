package com.smartedu.util;

import java.util.*;

public class Json {

    public static String str(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }

    public static String num(Number n) { return n == null ? "null" : n.toString(); }
    public static String bool(boolean b) { return b ? "true" : "false"; }

    public static String obj(String... keyValues) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(keyValues[i]).append("\":").append(keyValues[i + 1]);
        }
        return sb.append("}").toString();
    }

    public static String arr(List<String> items) {
        return "[" + String.join(",", items) + "]";
    }

    public static Map<String, String> parse(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        int i = 0;
        while (i < json.length()) {
            while (i < json.length() && (json.charAt(i) == ' ' || json.charAt(i) == ',' || json.charAt(i) == '\n' || json.charAt(i) == '\r')) i++;
            if (i >= json.length()) break;
            if (json.charAt(i) != '"') { i++; continue; }
            int keyStart = i + 1;
            i = json.indexOf('"', keyStart);
            if (i < 0) break;
            String key = json.substring(keyStart, i);
            i++;
            while (i < json.length() && (json.charAt(i) == ' ' || json.charAt(i) == ':')) i++;
            if (i >= json.length()) break;

            String value;
            if (json.charAt(i) == '"') {
                int valStart = i + 1;
                int end = valStart;
                while (end < json.length()) {
                    if (json.charAt(end) == '\\') { end += 2; continue; }
                    if (json.charAt(end) == '"') break;
                    end++;
                }
                value = json.substring(valStart, end)
                            .replace("\\\"", "\"").replace("\\\\", "\\")
                            .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                i = end + 1;
            } else if (json.charAt(i) == '[' || json.charAt(i) == '{') {
                char open = json.charAt(i), close = open == '[' ? ']' : '}';
                int depth = 0, start = i;
                while (i < json.length()) {
                    if (json.charAt(i) == open) depth++;
                    if (json.charAt(i) == close) depth--;
                    i++;
                    if (depth == 0) break;
                }
                value = json.substring(start, i);
            } else {
                int start = i;
                while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                value = json.substring(start, i).trim();
            }
            map.put(key, value);
        }
        return map;
    }

    public static String get(Map<String, String> map, String key) { return map.getOrDefault(key, ""); }
    public static int getInt(Map<String, String> map, String key) {
        try { return Integer.parseInt(get(map, key)); } catch (Exception e) { return 0; }
    }
}
