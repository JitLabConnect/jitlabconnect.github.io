package com.jitlab.connect.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.streams.thirdparty.api.ActivityService;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.ConfigResource;
import com.jitlab.connect.servlet.entity.JitLabRequest;
import com.jitlab.connect.servlet.entity.actions.Action;
import com.jitlab.connect.servlet.entity.actions.DoNothingAction;
import com.jitlab.connect.servlet.entity.actions.JiraAction;
import com.jitlab.connect.servlet.executor.ActionExecutor;
import com.jitlab.connect.servlet.executor.ActionExecutorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Scanned
public class JitLabConnect extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(JitLabConnect.class);
    @ComponentImport
    private final RemoteIssueLinkService linkService;
    @ComponentImport
    private final ActivityService activityService;
    @ComponentImport
    private CommentService commentService;
    @ComponentImport
    private IssueService issueService;
    /*@ComponentImport
    private ProjectService projectService;
    @ComponentImport
    private SearchService searchService;*/
    @ComponentImport
    private UserManager userManager;
    @ComponentImport
    private final com.atlassian.sal.api.user.UserManager pluginUserManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final TemplateRenderer renderer;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private ApplicationProperties applicationProperties;
    @ComponentImport
    private final I18nResolver i18n;

    @Inject
    public JitLabConnect(I18nResolver i18n, IssueService issueService,
                         com.atlassian.sal.api.user.UserManager pluginUserManager,
                         CommentService commentService, LoginUriProvider loginUriProvider,
                         TemplateRenderer renderer, PluginSettingsFactory pluginSettingsFactory,
                         ActivityService activityService, RemoteIssueLinkService linkService,
                         ApplicationProperties applicationProperties) {
        this.i18n = i18n;
        this.issueService = issueService;
        this.userManager = ComponentAccessor.getUserManager();
        this.pluginUserManager = pluginUserManager;
        this.commentService = commentService;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.activityService = activityService;
        this.applicationProperties = applicationProperties;
        this.linkService = linkService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // ADMIN PANEL
        UserProfile username = pluginUserManager.getRemoteUser(request);
        if (username == null || !pluginUserManager.isSystemAdmin(username.getUserKey())) {
            redirectToLogin(request, response);
            return;
        }

        response.setContentType("text/html;charset=utf-8");
        renderer.render("admin.vm", response.getWriter());
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(Utility.getUri(request)).toASCIIString());
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        String pluginToken = (String) Utility.getOrDefault(settings, ConfigResource.TOKEN, null);

        // TOKEN
        if (pluginToken == null) {
            log.error("Invalid token for JitLab plugin");
            res.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        }

        String requestToken = req.getHeader("X-Gitlab-Token");
        if ((requestToken == null) || !pluginToken.equals(requestToken)) {
            log.error("Invalid X-Gitlab-Token header");
            res.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        }

        // TRIGGER
        String requestEvent = req.getHeader("X-Gitlab-Event");
        JitLabRequest request = null;
        if ((requestEvent == null)) {
            log.error("Invalid X-Gitlab-Event header");
            res.sendError(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        } else if (requestEvent.equals("Merge Request Hook")) {
            request = UtilityParser.getRequestForMerge(Utility.streamToString(req.getReader()), i18n, settings);
        } else if (requestEvent.equals("Push Hook")) {
            request = UtilityParser.getRequestForPush(Utility.streamToString(req.getReader()), i18n, settings);
        } else {
            log.error("Invalid X-Gitlab-Event header: '{}'", requestEvent);
            return;
        }

        if (request == null) {
            return;
        }

        for (Action action : request.actions) {
            if (action instanceof JiraAction) {
                processAction(request.user, (JiraAction) action, settings);
            } else if (action instanceof DoNothingAction) {
                log.debug("Do nothing action");
            }
        }
    }

    private void processAction(String requestUser, JiraAction action, PluginSettings settings) {
        // USER
        ApplicationUser user = userManager.getUserByKey(requestUser);
        if (user == null) {
            try {
                Map<String, String> mapping = Utility.stringToMap((String) Utility.getOrDefault(settings, ConfigResource.MAPPING, ""));
                user = userManager.getUserByKey(mapping.get(requestUser));
            } catch (Exception ex) {
                // do nothing
            }

            if (user == null) {
                user = userManager.getUserByKey((String) Utility.getOrDefault(settings, ConfigResource.USER, ""));
                if (user == null) {
                    log.debug("Invalid user name '{}'", requestUser);
                    return;
                }
                log.debug("Use default user : '{}'", user.getUsername());
            } else {
                log.debug("Use mapping user : '{}'", user.getUsername());
            }
        }

        // USSUES
        if ((action.getIssues() == null) || (action.getIssues().size() == 0)) {
            log.debug("Issues keys not found for request");
            return;
        }

        List<MutableIssue> issues = populateIssues(action.getIssues(), user, settings);
        if ((issues == null) || issues.isEmpty()) {
            log.error("Issues not found in Jira for request");
            return;
        }

        ActionExecutor executor = new ActionExecutorImpl(i18n, commentService, activityService, linkService, applicationProperties);
        executor.execute(action, user, issues);
    }

    private List<MutableIssue> populateIssues(List<String> keys, ApplicationUser user, PluginSettings settings) {
        List<MutableIssue> issues = new ArrayList<MutableIssue>();
        for (String key : keys) {
            IssueService.IssueResult issue = issueService.getIssue(user, key);
            if ((issue != null) && issue.isValid()) {
                issues.add(issue.getIssue());
                if (Utility.getOrDefault(settings, ConfigResource.IS_ALL_ISSUES, "0").equals("0")) {
                    return issues;
                }
            } else {
                log.debug("Issue '{}' is not valid", key);
            }
        }
        return issues;
    }

}