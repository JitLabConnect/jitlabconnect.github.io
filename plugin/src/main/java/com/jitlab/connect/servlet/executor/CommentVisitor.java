package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Scanned
@Named("commentVisitor")
public class CommentVisitor implements ActionVisitor {
    private static final Logger log = LoggerFactory.getLogger(CommentVisitor.class);

    @ComponentImport
    private final CommentService commentService;
    @ComponentImport
    private final I18nResolver i18n;

    @Inject
    public CommentVisitor(I18nResolver i18n, CommentService commentService) {
        this.commentService = commentService;
        this.i18n = i18n;
    }

    @Override
    public void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, List<MutableIssue> issues) {
        String action = i18n.getText("jitlab-connect.text.mergerequest") + " " + mergeRequest.getUrl().linkJira() + " " + i18n.getText("jitlab-connect.text.is") + " " + mergeRequest.getEvent();
        String body = action + "{panel}" + mergeRequest.getTitle() + "{panel}";
        doComments(body, user, issues);
    }

    @Override
    public void processPushRequest(PushRequest pushRequest, ApplicationUser user, List<MutableIssue> issues) {
        String action = i18n.getText("jitlab-connect.text.changeset") + " " + pushRequest.getUrl().linkJira() + " " + i18n.getText("jitlab-connect.text.is") + " " + i18n.getText("jitlab-connect.text.pushed");
        String body = action + "{panel}" + pushRequest.getMessage() + "{panel}";
        doComments(body, user, issues);
    }

    private void doComments(String body, ApplicationUser user, List<MutableIssue> issues) {
        for (MutableIssue issue : issues) {
            try {
                log.debug("Push to JIRA comments ({}, {})", issue.getKey(), user.getUsername());

                commentService.create(
                        user,
                        commentService.validateCommentCreate(user,
                                CommentService.CommentParameters
                                        .builder()
                                        .issue(issue)
                                        .author(user)
                                        .body(body)
                                        .build()
                        ),
                        true);

            } catch (Exception ex) {
                log.debug("Unexpected error: {}", ex.getMessage());
            }
        }


    }
}
