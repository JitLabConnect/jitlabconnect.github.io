package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

public interface UserExtractor {
    ApplicationUser getUser(String userName, String displayName, PluginSettings pluginSettings);
}
