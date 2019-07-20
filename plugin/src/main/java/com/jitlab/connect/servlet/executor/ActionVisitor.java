package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;

import java.util.List;

public interface ActionVisitor {
    void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, List<MutableIssue> issues);

    void processPushRequest(PushRequest pushRequest, ApplicationUser user, List<MutableIssue> issues);
}
