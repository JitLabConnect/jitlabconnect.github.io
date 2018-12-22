package com.jitlab.connect.servlet.entity.actions;

import com.jitlab.connect.servlet.entity.AdaptiveUrl;

import java.util.List;

public class JiraAction implements Action {
    private String text;
    private AdaptiveUrl url;
    private String action;
    public List<String> issues;

    protected JiraAction(String text, AdaptiveUrl url, String action, List<String> issues) {
        this.text = text;
        this.url = url;
        this.action = action;
        this.issues = issues;
    }

    public String getText() {
        return text;
    }

    public AdaptiveUrl getUrl() {
        return url;
    }

    public String getAction() {
        return action;
    }

    public List<String> getIssues() {
        return issues;
    }
}
