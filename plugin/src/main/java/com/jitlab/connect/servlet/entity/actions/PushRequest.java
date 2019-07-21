package com.jitlab.connect.servlet.entity.actions;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.executor.ActionVisitor;

import java.util.Set;

public class PushRequest extends JiraAction {
    private String message;

    public PushRequest(AdaptiveUrl url, Set<String> issues, String message) {
        super(url, issues);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void process(ActionVisitor processor, ApplicationUser user, MutableIssue issue) {
        processor.processPushRequest(this, user, issue);
    }
}
