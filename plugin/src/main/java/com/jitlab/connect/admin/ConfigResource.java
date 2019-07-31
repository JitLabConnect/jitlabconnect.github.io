package com.jitlab.connect.admin;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.jitlab.connect.Utility;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.regex.Pattern;

@Path("/")
@Scanned
public class ConfigResource {
    //private static final String listSubPattern = "\\d+(?:,\\d+)*";
    //private static final String transitionsItemSubPattern = String.format("(?:%s:)?%s", listSubPattern, listSubPattern);
    //private static final Pattern transitionsPattern = Pattern.compile(String.format("^%s(;%s)*(;)?$", transitionsItemSubPattern, transitionsItemSubPattern));
    private static final Pattern mapPattern = Pattern.compile("^([^,;]+,[^,;]+)+(;([^,;]+,[^,;]+))*(;)?$");
    private static final Pattern listPattern = Pattern.compile("^\\d+(;\\d+)*(;)?$");

    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    @ComponentImport
    private final I18nResolver i18n;

    public static final String TOKEN = ".token";
    public static final String USER = ".user";
    public static final String MAPPING = ".mapping";
    public static final String SEARCH_BY_NAME = ".searchbyname";
    public static final String PROJECTS = ".projects";
    public static final String IS_ALL_ISSUES = ".isallissues";

    public static final String PROJECT_TITLE = ".projecttitle";
    public static final String PROJECT_TYPE = ".projecttype";
    public static final String PROJECT_LOCALID = ".projectlocalid";
    public static final String COMMIT = ".commit";
    public static final String MERGE_OPEN = ".mergeopen";
    public static final String MERGE_REOPEN = ".mergereopen";
    public static final String MERGE_MERGE = ".mergemerge";
    public static final String MERGE_CLOSE = ".mergeclose";
    public static final String MERGE_APPROVE = ".mergeapprove";
    public static final String COMMIT_TRANSITIONS = ".committransitions";
    public static final String MERGE_OPEN_TRANSITIONS = ".mergeopentransitions";
    public static final String MERGE_REOPEN_TRANSITIONS = ".mergereopentransitions";
    public static final String MERGE_MERGE_TRANSITIONS = ".mergemergetransitions";
    public static final String MERGE_CLOSE_TRANSITIONS = ".mergeclosetransitions";
    public static final String MERGE_APPROVE_TRANSITIONS = ".mergeapprovetransitions";
    public static final String IS_LINK_COMMIT = ".islinkcommit";
    public static final String IS_LINK_MERGE = ".islinkmerge";

