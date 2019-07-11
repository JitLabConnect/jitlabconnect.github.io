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
import java.util.Map;
import java.util.regex.Pattern;

@Path("/")
@Scanned
public class ConfigResource {
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
    public static final String COMMIT = ".commit";
    public static final String MERGE_OPEN = ".mergeopen";
    public static final String MERGE_REOPEN = ".mergereopen";
    public static final String MERGE_MERGE = ".mergemerge";
    public static final String MERGE_CLOSE = ".mergeclose";
    public static final String MERGE_APPROVE = ".mergeapprove";
    public static final String IS_ALL_ISSUES = ".isallissues";
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

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                Config config = new Config();

                config.setToken((String) Utility.getOrDefault(settings, TOKEN, ""));
                config.setUser((String) Utility.getOrDefault(settings, USER, ""));
                config.setMapping((String) Utility.getOrDefault(settings, MAPPING, ""));
                config.setSearchByName((String) Utility.getOrDefault(settings, SEARCH_BY_NAME, "0"));
                config.setCommit((String) Utility.getOrDefault(settings, COMMIT, "0"));
                config.setMergeOpen((String) Utility.getOrDefault(settings, MERGE_OPEN, "0"));
                config.setMergeReopen((String) Utility.getOrDefault(settings, MERGE_REOPEN, "0"));
                config.setMergeMerge((String) Utility.getOrDefault(settings, MERGE_MERGE, "0"));
                config.setMergeClose((String) Utility.getOrDefault(settings, MERGE_CLOSE, "0"));
                config.setMergeApprove((String) Utility.getOrDefault(settings, MERGE_APPROVE, "0"));
                config.setAllIssues((String) Utility.getOrDefault(settings, IS_ALL_ISSUES, "0"));
                config.setLinkCommit((String) Utility.getOrDefault(settings, IS_LINK_COMMIT, "0"));
                config.setLinkMerge((String) Utility.getOrDefault(settings, IS_LINK_MERGE, "0"));
                return config;
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
            return Response.ok(UpdatingResponse.error(i18n.getText("jitlab-connect.admin.response.error.user"))).build();
        }

        // mapping
        Map<String, String> mapping = null;
        try {
            mapping = Utility.stringToMap(config.getMapping());
            for (String name : mapping.values()) {
                if (userManager.getUserProfile(name) == null) {
                    return Response.ok(UpdatingResponse.error(i18n.getText("jitlab-connect.admin.response.error.mapping"))).build();
                }
            }
        } catch (Exception ex) {
        }
        if (mapping == null) {
            return Response.ok(UpdatingResponse.error(i18n.getText("jitlab-connect.admin.response.error.mapping"))).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(Config.CONFIG + TOKEN, config.getToken());
                pluginSettings.put(Config.CONFIG + USER, config.getUser());
                pluginSettings.put(Config.CONFIG + MAPPING, config.getMapping());
                pluginSettings.put(Config.CONFIG + SEARCH_BY_NAME, config.getSearchByName());
                pluginSettings.put(Config.CONFIG + COMMIT, config.getCommit());
                pluginSettings.put(Config.CONFIG + MERGE_OPEN, config.getMergeOpen());
                pluginSettings.put(Config.CONFIG + MERGE_REOPEN, config.getMergeReopen());
                pluginSettings.put(Config.CONFIG + MERGE_MERGE, config.getMergeMerge());
                pluginSettings.put(Config.CONFIG + MERGE_CLOSE, config.getMergeClose());
                pluginSettings.put(Config.CONFIG + MERGE_APPROVE, config.getMergeApprove());
                pluginSettings.put(Config.CONFIG + IS_ALL_ISSUES, config.getAllIssues());
                pluginSettings.put(Config.CONFIG + IS_LINK_COMMIT, config.getLinkCommit());
                pluginSettings.put(Config.CONFIG + IS_LINK_MERGE, config.getLinkMerge());
                return null;
            }
        });

        return Response.ok(UpdatingResponse.ok(i18n.getText("jitlab-connect.admin.response.ok"))).build();
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
