package com.jitlab.connect.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Sets;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.Config;
import com.jitlab.connect.admin.ConfigResource;
import com.jitlab.connect.admin.ProjectConfig;
import com.jitlab.connect.servlet.entity.JitLabRequest;
import com.jitlab.connect.servlet.entity.actions.Action;
import com.jitlab.connect.servlet.entity.actions.JiraAction;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import com.jitlab.connect.servlet.executor.*;
import com.jitlab.connect.servlet.users.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Scanned
public class JitLabConnect extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(JitLabConnect.class);

    @ComponentImport
    private final IssueService issueService;
    @ComponentImport
    private final com.atlassian.sal.api.user.UserManager pluginUserManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final TemplateRenderer renderer;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    private final ActivityVisitor activityVisitor;
    private final CommentVisitor commentVisitor;
    private final LinkVisitor linkVisitor;
    private final UserExtractor userExtractor;

    @Inject
    public JitLabConnect(
            IssueService issueService,
            com.atlassian.sal.api.user.UserManager pluginUserManager,
            LoginUriProvider loginUriProvider,
            TemplateRenderer renderer,
            PluginSettingsFactory pluginSettingsFactory,
            @ComponentImport UserPickerSearchService userSearchService,
            ActivityVisitor activityVisitor, CommentVisitor commentVisitor, LinkVisitor linkVisitor) {
        this.issueService = issueService;
        this.activityVisitor = activityVisitor;
        this.commentVisitor = commentVisitor;
        this.linkVisitor = linkVisitor;
        this.pluginUserManager = pluginUserManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;

        UserSearchParams userSearchParams = new UserSearchParams(false, true, true, false, null, null);
        UserManager userManager = ComponentAccessor.getUserManager();

        this.userExtractor =
                new DefaultUserExtractor(userManager,
                        new DisplayNameUserExtractor(userSearchService, userSearchParams,
                                new MappingUserExtractor(userManager,
                                        new NativeUserExtractor(userManager))));
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

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Config settings = ConfigResource.fromPluginSettings(pluginSettingsFactory.createGlobalSettings());
        String pluginToken = settings.getToken();

        // token
        if (StringUtils.isEmpty(pluginToken)) {
            log.error("Invalid token for JitLab plugin");
            res.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        }

        String requestToken = req.getHeader("X-Gitlab-Token");
        if (!pluginToken.equals(requestToken)) {
            log.error("Invalid X-Gitlab-Token header");
            res.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        }

        // request
        String requestType = req.getHeader("X-Gitlab-Event");
        Set<String> allowedTypes = Sets.newHashSet("Merge Request Hook", "Push Hook", "System Hook");
        JitLabRequest request;
        if ((requestType == null)) {
            log.error("Invalid X-Gitlab-Event header");
            res.sendError(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        } else if (allowedTypes.contains(requestType.trim())) {
            request = UtilityParser.parseRequest(Utility.streamToString(req.getReader()));
        } else {
            log.error("Invalid X-Gitlab-Event header: '{}'", requestType);
            res.sendError(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        }

        if (request == null || request.getActions().isEmpty()) {
            return;
        }

        // user
        ApplicationUser user = userExtractor.getUser(request.getUser(), request.getUserName(), settings);
        if (user == null) {
            log.error("Invalid user name '{}'", request.getUser());
            return;
        } else {
            log.debug("Use user : '{}'", user.getUsername());
        }

        ProjectConfig projectConfig = settings.getProjectConfigs().containsKey(request.getProjectId() + ";1")
                ? settings.getProjectConfigs().get(request.getProjectId() + ";1")
                : settings.getProjectConfigs().get("");

        // processors
        List<ActionVisitor> processors = new ArrayList<>();
        Action firstAction = request.getActions().get(0);
        if (firstAction instanceof MergeRequest) {
            processors.addAll(populateProcessors((MergeRequest) firstAction, projectConfig));
        } else if (firstAction instanceof PushRequest) {
            processors.addAll(populateProcessors((PushRequest) firstAction, projectConfig));
        }

        // execute
        Map<String, MutableIssue> issuesHash = new HashMap<>();
        for (Action action : request.getActions()) {
            if (action instanceof JiraAction) {
                processAction(user, (JiraAction) action, settings, processors, issuesHash);
            }
        }
    }

    private void processAction(ApplicationUser user, JiraAction action, Config settings, List<ActionVisitor> processors, Map<String, MutableIssue> issuesHash) {
        if (action.getIssues().isEmpty()) {
            log.debug("Issue keys not found for request");
            return;
        }

        List<MutableIssue> issues = populateIssues(action.getIssues(), user, issuesHash, settings.getAllIssues());
        if (issues.isEmpty()) {
            log.error("Issues not found in Jira for request");
            return;
        }
        for (MutableIssue issue : issues) {
            try {
                for (ActionVisitor processor : processors) {
                    action.process(processor, user, issue);
                }
            } catch (Exception ex) {
                log.error("Unexpected error: {}", ex.getMessage());
            }
        }
    }

    private List<MutableIssue> populateIssues(Set<String> keys, ApplicationUser
            user, Map<String, MutableIssue> issuesHash, boolean isAllIssues) {
        List<MutableIssue> issues = new ArrayList<>();
        for (String key : keys) {
            if (issuesHash.containsKey(key)) {
                issues.add(issuesHash.get(key));
                if (!isAllIssues) {
                    return issues;
                }
                continue;
            }

            IssueService.IssueResult issue = issueService.getIssue(ApplicationUsers.toDirectoryUser(user), key);
            if ((issue != null) && issue.isValid()) {
                issues.add(issue.getIssue());
                issuesHash.put(key, issue.getIssue());
                if (!isAllIssues) {
                    return issues;
                }
            } else {
                log.debug("Issue '{}' is not valid", key);
            }
        }
        return issues;
    }

    private List<ActionVisitor> populateProcessors(MergeRequest mergeRequest, ProjectConfig settings) {
        List<ActionVisitor> processors = new ArrayList<>();
        String action = mergeRequest.getEvent();
        List<Integer> transitions = null;
        String config = "0";
        if (action.equalsIgnoreCase("opened")) {
            config = settings.getMergeOpen();
            transitions = Utility.stringToList(settings.getMergeOpenTransitions());
        } else if (action.equalsIgnoreCase("reopened")) {
            config = settings.getMergeReopen();
            transitions = Utility.stringToList(settings.getMergeReopenTransitions());
        } else if (action.equalsIgnoreCase("merged")) {
            config = settings.getMergeMerge();
            transitions = Utility.stringToList(settings.getMergeMergeTransitions());
        } else if (action.equalsIgnoreCase("closed")) {
            config = settings.getMergeClose();
            transitions = Utility.stringToList(settings.getMergeCloseTransitions());
        } else if (action.equalsIgnoreCase("approved")) {
            config = settings.getMergeApprove();
            transitions = Utility.stringToList(settings.getMergeApproveTransitions());
        }

        if (config.equals("1")) {
            processors.add(commentVisitor);
        } else if (config.equals("2")) {
            processors.add(activityVisitor);
        }

        // link to issue
        if (settings.isLinkMerge()) {
            processors.add(linkVisitor);
        }

        if (transitions != null && !transitions.isEmpty()) {
            processors.add(new TransitionVisitor(transitions, issueService));
        }

        return processors;
    }

    private List<ActionVisitor> populateProcessors(PushRequest pushRequest, ProjectConfig settings) {
        List<ActionVisitor> processors = new ArrayList<>();
        String config = settings.getCommit();
        if (config.equals("1")) {
            processors.add(commentVisitor);
        } else if (config.equals("2")) {
            processors.add(activityVisitor);
        }

        // link to issue
        if (settings.isLinkCommit()) {
            processors.add(linkVisitor);
        }

        List<Integer> transitions = Utility.stringToList(settings.getCommitTransitions());
        if (!transitions.isEmpty()) {
            processors.add(new TransitionVisitor(transitions, issueService));
        }

        return processors;
    }
}
