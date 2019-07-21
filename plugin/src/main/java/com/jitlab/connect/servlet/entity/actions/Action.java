package com.jitlab.connect.servlet.entity.actions;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.executor.ActionVisitor;

public interface Action {
    void process(ActionVisitor processor, ApplicationUser user, MutableIssue issue);
}
