package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.admin.Config;

public interface UserExtractor {
    ApplicationUser getUser(String userName, String displayName, Config pluginSettings);
}
