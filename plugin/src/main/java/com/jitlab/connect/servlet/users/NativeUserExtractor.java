package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

import javax.annotation.Nullable;

public class NativeUserExtractor extends AbstractUserExtractor {
    private UserManager userManager;

    public NativeUserExtractor(UserManager userManager, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userManager = userManager;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, PluginSettings pluginSettings) {
        return userManager.getUserByName(userName);
    }
}
