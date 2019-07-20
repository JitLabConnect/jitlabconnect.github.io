package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.jitlab.connect.admin.Config;

import javax.annotation.Nullable;

public class DefaultUserExtractor extends AbstractUserExtractor {
    private UserManager userManager;

    public DefaultUserExtractor(UserManager userManager) {
        this.userManager = userManager;
    }

    public DefaultUserExtractor(UserManager userManager, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userManager = userManager;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, Config pluginSettings) {
        return userManager.getUserByName(pluginSettings.getUser());
    }
}
