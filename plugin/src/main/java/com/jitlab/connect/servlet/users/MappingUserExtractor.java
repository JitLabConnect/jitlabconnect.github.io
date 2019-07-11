package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.ConfigResource;

import javax.annotation.Nullable;
import java.util.Map;

public class MappingUserExtractor extends AbstractUserExtractor {
    private UserManager userManager;

    public MappingUserExtractor(UserManager userManager, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userManager = userManager;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, PluginSettings pluginSettings) {
        Map<String, String> mapping = null;
        try {
            mapping = Utility.stringToMap((String) Utility.getOrDefault(pluginSettings, ConfigResource.MAPPING, ""));
        } catch (Exception ex) {
            // do nothing
        }

        if (mapping != null && mapping.containsKey(userName)) {
            return userManager.getUserByName(mapping.get(userName));
        }

        return null;
    }
}
