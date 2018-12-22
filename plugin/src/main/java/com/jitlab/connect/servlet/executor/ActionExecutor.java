package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.entity.actions.Action;

import java.util.List;

public interface ActionExecutor {
    void execute(Action action, ApplicationUser user, List<MutableIssue> issues);
}
