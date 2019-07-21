package com.jitlab.connect.servlet.entity.actions;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.executor.ActionVisitor;

import java.util.Set;

public class MergeRequest extends JiraAction {
    private String title;
    private String description;
    private String event;
    private String state;

    public MergeRequest(AdaptiveUrl url, Set<String> issues, String title, String description, String event, String state) {
        super(url, issues);
        this.title = title;
        this.description = description;
        this.event = event;
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEvent() {
        return event;
    }

    public String getState() {
        return state;
    }

    @Override
    public void process(ActionVisitor processor, ApplicationUser user, MutableIssue issue) {
        processor.processMergeRequest(this, user, issue);
    }
}
