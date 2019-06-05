package com.jitlab.connect.servlet.entity.actions;

import com.jitlab.connect.servlet.entity.AdaptiveUrl;

import java.util.List;

public class LinkAction extends JiraAction {
    private boolean isResolved;
    private String summary;
    private boolean shouldBeUpdated;

    public LinkAction(String text, AdaptiveUrl url, String action, List<String> issues, boolean isResolved, String summary, boolean shouldBeUpdated) {
        super(text, url, action, issues);
        this.isResolved = isResolved;
        this.summary = summary;
        this.shouldBeUpdated = shouldBeUpdated;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isShouldBeUpdated() {
        return shouldBeUpdated;
    }
}