    @Inject
    public ConfigResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory,
                          TransactionTemplate transactionTemplate, I18nResolver i18n) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.i18n = i18n;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() {
                return fromPluginSettings(pluginSettingsFactory.createGlobalSettings());
            }
        })).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        //token
        if ((config.getToken() == null) || (!Pattern.matches("[a-zA-Z0-9]{20,100}", config.getToken()))) {
            return Response.ok(UpdatingResponse.error(i18n.getText("jitlab-connect.admin.response.error.token"))).build();
        }

        // default user
        if ((!config.getUser().equals("")) && userManager.getUserProfile(config.getUser()) == null) {
            return Response.ok(UpdatingResponse.error(String.format(i18n.getText("jitlab-connect.admin.response.error.user"), config.getUser()))).build();
        }

        // mapping
        if (!Utility.validateOrBlank(mapPattern, config.getMapping())) {
            return Response.ok(UpdatingResponse.error(i18n.getText("jitlab-connect.admin.response.error.mapping"))).build();
        }
        Map<String, String> mapping = Utility.stringToMap(config.getMapping());
        for (String name : mapping.values()) {
            if (userManager.getUserProfile(name) == null) {
                return Response.ok(UpdatingResponse.error(String.format(i18n.getText("jitlab-connect.admin.response.error.mapping.user"), name))).build();
            }
        }

        Map<String, ProjectConfig> actions = config.getProjectConfigs();
        if (actions == null || actions.isEmpty()) {
            return Response.ok(UpdatingResponse.error("")).build(); // TODO TEXT
        }

        for (Map.Entry<String, ProjectConfig> entry : actions.entrySet()) {

            // transitions
            if (!Utility.validateOrBlank(listPattern, entry.getValue().getCommitTransitions())
                    || !Utility.validateOrBlank(listPattern, entry.getValue().getMergeApproveTransitions())
                    || !Utility.validateOrBlank(listPattern, entry.getValue().getMergeCloseTransitions())
                    || !Utility.validateOrBlank(listPattern, entry.getValue().getMergeMergeTransitions())
                    || !Utility.validateOrBlank(listPattern, entry.getValue().getMergeOpenTransitions())
                    || !Utility.validateOrBlank(listPattern, entry.getValue().getMergeReopenTransitions())) {
                return Response.ok(UpdatingResponse.error(String.format(i18n.getText("jitlab-connect.admin.response.error.transitions"), entry.getValue().getTitle()))).build(); // tODO text
            }
        }

        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(Config.CONFIG + TOKEN, config.getToken());
                pluginSettings.put(Config.CONFIG + USER, config.getUser());
                pluginSettings.put(Config.CONFIG + MAPPING, config.getMapping().trim());
                pluginSettings.put(Config.CONFIG + SEARCH_BY_NAME, config.getSearchByName() ? "1" : "0");
                pluginSettings.put(Config.CONFIG + IS_ALL_ISSUES, config.getAllIssues() ? "1" : "0");

                Set<String> projects = new HashSet<>((List<String>) Utility.getOrDefault(pluginSettings, Config.CONFIG + PROJECTS, new ArrayList<>()));
                for (String projectKey : projects) {
                    if (!projectKey.isEmpty() && !config.getProjectConfigs().containsKey(projectKey)) {
                        // delete configs with prefix :projectId
                        String prefix = ":" + projectKey;
                        pluginSettings.remove(Config.CONFIG + prefix + PROJECT_TITLE);
                        pluginSettings.remove(Config.CONFIG + prefix + PROJECT_TYPE);
                        pluginSettings.remove(Config.CONFIG + prefix + PROJECT_LOCALID);

                        pluginSettings.remove(Config.CONFIG + prefix + COMMIT);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_OPEN);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_REOPEN);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_MERGE);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_CLOSE);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_APPROVE);
                        pluginSettings.remove(Config.CONFIG + prefix + COMMIT_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_OPEN_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_REOPEN_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_MERGE_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_CLOSE_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + MERGE_APPROVE_TRANSITIONS);
                        pluginSettings.remove(Config.CONFIG + prefix + IS_LINK_COMMIT);
                        pluginSettings.remove(Config.CONFIG + prefix + IS_LINK_MERGE);
                    }
                }

                pluginSettings.put(Config.CONFIG + PROJECTS, new ArrayList<>(config.getProjectConfigs().keySet()));

                for (Map.Entry<String, ProjectConfig> entry : config.getProjectConfigs().entrySet()) {
                    String key = entry.getKey().trim().toLowerCase();
                    String prefix = key.isEmpty() ? key : ":" + entry.getKey().trim().toLowerCase();
                    pluginSettings.put(Config.CONFIG + prefix + PROJECT_TITLE, entry.getValue().getTitle().trim());
                    pluginSettings.put(Config.CONFIG + prefix + PROJECT_TYPE, String.valueOf(entry.getValue().getType()));
                    pluginSettings.put(Config.CONFIG + prefix + PROJECT_LOCALID, entry.getValue().getLocalId().toLowerCase().trim());

                    pluginSettings.put(Config.CONFIG + prefix + COMMIT, entry.getValue().getCommit());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_OPEN, entry.getValue().getMergeOpen());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_REOPEN, entry.getValue().getMergeReopen());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_MERGE, entry.getValue().getMergeMerge());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_CLOSE, entry.getValue().getMergeClose());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_APPROVE, entry.getValue().getMergeApprove());
                    pluginSettings.put(Config.CONFIG + prefix + COMMIT_TRANSITIONS, entry.getValue().getCommitTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_OPEN_TRANSITIONS, entry.getValue().getMergeOpenTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_REOPEN_TRANSITIONS, entry.getValue().getMergeReopenTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_MERGE_TRANSITIONS, entry.getValue().getMergeMergeTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_CLOSE_TRANSITIONS, entry.getValue().getMergeCloseTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + MERGE_APPROVE_TRANSITIONS, entry.getValue().getMergeApproveTransitions().trim());
                    pluginSettings.put(Config.CONFIG + prefix + IS_LINK_COMMIT, entry.getValue().isLinkCommit() ? "1" : "0");
                    pluginSettings.put(Config.CONFIG + prefix + IS_LINK_MERGE, entry.getValue().isLinkMerge() ? "1" : "0");
                }
                return null;
            }
        });

        return Response.ok(UpdatingResponse.ok(i18n.getText("jitlab-connect.admin.response.ok"))).build();
    }

    public static Config fromPluginSettings(PluginSettings settings) {
        Config config = new Config();

        config.setToken((String) Utility.getOrDefault(settings, TOKEN, ""));
        config.setUser((String) Utility.getOrDefault(settings, USER, ""));
        config.setMapping((String) Utility.getOrDefault(settings, MAPPING, ""));
        config.setSearchByName(Utility.getOrDefault(settings, SEARCH_BY_NAME, "0").equals("1"));
        config.setAllIssues(Utility.getOrDefault(settings, IS_ALL_ISSUES, "0").equals("1"));
        List<String> projects = (List<String>) Utility.getOrDefault(settings, PROJECTS, new ArrayList<String>());
        config.setProjectConfigs(new HashMap<>());
        if (projects.isEmpty()) {
            projects.add("");
        }
        for (String project : projects) {
            ProjectConfig projectConfig = new ProjectConfig();
            projectConfig.setTitle((String) Utility.getOrDefault(settings, project, PROJECT_TITLE, "Default")); // TODO use i18n instead of string value
            projectConfig.setLocalId((String) Utility.getOrDefault(settings, project, PROJECT_LOCALID, "0"));
            projectConfig.setType(Integer.parseInt((String) Utility.getOrDefault(settings, project, PROJECT_TYPE, "0")));
            projectConfig.setCommit((String) Utility.getOrDefault(settings, project, COMMIT, "0"));
            projectConfig.setMergeOpen((String) Utility.getOrDefault(settings, project, MERGE_OPEN, "0"));
            projectConfig.setMergeReopen((String) Utility.getOrDefault(settings, project, MERGE_REOPEN, "0"));
            projectConfig.setMergeMerge((String) Utility.getOrDefault(settings, project, MERGE_MERGE, "0"));
            projectConfig.setMergeClose((String) Utility.getOrDefault(settings, project, MERGE_CLOSE, "0"));
            projectConfig.setMergeApprove((String) Utility.getOrDefault(settings, project, MERGE_APPROVE, "0"));
            projectConfig.setCommitTransitions((String) Utility.getOrDefault(settings, project, COMMIT_TRANSITIONS, ""));
            projectConfig.setMergeOpenTransitions((String) Utility.getOrDefault(settings, project, MERGE_OPEN_TRANSITIONS, ""));
            projectConfig.setMergeReopenTransitions((String) Utility.getOrDefault(settings, project, MERGE_REOPEN_TRANSITIONS, ""));
            projectConfig.setMergeMergeTransitions((String) Utility.getOrDefault(settings, project, MERGE_MERGE_TRANSITIONS, ""));
            projectConfig.setMergeCloseTransitions((String) Utility.getOrDefault(settings, project, MERGE_CLOSE_TRANSITIONS, ""));
            projectConfig.setMergeApproveTransitions((String) Utility.getOrDefault(settings, project, MERGE_APPROVE_TRANSITIONS, ""));
            projectConfig.setLinkCommit(Utility.getOrDefault(settings, project, IS_LINK_COMMIT, "0").equals("1"));
            projectConfig.setLinkMerge(Utility.getOrDefault(settings, project, IS_LINK_MERGE, "0").equals("1"));
            config.getProjectConfigs().put(project, projectConfig);
        }

        return config;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UpdatingResponse {
        public static final String RESPONSE_OK = "ok";
        public static final String RESPONSE_ERROR = "error";

        @XmlElement
        private String status;
        @XmlElement
        private String message;

        public static UpdatingResponse ok(String message) {
            return new UpdatingResponse(RESPONSE_OK, message);
        }

        public static UpdatingResponse error(String message) {
            return new UpdatingResponse(RESPONSE_ERROR, message);
        }

        private UpdatingResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
