package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.Config;

import javax.annotation.Nullable;
import java.util.Map;

public class MappingUserExtractor extends AbstractUserExtractor {
    private UserManager userManager;

    public MappingUserExtractor(UserManager userManager) {
        this.userManager = userManager;
    }

    public MappingUserExtractor(UserManager userManager, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userManager = userManager;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, Config pluginSettings) {
        Map<String, String> mapping = Utility.stringToMap(pluginSettings.getMapping(), false);


        if (mapping != null && mapping.containsKey(userName)) {
            return userManager.getUserByName(mapping.get(userName));
        }

        return null;
    }
}
