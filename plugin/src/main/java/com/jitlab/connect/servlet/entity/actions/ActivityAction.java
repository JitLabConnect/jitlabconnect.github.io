package com.jitlab.connect.servlet.entity.actions;

import com.jitlab.connect.servlet.entity.AdaptiveUrl;

import java.util.List;

public class ActivityAction extends JiraAction {
    private String title;

    public ActivityAction(String title, String text, AdaptiveUrl url, String action, List<String> issues) {
        super(text, url, action, issues);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
