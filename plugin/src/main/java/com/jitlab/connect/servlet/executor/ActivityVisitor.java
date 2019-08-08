package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.common.Either;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.thirdparty.api.*;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

@Scanned
@Named("activityVisitor")
public class ActivityVisitor implements ActionVisitor {
    private static final Logger log = LoggerFactory.getLogger(ActivityVisitor.class);

    @ComponentImport
    private final ApplicationProperties applicationProperties;
    @ComponentImport
    private final ActivityService activityService;
    @ComponentImport
    private final I18nResolver i18n;

    @Inject
    public ActivityVisitor(I18nResolver i18n, ApplicationProperties applicationProperties, ActivityService activityService) {
        this.applicationProperties = applicationProperties;
        this.activityService = activityService;
        this.i18n = i18n;
    }

    @Override
    public void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, MutableIssue issue) {
        String title = String.format(i18n.getText("jitlab-connect.text.activity.title.mergerequest"), user.getDisplayName(), mergeRequest.getEvent(), issue.getKey());
        String body = "<blockquote><p>" + String.format(i18n.getText("jitlab-connect.text.activity.body.mergerequest"), mergeRequest.getUrl().link())
                + "</p><div class=\"panel\" style=\"border-width: 1.0px;\"><div class=\"panelContent\"><p>"
                + mergeRequest.getTitle()
                + "</p></div></div></blockquote>";
        doActivity(mergeRequest.getUrl(), title, body, user, issue);
    }

    @Override
    public void processPushRequest(PushRequest pushRequest, ApplicationUser user, MutableIssue issue) {
        String title = String.format(i18n.getText("jitlab-connect.text.activity.title.changeset"), user.getDisplayName(), issue.getKey());
        String body = "<blockquote><p>" + String.format(i18n.getText("jitlab-connect.text.activity.body.changeset"), pushRequest.getUrl().link())
                + "</p><div class=\"panel\" style=\"border-width: 1.0px;\"><div class=\"panelContent\"><p>"
                + pushRequest.getMessage()
                + "</p></div></div></blockquote>";
        doActivity(pushRequest.getUrl(), title, body, user, issue);
    }

    private void doActivity(AdaptiveUrl url, String title, String body, ApplicationUser user, MutableIssue issue) {
        log.debug("Push to JIRA activity ({}, {})", issue.getKey(), user.getUsername());

        JiraAuthenticationContext authContext = ComponentAccessor.getJiraAuthenticationContext();
        authContext.setLoggedInUser(ComponentAccessor.getUserManager().getUserByKey(user.getKey()));

        Activity.Builder builder = new Activity.Builder(
                Application.application("JitLab Connect", URI.create("https://jitlabconnect.github.io/")),
                new DateTime(),
                new com.atlassian.streams.api.UserProfile.Builder(user.getUsername()).build());

        Either<ValidationErrors, Activity> result = builder
                //     .id(Option.option(url)) TODO
                .target(new ActivityObject
                        .Builder()
                        .urlString(Option.option(issue.getKey()))
                        .build())
                .title(Option.option(new Html(title)))
                .content(Option.option(new Html(body)))
                .url(Option.option(URI.create(url.getUrl())))
                .icon(Option.option(Image.withUrl(URI.create(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + "/download/resources/com.jitlab.jitlab-connect:jitlab-connect-resources/images/pluginIcon.png"))))
                .build();
        for (Activity activity : result.right()) {
            activityService.postActivity(activity);
        }

        for (ValidationErrors errors : result.left()) {
            log.error("Failed to push JIRA activity ({}, {})", issue.getKey(), errors.toString());
        }
    }
}
