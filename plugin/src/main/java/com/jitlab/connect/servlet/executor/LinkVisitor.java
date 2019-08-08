package com.jitlab.connect.servlet.executor;

import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Scanned
@Named("linkVisitor")
public class LinkVisitor implements ActionVisitor {
    private static final Logger log = LoggerFactory.getLogger(LinkVisitor.class);

    @ComponentImport
    private final RemoteIssueLinkService linkService;
    @ComponentImport
    private final ApplicationProperties applicationProperties;
    @ComponentImport
    private final I18nResolver i18n;

    @Inject
    public LinkVisitor(I18nResolver i18n, ApplicationProperties applicationProperties, RemoteIssueLinkService linkService) {
        this.linkService = linkService;
        this.applicationProperties = applicationProperties;
        this.i18n = i18n;
    }

    @Override
    public void processMergeRequest(MergeRequest mergeRequest, ApplicationUser user, MutableIssue issue) {
        String text = String.format(i18n.getText("jitlab-connect.text.link.text.mergerequest"), mergeRequest.getUrl().getId());
        doLink(mergeRequest.getUrl(),
                text,
                String.format(i18n.getText("jitlab-connect.text.link.summary.mergerequest"), mergeRequest.getState()),
                !mergeRequest.getState().equalsIgnoreCase("opened"),
                true,
                user,
                issue);
    }

    @Override
    public void processPushRequest(PushRequest pushRequest, ApplicationUser user, MutableIssue issue) {
        String text = String.format(i18n.getText("jitlab-connect.text.link.text.changeset"), pushRequest.getUrl().getId());
        doLink(pushRequest.getUrl(),
                text,
                "",
                true,
                false,
                user,
                issue);
    }

    private void doLink(AdaptiveUrl url, String text, String summary, boolean isResolved, boolean isShouldBeUpdated, ApplicationUser user, MutableIssue issue) {
        log.debug("Push to JIRA links ({}, {})", issue.getKey(), user.getUsername());

        RemoteIssueLink link = linkService.getRemoteIssueLinkByGlobalId(user, issue, generateLinkId(url)).getRemoteIssueLink();
        if (link == null) {
            // just for backward compatibility
            for (RemoteIssueLink link1 : linkService.getRemoteIssueLinksForIssue(user, issue).getRemoteIssueLinks()) {
                if (link1.getUrl().equalsIgnoreCase(url.getUrl())) {
                    link = link1;
                    break;
                }
            }
        }

        if ((link != null) && !isShouldBeUpdated) return;

        if (link != null) {
            // check old values
            if ((link.isResolved() == isResolved)
                    && (link.getTitle().equalsIgnoreCase(text))
                    && (link.getSummary() != null)
                    && link.getSummary().equalsIgnoreCase(summary)) return;

            //  update link
            RemoteIssueLink updated = new RemoteIssueLinkBuilder(link)
                    .resolved(isResolved)
                    .title(text)
                    .summary(summary)
                    .build();

            RemoteIssueLinkService.UpdateValidationResult updateValidateResult = linkService.validateUpdate(user, updated);

            if (updateValidateResult.isValid()) {
                RemoteIssueLinkService.RemoteIssueLinkResult result = linkService.update(user, updateValidateResult);
                if (result.isValid()) {
                    log.debug("Updated a link for JIRA task ({}, {})", issue.getKey(), user.getUsername());
                    return;
                }
            }

            log.error("Failed to update a link for JIRA task ({}, {})", issue.getKey(), user.getUsername());
            return;
        }

        // create link
        link = new RemoteIssueLinkBuilder()
                .globalId("jitlab" + url.getUrl())
                .issueId(issue.getId())
                .applicationName("JitLab Connect")
                .applicationType("com.jitlab.connect")
                .iconUrl(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + "/download/resources/com.jitlab.jitlab-connect:jitlab-connect-resources/images/pluginIcon.png")
                .relationship("GitLab")
                .title(text)
                .url(url.getUrl())
                .resolved(isResolved)
                .summary(summary)
                .build();

        RemoteIssueLinkService.CreateValidationResult createValidateResult = linkService.validateCreate(user, link);

        if (createValidateResult.isValid()) {
            RemoteIssueLinkService.RemoteIssueLinkResult result = linkService.create(user, createValidateResult);
            if (result.isValid()) {
                log.debug("Created a link for JIRA task ({}, {})", issue.getKey(), user.getUsername());
                return;
            }
        }

        log.error("Failed to create a link for JIRA task ({}, {})", issue.getKey(), user.getUsername());
    }

    private String generateLinkId(AdaptiveUrl url) {
        return "jitlab" + url.getUrl();
    }
}
