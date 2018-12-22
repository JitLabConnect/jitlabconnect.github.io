package com.jitlab.connect.servlet.entity.actions;

import com.jitlab.connect.servlet.entity.AdaptiveUrl;

import java.util.List;

public class LinkAction extends JiraAction {
    public LinkAction(String text, AdaptiveUrl url, String action, List<String> issues) {
        super(text, url, action, issues);
    }

}