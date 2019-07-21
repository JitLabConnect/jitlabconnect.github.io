package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransitionVisitor implements ActionVisitor {
    private static final Logger log = LoggerFactory.getLogger(TransitionVisitor.class);
    private final IssueService issueService;
    private final List<Integer> transitions;

    public TransitionVisitor(List<Integer> transitions, IssueService issueService) {
        this.issueService = issueService;
        this.transitions = transitions;
    }

    @Override
    public void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, MutableIssue issue) {
        doTransitions(transitions, user, issue);
        /*WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
        JiraWorkflow workflow = workflowManager.getWorkflow(issue);
        workflow.getActionsByName() getLinkedStep(issue.getStatus()).getActions();
        ActionDescriptor[] c = workflow.getAllActions().toArray(new ActionDescriptor[0]);*/
    }

    @Override
    public void processPushRequest(PushRequest pushRequest, ApplicationUser user, MutableIssue issue) {
        doTransitions(transitions, user, issue);
    }

    private void doTransitions(List<Integer> actions, ApplicationUser user, MutableIssue issue) {
        for (int actionId : actions) {
            try {
                doTransition(actionId, user, issue);
            } catch (Exception ex) {
                log.error("Unexpected error: {}", ex.getMessage());
            }
        }
    }

    private void doTransition(int actionId, ApplicationUser user, MutableIssue issue) {
        log.debug("Push to JIRA transitions ({}, {})", issue.getKey(), user.getUsername());
        IssueService.TransitionValidationResult validationResult = issueService.validateTransition(user, issue.getId(), actionId, issueService.newIssueInputParameters());
        if (validationResult.isValid()) {
            IssueService.IssueResult transResult = issueService.transition(user, validationResult);
            if (transResult.isValid()) {
                log.debug("Created a transition for JIRA task ({}, {})", issue.getKey(), user.getUsername());
                return;
            }
        }

        log.error("Failed to create a transition for JIRA task ({}, {})", issue.getKey(), user.getUsername());
    }
}
