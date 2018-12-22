package com.jitlab.connect;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.jitlab.connect.admin.Config;

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

    public static Map<String, String> stringToMap(String input) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();
        if (input.equals("")) {
            return map;
        }

        String[] nameValuePairs = input.split(";");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split(",");
            String key = URLDecoder.decode(nameValue[0], "UTF-8");
            String value = URLDecoder.decode(nameValue[1], "UTF-8");
            if (key.equals("") || value.equals("")) {
                throw new RuntimeException();
            }
            map.put(key, value);
        }

        return map;
    }

    public static Object getOrDefault(PluginSettings settings, String key, Object value) {
        Object v = settings.get(Config.CONFIG + key);
        return (v != null) ? v : value;
    }

    public static List<String> getUniqueArray(Pattern tagMatcher, String str) {
        Matcher m = tagMatcher.matcher(str);
        HashSet<String> set = new HashSet<String>();
        while (m.find()) {
            set.add(m.group());
        }
        return new ArrayList<String>(set);
    }
}
