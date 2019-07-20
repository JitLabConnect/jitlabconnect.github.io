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
import java.util.List;

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
    public void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, List<MutableIssue> issues) {
        String actionTitle = mergeRequest.getEvent() + " " + i18n.getText("jitlab-connect.text.mergerequest").toLowerCase();
        String body = "<blockquote><p>" + i18n.getText("jitlab-connect.text.mergerequest") + " " + mergeRequest.getUrl().link()
                + "</p><div class=\"panel\" style=\"border-width: 1.0px;\"><div class=\"panelContent\"><p>"
                + mergeRequest.getTitle()
                + "</p></div></div></blockquote>";
        doActivity(mergeRequest.getUrl(), actionTitle, body, user, issues);
    }

    @Override
    public void processPushRequest(PushRequest pushRequest, ApplicationUser user, List<MutableIssue> issues) {
        String actionTitle = i18n.getText("jitlab-connect.text.pushedchangeset").toLowerCase();
        String body = "<blockquote><p>" + i18n.getText("jitlab-connect.text.changeset") + " " + pushRequest.getUrl().link()
                + "</p><div class=\"panel\" style=\"border-width: 1.0px;\"><div class=\"panelContent\"><p>"
                + pushRequest.getMessage()
                + "</p></div></div></blockquote>";
        doActivity(pushRequest.getUrl(), actionTitle, body, user, issues);
    }

    private void doActivity(AdaptiveUrl url, String actionTitle, String body, ApplicationUser user, List<MutableIssue> issues) {
        JiraAuthenticationContext authContext = ComponentAccessor.getJiraAuthenticationContext();
        authContext.setLoggedInUser(ComponentAccessor.getUserManager().getUserByKey(user.getKey()));

        for (MutableIssue issue : issues) {
            try {
                log.debug("Push to JIRA activity ({}, {})", issue.getKey(), user.getUsername());

                String title = new StringBuilder("<strong>")
                        .append(user.getDisplayName())
                        .append("</strong> ")
                        .append(" ")
                        .append(actionTitle)
                        .append(" ")
                        .append(i18n.getText("jitlab-connect.text.for"))
                        .append(" ")
                        .append(issue.getKey())
                        .toString();

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
                        .icon(Option.option(Image.withUrl(URI.create(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + "/download/resources/com.jitlab.plugin:jitlab-connect-resources/images/pluginIcon.png"))))
                        .build();
                for (Activity activity : result.right()) {
                    log.debug("Push to JIRA activity ({}, {})", issue.getKey(), user.getUsername());
                    activityService.postActivity(activity);
                }

                for (ValidationErrors errors : result.left()) {
                    log.error("Failed to push JIRA activity ({}, {})", issue.getKey(), errors.toString());
                }
            } catch (Exception ex) {
                log.debug("Unexpected error: {}", ex.getMessage());
            }
        }
    }
}
