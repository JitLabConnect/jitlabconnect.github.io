package com.jitlab.connect.servlet.entity.actions;

import com.jitlab.connect.servlet.entity.AdaptiveUrl;

import java.util.Set;

public abstract class JiraAction implements Action {
    private AdaptiveUrl url;
    private Set<String> issues;

    protected JiraAction(AdaptiveUrl url, Set<String> issues) {
        this.url = url;
        this.issues = issues;
    }

    public AdaptiveUrl getUrl() {
        return url;
    }

    public Set<String> getIssues() {
        return issues;
    }
}
