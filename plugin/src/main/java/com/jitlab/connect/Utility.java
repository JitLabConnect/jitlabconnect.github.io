package com.jitlab.connect;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.jitlab.connect.admin.Config;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    public static URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    public static String streamToString(BufferedReader reader) throws IOException {
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append(",");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(input)) {
            return map;
        }

        String[] nameValuePairs = input.split(";");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split(",");
            try {
                String key = URLDecoder.decode(nameValue[0], "UTF-8");
                String value = URLDecoder.decode(nameValue[1], "UTF-8");
                map.put(key, value);
            } catch (Exception ignored) {
            }
        }

        return map;
    }

    /*public static List<Pair<List<Integer>, List<Integer>>> parseTransitions(String input) {
        List<Pair<List<Integer>, List<Integer>>> list = new ArrayList<>();
        if (StringUtils.isEmpty(input)) {
            return list;
        }

        String[] items = input.split(";");
        for (String item : items) {
            String[] pair = item.split(":");
            if (pair.length == 0) continue;

            List<Integer> values = stringToList(pair[pair.length - 1]);
            list.add(new ImmutablePair<>(pair.length < 2 ? stringToList(pair[0]) : Collections.emptyList(), values));
        }

        return list;
    }*/

    public static List<Integer> stringToList(String input) {
        List<Integer> list = new ArrayList<>();
        if (StringUtils.isEmpty(input)) {
            return list;
        }

        String[] items = input.split(";");
        for (String item : items) {
            try {
                int number = Integer.parseUnsignedInt(item);
                list.add(number);
            } catch (Exception ignored) {
            }
        }

        return list;
    }

    public static Object getOrDefault(PluginSettings settings, String key, Object value) {
        Object v = settings.get(Config.CONFIG + key);
        return (v != null) ? v : value;
    }

    public static Object getOrDefault(PluginSettings settings, String projectId, String key, Object value) {
        if (!projectId.isEmpty()) {
            key = ":" + projectId + key;
        }
        Object v = settings.get(Config.CONFIG + key);
        return (v != null) ? v : value;
    }

    public static Set<String> getUniqueArray(Pattern tagMatcher, String str) {
        Matcher m = tagMatcher.matcher(str);
        HashSet<String> set = new HashSet<>();
        while (m.find()) {
            set.add(m.group());
        }
        return set;
    }

    public static List<String> getArray(Pattern tagMatcher, String str) {
        Matcher m = tagMatcher.matcher(str);
        List<String> set = new ArrayList<>();
        while (m.find()) {
            set.add(m.group());
        }
        return set;
    }

    public static String getPastForm(String verb) {
        verb = verb.toLowerCase();
        if (verb.substring(Math.max(verb.length() - 2, 0)).equalsIgnoreCase("ed")) {
            // do nothing
        } else if (verb.substring(Math.max(verb.length() - 1, 0)).equalsIgnoreCase("e")) {
            verb += "d";
        } else {
            verb += "ed";
        }

        return verb;
    }

    public static boolean validateOrBlank(Pattern pattern, String str) {
        return (StringUtils.isBlank(str)) || pattern.matcher(str).matches();
    }
}
