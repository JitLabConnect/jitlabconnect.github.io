package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.ConfigResource;

import javax.annotation.Nullable;

public class DefautUserExtractor extends AbstractUserExtractor {
    private UserManager userManager;

    public DefautUserExtractor(UserManager userManager, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userManager = userManager;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, PluginSettings pluginSettings) {
        return userManager.getUserByName((String) Utility.getOrDefault(pluginSettings, ConfigResource.USER, ""));
    }
}
