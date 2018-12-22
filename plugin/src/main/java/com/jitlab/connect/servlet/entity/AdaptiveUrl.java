package com.jitlab.connect.servlet.entity;

import java.util.List;

public class AdaptiveUrl {
    private String id;
    private String url;

    public AdaptiveUrl(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String link() {
        return "<a href = '" + url + "'>" + id + "</a>";
    }

    public String linkJira() {
        return "[" + id + "|" + url + "]";
    }

    public static String link(List<AdaptiveUrl> urls, String separator) {
        if ((urls == null) || urls.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(urls.get(0).link());
        for (int i = 1; i < urls.size(); i++) {
            AdaptiveUrl url = urls.get(i);
            sb.append(separator).append(url.link());
        }

        return sb.toString();
    }

    public static String linkJira(List<AdaptiveUrl> urls, String separator) {
        if ((urls == null) || urls.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(urls.get(0).linkJira());
        for (int i = 1; i < urls.size(); i++) {
            AdaptiveUrl url = urls.get(i);
            sb.append(separator).append(url.linkJira());
        }

        return sb.toString();
    }
}
